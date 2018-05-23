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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;

import com.celements.auth.IAuthenticationServiceRole;
import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
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
  public static final String CEL_PASSWORD_RECOVERY_SUCCESS = "cel_password_recovery_success";

  public static final String CEL_PASSWORD_RECOVERY_SUBJECT_KEY = "cel_password_recovery_default_subject";

  static final String CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY = "cel_register_acount_activation_mail_subject";

  public String recoverPassword() {
    String email = Strings.nullToEmpty(getContext().getRequest().get("j_username")).trim();
    if (!email.isEmpty()) {
      Optional<User> account = getUserService().getUserForLoginField(email, Arrays.asList("email"));
      if (account.isPresent()) {
        return recoverPassword(account.get().getDocRef(), email);
      }
    }
    return getContext().getMessageTool().get(CEL_PASSWORD_RECOVERY_FAILED, Arrays.asList(email));
  }

  /**
   * @deprecated since 3.0 instead use {@link #recoverPassword(DocumentReference, String)}
   */
  @Deprecated
  public String recoverPassword(String account, String input) {
    DocumentReference userDocRef = null;
    if (!Strings.nullToEmpty(account).trim().isEmpty()) {
      userDocRef = getUserService().completeUserDocRef(account);
    }
    return recoverPassword(userDocRef, input);
  }

  public String recoverPassword(DocumentReference userDocRef, String input) {
    input = Strings.nullToEmpty(input).trim();
    String resultMsgKey = CEL_PASSWORD_RECOVERY_FAILED;
    if (userDocRef != null) {
      input = input.isEmpty() ? userDocRef.getName() : input;
      resultMsgKey = setForcePwdAndSendMail(userDocRef);
    }
    LOGGER.debug("recoverPassword - for user '{}' and input '{}' got: {}", userDocRef, input,
        resultMsgKey);
    return getWebUtilsService().getAdminMessageTool().get(resultMsgKey, Arrays.asList(input));
  }

  private String setForcePwdAndSendMail(DocumentReference userDocRef) {
    String result = CEL_PASSWORD_RECOVERY_FAILED;
    try {
      User user = getUserService().getUser(userDocRef);
      if (!user.isActive()) {
        sendNewValidationToAccountEmail(userDocRef);
        result = CEL_PASSWORD_RECOVERY_SUCCESS;
      } else {
        Optional<String> email = user.getEmail();
        LOGGER.debug("setForcePwdAndSendMail - email: '{}'", email.orNull());
        if (email.isPresent()) {
          String validkey = setUserFieldsForPasswordRecovery(userDocRef);
          VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
          vcontext.put(XWikiUsersClass.FIELD_EMAIL.getName(), email.get());
          vcontext.put(XWikiUsersClass.FIELD_VALID_KEY.getName(), validkey);
          boolean sentRecoveryMail = sendRecoveryMail(email.get(),
              getWebUtilsService().getAdminLanguage(userDocRef),
              getWebUtilsService().getDefaultAdminLanguage());
          LOGGER.debug("setForcePwdAndSendMail - sentRecoveryMail: '{}'", sentRecoveryMail);
          if (sentRecoveryMail) {
            result = CEL_PASSWORD_RECOVERY_SUCCESS;
          }
        }
      }
    } catch (UserInstantiationException | SendValidationFailedException | XWikiException
        | QueryException | DocumentAccessException exc) {
      LOGGER.warn("setForcePwdAndSendMail - failed for user '{}'", userDocRef, exc);
    }
    return result;
  }

  private String setUserFieldsForPasswordRecovery(DocumentReference userDocRef)
      throws QueryException, DocumentNotExistsException, DocumentSaveException {
    XWikiDocument userDoc = getModelAccess().getDocument(userDocRef);
    String validkey = getAuthService().getUniqueValidationKey();
    getModelAccess().setProperty(userDoc, XWikiUsersClass.FIELD_FORCE_PWD_CHANGE, true);
    getModelAccess().setProperty(userDoc, XWikiUsersClass.FIELD_VALID_KEY, validkey);
    getModelAccess().saveDocument(userDoc, "Password Recovery - set validkey and force_pwd_change",
        true);
    return validkey;
  }

  private boolean sendRecoveryMail(String email, String lang, String defLang) throws XWikiException,
      DocumentNotExistsException {
    String sender = new CelMailConfiguration().getDefaultAdminSenderAddress();
    String subject = getPasswordRecoverySubject(lang, defLang);
    String textContent = getPasswordRecoveryMailContent("PasswordRecoverMailTextContent", lang,
        defLang);
    String htmlContent = getPasswordRecoveryMailContent("PasswordRecoverMailHtmlContent", lang,
        defLang);
    if ((htmlContent != null) || (textContent != null)) {
      return sendMail(sender, null, email, null, null, subject, htmlContent, textContent, null,
          null) == 0;
    }
    return false;
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

  @Deprecated
  public boolean sendNewValidationToAccountEmail(String accountName)
      throws SendValidationFailedException {
    DocumentReference userDocRef = getUserService().completeUserDocRef(accountName);
    return sendNewValidationToAccountEmail(userDocRef);
  }

  public boolean sendNewValidationToAccountEmail(@NotNull DocumentReference userDocRef)
      throws SendValidationFailedException {
    return sendNewValidationToAccountEmail(userDocRef, (DocumentReference) null);
  }

  @Deprecated
  public boolean sendNewValidationToAccountEmail(String accountName,
      DocumentReference activationMailDocRef) throws SendValidationFailedException {
    DocumentReference userDocRef = getUserService().completeUserDocRef(accountName);
    return sendNewValidationToAccountEmail(userDocRef, activationMailDocRef);
  }

  public boolean sendNewValidationToAccountEmail(@NotNull DocumentReference userDocRef,
      @Nullable DocumentReference activationMailDocRef) throws SendValidationFailedException {
    try {
      User user = getUserService().getUser(userDocRef);
      String validkey = createNewValidationTokenForUser(userDocRef);
      String oldLanguage = getContext().getLanguage();
      Optional<String> newAdminLanguage = user.getAdminLanguage();
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      Object oldAdminLanguage = vcontext.get("admin_language");
      if (newAdminLanguage.isPresent()) {
        getContext().setLanguage(newAdminLanguage.get());
        vcontext.put("admin_language", newAdminLanguage.get());
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
        Optional<String> email = user.getEmail();
        if (email.isPresent()) {
          sentSuccessful = sendValidationMessageToEmailAdr(email.get(), validkey,
              activationMailDocRef, newAdminLanguage.orNull(),
              getWebUtilsService().getDefaultAdminLanguage());
        }
      } finally {
        getContext().setLanguage(oldLanguage);
        vcontext.put("admin_language", oldAdminLanguage);
        vcontext.put("adminMsg", getWebUtilsService().getAdminMessageTool());
      }
      return sentSuccessful;
    } catch (UserInstantiationException | CreatingValidationTokenFailedException exp) {
      throw new SendValidationFailedException("sending new validation to user '" + userDocRef
          + "' failed", exp);
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
    return getNewValidationTokenForUser(getUserService().completeUserDocRef(accountName));
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

  public String createNewValidationTokenForUser(@NotNull DocumentReference userDocRef)
      throws CreatingValidationTokenFailedException {
    try {
      XWikiDocument userDoc = getUserService().getUser(userDocRef).getDocument();
      final String validkey = getAuthService().getUniqueValidationKey();
      getModelAccess().setProperty(userDoc, XWikiUsersClass.FIELD_VALID_KEY, validkey);
      getModelAccess().saveDocument(userDoc, "creating new validkey");
      return validkey;
    } catch (UserInstantiationException | QueryException | DocumentSaveException exp) {
      throw new CreatingValidationTokenFailedException("Failed to create a new validkey for user: "
          + userDocRef.getName(), exp);
    }
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

  IMailObjectRole getCelSendMail() {
    return Utils.getComponent(IMailObjectRole.class);
  }

  private XWikiContext getContext() {
    return Utils.getComponent(ModelContext.class).getXWikiContext();
  }

  IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private IDefaultEmptyDocStrategyRole getDefaultEmptyDocStrategy() {
    return Utils.getComponent(IDefaultEmptyDocStrategyRole.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

  private IAuthenticationServiceRole getAuthService() {
    return Utils.getComponent(IAuthenticationServiceRole.class);
  }

}
