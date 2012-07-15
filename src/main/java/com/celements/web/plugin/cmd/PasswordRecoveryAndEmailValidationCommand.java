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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PasswordRecoveryAndEmailValidationCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      PasswordRecoveryAndEmailValidationCommand.class);
  private CelSendMail injectedCelSendMail;
  IWebUtilsService webUtilsService;

  public String recoverPassword() {
    String email = getContext().getRequest().get("j_username");
    if((email != null) && !"".equals(email.trim())) {
      try {
        String account = new UserNameForUserDataCommand().getUsernameForUserData(email,
            "email", getContext());
        return recoverPassword(account, email);
      } catch (XWikiException e) {
        LOGGER.error("Exception getting userdoc for email '" + email + "'", e);
      }
    }
    List<String> params = new ArrayList<String>();
    params.add(email);
    return getContext().getMessageTool().get("cel_password_recovery_failed", params);
  }
  
  // Allow recovery exclusively for email input
  public String recoverPassword(String account, String input) {
    List<String> params = new ArrayList<String>();
    params.add(input);
    String resultMsgKey = "cel_password_recovery_failed";
    if((account != null) && (!"".equals(account.trim()))) {
      account = completeAccountFN(account);
      DocumentReference accountDocRef = getWebUtilsService().resolveDocumentReference(
          account);
      XWikiDocument userDoc = null;
      BaseObject userObj = null;
      if(getContext().getWiki().exists(accountDocRef, getContext())){
        try {
          userDoc = getContext().getWiki().getDocument(accountDocRef, getContext());
        } catch (XWikiException e) {
          LOGGER.error("Exception getting document '" + account + "'", e);
        }
        userObj = userDoc.getXObject(getUserClassRef(accountDocRef.getWikiReference(
            ).getName()));
      }
      if((userDoc != null) && (userObj != null)){
        String sendResult = null;
        try {
          sendResult = setForcePwdAndSendMail(account, userObj, userDoc);
        } catch (XWikiException e) {
          LOGGER.error("Exception setting ForcePwd or sending mail for user '" + account 
              + "'", e);
        }
        if(sendResult != null) {
          resultMsgKey = sendResult;
        }
      }
    }
    LOGGER.debug("recover result msg: '" + resultMsgKey + "' param: '" + params.get(0) + 
        "'");
    return getContext().getMessageTool().get(resultMsgKey, params);
  }

  private String completeAccountFN(String account) {
    if(!account.contains(".")) {
      account = "XWiki." + account;
    }
    return account;
  }

  private String setForcePwdAndSendMail(String account, BaseObject userObj,
      XWikiDocument userDoc) throws XWikiException {
    String result = null;
    if(userObj.getIntValue("active") == 0) {
      sendNewValidation(account);
      result = "cel_password_recovery_success";
    } else {
      String email = userObj.getStringValue("email");
      LOGGER.debug("email: '" + email + "'");
      if((email != null) && (email.trim().length() > 0)) {
        String validkey = setUserFieldsForPasswordRecovery(userDoc, userObj);
        
        VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
        vcontext.put("email", email);
        vcontext.put("validkey", validkey);
        
        int sendRecoveryMail = sendRecoveryMail(email);
        LOGGER.debug("sendRecoveryMail: '" + sendRecoveryMail + "'");
        if(sendRecoveryMail == 0) { // successfully sent == 0
          result = "cel_password_recovery_success";
        }
      }
    }
    return result;
  }

  private String setUserFieldsForPasswordRecovery(XWikiDocument userDoc,
      BaseObject userObj) throws XWikiException {
    userObj.set("force_pwd_change", 1, getContext());
    String validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(
        getContext());
    userObj.set("validkey", validkey, getContext());
    getContext().getWiki().saveDocument(userDoc,
        "Password Recovery - set validkey and force_pwd_change", true, getContext());
    return validkey;
  }

  private int sendRecoveryMail(String email) throws XWikiException {
    String sender = getContext().getWiki().getXWikiPreference("admin_email",
        getContext());
    String subject = getPasswordRecoverySubject();
    String textContent = getPasswordRecoveryMailContent("PasswordRecoverMailTextContent");
    String htmlContent = getPasswordRecoveryMailContent("PasswordRecoverMailHtmlContent");
    if((htmlContent != null) || (textContent != null)) {
      return sendMail(sender, null, email, null, null, subject, htmlContent, textContent,
          null, null);
    }
    return -1;
  }

  private String getPasswordRecoveryMailContent(String template)
      throws XWikiException {
    String mailContent = null;
    String newContent = "";
    XWikiDocument doc = getRTEDocWithCelementswebFallback(new DocumentReference(
        getContext().getDatabase(), "Tools", template));
    if (doc != null) {
      newContent = getContext().getWiki().getRenderingEngine().renderText(
          doc.getTranslatedContent(getContext()), getContext().getDoc(), getContext());
    }
    if ("".equals(newContent)) {
      template = template + "_" + getContext().getLanguage() + ".vm";
      try {
        newContent = getContext().getWiki().evaluateTemplate("celMails/" + template,
            getContext());
      } catch (IOException exp) {
        LOGGER.info("failed to get mail template [" + template + "].", exp);
      }
    }
    if (!"".equals(newContent)) {
      mailContent = newContent;
    }
    return mailContent;
  }

  private String getPasswordRecoverySubject() throws XWikiException {
    String subject = "$msg.get('cel_password_recovery_default_subject')";
    XWikiDocument doc = getRTEDocWithCelementswebFallback(new DocumentReference(
        getContext().getDatabase(), "Tools", "PasswordRecoverMailTextContent"));
    if(doc == null) {
      doc = getRTEDocWithCelementswebFallback(new DocumentReference(
          getContext().getDatabase(), "Tools", "PasswordRecoverMailHtmlContent"));
    }
    if ((doc != null) && (doc.getTitle() != null) && !"".equals(doc.getTitle().trim())) {
      subject = doc.getTitle();
    }
    subject = getContext().getWiki().getRenderingEngine().renderText(subject,
        getContext().getDoc(), getContext());
    return subject;
  }

  private XWikiDocument getRTEDocWithCelementswebFallback(
      DocumentReference templateDocRef) throws XWikiException {
    XWikiDocument doc = null;
    DocumentReference templateCentralDocRef = new DocumentReference("celements2web",
        templateDocRef.getLastSpaceReference().getName(), templateDocRef.getName());
    if(!new EmptyCheckCommand().isEmptyRTEDocument(templateDocRef)) {
      doc = getContext().getWiki().getDocument(templateDocRef, getContext());
    } else if(!new EmptyCheckCommand().isEmptyRTEDocument(templateCentralDocRef)) {
      doc = getContext().getWiki().getDocument(templateCentralDocRef, getContext());
    }
    return doc;
  }

  public void sendNewValidation(String login, String possibleFields
      ) throws XWikiException {
    String user = new UserNameForUserDataCommand().getUsernameForUserData(login,
        possibleFields, getContext());
    sendNewValidation(user);
  }

  public void sendNewValidation(String accountName) throws XWikiException {
    accountName = completeAccountFN(accountName);
    DocumentReference accountDocRef = getWebUtilsService().resolveDocumentReference(
        accountName);
    String validkey = getNewValidationTokenForUser(accountDocRef);
    
    XWikiDocument doc = getContext().getWiki().getDocument(accountDocRef, getContext());
    BaseObject obj = doc.getXObject(getUserClassRef(accountDocRef.getWikiReference(
        ).getName()));
    String oldLanguage = getContext().getLanguage();
    String newAdminLanguage = obj.getStringValue("admin_language");
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    Object oldAdminLanguage = vcontext.get("admin_language"); 
    if ((newAdminLanguage != null) && !"".equals(newAdminLanguage)) {
      getContext().setLanguage(newAdminLanguage);
      vcontext.put("admin_language", newAdminLanguage);
      vcontext.put("adminMsg", getWebUtilsService().getAdminMessageTool());
    }
    sendValidationMessage(obj.getStringValue("email"), validkey, new DocumentReference(
        getContext().getDatabase(), "Tools", "AccountActivationMail"));
    getContext().setLanguage(oldLanguage);
    vcontext.put("admin_language", oldAdminLanguage); 
    vcontext.put("adminMsg", getWebUtilsService().getAdminMessageTool());
  }

  /**
   * 
   * @param accountName
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @Deprecated since 2.14.0 instead use getNewValidationTokenForUser(DocumentReference)
   */
  @Deprecated
  public String getNewValidationTokenForUser(String accountName,
      XWikiContext context) throws XWikiException {
    return getNewValidationTokenForUser(getWebUtilsService().resolveDocumentReference(
        accountName));
  }

  public String getNewValidationTokenForUser(DocumentReference accountDocRef
      ) throws XWikiException {
    String validkey = null;
    if (getContext().getWiki().exists(accountDocRef, getContext())) {
      validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(
          getContext());
      XWikiDocument doc1 = getContext().getWiki().getDocument(accountDocRef,
          getContext());
      BaseObject obj1 = doc1.getXObject(getUserClassRef(accountDocRef.getWikiReference(
          ).getName()));
      obj1.set("validkey", validkey, getContext());
      getContext().getWiki().saveDocument(doc1, getContext());
    }
    return validkey;
  }

  private DocumentReference getUserClassRef(String dbName) {
    return new DocumentReference(dbName, "XWiki", "XWikiUsers");
  }

  /**
   * 
   * @param to
   * @param validkey
   * @param contentDocName
   * @param context
   * @throws XWikiException
   * 
   * @Deprecated since 2.14.0 instead use sendValidationMessage(String, String,
   *             DocumentReference)
   */
  @Deprecated
  public void sendValidationMessage(String to, String validkey, String contentDocName,
      XWikiContext context) throws XWikiException {
    DocumentReference contentDocRef = getWebUtilsService().resolveDocumentReference(
        contentDocName);
    sendValidationMessage(to, validkey, contentDocRef);
  }

  public void sendValidationMessage(String to, String validkey,
      DocumentReference contentDocRef) throws XWikiException {
    String sender = "";
    String subject = "";
    String content = "";
    sender = getContext().getWiki().getXWikiPreference("admin_email", getContext());
    XWikiDocument contentDoc = null;
    if(getContext().getWiki().exists(contentDocRef, getContext())) {
      contentDoc = getContext().getWiki().getDocument(contentDocRef, getContext());
    } else {
      DocumentReference contentCentralDocRef = new DocumentReference("celements2web",
          contentDocRef.getLastSpaceReference().getName(), contentDocRef.getName());
      contentDoc = getContext().getWiki().getDocument(contentCentralDocRef, getContext());
    }
    setValidationInfoInContext(to, validkey);
    content = contentDoc.getTranslatedDocument(getContext()).getRenderedContent(
        getContext());
    //For syntaxes other than xwiki/1.0: set output syntax for renderedTitle
    subject = contentDoc.getTranslatedDocument(getContext()).getTitle();
    subject = getContext().getWiki().getRenderingEngine().interpretText(subject,
        contentDoc, getContext());
    sendMail(sender, null, to, null, null, subject, content, "", null, null);
  }

  void setValidationInfoInContext(String to, String validkey) throws XWikiException {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    vcontext.put("email", to);
    vcontext.put("validkey", validkey);
    try {
      vcontext.put("activationLink", getContext().getWiki().getExternalURL(
          "Content.login", "view", "email=" + URLEncoder.encode(to, "UTF-8") + "&ac="
          + validkey, getContext()));
    } catch (UnsupportedEncodingException exp) {
      LOGGER.error("Failed to encode [" + to + "] for activation link.", exp);
    }
  }
  
  int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others){
    CelSendMail sender = getCelSendMail();
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
  
  CelSendMail getCelSendMail() {
    if(injectedCelSendMail != null) {
      return injectedCelSendMail;
    }
    return new CelSendMail(getContext());
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  IWebUtilsService getWebUtilsService() {
    if (webUtilsService != null) {
      return webUtilsService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

}
