package com.celements.web.plugin.cmd;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.mailsender.MailConfiguration;
import com.xpn.xwiki.web.Utils;

/**
 * The Mail Configuration must only be carefully changed to NOT generate security issues.
 * It is important that the default smtp-server configuration for smtp-user, smtp-password
 * smtp-host, smtp-port AND extra-props must be kept integer. Thus all or none must be
 * taken from the XWikiPreferences. They MUST NOT BE MIXED with default server
 * configuration. If they get mixed and default server configuration includes smtp-auth
 * username and password they can get undisclosed in the following attack scenario: The
 * smtp-host config is overwritten to some special server tracking username and passwords.
 * Mixing the configuration leads to the server default username and password would be
 * sent to this unfriendly server.
 * 
 * @author fabian
 * @since 2.34.0
 */
public class CelMailConfiguration extends MailConfiguration {

  private static Log LOGGER = LogFactory.getFactory().getInstance(CelMailConfiguration.class);

  public static final String MAIL_DEFAULT_ADMIN_EMAIL_KEY = "celements.mail.default.admin_email";

  public static final String MAIL_DEFAULT_SMTP_FROM_KEY = "celements.mail.default.smtp_from";

  public CelMailConfiguration() {
    setPort(-1);
    setHost("");
  }

  void readVirtualWikiHostConfiguration() {
    String smtpServer = getXWiki().getXWikiPreference("smtp_server", getContext());
    if (!StringUtils.isBlank(smtpServer)) {
      setHost(smtpServer);
    }
    int port = getXWiki().getXWikiPreferenceAsInt("smtp_port", -1, getContext());
    setPort(port);
    String smtpServerUsername = getXWiki().getXWikiPreference("smtp_server_username", getContext());
    String smtpServerPassword = getXWiki().getXWikiPreference("smtp_server_password", getContext());
    if (!StringUtils.isEmpty(smtpServerUsername) && !StringUtils.isEmpty(smtpServerPassword)) {
      setSmtpUsername(smtpServerUsername);
      setSmtpPassword(smtpServerPassword);
    }

    String javaMailExtraProps = getXWiki().getXWikiPreference("javamail_extra_props", getContext());
    if (!StringUtils.isEmpty(javaMailExtraProps)) {
      setExtraProperties(javaMailExtraProps);
    }
  }

  public String getDefaultAdminSenderAddress() {
    return getXWiki().getXWikiPreference("admin_email", MAIL_DEFAULT_ADMIN_EMAIL_KEY, "",
        getContext());
  }

  public String getDefaultGeneralSenderAddress() {
    return getXWiki().getXWikiPreference("smtp_from", MAIL_DEFAULT_SMTP_FROM_KEY, "", getContext());
  }

  boolean usesAuthentication_localConfig() {
    return !StringUtils.isEmpty(getSmtpUsername_internal()) && !StringUtils.isEmpty(
        getSmtpPassword_internal());
  }

  void checkHostConfiguration() {
    if (noHostConfig()) {
      readVirtualWikiHostConfiguration();
      if (noHostConfig()) {
        readServerDefaultHostConfiguration();
      }
    }
  }

  void readServerDefaultHostConfiguration() {
    String smtpServer = getXWiki().Param("celements.mail.default.smtp_server", "");
    super.setHost(smtpServer);
    int port = (int) getXWiki().ParamAsLong("celements.mail.default.smtp_port", -1);
    setPort(port);
    String smtpServerUsername = getXWiki().Param("celements.mail.default.smtp_server_username", "");
    String smtpServerPassword = getXWiki().Param("celements.mail.default.smtp_server_password", "");
    if (!StringUtils.isEmpty(smtpServerUsername) && !StringUtils.isEmpty(smtpServerPassword)) {
      super.setSmtpUsername(smtpServerUsername);
      super.setSmtpPassword(smtpServerPassword);
    }
    String javaMailExtraProps = getXWiki().Param("celements.mail.default.javamail_extra_props", "");
    if (!StringUtils.isEmpty(javaMailExtraProps)) {
      setExtraProperties(javaMailExtraProps);
    }
    LOGGER.debug("setting default HostConfiguration smtp_server_username [" + smtpServerUsername
        + "] using smtp-password [" + !"".equals(smtpServerPassword) + "] and host [" + smtpServer
        + ":" + port + "] and javaMailExtraProps [" + javaMailExtraProps + "].");
  }

  boolean noHostConfig() {
    Properties testProperties = new Properties();
    appendExtraProperties_internal(testProperties, true);
    return (!usesAuthentication_localConfig() && StringUtils.isEmpty(getHost_internal())
        && (getPort_internal() <= -1) && testProperties.isEmpty());
  }

  String getHost_internal() {
    return super.getHost();
  }

  @Override
  public String getHost() {
    checkHostConfiguration();
    if (!StringUtils.isEmpty(getHost_internal())) {
      return getHost_internal();
    } else if (noHostConfig()) {
      return "localhost";
    }
    return "";
  }

  int getPort_internal() {
    return super.getPort();
  }

  @Override
  public int getPort() {
    checkHostConfiguration();
    if (getPort_internal() > -1) {
      return getPort_internal();
    } else {
      return 25;
    }
  }

  @Override
  public String getFrom() {
    if (StringUtils.isEmpty(super.getFrom())) {
      super.setFrom(getDefaultGeneralSenderAddress());
    }
    if (StringUtils.isEmpty(super.getFrom())) {
      super.setFrom(getDefaultAdminSenderAddress());
    }
    return super.getFrom();
  }

  @Override
  public String getSmtpUsername() {
    checkHostConfiguration();
    return getSmtpUsername_internal();
  }

  String getSmtpUsername_internal() {
    return super.getSmtpUsername();
  }

  @Override
  public String getSmtpPassword() {
    checkHostConfiguration();
    return getSmtpPassword_internal();
  }

  String getSmtpPassword_internal() {
    return super.getSmtpPassword();
  }

  @Override
  public void setExtraProperties(String extraPropertiesString) {
    String extraProps = extraPropertiesString;
    if (!StringUtils.isEmpty(extraPropertiesString)) {
      extraProps = extraPropertiesString.replaceAll(",", "\n");
    }
    super.setExtraProperties(extraProps);
  }

  @Override
  public void appendExtraPropertiesTo(Properties externalProperties, boolean overwrite) {
    checkHostConfiguration();
    appendExtraProperties_internal(externalProperties, overwrite);
  }

  void appendExtraProperties_internal(Properties externalProperties, boolean overwrite) {
    super.appendExtraPropertiesTo(externalProperties, overwrite);
  }

  private XWiki getXWiki() {
    return getContext().getWiki();
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

}
