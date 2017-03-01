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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PasswordRecoveryAndEmailValidationCommand {

  private static Logger LOGGER = LoggerFactory.getLogger(
      PasswordRecoveryAndEmailValidationCommand.class);

  public static final String CEL_PASSWORD_RECOVERY_FAILED = "cel_password_recovery_failed";

  public static final String CEL_PASSWORD_RECOVERY_SUBJECT_KEY = "cel_password_recovery_default_subject";

  static final String CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY = "cel_register_acount_activation_mail_subject";

  private CelSendMail injectedCelSendMail;
  IWebUtilsService injected_webUtilsService;

  public String recoverPassword() {
    String email = getContext().getRequest().get("j_username");
    if ((email != null) && !"".equals(email.trim())) {
      try {
        String account = new UserNameForUserDataCommand().getUsernameForUserData(email, "email",
            getContext());
        return recoverPassword(account, email);
      } catch (XWikiException e) {
        LOGGER.error("Exception getting userdoc for email '" + email + "'", e);
      }
    }
    List<String> params = new ArrayList<>();
    params.add(email);
    return getContext().getMessageTool().get(CEL_PASSWORD_RECOVERY_FAILED, params);
  }

  // Allow recovery exclusively for email input
  public String recoverPassword(String account, String input) {
    List<String> params = new ArrayList<>();
    params.add(input);
    String resultMsgKey = CEL_PASSWORD_RECOVERY_FAILED;
    if ((account != null) && (!"".equals(account.trim()))) {
      DocumentReference accountDocRef = completeAccountDocRef(account);
      try {
        XWikiDocument userDoc = getModelAccess().getDocument(accountDocRef);
        BaseObject userObj = userDoc.getXObject(getUserClassRef(
            accountDocRef.getWikiReference().getName()));
        if (userObj != null) {
          String sendResult = null;
          sendResult = setForcePwdAndSendMail(account, userObj, userDoc);
          if (sendResult != null) {
            resultMsgKey = sendResult;
          }
        }
      } catch (DocumentLoadException | DocumentNotExistsException exp) {
        LOGGER.error("Exception getting document '{}'", account, exp);
      } catch (XWikiException | DocumentSaveException exp) {
        LOGGER.error("Exception setting ForcePwd or sending mail for user '{}'", account, exp);
      }
    }
    LOGGER.debug("recover result msg: '" + resultMsgKey + "' param: '" + params.get(0) + "'");
    return getWebUtilsService().getAdminMessageTool().get(resultMsgKey, params);
  }

  private DocumentReference completeAccountDocRef(String accountName) {
    DocumentReference accountDocRef;
    if (accountName.startsWith("XWiki.")) {
      accountDocRef = getWebUtilsService().resolveDocumentReference(accountName);
    } else {
      accountDocRef = new DocumentReference(getContext().getDatabase(), "XWiki", accountName);
    }
    return accountDocRef;
  }

  private String setForcePwdAndSendMail(String account, BaseObject userObj, XWikiDocument userDoc)
      throws XWikiException, DocumentSaveException, DocumentLoadException,
      DocumentNotExistsException {
    String result = null;
    if (userObj.getIntValue("active") == 0) {
      sendNewValidation(account);
      result = "cel_password_recovery_success";
    } else {
      String email = userObj.getStringValue("email");
      LOGGER.debug("email: '" + email + "'");
      if ((email != null) && (email.trim().length() > 0)) {
        String validkey = setUserFieldsForPasswordRecovery(userDoc, userObj);

        VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
        vcontext.put("email", email);
        vcontext.put("validkey", validkey);

        int sendRecoveryMail = sendRecoveryMail(email, getWebUtilsService().getAdminLanguage(
            userDoc.getDocumentReference()), getWebUtilsService().getDefaultAdminLanguage());
        LOGGER.debug("sendRecoveryMail: '" + sendRecoveryMail + "'");
        if (sendRecoveryMail == 0) { // successfully sent == 0
          result = "cel_password_recovery_success";
        }
      }
    }
    return result;
  }

  private String setUserFieldsForPasswordRecovery(XWikiDocument userDoc, BaseObject userObj)
      throws XWikiException, DocumentSaveException {
    userObj.set("force_pwd_change", 1, getContext());
    String validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(getContext());
    userObj.set("validkey", validkey, getContext());
    getModelAccess().saveDocument(userDoc, "Password Recovery - set validkey and force_pwd_change",
        true);
    return validkey;
  }

  private int sendRecoveryMail(String email, String lang, String defLang) throws XWikiException,
      DocumentLoadException, DocumentNotExistsException {
    String sender = new CelMailConfiguration().getDefaultAdminSenderAddress();
    String subject = getPasswordRecoverySubject(lang, defLang);
    String textContent = getPasswordRecoveryMailContent("PasswordRecoverMailTextContent", lang,
        defLang);
    String htmlContent = getPasswordRecoveryMailContent("PasswordRecoverMailHtmlContent", lang,
        defLang);
    if ((htmlContent != null) || (textContent != null)) {
      return sendMail(sender, null, email, null, null, subject, htmlContent, textContent, null,
          null);
    }
    return -1;
  }

  private String getPasswordRecoveryMailContent(String template, String lang, String defLang)
      throws XWikiException, DocumentLoadException, DocumentNotExistsException {
    String mailContent = null;
    String newContent = "";
    XWikiDocument doc = getRTEDocWithCelementswebFallback(new DocumentReference(
        getContext().getDatabase(), "Tools", template));
    if (doc != null) {
      newContent = getContext().getWiki().getRenderingEngine().renderText(doc.getTranslatedContent(
          getContext()), getContext().getDoc(), getContext());
    }
    if ("".equals(newContent)) {
      newContent = getWebUtilsService().renderInheritableDocument(new DocumentReference(
          getContext().getDatabase(), "Mails", template), lang, defLang);
    }
    if (!"".equals(newContent)) {
      mailContent = newContent;
    }
    return mailContent;
  }

  private String getPasswordRecoverySubject(String lang, String defLang)
      throws DocumentLoadException, DocumentNotExistsException {
    String subject = "";
    XWikiDocument doc = getRTEDocWithCelementswebFallback(new DocumentReference(
        getContext().getDatabase(), "Tools", "PasswordRecoverMailTextContent"));
    if (doc == null) {
      doc = getRTEDocWithCelementswebFallback(new DocumentReference(getContext().getDatabase(),
          "Tools", "PasswordRecoverMailHtmlContent"));
    }
    if ((doc != null) && (doc.getTitle() != null) && !"".equals(doc.getTitle().trim())) {
      subject = doc.getTitle();
    }
    subject = getContext().getWiki().getRenderingEngine().renderText(subject, getContext().getDoc(),
        getContext());
    if (getDefaultEmptyDocStrategy().isEmptyRTEString(subject)) {
      List<String> params = Arrays.asList(getContext().getRequest().getHeader("host"));
      subject = getWebUtilsService().getMessageTool(lang).get(CEL_PASSWORD_RECOVERY_SUBJECT_KEY,
          params);
      if (CEL_PASSWORD_RECOVERY_SUBJECT_KEY.equals(subject) && (defLang != null)) {
        subject = getWebUtilsService().getMessageTool(defLang).get(
            CEL_PASSWORD_RECOVERY_SUBJECT_KEY, params);
      }
    }
    return subject;
  }

  private XWikiDocument getRTEDocWithCelementswebFallback(DocumentReference templateDocRef)
      throws DocumentLoadException, DocumentNotExistsException {
    XWikiDocument doc = null;
    DocumentReference templateCentralDocRef = new DocumentReference("celements2web",
        templateDocRef.getLastSpaceReference().getName(), templateDocRef.getName());
    if (!getDefaultEmptyDocStrategy().isEmptyDocumentTranslated(templateDocRef)) {
      doc = getModelAccess().getDocument(templateDocRef);
    } else if (!getDefaultEmptyDocStrategy().isEmptyDocumentTranslated(templateCentralDocRef)) {
      doc = getModelAccess().getDocument(templateCentralDocRef);
    }
    return doc;
  }

  /**
   * @deprecated since 2.79 instead use sendNewValidationToAccountEmail(String, String,
   *             DocumentReference)
   */
  @Deprecated
  public boolean sendNewValidation(String login, String possibleFields) throws XWikiException {
    String user = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleFields,
        getContext());
    return sendNewValidation(user);
  }

  /**
   * @deprecated since 2.79 instead use sendNewValidationToAccountEmail(String, String,
   *             DocumentReference)
   */
  @Deprecated
  public void sendNewValidation(String login, String possibleFields,
      DocumentReference activationMailDocRef) throws XWikiException {
    String user = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleFields,
        getContext());
    sendNewValidation(user, activationMailDocRef);
  }

  /**
   * @deprecated since 2.79 instead use sendNewValidationToAccountEmail(String)
   */
  @Deprecated
  public boolean sendNewValidation(String accountName) throws XWikiException {
    return sendNewValidation(accountName, (DocumentReference) null);
  }

  /**
   * @deprecated since 2.79 instead use sendNewValidationToAccountEmail(String, DocumentReference)
   */
  @Deprecated
  public boolean sendNewValidation(String accountName, DocumentReference activationMailDocRef)
      throws XWikiException {
    try {
      return sendNewValidationToAccountEmail(accountName, activationMailDocRef);
    } catch (SendValidationFailedException exp) {
      if (exp.getCause() instanceof XWikiException) {
        throw (XWikiException) exp.getCause();
      } else {
        throw new XWikiException(XWikiException.MODULE_XWIKI_PLUGINS,
            XWikiException.ERROR_XWIKI_UNKNOWN, "sendNewValiation failed.", exp);
      }
    }
  }

  public boolean sendNewValidationToAccountEmail(String login, String possibleFields)
      throws SendValidationFailedException {
    try {
      String user = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleFields,
          getContext());
      return sendNewValidationToAccountEmail(user);
    } catch (XWikiException exp) {
      throw new SendValidationFailedException("sending new validation to accountName '" + login
          + "' failed", exp);
    }
  }

  public void sendNewValidationToAccountEmail(String login, String possibleFields,
      DocumentReference activationMailDocRef) throws SendValidationFailedException {
    String user;
    try {
      user = new UserNameForUserDataCommand().getUsernameForUserData(login, possibleFields,
          getContext());
      sendNewValidationToAccountEmail(user, activationMailDocRef);
    } catch (XWikiException exp) {
      throw new SendValidationFailedException("sending new validation to accountName '" + login
          + "' failed", exp);
    }
  }

  public boolean sendNewValidationToAccountEmail(String accountName)
      throws SendValidationFailedException {
    return sendNewValidationToAccountEmail(accountName, (DocumentReference) null);
  }

  public boolean sendNewValidationToAccountEmail(String accountName,
      DocumentReference activationMailDocRef) throws SendValidationFailedException {
    try {
      DocumentReference accountDocRef = completeAccountDocRef(accountName);
      String validkey = getNewValidationTokenForUser(accountDocRef);

      XWikiDocument doc = getModelAccess().getDocument(accountDocRef);
      BaseObject obj = doc.getXObject(getUserClassRef(accountDocRef.getWikiReference().getName()));
      String oldLanguage = getContext().getLanguage();
      String newAdminLanguage = obj.getStringValue("admin_language");
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      Object oldAdminLanguage = vcontext.get("admin_language");
      if ((newAdminLanguage != null) && !"".equals(newAdminLanguage)) {
        getContext().setLanguage(newAdminLanguage);
        vcontext.put("admin_language", newAdminLanguage);
        vcontext.put("adminMsg", getWebUtilsService().getAdminMessageTool());
      }
      if (activationMailDocRef == null) {
        activationMailDocRef = getDefaultAccountActivationMailDocRef();
      } else if (!getModelAccess().exists(activationMailDocRef)) {
        LOGGER.warn("Failed to get activation mail [" + activationMailDocRef
            + "] now using default.");
        activationMailDocRef = getDefaultAccountActivationMailDocRef();
      }
      boolean sentSuccessful = false;
      try {
        sentSuccessful = sendValidationMessage(obj.getStringValue("email"), validkey,
            activationMailDocRef, newAdminLanguage, getWebUtilsService().getDefaultAdminLanguage());
      } finally {
        getContext().setLanguage(oldLanguage);
        vcontext.put("admin_language", oldAdminLanguage);
        vcontext.put("adminMsg", getWebUtilsService().getAdminMessageTool());
      }
      return sentSuccessful;
    } catch (XWikiException | DocumentLoadException | DocumentNotExistsException exp) {
      throw new SendValidationFailedException("sending new validation to accountName '"
          + accountName + "' failed", exp);
    }
  }

  private DocumentReference getDefaultAccountActivationMailDocRef() {
    return new DocumentReference(getContext().getDatabase(), "Tools", "AccountActivationMail");
  }

  /**
   * @Deprecated since 2.14.0 instead use getNewValidationTokenForUser(DocumentReference)
   */
  @Deprecated
  public String getNewValidationTokenForUser(String accountName, XWikiContext context)
      throws XWikiException {
    return getNewValidationTokenForUser(getWebUtilsService().resolveDocumentReference(accountName));
  }

  /**
   * @deprecated since 2.79 instead use createNewValidationTokenForUser(DocumentReference)
   */
  @Deprecated
  public String getNewValidationTokenForUser(DocumentReference accountDocRef)
      throws XWikiException {
    try {
      return createNewValidationTokenForUser(accountDocRef);
    } catch (CreatingValidationTokenFailedException exp) {
      if (exp.getCause() instanceof XWikiException) {
        throw (XWikiException) exp.getCause();
      } else {
        throw new XWikiException(XWikiException.MODULE_XWIKI_PLUGINS,
            XWikiException.ERROR_XWIKI_UNKNOWN, "sendNewValiation failed.", exp);
      }
    }
  }

  public String createNewValidationTokenForUser(@NotNull DocumentReference accountDocRef)
      throws CreatingValidationTokenFailedException {
    try {
      XWikiDocument doc1 = getModelAccess().getDocument(accountDocRef);
      BaseObject obj1 = doc1.getXObject(getUserClassRef(
          accountDocRef.getWikiReference().getName()));
      final String validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(
          getContext());
      obj1.set("validkey", validkey, getContext());
      getModelAccess().saveDocument(doc1, "creating new validkey");
      return validkey;
    } catch (XWikiException | DocumentSaveException | DocumentLoadException exp) {
      throw new CreatingValidationTokenFailedException("Failed to create a new validkey for user "
          + accountDocRef.getName(), exp);
    } catch (DocumentNotExistsException exp) {
      LOGGER.info("creating validkey failed, because user document does not exist for '"
          + accountDocRef.getName() + "'");
    }
    return null;
  }

  private DocumentReference getUserClassRef(String dbName) {
    return new DocumentReference(dbName, "XWiki", "XWikiUsers");
  }

  /**
   * @Deprecated since 2.14.0 instead use sendValidationMessage(String, String,
   *             DocumentReference)
   */
  @Deprecated
  public void sendValidationMessage(String to, String validkey, String contentDocName,
      XWikiContext context) throws XWikiException {
    DocumentReference contentDocRef = getWebUtilsService().resolveDocumentReference(contentDocName);
    sendValidationMessage(to, validkey, contentDocRef);
  }

  /**
   * @Deprecated since 2.34.0 instead use sendValidationMessage(String, String,
   *             DocumentReference, String)
   */
  @Deprecated
  public void sendValidationMessage(String to, String validkey, DocumentReference contentDocRef)
      throws XWikiException {
    sendValidationMessage(to, validkey, contentDocRef,
        getWebUtilsService().getDefaultAdminLanguage());
  }

  public void sendValidationMessage(String to, String validkey, DocumentReference contentDocRef,
      String lang) throws XWikiException {
    sendValidationMessage(to, validkey, contentDocRef, lang, null);
  }

  /**
   * @deprecated since 2.79 instead use sendValidationMessageToEmailAdr(String, String,
   *             DocumentReference,
   *             String, String)
   */
  @Deprecated
  public boolean sendValidationMessage(String to, String validkey, DocumentReference contentDocRef,
      String lang, String defLang) throws XWikiException {
    try {
      return sendValidationMessageToEmailAdr(to, validkey, contentDocRef, lang, defLang);
    } catch (SendValidationFailedException exp) {
      if (exp.getCause() instanceof XWikiException) {
        throw (XWikiException) exp.getCause();
      } else {
        throw new XWikiException(XWikiException.MODULE_XWIKI_PLUGINS,
            XWikiException.ERROR_XWIKI_UNKNOWN, "sendNewValiation failed.", exp);
      }
    }
  }

  public boolean sendValidationMessageToEmailAdr(@NotNull String to, @NotNull String validkey,
      @NotNull DocumentReference contentDocRef, @Nullable String lang, @Nullable String defLang)
      throws SendValidationFailedException {
    try {
      String sender = "";
      String subject = "";
      String content = "";
      XWikiDocument contentDoc = null;
      DocumentReference contentCentralDocRef = new DocumentReference("celements2web",
          contentDocRef.getLastSpaceReference().getName(), contentDocRef.getName());
      if (getModelAccess().exists(contentDocRef)) {
        contentDoc = getModelAccess().getDocument(contentDocRef);
      } else if (getModelAccess().exists(contentCentralDocRef)) {
        contentDoc = getModelAccess().getDocument(contentCentralDocRef);
      }
      sender = getFromEmailAdr(sender, contentDoc);
      setValidationInfoInContext(to, validkey);
      content = getValidationEmailContent(contentDoc, lang, defLang);
      subject = getValidationEmailSubject(contentDoc, lang, defLang);
      return sendMail(sender, null, to, null, null, subject, content, "", null, null) == 0;
    } catch (XWikiException | DocumentLoadException | DocumentNotExistsException exp) {
      throw new SendValidationFailedException("send validation message to " + to + " failed.", exp);
    }
  }

  public String getValidationEmailSubject(XWikiDocument contentDoc, String lang, String defLang)
      throws XWikiException {
    String subject = "";
    if (contentDoc != null) {
      // For syntaxes other than xwiki/1.0: set output syntax for renderedTitle
      subject = contentDoc.getTranslatedDocument(lang, getContext()).getTitle();
      subject = getContext().getWiki().getRenderingEngine().interpretText(subject, contentDoc,
          getContext());
    }
    if (getDefaultEmptyDocStrategy().isEmptyRTEString(subject)) {
      List<String> params = Arrays.asList(getContext().getRequest().getHeader("host"));
      subject = getWebUtilsService().getMessageTool(lang).get(
          CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY, params);
      if (CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY.equals(subject) && (defLang != null)) {
        subject = getWebUtilsService().getMessageTool(defLang).get(
            CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY, params);
      }
    }
    return subject;
  }

  public String getValidationEmailContent(@Nullable XWikiDocument contentDoc, @Nullable String lang,
      @Nullable String defLang) throws XWikiException {
    String content = "";
    if (contentDoc != null) {
      content = contentDoc.getTranslatedDocument(lang, getContext()).getRenderedContent(
          getContext());
    }
    if (getDefaultEmptyDocStrategy().isEmptyRTEString(content)) {
      content = getWebUtilsService().renderInheritableDocument(getDefaultMailDocRef(), lang,
          defLang);
    }
    return content;
  }

  DocumentReference getDefaultMailDocRef() {
    return new DocumentReference(getContext().getDatabase(), "Mails", "AccountActivationMail");
  }

  public String getFromEmailAdr(@NotNull String sender, @Nullable XWikiDocument contentDoc) {
    if (contentDoc != null) {
      DocumentReference mailSenderClassRef = new DocumentReference(getContext().getDatabase(),
          "Celements2", "FormMailClass");
      BaseObject senderObj = contentDoc.getXObject(mailSenderClassRef);
      if (senderObj != null) {
        sender = senderObj.getStringValue("emailFrom");
      }
    }
    if ("".equals(sender.trim())) {
      sender = new CelMailConfiguration().getDefaultAdminSenderAddress();
    }
    return sender;
  }

  void setValidationInfoInContext(@NotNull String to, @NotNull String validkey)
      throws XWikiException {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    vcontext.put("email", to);
    vcontext.put("validkey", validkey);
    vcontext.put("activationLink", getActivationLink(to, validkey));
  }

  String getActivationLink(String to, String validkey) throws XWikiException {
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("view", "XWiki.XWikiGuest",
          "Content.login", getContext())) {
        return getContext().getWiki().getExternalURL("Content.login", "view", "email="
            + URLEncoder.encode(to, "UTF-8") + "&ac=" + validkey, getContext());
      } else {
        return getContext().getWiki().getExternalURL("XWiki.XWikiLogin", "login", "email="
            + URLEncoder.encode(to, "UTF-8") + "&ac=" + validkey, getContext());
      }
    } catch (UnsupportedEncodingException exp) {
      LOGGER.error("Failed to encode [" + to + "] for activation link.", exp);
    }
    return null;
  }

  int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others) {
    IMailObjectRole sender = getCelSendMail();
    sender.setFrom(from);
    sender.setReplyTo(replyTo);
    sender.setTo(to);
    sender.setCc(cc);
    sender.setBcc(bcc);
    sender.setSubject(subject);
    sender.setHtmlContent(htmlContent, false);
    sender.setTextContent(textContent);
    sender.setAttachments(attachments);
    sender.setOthers(others);
    return sender.sendMail();
  }

  void injectCelSendMail(CelSendMail celSendMail) {
    this.injectedCelSendMail = celSendMail;
  }

  IMailObjectRole getCelSendMail() {
    if (injectedCelSendMail != null) {
      return injectedCelSendMail;
    }
    return Utils.getComponent(IMailObjectRole.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  IWebUtilsService getWebUtilsService() {
    if (injected_webUtilsService != null) {
      return injected_webUtilsService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

  private IDefaultEmptyDocStrategyRole getDefaultEmptyDocStrategy() {
    return Utils.getComponent(IDefaultEmptyDocStrategyRole.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
