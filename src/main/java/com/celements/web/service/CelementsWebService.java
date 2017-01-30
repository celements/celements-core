package com.celements.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

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
import com.celements.model.classes.ClassDefinition;
import com.celements.rendering.RenderCommand;
import com.celements.web.UserCreateException;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiResponse;

@Component
public class CelementsWebService implements ICelementsWebServiceRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(CelementsWebService.class);

  private List<String> supportedAdminLangList;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement(XWikiRightsClass.CLASS_DEF_HINT)
  private ClassDefinition xWikiRightsClass;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public String getEmailAdressForUser(String username) {
    return getEmailAdressForUser(webUtilsService.resolveDocumentReference(username));
  }

  @Override
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
        _LOGGER.error("Exception while getting a XWikiDocument. docRef:['" + userDocRef + "]'",
            exp);
      }
    }
    return null;
  }

  @Override
  public int createUser(boolean validate) throws XWikiException {
    String possibleLogins = new PossibleLoginsCommand().getPossibleLogins();
    return createUser(getUniqueNameValueRequestMap(), possibleLogins, validate);
  }

  @Override
  public synchronized int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws XWikiException {
    try {
      if (createNewUser(userData, possibleLogins, validate) != null) {
        return 1;
      }
    } catch (UserCreateException uce) {
      if (uce.getCause() instanceof XWikiException) {
        throw (XWikiException) uce.getCause();
      } else {
        throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
            XWikiException.ERROR_XWIKI_UNKNOWN, "failed to create user", uce);
      }
    }
    return -1;
  }

  @Override
  public synchronized @NotNull XWikiUser createNewUser(@NotNull Map<String, String> userData,
      @NotNull String possibleLogins, boolean validate) throws UserCreateException {
    String accountName = "";
    String accountFullName = null;
    if (userData.containsKey("xwikiname")) {
      accountName = userData.get("xwikiname");
      userData.remove("xwikiname");
    } else {
      while (accountName.equals("") || getContext().getWiki().exists(
          webUtilsService.resolveDocumentReference(accountFullName), getContext())) {
        accountName = getContext().getWiki().generateRandomString(12);
        accountFullName = "XWiki." + accountName;
      }
    }
    String validkey = "";
    int success = -1;
    try {
      if (areIdentifiersUnique(userData, possibleLogins, getContext())) {
        if (!userData.containsKey("password")) {
          String password = getContext().getWiki().generateRandomString(8);
          userData.put("password", password);
        }
        if (!userData.containsKey("validkey")) {
          validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(getContext());
          userData.put("validkey", validkey);
        } else {
          validkey = userData.get("validkey");
        }
        if (!userData.containsKey("active")) {
          userData.put("active", "0");
        }
        String content = "#includeForm(\"XWiki.XWikiUserSheet\")";

        success = getContext().getWiki().createUser(accountName, userData,
            webUtilsService.resolveDocumentReference("XWiki.XWikiUsers"), content, null, "edit",
            getContext());
      }
    } catch (XWikiException excp) {
      _LOGGER.error("Exception while creating a new user", excp);
      throw new UserCreateException(excp);
    }

    XWikiUser newUser = null;
    if (success == 1) {
      setRightsOnUserDoc(accountFullName);
      newUser = new XWikiUser(accountFullName);
      if (validate) {
        _LOGGER.info("send account validation mail with data: accountname='" + accountName
            + "', email='" + userData.get("email") + "', validkey='" + validkey + "'");
        try {
          new PasswordRecoveryAndEmailValidationCommand().sendValidationMessage(userData.get(
              "email"), validkey, webUtilsService.resolveDocumentReference(
                  "Tools.AccountActivationMail"), webUtilsService.getDefaultAdminLanguage());
        } catch (XWikiException e) {
          _LOGGER.error("Exception while sending validation mail to '" + userData.get("email")
              + "'", e);
          throw new UserCreateException(e);
        }
      }
    }
    if (newUser == null) {
      throw new UserCreateException("Failed to create a new user");
    }
    return newUser;
  }

  void setRightsOnUserDoc(@NotNull String accountFullName) throws UserCreateException {
    try {
      XWikiDocument doc = modelAccess.getDocument(webUtilsService.resolveDocumentReference(
          accountFullName)); // accountFullName
      List<BaseObject> rightsObjs = modelAccess.getXObjects(doc, xWikiRightsClass.getClassRef());
      if (rightsObjs.size() > 0) {
        rightsObjs.get(0).setStringValue("users", webUtilsService.getRefLocalSerializer().serialize(
            doc.getDocumentReference()));
        rightsObjs.get(0).set("allow", "1", getContext());
        rightsObjs.get(0).set("levels", "view,edit,delete", getContext());
        rightsObjs.get(0).set("groups", "", getContext());
      }
      if (rightsObjs.size() > 1) {
        rightsObjs.get(1).set("users", "", getContext());
        rightsObjs.get(1).set("allow", "1", getContext());
        rightsObjs.get(1).set("levels", "view,edit,delete", getContext());
        rightsObjs.get(1).set("groups", "XWiki.XWikiAdminGroup", getContext());
      }

      modelAccess.saveDocument(doc, "added rights objects to created user");
    } catch (DocumentLoadException | DocumentNotExistsException | DocumentSaveException excp) {
      _LOGGER.error("Exception while trying to add rights to newly created user", excp);
      throw new UserCreateException(excp);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> getUniqueNameValueRequestMap() {
    Map<String, String[]> params = getContext().getRequest().getParameterMap();
    Map<String, String> resultMap = new HashMap<String, String>();
    for (String key : params.keySet()) {
      if ((params.get(key) != null) && (params.get(key).length > 0)) {
        resultMap.put(key, params.get(key)[0]);
      } else {
        resultMap.put(key, "");
      }
    }
    return resultMap;
  }

  private boolean areIdentifiersUnique(Map<String, String> userData, String possibleLogins,
      XWikiContext context) throws XWikiException {
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if (!"".equals(key.trim()) && (("," + possibleLogins + ",").indexOf("," + key + ",") >= 0)) {
        String user = new UserNameForUserDataCommand().getUsernameForUserData(userData.get(key),
            possibleLogins, context);
        if ((user == null) || (user.length() > 0)) { // user == "" means there is no such
                                                     // user
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  @Override
  public List<String> getSupportedAdminLanguages() {
    if (supportedAdminLangList == null) {
      supportedAdminLangList = Arrays.asList(new String[] { "de", "fr", "en", "it" });
    }
    return supportedAdminLangList;
  }

  @Override
  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    boolean success = false;
    if (getContext().getWiki().exists(webUtilsService.resolveDocumentReference(renderDocFullName),
        getContext())) {
      XWikiDocument renderDoc;
      try {
        renderDoc = getContext().getWiki().getDocument(webUtilsService.resolveDocumentReference(
            renderDocFullName), getContext());
        adjustResponseHeader(filename, getContext().getResponse());
        setResponseContent(renderDoc, getContext().getResponse());
      } catch (XWikiException e) {
        _LOGGER.error("", e);
      }
      getContext().setFinished(true);
    }
    return success;
  }

  private void adjustResponseHeader(String filename, XWikiResponse response) {
    response.setContentType("text/plain");
    String ofilename = Util.encodeURI(filename, getContext()).replaceAll("\\+", " ");
    response.addHeader("Content-disposition", "attachment; filename=\"" + ofilename
        + "\"; charset='UTF-8'");
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
      response.getOutputStream().write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
      response.getOutputStream().write(data);
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
          "Exception while sending response", e);
    }
  }

  @Override
  public void setSupportedAdminLanguages(List<String> supportedAdminLangList) {
    this.supportedAdminLangList = supportedAdminLangList;
  }
}
