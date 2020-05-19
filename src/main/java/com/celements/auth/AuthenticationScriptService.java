package com.celements.auth;

import static com.google.common.base.MoreObjects.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.auth.user.UserService;
import com.celements.model.context.ModelContext;
import com.celements.rights.access.RightsAccessScriptService;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.RemoteUserValidator;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@Component("authentication")
public class AuthenticationScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationScriptService.class);

  @Requirement
  private IAuthenticationServiceRole authenticationService;

  @Requirement
  private UserService userService;

  @Requirement
  private ConfigurationSource cfgSrc;

  @Requirement
  private ModelContext context;

  @Deprecated
  private XWikiContext getXWikiContext() {
    return context.getXWikiContext();
  }

  // TODO [CELDEV-698] rights checks missing
  public String getUsernameForUserData(String login) {
    String possibleLogins = new PossibleLoginsCommand().getPossibleLogins();
    String account = "";
    try {
      LOGGER.debug("executing getUsernameForUserData in plugin");
      account = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleLogins,
          getXWikiContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get usernameForUserData for login [" + login
          + "] and possibleLogins [" + possibleLogins + "].", exp);
    }
    return account;
  }

  public String getUsernameForUserData(String login, String possibleLogins) {
    String account = "";
    if (hasProgrammingRights() || hasAdminRights()) {
      try {
        LOGGER.debug("executing getUsernameForUserData in plugin");
        account = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleLogins,
            getXWikiContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get usernameForUserData for login [" + login
            + "] and possibleLogins [" + possibleLogins + "].", exp);
      }
    } else {
      LOGGER.debug("missing ProgrammingRights for [" + context.getXWikiContext().get("sdoc")
          + "]: getUsernameForUserData cannot be executed!");
    }
    return account;
  }

  public String getPasswordHash(String encoding, String str) {
    return authenticationService.getPasswordHash(encoding, str);
  }

  public String getPasswordHash(String str) {
    return getPasswordHash("hash:SHA-512:", str);
  }

  public boolean sendNewValidation(String user, String possibleFields) {
    if ((hasAdminRights() || hasProgrammingRights()) && (user != null)
        && (user.trim().length() > 0)) {
      LOGGER.debug("sendNewValidation for user [" + user + "].");
      try {
        return new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user,
            possibleFields);
      } catch (XWikiException exp) {
        LOGGER.error("sendNewValidation: failed.", exp);
      }
    }
    return false;
  }

  public void sendNewValidation(String user, String possibleFields,
      DocumentReference mailContentDocRef) {
    if ((hasAdminRights() || hasProgrammingRights()) && (user != null)
        && (user.trim().length() > 0)) {
      LOGGER.debug("sendNewValidation for user [" + user + "] using mail [" + mailContentDocRef
          + "].");
      try {
        new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user, possibleFields,
            mailContentDocRef);
      } catch (XWikiException exp) {
        LOGGER.error("sendNewValidation: failed.", exp);
      }
    } else {
      LOGGER.warn("sendNewValidation: new validation email for user [" + user + "] not sent.");
    }
  }

  public String getNewValidationTokenForUser() {
    if (hasProgrammingRights() && (context.getUser() != null)) {
      try {
        DocumentReference accountDocRef = userService.resolveUserDocRef(
            context.getUser().getUser());
        return new PasswordRecoveryAndEmailValidationCommand().getNewValidationTokenForUser(
            accountDocRef);
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: " + context.getUser(), exp);
      }
    }
    return null;
  }

  public String getNewCelementsTokenForUser(Boolean guestPlus) {
    if (context.getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand().getNewCelementsTokenForUserWithAuthentication(
            context.getUser().getUser(), guestPlus, getXWikiContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: " + context.getUser(), exp);
      }
    }
    return null;
  }

  public String getNewCelementsTokenForUser(Boolean guestPlus, int minutesValid) {
    if (context.getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand().getNewCelementsTokenForUserWithAuthentication(
            context.getUser().getUser(), guestPlus, minutesValid, getXWikiContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: " + context.getUser(), exp);
      }
    }
    return null;
  }

  public String getNewCelementsTokenForUser() {
    return getNewCelementsTokenForUser(false);
  }

  public Map<String, String> activateAccount(String activationCode) {
    try {
      return authenticationService.activateAccount(activationCode);
    } catch (AccountActivationFailedException authExp) {
      LOGGER.info("Failed to activate account", authExp);
    }
    return Collections.emptyMap();
  }

  /*
   * TODO [CELDEV-698] Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public String getUniqueValidationKey() throws XWikiException {
    return new NewCelementsTokenForUserCommand().getUniqueValidationKey(getXWikiContext());
  }

  /*
   * TODO [CELDEV-698] Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   * also rights check missing
   */
  public String recoverPassword() throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword();
  }

  /*
   * TODO [CELDEV-698] Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   * also rights check missing
   */
  public String recoverPassword(String account) throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword(account, account);
  }

  /*
   * TODO [CELDEV-698] Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins) throws XWikiException {
    return authenticationService.checkAuth(logincredential, password, rememberme, possibleLogins,
        null);
  }

  /*
   * TODO [CELDEV-698] Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, boolean noRedirect) throws XWikiException {
    return authenticationService.checkAuth(logincredential, password, rememberme, possibleLogins,
        noRedirect);
  }

  public String isValidUserJSON(String username, String password, String memberOfGroup,
      List<String> returnGroupMemberships) {
    RemoteUserValidator validater = new RemoteUserValidator();
    if (hasProgrammingRights()) {
      return validater.isValidUserJSON(username, password, memberOfGroup, returnGroupMemberships,
          getXWikiContext());
    }
    return null;
  }

  public String getLogoutRedirectURL() {
    XWiki xwiki = getXWikiContext().getWiki();
    String logoutRedirectConf = xwiki.getSpacePreference("LogoutRedirect",
        "celements.logoutRedirect", xwiki.getDefaultSpace(getXWikiContext()) + ".WebHome",
        getXWikiContext());
    String logoutRedirectURL = logoutRedirectConf;
    if (!logoutRedirectConf.startsWith("http://") && !logoutRedirectConf.startsWith("https://")) {
      logoutRedirectURL = xwiki.getURL(logoutRedirectConf, "view", "logout=1", getXWikiContext());
    }
    return logoutRedirectURL;
  }

  public String getLoginRedirectURL() {
    XWiki xwiki = getXWikiContext().getWiki();
    String loginRedirectConf = xwiki.getSpacePreference("LoginRedirect", "celements.loginRedirect",
        xwiki.getDefaultSpace(getXWikiContext()) + ".WebHome", getXWikiContext());
    String loginRedirectURL = loginRedirectConf;
    if (!loginRedirectConf.startsWith("http://") && !loginRedirectConf.startsWith("https://")) {
      loginRedirectURL = xwiki.getURL(loginRedirectConf, "view", "", getXWikiContext());
    }
    return loginRedirectURL;
  }

  /**
   * API to check rights on a document for a given user or group
   *
   * @deprecated since 3.0 use {@link RightsAccessScriptService}
   * @param level
   *          right to check (view, edit, comment, delete)
   * @param user
   *          user or group for which to check the right
   * @param isUser
   *          true for users and false for group
   * @param docname
   *          document on which to check the rights
   * @return true if right is granted/false if not
   */
  @Deprecated
  public boolean hasAccessLevel(String level, String user, boolean isUser,
      DocumentReference docRef) {
    try {
      return authenticationService.hasAccessLevel(level, user, isUser, docRef);
    } catch (Exception exp) {
      LOGGER.warn("hasAccessLevel failed for level[" + level + "] user[" + user + "] " + "docRef["
          + docRef + "] isUser[" + isUser + "]", exp);
      return false;
    }
  }

  public boolean isLoginDisabled() {
    return firstNonNull(cfgSrc.getProperty("celements.auth.login.disabled", false), false);
  }

  // TODO [CELDEV-698] use RightsAccessService
  private boolean hasProgrammingRights() {
    return getXWikiContext().getWiki().getRightService().hasProgrammingRights(getXWikiContext());
  }

  // TODO [CELDEV-698] use RightsAccessService
  private boolean hasAdminRights() {
    return getXWikiContext().getWiki().getRightService().hasAdminRights(getXWikiContext());
  }
}
