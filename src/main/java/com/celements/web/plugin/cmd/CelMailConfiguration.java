package com.celements.web.plugin.cmd;

import org.apache.commons.lang.StringUtils;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.mailsender.MailConfiguration;
import com.xpn.xwiki.web.Utils;

public class CelMailConfiguration extends MailConfiguration {

  public CelMailConfiguration() {

    String smtpServer = getXWiki().getXWikiPreference("smtp_server",
        "celements.mail.default.smtp_server", "", getContext());
    if (!StringUtils.isBlank(smtpServer)) {
      setHost(smtpServer);
    }

    int port = getXWiki().getXWikiPreferenceAsInt("smtp_port",
        "celements.mail.default.smtp_port", 25, getContext());
    setPort(port);

    String from = getXWiki().getXWikiPreference("admin_email",
        "celements.mail.default.admin_email", "", getContext());
    if (!StringUtils.isBlank(from)) {
      setFrom(from);
    }

    String smtpServerUsername = getXWiki().getXWikiPreference("smtp_server_username",
        "celements.mail.default.smtp_server_username", "", getContext());
    String smtpServerPassword = getXWiki().getXWikiPreference("smtp_server_password",
        "celements.mail.default.smtp_server_password", "", getContext());
    if (!StringUtils.isEmpty(smtpServerUsername)
        && !StringUtils.isEmpty(smtpServerPassword)) {
      setSmtpUsername(smtpServerUsername);
      setSmtpPassword(smtpServerPassword);
    }

    String javaMailExtraProps = getXWiki().getXWikiPreference("javamail_extra_props",
        "celements.mail.default.javamail_extra_props", "", getContext());
    if (!StringUtils.isEmpty(javaMailExtraProps)) {
      setExtraProperties(javaMailExtraProps);
    }
  }

  public XWiki getXWiki() {
    return getContext().getWiki();
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

}
