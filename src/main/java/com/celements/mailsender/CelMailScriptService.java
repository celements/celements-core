package com.celements.mailsender;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.CelSendMail;
import com.xpn.xwiki.api.Attachment;

@Component("celmail")
public class CelMailScriptService implements ScriptService {
  
  private CelSendMail celSendMail;
  
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
  
  public boolean isValidEmail(String email) {
    //chars - a-z A-Z 0-9 œ à á â ã ä å æ ç è é ê ë ì í î ï ð ñ ò ó ô õ ö ø ù ú û ü ý þ ÿ
    String wordChars = "\\w\\-\\u0153\\u00E0-\\u00F6\\u00F8-\\u00FF";
    String regex = "^[" + wordChars + "\\.\\+]+[@][" + wordChars + "]+([.]([" + wordChars 
        + "]+))+$";
    return email.matches(regex);
  }
  
  private CelSendMail getCelSendMail() {
    if(celSendMail != null) {
      return celSendMail;
    }
    return new CelSendMail();
  }
}
