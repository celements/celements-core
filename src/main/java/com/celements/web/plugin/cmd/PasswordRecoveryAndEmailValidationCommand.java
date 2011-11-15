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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PasswordRecoveryAndEmailValidationCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      PasswordRecoveryAndEmailValidationCommand.class);
  private CelSendMail injectedCelSendMail;

  public String recoverPassword(XWikiContext context) {
    String email = context.getRequest().get("j_username");
    if((email != null) && !"".equals(email.trim())) {
      try {
        String account = new UserNameForUserDataCommand().getUsernameForUserData(email,
            "email", context);
        return recoverPassword(account, email, context);
      } catch (XWikiException e) {
        mLogger.error("Exception getting userdoc for email '" + email + "'", e);
      }
    }
    List<String> params = new ArrayList<String>();
    params.add(email);
    return context.getMessageTool().get("cel_password_recovery_failed", params);
  }
  
  // Allow recovery exclusively for email input
  public String recoverPassword(String account, String input, XWikiContext context) {
    List<String> params = new ArrayList<String>();
    params.add(input);
    String resultMsgKey = "cel_password_recovery_failed";
    if((account != null) && (!"".equals(account.trim()))) {
      if(!account.contains(".")) {
        account = "XWiki." + account;
      }
      XWikiDocument userDoc = null;
      BaseObject userObj = null;
      if(context.getWiki().exists(account, context)){
        try {
          userDoc = context.getWiki().getDocument(account, context);
        } catch (XWikiException e) {
          mLogger.error("Exception getting document '" + account + "'", e);
        }
        userObj = userDoc.getObject("XWiki.XWikiUsers");
      }
      if((userDoc != null) && (userObj != null)){
        String sendResult = null;
        try {
          sendResult = setForcePwdAndSendMail(account, userObj, userDoc, context);
        } catch (XWikiException e) {
          mLogger.error("Exception setting ForcePwd or sending mail for user '" + account 
              + "'", e);
        }
        if(sendResult != null) {
          resultMsgKey = sendResult;
        }
      }
    }
    mLogger.debug("recover result msg: '" + resultMsgKey + "' param: '" + params.get(0) + 
        "'");
    return context.getMessageTool().get(resultMsgKey, params);
  }

  private String setForcePwdAndSendMail(String account, BaseObject userObj,
      XWikiDocument userDoc, XWikiContext context) throws XWikiException {
    String result = null;
    if(userObj.getIntValue("active") == 0) {
      sendNewValidation(account, context);
      result = "cel_password_recovery_success";
    } else {
      String email = userObj.getStringValue("email");
      mLogger.debug("email: '" + email + "'");
      if((email != null) && (email.trim().length() > 0)) {
        String validkey = setUserFieldsForPasswordRecovery(userDoc, userObj,context);
        
        VelocityContext vcontext = (VelocityContext) context.get("vcontext");
        vcontext.put("email", email);
        vcontext.put("validkey", validkey);
        
        int sendRecoveryMail = sendRecoveryMail(email, context);
        mLogger.debug("sendRecoveryMail: '" + sendRecoveryMail + "'");
        if(sendRecoveryMail == 0) { // successfully sent == 0
          result = "cel_password_recovery_success";
        }
      }
    }
    return result;
  }

  private String setUserFieldsForPasswordRecovery(XWikiDocument userDoc,
      BaseObject userObj, XWikiContext context) throws XWikiException {
    userObj.set("force_pwd_change", 1, context);
    String validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(
        context);
    userObj.set("validkey", validkey, context);
    context.getWiki().saveDocument(userDoc,
        "Password Recovery - set validkey and force_pwd_change", true, context);
    return validkey;
  }

  private int sendRecoveryMail(String email,
      XWikiContext context) throws XWikiException {
    String sender = context.getWiki().getXWikiPreference("admin_email", context);
    String subject = getPasswordRecoverySubject(context);
    String textContent = getPasswordRecoveryMailContent("PasswordRecoverMailTextContent",
        context);
    String htmlContent = getPasswordRecoveryMailContent("PasswordRecoverMailHtmlContent",
        context);
    if((htmlContent != null) || (textContent != null)) {
      return sendMail(sender, null, email, null, null, subject, htmlContent, textContent,
          null, null, context);
    }
    return -1;
  }

  private String getPasswordRecoveryMailContent(String template, XWikiContext context)
      throws XWikiException {
    String mailContent = null;
    String newContent = "";
    XWikiDocument doc = getRTEDocWithCelementswebFallback("Tools." + template, context);
    if (doc != null) {
      newContent = context.getWiki().getRenderingEngine().renderText(
          doc.getTranslatedContent(context), context.getDoc(), context);
    }
    if ("".equals(newContent)) {
      template = template + "_" + context.getLanguage() + ".vm";
      try {
        newContent = context.getWiki().evaluateTemplate("celMails/" + template, context);
      } catch (IOException exp) {
        mLogger.info("failed to get mail template [" + template + "].", exp);
      }
    }
    if (!"".equals(newContent)) {
      mailContent = newContent;
    }
    return mailContent;
  }

  private String getPasswordRecoverySubject(XWikiContext context) throws XWikiException {
    String subject = "$msg.get('cel_password_recovery_default_subject')";
    XWikiDocument doc = getRTEDocWithCelementswebFallback(
        "Tools.PasswordRecoverMailTextContent", context);
    if(doc == null) {
      doc = getRTEDocWithCelementswebFallback("Tools.PasswordRecoverMailHtmlContent",
          context);
    }
    if ((doc != null) && (doc.getTitle() != null) && !"".equals(doc.getTitle().trim())) {
      subject = doc.getTitle();
    }
    subject = context.getWiki().getRenderingEngine().renderText(subject, context.getDoc(),
        context);
    return subject;
  }

  private XWikiDocument getRTEDocWithCelementswebFallback(String fullname,
      XWikiContext context) throws XWikiException {
    XWikiDocument doc = null;
    if(!new EmptyCheckCommand().isEmptyRTEDocument(fullname, context)) {
      doc = context.getWiki().getDocument(fullname, context);
    } else if(!new EmptyCheckCommand().isEmptyRTEDocument("celements2web:" + fullname,
        context)) {
      doc = context.getWiki().getDocument("celements2web:" + fullname, context);
    }
    return doc;
  }

  public void sendNewValidation(String login, String possibleFields,
      XWikiContext context) throws XWikiException {
    String user = new UserNameForUserDataCommand().getUsernameForUserData(login,
        possibleFields, context);
    sendNewValidation(user, context);
  }

  public void sendNewValidation(String accountName,
      XWikiContext context) throws XWikiException {
    if(!accountName.contains(".")) {
      accountName = "XWiki." + accountName;
    }
    String validkey = getNewValidationTokenForUser(accountName, context);
    
    XWikiDocument doc = context.getWiki().getDocument(accountName, context);
    BaseObject obj = doc.getObject("XWiki.XWikiUsers");
    sendValidationMessage(obj.getStringValue("email"), validkey,
        "Tools.AccountActivationMail", context);
  }

  public String getNewValidationTokenForUser(String accountName,
      XWikiContext context) throws XWikiException {
    String validkey = null;
    if (context.getWiki().exists(accountName, context)) {
      validkey = new NewCelementsTokenForUserCommand().getUniqueValidationKey(context);
      XWikiDocument doc1 = context.getWiki().getDocument(accountName, context);
      BaseObject obj1 = doc1.getObject("XWiki.XWikiUsers");
      obj1.set("validkey", validkey, context);
      context.getWiki().saveDocument(doc1, context);
    }
    return validkey;
  }
  
  public void sendValidationMessage(String to, String validkey, 
      String contentDocName, XWikiContext context) throws XWikiException {
    String sender = "";
    String subject = "";
    String content = "";
    sender = context.getWiki().getXWikiPreference("admin_email", context);
    XWikiDocument contentDoc = null;
    if(context.getWiki().exists(contentDocName, context)) {
      contentDoc = context.getWiki().getDocument(contentDocName, context);
    } else {
      contentDoc = context.getWiki().getDocument("celements2web:" + contentDocName, 
          context);
    }
    setValidationInfoInContext(to, validkey, context);
    content = contentDoc.getTranslatedDocument(context).getRenderedContent(context);
    //For syntaxes other than xwiki/1.0: set output syntax for renderedTitle
    subject = contentDoc.getTranslatedDocument(context).getTitle();
    subject = context.getWiki().getRenderingEngine().interpretText(subject, contentDoc,
        context);
    sendMail(sender, null, to, null, null, subject, content, "", null, null, context);
  }

  void setValidationInfoInContext(String to, String validkey,
      XWikiContext context) throws XWikiException {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    vcontext.put("email", to);
    vcontext.put("validkey", validkey);
    vcontext.put("activationLink", context.getWiki().getExternalURL("Tools.Login", "view",
        "xpage=activateaccount&ac=" + validkey, context));
  }
  
  int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others,
      XWikiContext context){
    CelSendMail sender = getCelSendMail(context);
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
  
  CelSendMail getCelSendMail(XWikiContext context) {
    if(injectedCelSendMail != null) {
      return injectedCelSendMail;
    }
    return new CelSendMail(context);
  }

}
