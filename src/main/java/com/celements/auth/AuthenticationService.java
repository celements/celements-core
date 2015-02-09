package com.celements.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.CelementsWebScriptService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class AuthenticationService implements IAuthenticationServiceRole {

  private static Logger _LOGGER  = LoggerFactory.getLogger(AuthenticationService.class);
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  @Override 
  public String getPasswordHash(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }
  
  @Override 
  public Map<String, String> activateAccount(String activationCode) throws XWikiException{
    _LOGGER.debug("activateAccount: for code " + activationCode);
    Map<String, String> userAccount = new HashMap<String, String>();
    String hashedCode = getPasswordHash("hash:SHA-512:", activationCode);
    String username = new UserNameForUserDataCommand().getUsernameForUserData(hashedCode,
        "validkey", getContext());
    _LOGGER.debug("activateAccount: username = " + username);
    
    if((username != null) && !username.equals("")){
      String password = getContext().getWiki().generateRandomString(24);

      DocumentReference userDocRef = webUtilsService.resolveDocumentReference(username);
      _LOGGER.debug("activateAccount: userDocRef = " + userDocRef);
      XWikiDocument doc = getContext().getWiki().getDocument(userDocRef, getContext());
      _LOGGER.debug("activateAccount: userDoc = " + doc);
      DocumentReference userObjRef = webUtilsService.resolveDocumentReference(
          "XWiki.XWikiUsers");
      _LOGGER.debug("activateAccount: userObjRef = " + userObjRef);
      BaseObject obj = doc.getXObject(userObjRef);
      _LOGGER.debug("activateAccount: userObj = " + obj);

//      obj.set("validkey", "", context);
      obj.set("active", "1", getContext());
      obj.set("force_pwd_change", "1", getContext());
      obj.set("password", password, getContext());
      
      getContext().getWiki().saveDocument(doc, getContext());
      
      userAccount.put("username", username);
      userAccount.put("password", password);
    }
    
    return userAccount;
  }
  
  @Override 
  public XWikiUser checkAuth(String logincredential, String password,
      String rememberme, String possibleLogins, Boolean noRedirect) 
          throws XWikiException {
    String loginname = new UserNameForUserDataCommand().getUsernameForUserData(
        logincredential, possibleLogins, getContext());
    if ("".equals(loginname) && possibleLogins.matches("(.*,)?loginname(,.*)?")) {
        loginname = logincredential;
    }
    if (noRedirect != null) {
      getContext().put("ajax", noRedirect);
    }
    return getContext().getWiki().getAuthService().checkAuth(loginname, password, 
        rememberme, getContext());
  }
}
