package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class CelMailConfigurationTest extends AbstractBridgedComponentTestCase {

  private CelMailConfiguration celMailConfiguration;
  private XWiki xwiki;
  private XWikiContext context;

  @Before
  public void setUp_CelMailConfigurationTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    celMailConfiguration = new CelMailConfiguration();
  }

  @Test
  public void testCelMailConfiguration() {
    /* no complicated setup. read config if needed before getting some values */
    replayDefault();
    CelMailConfiguration testMailConfig = new CelMailConfiguration();
    assertNotNull(testMailConfig);
    assertEquals("", testMailConfig.getHost_internal());
    assertEquals(-1, testMailConfig.getPort_internal());
    verifyDefault();
  }

  @Test
  public void testGetPort_internal_default() {
    replayDefault();
    assertEquals(-1, celMailConfiguration.getPort_internal());
    verifyDefault();
  }

  @Test
  public void testGetHost_internal_default() {
    replayDefault();
    assertEquals("", celMailConfiguration.getHost_internal());
    verifyDefault();
  }

  @Test
  public void testGetDefaultAdminSenderAddress() {
    expect(xwiki.getXWikiPreference(eq("admin_email"),
        eq(CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))
        ).andReturn("test@unit.test");
    replayDefault();
    assertEquals("test@unit.test", celMailConfiguration.getDefaultAdminSenderAddress());
    verifyDefault();
  }

  @Test
  public void testGetDefaultGeneralSenderAddress() {
    expect(xwiki.getXWikiPreference(eq("smtp_from"),
        eq(CelMailConfiguration.MAIL_DEFAULT_SMTP_FROM_KEY), eq(""), same(context))
        ).andReturn("test@unit.test");
    replayDefault();
    assertEquals("test@unit.test", celMailConfiguration.getDefaultGeneralSenderAddress());
    verifyDefault();
  }

  @Test
  public void testGetFrom() {
    expect(xwiki.getXWikiPreference(eq("smtp_from"),
        eq(CelMailConfiguration.MAIL_DEFAULT_SMTP_FROM_KEY), eq(""), same(context))
        ).andReturn("test@unit.test").once();
    replayDefault();
    assertEquals("test@unit.test", celMailConfiguration.getFrom());
    assertEquals("multiple reads must not lead to multiple config reads.",
        "test@unit.test", celMailConfiguration.getFrom());
    verifyDefault();
  }

  @Test
  public void testGetFrom_setBackToEmpty() {
    expect(xwiki.getXWikiPreference(eq("smtp_from"),
        eq(CelMailConfiguration.MAIL_DEFAULT_SMTP_FROM_KEY), eq(""), same(context))
        ).andReturn("test@unit.test").times(2);
    replayDefault();
    assertEquals("test@unit.test", celMailConfiguration.getFrom());
    celMailConfiguration.setFrom("");
    assertEquals("set back to empty must tricker the default configuration",
        "test@unit.test", celMailConfiguration.getFrom());
    verifyDefault();
  }

  @Test
  public void testGetFrom_overwrittenBy_setFrom() {
    expect(xwiki.getXWikiPreference(eq("smtp_from"),
        eq(CelMailConfiguration.MAIL_DEFAULT_SMTP_FROM_KEY), eq(""), same(context))
        ).andReturn("test@unit.test").anyTimes();
    replayDefault();
    celMailConfiguration.setFrom("myTest@specialUnit.test");
    assertEquals("myTest@specialUnit.test", celMailConfiguration.getFrom());
    verifyDefault();
  }

  @Test
  public void testGetFrom_fallbackToAdminEmailAdress() {
    expect(xwiki.getXWikiPreference(eq("smtp_from"),
        eq(CelMailConfiguration.MAIL_DEFAULT_SMTP_FROM_KEY), eq(""), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("admin_email"),
        eq(CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))
        ).andReturn("test@unit.test").once();
    replayDefault();
    assertEquals("test@unit.test", celMailConfiguration.getFrom());
    assertEquals("multiple reads must not lead to multiple config reads.",
        "test@unit.test", celMailConfiguration.getFrom());
    verifyDefault();
  }

  @Test
  public void testusesAuthentication_localConfig_yes() {
    celMailConfiguration.setSmtpUsername("username");
    celMailConfiguration.setSmtpPassword("password");
    replayDefault();
    assertTrue(celMailConfiguration.usesAuthentication_localConfig());
    verifyDefault();
  }

  @Test
  public void testusesAuthentication_localConfig_smtp_username_empty() {
    celMailConfiguration.setSmtpUsername("");
    celMailConfiguration.setSmtpPassword("password");
    replayDefault();
    assertFalse(celMailConfiguration.usesAuthentication_localConfig());
    verifyDefault();
  }

  @Test
  public void testusesAuthentication_localConfig_smtp_password_empty() {
    celMailConfiguration.setSmtpUsername("username");
    celMailConfiguration.setSmtpPassword("");
    replayDefault();
    assertFalse(celMailConfiguration.usesAuthentication_localConfig());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_yes() {
    replayDefault();
    assertTrue(celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_usingLocalSmtpAuth() {
    celMailConfiguration.setSmtpUsername("username");
    celMailConfiguration.setSmtpPassword("password");
    replayDefault();
    assertTrue(celMailConfiguration.usesAuthentication_localConfig());
    assertFalse(celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_usingLocalHost() {
    celMailConfiguration.setHost("unit.test.host");
    replayDefault();
    assertFalse(celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_usingLocalPort() {
    celMailConfiguration.setPort(34);
    replayDefault();
    assertFalse(celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_usingLocalProperties() {
    celMailConfiguration.setExtraProperties("a=b,c=d");
    replayDefault();
    assertFalse(celMailConfiguration.noHostConfig());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraProperties_internal(extProps, true);
    assertFalse(extProps.isEmpty());
    verifyDefault();
  }

  @Test
  public void testNoHostConfig_emptyProperties() {
    celMailConfiguration.setExtraProperties("");
    replayDefault();
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraProperties_internal(extProps, true);
    assertTrue(extProps.isEmpty());
    verifyDefault();
  }

  @Test
  public void testSetExtraProperties() {
    celMailConfiguration.setExtraProperties("a=b,c=d");
    replayDefault();
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps, true);
    assertEquals("b", extProps.get("a"));
    assertEquals("d", extProps.get("c"));
    verifyDefault();
  }

  @Test
  public void testReadServerDefaultHostConfiguration() {
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").anyTimes();
    replayDefault();
    celMailConfiguration.readServerDefaultHostConfiguration();
    assertEquals("mySmtpUserName", celMailConfiguration.getSmtpUsername_internal());
    assertEquals("myServerPassword", celMailConfiguration.getSmtpPassword_internal());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost_internal());
    assertEquals(567, celMailConfiguration.getPort_internal());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_fallbackToDefault_completeConfig() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").once();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).once();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").once();
    replayDefault();
    assertEquals("mySmtpUserName", celMailConfiguration.getSmtpUsername());
    assertEquals("myServerPassword", celMailConfiguration.getSmtpPassword());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals(567, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_fallbackToDefault_onlyUsername() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("myLocalUsername").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").once();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).once();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").once();
    replayDefault();
    assertEquals("mySmtpUserName", celMailConfiguration.getSmtpUsername());
    assertEquals("myServerPassword", celMailConfiguration.getSmtpPassword());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals(567, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_fallbackToDefault_onlyPassword() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("myLocalPassword").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").once();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).once();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").once();
    replayDefault();
    assertEquals("mySmtpUserName", celMailConfiguration.getSmtpUsername());
    assertEquals("myServerPassword", celMailConfiguration.getSmtpPassword());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals(567, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_localHost() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("myLocalHost").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").anyTimes();
    replayDefault();
    assertNull(celMailConfiguration.getSmtpUsername());
    assertNull(celMailConfiguration.getSmtpPassword());
    assertEquals("myLocalHost", celMailConfiguration.getHost());
    assertEquals(25, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertTrue(extProps.isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_localPort() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(567).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").anyTimes();
    replayDefault();
    assertNull(celMailConfiguration.getSmtpUsername());
    assertNull(celMailConfiguration.getSmtpPassword());
    assertEquals("", celMailConfiguration.getHost());
    assertEquals(567, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertTrue(extProps.isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_localExtraProps() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("testLocalProp=testLocalValue,localProp2=valueLocal2").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").anyTimes();
    replayDefault();
    assertNull(celMailConfiguration.getSmtpUsername());
    assertNull(celMailConfiguration.getSmtpPassword());
    assertEquals("", celMailConfiguration.getHost());
    assertEquals(25, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testLocalValue", extProps.get("testLocalProp"));
    assertEquals("valueLocal2", extProps.get("localProp2"));
    assertNull(extProps.get("testProp"));
    assertNull(extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_localUsernameAndPassword() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("myLocalPassword").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("myLocalUsername").once();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myServerPassword").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpUserName").anyTimes();
    replayDefault();
    assertEquals("myLocalUsername", celMailConfiguration.getSmtpUsername());
    assertEquals("myLocalPassword", celMailConfiguration.getSmtpPassword());
    assertEquals("", celMailConfiguration.getHost());
    assertEquals(25, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertTrue(extProps.isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_fallbackToDefault_onlyUsername_NoPassword() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpAuthUserName").atLeastOnce();
    replayDefault();
    assertNull(celMailConfiguration.getSmtpUsername());
    verifyDefault();
  }

  @Test
  public void testGetSmtpUsername_fallbackToDefault_usernameAndPassword() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myNotSecurePassword").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpAuthUserName").once();
    replayDefault();
    assertEquals("mySmtpAuthUserName", celMailConfiguration.getSmtpUsername());
    assertTrue("after setting server_username usesAuthentication_localConfig must be"
        + " true.", celMailConfiguration.usesAuthentication_localConfig());
    assertFalse("after setting server_username noHostConfig must be false.",
        celMailConfiguration.noHostConfig());
    assertEquals("multiple reads must not lead to multiple config reads.",
        "mySmtpAuthUserName", celMailConfiguration.getSmtpUsername());
    assertEquals("setting any smtp-server config must deactivated fallback to"
        + " 'localhost'", "", celMailConfiguration.getHost());
    verifyDefault();
  }

  @Test
  public void testGetSmtpPassword_fallbackToDefault_usernameAndPassword() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myNotSecurePassword").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("mySmtpAuthUserName").once();
    replayDefault();
    assertEquals("myNotSecurePassword", celMailConfiguration.getSmtpPassword());
    assertTrue("after setting server_username usesAuthentication_localConfig must be"
        + " true.", celMailConfiguration.usesAuthentication_localConfig());
    assertFalse("after setting server_username noHostConfig must be false.",
        celMailConfiguration.noHostConfig());
    assertEquals("multiple reads must not lead to multiple config reads.",
        "myNotSecurePassword", celMailConfiguration.getSmtpPassword());
    assertEquals("setting any smtp-server config must deactivated fallback to"
        + " 'localhost'", "", celMailConfiguration.getHost());
    verifyDefault();
  }

  @Test
  public void testGetSmtpPassword_fallbackToDefault_only_password_NoUsername() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("myNotSecurePassword").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").once();
    replayDefault();
    assertNull(celMailConfiguration.getSmtpPassword());
    assertFalse("after setting only server_username usesAuthentication_localConfig must"
        + " be false.", celMailConfiguration.usesAuthentication_localConfig());
    verifyDefault();
  }

  @Test
  public void testGetHost_fallbackToDefault() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("smtp.unit.test").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals("multiple reads must not lead to multiple config reads.",
        "smtp.unit.test", celMailConfiguration.getHost());
    verifyDefault();
  }

  @Test
  public void testGetHost_fallbackToDefault_noDefaultHost_usingLocalhost() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals("localhost", celMailConfiguration.getHost());
    assertEquals(25, celMailConfiguration.getPort());
    assertTrue("after not setting any smtp-server configuration noHostConfig must still"
        + " be true.", celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testGetHost_fallbackToDefault_deactivatedDefaultHost() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("-").once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals("empty string defaults to 'localhost' in javamail!", "-",
        celMailConfiguration.getHost());
    assertEquals("multiple reads must not lead to multiple config reads.", "-",
        celMailConfiguration.getHost());
    verifyDefault();
  }

  @Test
  public void testGetPort_fallbackToDefault() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(567L).once();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals(567, celMailConfiguration.getPort());
    assertEquals("multiple reads must not lead to multiple config reads.", 567,
        celMailConfiguration.getPort());
    verifyDefault();
  }

  @Test
  public void testGetPort_fallbackToDefault_noDefaultPort() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals(25, celMailConfiguration.getPort());
    assertTrue("after not setting any smtp-server configuration noHostConfig must still"
        + " be true.", celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testSetProps_fallbackToDefault() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("testProp=testValue,prop2=value2").once();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    // multiple reads must not lead to multiple config reads.
    Properties extProps2 = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps2 , true);
    assertEquals("testValue", extProps2.get("testProp"));
    assertEquals("value2", extProps2.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testSetProps_fallbackToDefault_noDefaultProps() {
    expect(xwiki.getXWikiPreference(eq("smtp_server"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"), eq(-1), same(context))
        ).andReturn(-1).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mail.default.smtp_port"), eq(-1L))
        ).andReturn(-1L).anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.javamail_extra_props"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_password"), eq(""))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.mail.default.smtp_server_username"), eq(""))
        ).andReturn("").anyTimes();
    replayDefault();
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertTrue(extProps.isEmpty());
    assertTrue("after not setting any smtp-server configuration noHostConfig must still"
        + " be true.", celMailConfiguration.noHostConfig());
    verifyDefault();
  }

  @Test
  public void testCheckHostConfiguration_skipReadConfigIfManualSetup_complete() {
    celMailConfiguration.setSmtpUsername("mySmtpUserName");
    celMailConfiguration.setSmtpPassword("myServerPassword");
    celMailConfiguration.setHost("smtp.unit.test");
    celMailConfiguration.setPort(567);
    celMailConfiguration.setExtraProperties("testProp=testValue\nprop2=value2");
    replayDefault();
    celMailConfiguration.checkHostConfiguration();
    assertEquals("mySmtpUserName", celMailConfiguration.getSmtpUsername());
    assertEquals("myServerPassword", celMailConfiguration.getSmtpPassword());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals(567, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertEquals("testValue", extProps.get("testProp"));
    assertEquals("value2", extProps.get("prop2"));
    verifyDefault();
  }

  @Test
  public void testCheckHostConfiguration_skipReadConfigIfManualSetup_incomplete() {
    celMailConfiguration.setHost("smtp.unit.test");
    replayDefault();
    celMailConfiguration.checkHostConfiguration();
    assertNull(celMailConfiguration.getSmtpUsername());
    assertNull(celMailConfiguration.getSmtpPassword());
    assertEquals("smtp.unit.test", celMailConfiguration.getHost());
    assertEquals(25, celMailConfiguration.getPort());
    Properties extProps = new Properties();
    celMailConfiguration.appendExtraPropertiesTo(extProps , true);
    assertTrue(extProps.isEmpty());
    verifyDefault();
  }

}
