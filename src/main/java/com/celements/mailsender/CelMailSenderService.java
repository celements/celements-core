package com.celements.mailsender;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;

import com.celements.web.plugin.cmd.IMailObjectRole;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.web.Utils;

@Component
public class CelMailSenderService implements IMailSenderRole {

  private IMailObjectRole injectedMailObject;

  @Override
  public int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others) {
    return sendMail(from, replyTo, to, cc, bcc, subject, htmlContent, textContent,
        attachments, others, false);
  }

  @Override
  public int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others, boolean isLatin1) {
    IMailObjectRole sender = getNewMailObject();
    sender.setFrom(from);
    sender.setReplyTo(replyTo);
    sender.setTo(to);
    sender.setCc(cc);
    sender.setBcc(bcc);
    sender.setSubject(subject);
    sender.setHtmlContent(htmlContent, isLatin1);
    sender.setTextContent(textContent);
    sender.setAttachments(attachments);
    sender.setOthers(others);
    return sender.sendMail();
  }

  /**
   * use for tests only!!!
   */
  void internal_injectedMailObject(IMailObjectRole mockMailObject) {
    this.injectedMailObject = mockMailObject;
  }

  IMailObjectRole getNewMailObject() {
    if (injectedMailObject != null) {
      return injectedMailObject;
    }
    return Utils.getComponent(IMailObjectRole.class);
  }

}
