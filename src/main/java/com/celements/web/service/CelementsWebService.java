package com.celements.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiResponse;

@Component
public class CelementsWebService implements ICelementsWebServiceRole {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebService.class);
  
  private List<String> supportedAdminLangList;
  
  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public String getEmailAdressForUser(String username) {
    return getEmailAdressForUser(webUtilsService.resolveDocumentReference(username));
  }
  
  public String getEmailAdressForUser(DocumentReference userDocRef) {
    if (getContext().getWiki().exists(userDocRef, getContext())) {
      try {
        XWikiDocument doc = getContext().getWiki().getDocument(userDocRef, getContext());
        BaseObject obj = doc.getXObject(webUtilsService.resolveDocumentReference(
            "XWiki.XWikiUsers"));
        if (obj != null) {
          return obj.getStringValue("email");
        }
      } catch (XWikiException exp) {
        LOGGER.error("Exception while getting a XWikiDocument. docRef:['" + userDocRef
            + "]'", exp);
      }
    }
    return null;
  }
  
  public int createUser(boolean validate) throws XWikiException{
    String possibleLogins = new PossibleLoginsCommand().getPossibleLogins();
    return createUser(getUniqueNameValueRequestMap(), possibleLogins, 
        validate);
  }
  
  public synchronized int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws XWikiException {
    String accountName = "";
    if (userData.containsKey("xwikiname")) {
      accountName = userData.get("xwikiname");
      userData.remove("xwikiname");
    } else {
      while (accountName.equals("") || getContext().getWiki().exists(
          webUtilsService.resolveDocumentReference("XWiki." + accountName), 
          getContext())) {
        accountName = getContext().getWiki().generateRandomString(12);
      }
    }
    String validkey = "";
    int success = -1;
    if(areIdentifiersUnique(userData, possibleLogins, getContext())) {
      if(!userData.containsKey("password")) {
        String password = getContext().getWiki().generateRandomString(8);
        userData.put("password", password);
      }
      if(!userData.containsKey("validkey")) {
        validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(
            getContext());
        userData.put("validkey", validkey);
      } else {
        validkey = userData.get("validkey");
      }
      if(!userData.containsKey("active")) {
        userData.put("active", "0");
      }
      String content = "#includeForm(\"XWiki.XWikiUserSheet\")";
      
      success = getContext().getWiki().createUser(accountName, userData, 
          webUtilsService.resolveDocumentReference("XWiki.XWikiUsers"), content, 
          null, "edit", getContext());
    }
    
    if(success == 1){
      // Set rights on user doc
      XWikiDocument doc = getContext().getWiki().getDocument(
          webUtilsService.resolveDocumentReference("XWiki." + accountName), getContext());
      List<BaseObject> rightsObjs = doc.getXObjects(
          webUtilsService.resolveDocumentReference("XWiki.XWikiRights"));
      for (BaseObject rightObj : rightsObjs) {
        if(rightObj.getStringValue("groups").equals("")){
          rightObj.setStringValue("users", webUtilsService.getRefLocalSerializer(
              ).serialize(doc.getDocumentReference()));
          rightObj.set("allow", "1", getContext());
          rightObj.set("levels", "view,edit,delete", getContext());
          rightObj.set("groups", "", getContext());
        } else{
          rightObj.set("users", "", getContext());
          rightObj.set("allow", "1", getContext());
          rightObj.set("levels", "view,edit,delete", getContext());
          rightObj.set("groups", "XWiki.XWikiAdminGroup", getContext());
        }
      }
      getContext().getWiki().saveDocument(doc, getContext());
      
      if(validate) {
        LOGGER.info("send account validation mail with data: accountname='" + accountName
            + "', email='" + userData.get("email") + "', validkey='" + validkey + "'");
        try{
          new PasswordRecoveryAndEmailValidationCommand().sendValidationMessage(
              userData.get("email"), validkey, webUtilsService.resolveDocumentReference(
                  "Tools.AccountActivationMail"), 
                  webUtilsService.getDefaultAdminLanguage());
        } catch(XWikiException e){
          LOGGER.error("Exception while sending validation mail to '" + 
              userData.get("email") + "'", e);
        }
      }
    }
    return success;
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, String> getUniqueNameValueRequestMap() {
    Map<String, String[]> params = getContext().getRequest().getParameterMap();
    Map<String, String> resultMap = new HashMap<String, String>();
    for (String key : params.keySet()) {
      if((params.get(key) != null) && (params.get(key).length > 0)) {
        resultMap.put(key, params.get(key)[0]);
      } else {
        resultMap.put(key, "");
      }
    }
    return resultMap;
  }  
  
  private boolean areIdentifiersUnique(Map<String, String> userData, 
      String possibleLogins, XWikiContext context) throws XWikiException {
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if(!"".equals(key.trim()) && (("," + possibleLogins + ",").indexOf("," + key + ",") >= 0)) {
        String user = new UserNameForUserDataCommand().getUsernameForUserData(
            userData.get(key), possibleLogins, context);
        if((user == null) || (user.length() > 0)) { //user == "" means there is no such user
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  public List<String> getSupportedAdminLanguages() {
    if (supportedAdminLangList == null) {
      supportedAdminLangList =  Arrays.asList(new String[] {"de","fr","en","it"});
    }
    return supportedAdminLangList;
  }
  
  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    boolean success = false;
    if(getContext().getWiki().exists(webUtilsService.resolveDocumentReference(
        renderDocFullName), getContext())) {
      XWikiDocument renderDoc;
      try {
        renderDoc = getContext().getWiki().getDocument(
            webUtilsService.resolveDocumentReference(renderDocFullName), getContext());
        adjustResponseHeader(filename, getContext().getResponse());
        setResponseContent(renderDoc, getContext().getResponse());
      } catch (XWikiException e) {
        LOGGER.error(e);
      }
      getContext().setFinished(true);
    }
    return success;
  }
  
  private void adjustResponseHeader(String filename, XWikiResponse response) {
    response.setContentType("text/plain");
    String ofilename = Util.encodeURI(filename, getContext()).replaceAll("\\+", " ");
    response.addHeader("Content-disposition", "attachment; filename=\"" + ofilename + 
        "\"; charset='UTF-8'");
  }
  
  private void setResponseContent(XWikiDocument renderDoc, XWikiResponse response) 
      throws XWikiException {
    String renderedContent = new RenderCommand().renderDocument(renderDoc);
    byte[] data = {};
    try {
      data = renderedContent.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    response.setContentLength(data.length + 3);
    try {
      response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
      response.getOutputStream().write(data);
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
          "Exception while sending response", e);
    }
  }
}
