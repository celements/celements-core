package com.celements.web.plugin.cmd;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.mailsender.MailConfiguration;
import com.xpn.xwiki.web.Utils;

public class CelMailConfiguration extends MailConfiguration {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelMailConfiguration.class);

  public CelMailConfiguration() {
    String smtpServer = getXWiki().getXWikiPreference("smtp_server",
        "celements.mail.default.smtp_server", "", getContext());
    LOGGER.debug("reading smtp_server from configuration [" + smtpServer + "].");
    if (!StringUtils.isBlank(smtpServer)) {
      setHost(smtpServer);
    }

    int port = getXWiki().getXWikiPreferenceAsInt("smtp_port",
        "celements.mail.default.smtp_port", 25, getContext());
    LOGGER.debug("reading smtp_port from configuration [" + port + "].");
    setPort(port);

    String from = getXWiki().getXWikiPreference("admin_email",
        "celements.mail.default.admin_email", "", getContext());
    LOGGER.debug("reading admin_email from configuration [" + from + "].");
    if (!StringUtils.isBlank(from)) {
      setFrom(from);
    }

    String smtpServerUsername = getXWiki().getXWikiPreference("smtp_server_username",
        "celements.mail.default.smtp_server_username", "", getContext());
    String smtpServerPassword = getXWiki().getXWikiPreference("smtp_server_password",
        "celements.mail.default.smtp_server_password", "", getContext());
    LOGGER.debug("reading smtp_server_username AND smtp_server_password from"
        + " configuration smtp_server_username [" + smtpServerUsername
        + "] smtp_server_username [" + smtpServerPassword + "].");
    if (!StringUtils.isEmpty(smtpServerUsername)
        && !StringUtils.isEmpty(smtpServerPassword)) {
      setSmtpUsername(smtpServerUsername);
      setSmtpPassword(smtpServerPassword);
    }

    String javaMailExtraProps = getXWiki().getXWikiPreference("javamail_extra_props",
        "celements.mail.default.javamail_extra_props", "", getContext());
    LOGGER.debug("reading javamail_extra_props from configuration ["
        + javaMailExtraProps + "].");
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
