package com.celements.auth;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.RemoteUserValidator;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.celements.web.token.TokenLDAPAuthServiceImpl;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

@Component("authentication")
public class AuthenticationScriptService implements ScriptService {
  
  private static Logger _LOGGER  = LoggerFactory.getLogger(
      AuthenticationScriptService.class);
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private IAuthenticationServiceRole authenticationService;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public String getUsernameForUserData(String login) {
    String possibleLogins = new PossibleLoginsCommand().getPossibleLogins();
    String account = "";
    try {
      _LOGGER.debug("executing getUsernameForUserData in plugin");
      account = new UserNameForUserDataCommand().getUsernameForUserData(login,
          possibleLogins, getContext());
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get usernameForUserData for login [" + login
          + "] and possibleLogins [" + possibleLogins + "].", exp);
    }
    return account;
  }
  
  public String getUsernameForUserData(String login, String possibleLogins) {
    String account = "";
    if(hasProgrammingRights() || hasAdminRights()) {
      try {
        _LOGGER.debug("executing getUsernameForUserData in plugin");
        account = new UserNameForUserDataCommand().getUsernameForUserData(login,
            possibleLogins, getContext());
      } catch (XWikiException exp) {
        _LOGGER.error("Failed to get usernameForUserData for login [" + login
            + "] and possibleLogins [" + possibleLogins + "].", exp);
      }
    } else {
      _LOGGER.debug("missing ProgrammingRights for [" + getContext().get("sdoc")
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
      _LOGGER.debug("sendNewValidation for user [" + user + "].");
      try {
        return new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user,
            possibleFields);
      } catch (XWikiException exp) {
        _LOGGER.error("sendNewValidation: failed.", exp);
      }
    }
    return false;
  }
  
  public void sendNewValidation(String user, String possibleFields,
      DocumentReference mailContentDocRef) {
    if ((hasAdminRights() || hasProgrammingRights()) && (user != null)
        && (user.trim().length() > 0)) {
      _LOGGER.debug("sendNewValidation for user [" + user + "] using mail ["
          + mailContentDocRef + "].");
      try {
        new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user,
            possibleFields, mailContentDocRef);
      } catch (XWikiException exp) {
        _LOGGER.error("sendNewValidation: failed.", exp);
      }
    } else {
      _LOGGER.warn("sendNewValidation: new validation email for user [" + user
          + "] not sent.");
    }
  }
  
  public String getNewValidationTokenForUser() {
    if(hasProgrammingRights() && (getContext().getUser() != null)) {
      try {
        DocumentReference accountDocRef = webUtilsService.resolveDocumentReference(
            getContext().getUser());
        return new PasswordRecoveryAndEmailValidationCommand(
            ).getNewValidationTokenForUser(accountDocRef);
      } catch (XWikiException exp) {
        _LOGGER.error("Failed to create new validation Token for user: "
            + getContext().getUser(), exp);
      }
    }
    return null;
  }
  
  public String getNewCelementsTokenForUser(Boolean guestPlus) {
    if(getContext().getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand(
            ).getNewCelementsTokenForUserWithAuthentication(getContext().getUser(), guestPlus,
                getContext());
      } catch (XWikiException exp) {
        _LOGGER.error("Failed to create new validation Token for user: "
            + getContext().getUser(), exp);
      }
    }
    return null;
  }
  
  public String getNewCelementsTokenForUser(Boolean guestPlus, int minutesValid) {
    if(getContext().getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand(
            ).getNewCelementsTokenForUserWithAuthentication(getContext().getUser(), 
                guestPlus, minutesValid, getContext());
      } catch (XWikiException exp) {
        _LOGGER.error("Failed to create new validation Token for user: "
            + getContext().getUser(), exp);
      }
    }
    return null;
  }
  
  public String getNewCelementsTokenForUser() {
    return getNewCelementsTokenForUser(false);
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public Map<String, String> activateAccount(String activationCode) throws XWikiException{
    return authenticationService.activateAccount(activationCode);
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public String getUniqueValidationKey() throws XWikiException {
    return new NewCelementsTokenForUserCommand().getUniqueValidationKey(getContext());
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public String recoverPassword() throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword();
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public String recoverPassword(String account) throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword(account,
        account);
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins) throws XWikiException {
    return authenticationService.checkAuth(logincredential, password, rememberme, 
        possibleLogins, null);
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, boolean noRedirect) throws XWikiException {
    return authenticationService.checkAuth(logincredential, password, rememberme, 
        possibleLogins, noRedirect);
  }
  
  
  
  public String isValidUserJSON(String username, String password, String memberOfGroup,
      List<String> returnGroupMemberships) {
    RemoteUserValidator validater = new RemoteUserValidator();
    if(hasProgrammingRights()) {
      return validater.isValidUserJSON(username, password, memberOfGroup,
          returnGroupMemberships, getContext());
    }
    return null;
  }
  
  public String getLogoutRedirectURL() {
    XWiki xwiki = getContext().getWiki();
    String logoutRedirectConf = xwiki.getSpacePreference("LogoutRedirect",
        "celements.logoutRedirect", xwiki.getDefaultSpace(getContext()) + ".WebHome", 
        getContext());
    String logoutRedirectURL = logoutRedirectConf;
    if (!logoutRedirectConf.startsWith("http://")
        && !logoutRedirectConf.startsWith("https://")) {
      logoutRedirectURL = xwiki.getURL(logoutRedirectConf, "view", "logout=1", 
          getContext());
    }
    return logoutRedirectURL;
  }
  
  public String getLoginRedirectURL() {
    XWiki xwiki = getContext().getWiki();
    String loginRedirectConf = xwiki.getSpacePreference("LoginRedirect",
        "celements.loginRedirect", xwiki.getDefaultSpace(getContext()) + ".WebHome", 
        getContext());
    String loginRedirectURL = loginRedirectConf;
    if (!loginRedirectConf.startsWith("http://")
        && !loginRedirectConf.startsWith("https://")) {
      loginRedirectURL = xwiki.getURL(loginRedirectConf, "view", "", getContext());
    }
    return loginRedirectURL;
  }
  
  /**
   * API to check rights on a document for a given user or group
   * 
   * @param level right to check (view, edit, comment, delete)
   * @param user user or group for which to check the right
   * @param isUser true for users and false for group
   * @param docname document on which to check the rights
   * @return true if right is granted/false if not
   */
  public boolean hasAccessLevel(String level, String user, boolean isUser,
      DocumentReference docRef) {
    try {
      return authenticationService.hasAccessLevel(level, user, isUser, docRef);
    } catch (Exception exp) {
      _LOGGER.warn("hasAccessLevel failed for level[" +level+"] user["+user+"] " +
      		"docRef["+docRef+"] isUser["+isUser+"]", exp);
      return false;
    }
  }

  private boolean hasProgrammingRights() {
    return getContext().getWiki().getRightService().hasProgrammingRights(getContext());
  }

  private boolean hasAdminRights() {
    return getContext().getWiki().getRightService().hasAdminRights(getContext());
  }
}
