/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.token;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.logging.LogUtils.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.ImmutableList.*;
import static java.util.Arrays.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.model.util.ModelUtils;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl;
import com.xpn.xwiki.web.Utils;

public class TokenLDAPAuthServiceImpl extends XWikiLDAPAuthServiceImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      TokenLDAPAuthServiceImpl.class);

  @Override
  public XWikiUser checkAuth(XWikiContext context) throws XWikiException {
    if ((context.getResponse() != null) && !"".equals(context.getWiki().Param("celements.auth.P3P",
        ""))) {
      context.getResponse().addHeader("P3P", "CP=\"" + context.getWiki().Param("celements.auth.P3P")
          + "\"");
    }
    return Optional.ofNullable(checkAuthByToken(context)
        .orElseGet(rethrowSupplier(() -> super.checkAuth(context))))
        .filter(this::isNotSuspended)
        .orElse(null);
  }

  private boolean isNotSuspended(XWikiUser xUser) {
    try {
      User user = getUserService().getUser(getModelUtils().resolveRef(xUser.getUser(),
          DocumentReference.class));
      return !user.isSuspended();
    } catch (UserInstantiationException uie) {
      LOGGER.warn("isNotSuspended - unable to instantiate user [{}]", xUser.getUser());
      return false;
    }
  }

  private Optional<XWikiUser> checkAuthByToken(XWikiContext context) throws XWikiException {
    XWikiUser user = null;
    if (context.getRequest() != null) {
      String token = context.getRequest().getParameter("token");
      String username = context.getRequest().getParameter("username");
      boolean hasToken = (token != null) && !"".equals(token);
      if (hasToken && (username != null) && !"".equals(username)) {
        LOGGER.info("checkAuthByToken - user [{}] with token [{}]", username, hasToken);
        user = checkAuthByToken(username, token, context);
      }
      if (user == null) {
        LOGGER.info("checkAuthByToken - skipped/failed for user [{}] with token [{}]",
            username, hasToken);
      }
    }
    return Optional.ofNullable(user);
  }

  public XWikiUser checkAuthByToken(String loginname, String userToken, XWikiContext context)
      throws XWikiException {
    if ((loginname != null) && !"".equals(loginname)) {
      String usernameByToken = getUsernameForToken(userToken, context);
      if (!usernameByToken.isEmpty() && (usernameByToken.equals("XWiki." + loginname)
          || usernameByToken.equals("xwiki:XWiki." + loginname))) {
        LOGGER.info("checkAuthByToken: user [{}] identified by userToken.", usernameByToken);
        context.setUser(usernameByToken);
        return context.getXWikiUser();
      } else {
        LOGGER.warn("checkAuthByToken: username could not be identified by token");
      }
    }
    return null;
  }

  public String getUsernameForToken(String userToken, XWikiContext context) throws XWikiException {
    List<String> users = ImmutableList.of();
    if (!nullToEmpty(userToken).trim().isEmpty()) {
      users = queryUsersForToken(userToken, context).stream()
          .filter(user -> !user.trim().isEmpty())
          .collect(toImmutableList());
    } else {
      LOGGER.warn("No valid token given: [{}]", userToken);
    }
    if (users.size() > 1) {
      LOGGER.warn("Found more than one user for token [{}]", userToken);
      return "";
    } else {
      LOGGER.info("getUsernameForToken: returning user {}", users);
      return users.stream().findFirst().orElse("");
    }
  }

  private List<String> queryUsersForToken(String userToken, XWikiContext context)
      throws XWikiException {
    final String hql = ", BaseObject as obj, Classes.TokenClass as token "
        + "where doc.space='XWiki' "
        + "and obj.name=doc.fullName "
        + "and token.tokenvalue=? "
        + "and token.validuntil>=? "
        + "and obj.id=token.id ";
    List<Object> params = ImmutableList.of(
        encryptString("hash:SHA-512:", userToken),
        new Date());
    XWikiStoreInterface store = context.getWiki().getStore();
    List<String> users = store.searchDocumentsNames(hql, 0, 0, params, context);
    LOGGER.debug("getUsernameForToken - in db [{}] and found {} with parameters {}",
        context.getDatabase(), users.size(), defer(() -> deepToString(params.toArray())));
    if (users.isEmpty()) {
      String currDb = context.getDatabase();
      try {
        String mainDb = getModelUtils().getMainWikiRef().getName();
        context.setDatabase(mainDb);
        users = store.searchDocumentsNames(hql, 0, 0, params, context);
        LOGGER.debug("getUsernameForToken - in db [{}] and found {} with parameters {}",
            context.getDatabase(), users.size(), defer(() -> deepToString(params.toArray())));
        users = users.stream().map(user -> mainDb + ":" + user).collect(toImmutableList());
      } finally {
        context.setDatabase(currDb);
      }
    }
    return users;
  }

  String encryptString(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }

  static UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

  static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
