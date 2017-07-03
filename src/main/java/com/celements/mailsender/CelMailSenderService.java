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

  // chars - a-z A-Z 0-9 œ à á â ã ä å æ ç è é ê ë ì í î ï ð ñ ò ó ô õ ö ø ù ú û ü ý þ ÿ
  public static final String EMAIL_VALIDATION_REGEX = "^[\\w\\-\\u0153\\u00E0-\\u00F6\\u00F8-\\u00FF.+]+[@][\\w\\-\""
      + "\u0153\\u00E0-\\u00F6\\u00F8-\\u00FF]+([.]([\\w\\-\\u0153\\u00E0-\\u00F6\\u00F8-\\u00FF]+))+$";

  public static final String EMAIL_VALIDATION_REGEX_CLASS_DEFINITIONS = "/" + EMAIL_VALIDATION_REGEX
      + "/";

  @Override
  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others) {
    return sendMail(from, replyTo, to, cc, bcc, subject, htmlContent, textContent, attachments,
        others, false);
  }

  @Override
  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others, boolean isLatin1) {
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

  @Override
  public boolean isValidEmail(String email) {
    return email.matches(getEmailValidationRegex());
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

  @Override
  public String getEmailValidationRegex() {
    return EMAIL_VALIDATION_REGEX;
  }

  @Override
  public String getEmailValidationRegexForClassDefinitions() {
    return EMAIL_VALIDATION_REGEX_CLASS_DEFINITIONS;
  }
}
