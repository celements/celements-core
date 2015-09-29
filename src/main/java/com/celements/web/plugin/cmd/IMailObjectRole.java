package com.celements.web.plugin.cmd;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Attachment;

@ComponentRole
public interface IMailObjectRole {

  public int sendMail();

  public void setOthers(Map<String, String> others);

  public void setAttachments(List<Attachment> attachments);

  public void setTextContent(String textContent);

  public void setHtmlContent(String htmlContent, boolean isLatin1);

  public CelMailConfiguration getMailConfiguration();

  public void setSubject(String subject);

  public void setBcc(String bcc);

  public void setCc(String cc);

  public void setTo(String to);

  public void setReplyTo(String replyTo);

  public void setFrom(String from);

}
