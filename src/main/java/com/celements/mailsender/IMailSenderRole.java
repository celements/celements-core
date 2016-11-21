package com.celements.mailsender;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Attachment;

@ComponentRole
public interface IMailSenderRole {

  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others);

  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others, boolean isLatin1);

  public boolean isValidEmail(String email);

  public String getEmailValidationRegex();

  public String getEmailValidationRegexForClassDefinitions();

}
