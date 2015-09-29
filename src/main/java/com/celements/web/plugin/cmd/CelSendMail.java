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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.plugin.mailsender.Mail;
import com.xpn.xwiki.plugin.mailsender.MailSenderPluginApi;
import com.xpn.xwiki.web.Utils;

/**
 * Do not use this class directly. Instead use IMailSenderRole to send emails.
 * In the future it is planned, that IMailSenderRole will buffer, store and allow to
 * resent email messages, if temporary failure occur. You wont be able to take advantage
 * from this features if you directly access IMailObjectRole.
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class CelSendMail implements IMailObjectRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(CelSendMail.class);
  
  private Mail mail;
  private CelMailConfiguration mailConfiguration;

  private PlainTextCommand plainTextCmd = new PlainTextCommand();

  /**
   * @deprecated since 2.34.0 instead use new CelSendMail()
   */
  @Deprecated
  public CelSendMail(XWikiContext context) {
  }
  
  /**
   * @deprecated since 2.67.0 instead use Component Manager access to IMailObjectRole
   */
  @Deprecated
  public CelSendMail() {}
  
  @Override
  public void setFrom(String from) {
    getMailObject().setFrom(from);
  }
  
  @Override
  public void setReplyTo(String replyTo) {
    if((replyTo != null) && (replyTo.trim().length() > 0)) {
      getMailObject().setHeader("reply-to", replyTo);
    }
  }
  
  @Override
  public void setTo(String to) {
    getMailObject().setTo(to);
  }
  
  @Override
  public void setCc(String cc) {
    getMailObject().setCc(cc);
  }
  
  @Override
  public void setBcc(String bcc) {
    getMailObject().setBcc(bcc);
  }
  
  @Override
  public void setSubject(String subject) {
    getMailObject().setSubject(subject);
  }

  @Override
  public CelMailConfiguration getMailConfiguration() {
    if (mailConfiguration == null) {
      mailConfiguration = new CelMailConfiguration();
    }
    return mailConfiguration;
  }

  @Override
  public void setHtmlContent(String htmlContent, boolean isLatin1) {
    // TODO Probabely can be removed as soon as all installations are on xwiki 2+
    if(isLatin1) {
      try {
        getMailObject().setHtmlPart(new String(htmlContent.getBytes(), "ISO-8859-1"));
      } catch (UnsupportedEncodingException e) {
        getMailObject().setHtmlPart(htmlContent);
      }
    } else {
      getMailObject().setHtmlPart(htmlContent);
    }
    if((getMailObject().getTextPart() == null)
        || "".equals(getMailObject().getTextPart().trim())) {
      String textContent = getContext().getMessageTool().get("cel_plain_text_mail") + "\r\n\r\n";
      textContent += plainTextCmd.convertToPlainText(htmlContent);
      getMailObject().setTextPart(textContent);
    }
  }
  
  @Override
  public void setTextContent(String textContent) {
    if((textContent != null) && (!"".equals(textContent))) {
      getMailObject().setTextPart(textContent);
    }
  }
  
  @Override
  public void setAttachments(List<Attachment> attachments) {
    if(attachments == null) {
      attachments = Collections.emptyList();
    }
    getMailObject().setAttachments(attachments);
  }
  
  @Override
  public void setOthers(Map<String, String> others) {
    if(others != null){
      for (String other : others.keySet()) {
        getMailObject().setHeader(other, others.get(other));
      }
    }
  }
  
  //TODO check if minimum required fields are set?
  @Override
  public int sendMail() {
    int sendResult = -999;
    if(mail != null) {
      LOGGER.trace("Sending Mail: \nfrom = '" + mail.getFrom() + "'\n" +
          "replyTo='" + mail.getTo() + "'\n" + "\n" + mail);
      MailSenderPluginApi mailPlugin = (MailSenderPluginApi)getContext().getWiki(
          ).getPluginApi("mailsender", getContext());
      sendResult = mailPlugin.sendMail(mail, getMailConfiguration());
      LOGGER.info("Sent Mail from '" + mail.getFrom() + "' to '" + mail.getTo() + 
          "'. Result was '" + sendResult + "'. Time: " + 
          Calendar.getInstance().getTimeInMillis());
    } else {
      LOGGER.info("Mail Object is null. Send result was '" + sendResult + "'. Time: " + 
          Calendar.getInstance().getTimeInMillis());
    }
    return sendResult;
  }

  Mail getMailObject() {
    if(mail == null) {
      mail = new Mail();
    }
    return mail;
  }

  void injectMail(Mail mail) {
    this.mail = mail;
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

}
