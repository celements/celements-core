package com.celements.web;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;

public class CelementsUserService {

  private static Logger _LOGGER = LoggerFactory.getLogger(CelementsUserService.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement(XWikiRightsClass.CLASS_DEF_HINT)
  private ClassDefinition xWikiRightsClass;

  @Requirement
  private ModelContext context;

  // TODO get rid of
  @Deprecated
  private XWikiContext getContext() {
    return context.getXWikiContext();
  }

  // TODO get rid of
  @Deprecated
  private XWiki getXWiki() {
    return getContext().getWiki();
  }

  public synchronized @NotNull XWikiUser createNewUser(@NotNull Map<String, String> userData,
      @NotNull String possibleLogins, boolean validate) throws UserCreateException {
    DocumentReference userDocRef = getOrGenerateUserDocRef(userData.remove("xwikiname"));
    String validkey = "";
    int success = -1;
    try {
      if (areIdentifiersUnique(userData, possibleLogins, getContext())) {
        if (!userData.containsKey("password")) {
          String password = RandomStringUtils.randomAlphanumeric(8);
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
        success = getXWiki().createUser(userDocRef.getName(), userData, getXWikiUsersClassDocRef(),
            content, null, "edit", getContext());
      }
    } catch (XWikiException excp) {
      _LOGGER.error("Exception while creating a new user", excp);
      throw new UserCreateException(excp);
    }
    XWikiUser newUser = null;
    if (success == 1) {
      setRightsOnUserDoc(userDocRef);
      newUser = new XWikiUser(modelUtils.serializeRefLocal(userDocRef));
      if (validate) {
        _LOGGER.info("send account validation mail with data: accountname='" + userDocRef.getName()
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

  private DocumentReference getOrGenerateUserDocRef(String accountName) {
    accountName = Strings.nullToEmpty(accountName);
    if (accountName.isEmpty()) {
      accountName = RandomStringUtils.randomAlphanumeric(12);
    }
    SpaceReference userSpaceRef = new SpaceReference("XWiki", context.getWikiRef());
    DocumentReference userDocRef = new DocumentReference(accountName, userSpaceRef);
    while (modelAccess.exists(userDocRef)) {
      userDocRef = new DocumentReference(RandomStringUtils.randomAlphanumeric(12), userSpaceRef);
    }
    return userDocRef;
  }

  private DocumentReference getXWikiUsersClassDocRef() {
    return webUtilsService.resolveDocumentReference("XWiki.XWikiUsers");
  }

  void setRightsOnUserDoc(DocumentReference userDocRef) throws UserCreateException {
    try {
      XWikiDocument doc = modelAccess.getDocument(userDocRef);
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

  private boolean areIdentifiersUnique(Map<String, String> userData, String possibleLogins,
      XWikiContext context) throws XWikiException {
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if (!"".equals(key.trim()) && (("," + possibleLogins + ",").indexOf("," + key + ",") >= 0)) {
        String user = new UserNameForUserDataCommand().getUsernameForUserData(userData.get(key),
            possibleLogins, context);
        if ((user == null) || (user.length() > 0)) { // user == "" means there is no such user
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

}
