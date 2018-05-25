package com.celements.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class AuthenticationService implements IAuthenticationServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private UserService userService;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Deprecated
  private XWikiContext getXWikiContext() {
    return context.getXWikiContext();
  }

  @Override
  public String getPasswordHash(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }

  @Override
  public Map<String, String> activateAccount(String activationCode)
      throws AccountActivationFailedException {
    LOGGER.debug("activateAccount: for code '{}'", activationCode);
    try {
      String hashedCode = getPasswordHash("hash:SHA-512:", activationCode);
      Optional<User> user = userService.getUserForLoginField(hashedCode, Arrays.asList(
          XWikiUsersClass.FIELD_VALID_KEY.getName()));
      LOGGER.debug("activateAccount: user = {}", user.orNull());
      if (user.isPresent()) {
        String password = RandomStringUtils.randomAlphanumeric(24);
        XWikiDocument userDoc = modelAccess.getDocument(user.get().getDocRef());
        modelAccess.setProperty(userDoc, XWikiUsersClass.FIELD_ACTIVE, true);
        modelAccess.setProperty(userDoc, XWikiUsersClass.FIELD_FORCE_PWD_CHANGE, true);
        modelAccess.setProperty(userDoc, XWikiUsersClass.FIELD_PASSWORD, password);
        modelAccess.saveDocument(userDoc, "activate account");
        Map<String, String> userAccount = new HashMap<>();
        userAccount.put("username", modelUtils.serializeRefLocal(user.get().getDocRef()));
        userAccount.put("password", password);
        return userAccount;
      } else {
        throw new AccountActivationFailedException("Invalid hashed hashedCode: " + hashedCode);
      }
    } catch (DocumentAccessException exp) {
      throw new AccountActivationFailedException(exp);
    }
  }

  @Override
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, Boolean noRedirect) throws XWikiException {
    String loginname = new UserNameForUserDataCommand().getUsernameForUserData(logincredential,
        possibleLogins, getXWikiContext());
    if ("".equals(loginname) && possibleLogins.matches("(.*,)?loginname(,.*)?")) {
      loginname = logincredential;
    }
    if (noRedirect != null) {
      getXWikiContext().put("ajax", noRedirect);
    }
    return getXWikiContext().getWiki().getAuthService().checkAuth(loginname, password, rememberme,
        getXWikiContext());
  }

  @Override
  @Deprecated
  public boolean hasAccessLevel(String level, String userName, boolean isUser,
      DocumentReference docRef) throws XWikiException {
    XWikiUser user = new XWikiUser(userName);
    Optional<EAccessLevel> right = EAccessLevel.getAccessLevel(level);
    return right.isPresent() ? rightsAccess.hasAccessLevel(docRef, right.get(), user) : false;
  }

  @Override
  public String getUniqueValidationKey() throws QueryException {
    String xwql = "select usr.validkey from Document doc, doc.object(XWiki.XWikiUsers) usr "
        + "where usr.validkey <> ''";
    Set<String> existingKeys = ImmutableSet.copyOf(queryManager.createQuery(xwql,
        Query.XWQL).<String>execute());
    String validkey;
    do {
      validkey = RandomStringUtils.randomAlphanumeric(24);
    } while (existingKeys.contains(validkey));
    return validkey;
  }

}
