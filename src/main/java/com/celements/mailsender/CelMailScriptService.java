package com.celements.mailsender;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.CelSendMail;
import com.xpn.xwiki.api.Attachment;

@Component("celmail")
public class CelMailScriptService implements ScriptService{
  
  private CelSendMail celSendMail;
  
  @Requirement
  private Execution execution;

  public int sendMail(
      String from, String replyTo,
      String to, String cc, String bcc,
      String subject, String htmlContent, String textContent,
      List<Attachment> attachments, Map<String, String> others) {
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
  
  private CelSendMail getCelSendMail() {
    if(celSendMail != null) {
      return celSendMail;
    }
    return new CelSendMail();
  }
}
