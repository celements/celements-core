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
package com.celements.web.plugin.cmd;

import java.util.Collection;
import java.util.Collections;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

@Deprecated
public class UserNameForUserDataCommand {

  /**
   * @deprecated since 3.0 instead use
   *             {@link UserService#getUserForLoginField(String, java.util.List)}
   */
  @Deprecated
  public String getUsernameForUserData(String login, String possibleLogins, XWikiContext context)
      throws XWikiException {
    if (!Strings.nullToEmpty(login).trim().isEmpty()) {
      Collection<String> possibleLoginFields = Collections.emptyList();
      if (possibleLogins != null) {
        possibleLogins = possibleLogins.replace("loginname", UserService.DEFAULT_LOGIN_FIELD);
        possibleLoginFields = Splitter.on(",").omitEmptyStrings().splitToList(possibleLogins);
      }
      Optional<User> user = getUserService().getUserForLoginField(login, possibleLoginFields);
      if (user.isPresent()) {
        return user.get().asXWikiUser().getUser();
      }
    }
    return "";
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

}
