package com.celements.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

@Component
public class AuthenticationService implements IAuthenticationServiceRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public String getPasswordHash(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }

  @Override
  public Map<String, String> activateAccount(String activationCode)
      throws AccountActivationFailedException {
    _LOGGER.debug("activateAccount: for code " + activationCode);
    try {
      Map<String, String> userAccount = new HashMap<>();
      String hashedCode = getPasswordHash("hash:SHA-512:", activationCode);
      String username = new UserNameForUserDataCommand().getUsernameForUserData(hashedCode,
          "validkey", getContext());
      _LOGGER.debug("activateAccount: username = " + username);

      if ((username != null) && !username.equals("")) {
        String password = getContext().getWiki().generateRandomString(24);

        DocumentReference userDocRef = webUtilsService.resolveDocumentReference(username);
        _LOGGER.debug("activateAccount: userDocRef = " + userDocRef);
        XWikiDocument doc = modelAccess.getDocument(userDocRef);
        _LOGGER.debug("activateAccount: userDoc = " + doc);
        DocumentReference userObjRef = webUtilsService.resolveDocumentReference("XWiki.XWikiUsers");
        _LOGGER.debug("activateAccount: userObjRef = " + userObjRef);
        BaseObject obj = doc.getXObject(userObjRef);
        _LOGGER.debug("activateAccount: userObj = " + obj);

        modelAccess.setProperty(obj, XWikiUsersClass.FIELD_ACTIVE, true);
        modelAccess.setProperty(obj, XWikiUsersClass.FIELD_FORCE_PWD_CHANGE, true);
        modelAccess.setProperty(obj, XWikiUsersClass.FIELD_PASSWORD, password);

        modelAccess.saveDocument(doc, "activate account");

        userAccount.put("username", username);
        userAccount.put("password", password);
      }
      return userAccount;
    } catch (XWikiException | DocumentSaveException | DocumentLoadException
        | DocumentNotExistsException exp) {
      throw new AccountActivationFailedException(exp);
    }
  }

  @Override
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, Boolean noRedirect) throws XWikiException {
    String loginname = new UserNameForUserDataCommand().getUsernameForUserData(logincredential,
        possibleLogins, getContext());
    if ("".equals(loginname) && possibleLogins.matches("(.*,)?loginname(,.*)?")) {
      loginname = logincredential;
    }
    if (noRedirect != null) {
      getContext().put("ajax", noRedirect);
    }
    return getContext().getWiki().getAuthService().checkAuth(loginname, password, rememberme,
        getContext());
  }

  /**
   * API to check rights on a document for a given user or group
   *
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
  @Override
  public boolean hasAccessLevel(String level, String user, boolean isUser, DocumentReference docRef)
      throws XWikiException {
    return ((XWikiRightServiceImpl) getContext().getWiki().getRightService()).hasAccessLevel(level,
        user, webUtilsService.getRefDefaultSerializer().serialize(docRef), isUser, getContext());
  }

}
