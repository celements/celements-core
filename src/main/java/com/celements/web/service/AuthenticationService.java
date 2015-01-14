package com.celements.web.service;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class AuthenticationService implements IAuthenticationServiceRole {
  
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
    Map<String, String> userAccount = new HashMap<String, String>();
    String hashedCode = getPasswordHash("hash:SHA-512:", activationCode);
    String username = new UserNameForUserDataCommand().getUsernameForUserData(hashedCode,
        "validkey", getContext());
    
    if((username != null) && !username.equals("")){
      String password = getContext().getWiki().generateRandomString(24);
      
      XWikiDocument doc = getContext().getWiki().getDocument(
          webUtilsService.resolveDocumentReference(getContext().getUser()), getContext());
      
      BaseObject obj = doc.getXObject(webUtilsService.resolveDocumentReference(
          "XWiki.XWikiUsers"));

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
