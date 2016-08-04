/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.TestMessageTool;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class PasswordRecoveryAndEmailValidationCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private PasswordRecoveryAndEmailValidationCommand passwdRecValidCmd;
  private IWebUtilsService mockWebUtilsService;
  private XWikiRequest request;
  private XWikiRightService rightServiceMock;

  @Before
  public void setUp_PasswordRecoveryAndEmailValidationCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    passwdRecValidCmd = new PasswordRecoveryAndEmailValidationCommand();
    mockWebUtilsService = createMockAndAddToDefault(IWebUtilsService.class);
    rightServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    request = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(request);
    passwdRecValidCmd.injected_webUtilsService = mockWebUtilsService;
  }

  @Test
  public void testGetDefaultMailDocRef() {
    DocumentReference defaultAccountActivation = new DocumentReference(context.getDatabase(),
        "Mails", "AccountActivationMail");
    replayDefault();
    assertEquals(defaultAccountActivation, passwdRecValidCmd.getDefaultMailDocRef());
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailContent() throws Exception {
    DocumentReference defaultAccountActivation = new DocumentReference(context.getDatabase(),
        "Mails", "AccountActivationMail");
    String expectedRenderedContent = "expectedRenderedContent";
    expect(mockWebUtilsService.renderInheritableDocument(eq(defaultAccountActivation), eq("de"), eq(
        "en"))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, passwdRecValidCmd.getValidationEmailContent(null, "de",
        "en"));
    verifyDefault();
  }

  @Test
  public void testGetActivationLink_Contentlogin_notRestricted() throws Exception {
    String toAdr = "mytest@unit.test";
    String validkey = "1j392k347";
    String expectedActivationLink = "http://www.unit-test.test/login"
        + "?email=mytest%40unit.test&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=mytest%40unit.test&ac="
        + validkey), same(context))).andReturn(expectedActivationLink);
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals(expectedActivationLink, passwdRecValidCmd.getActivationLink(toAdr, validkey));
    verifyDefault();
  }

  @Test
  public void testGetActivationLink_Contentlogin_restricted() throws Exception {
    String toAdr = "mytest@unit.test";
    String validkey = "1j392k347";
    String expectedActivationLink = "http://www.unit-test.test/login/XWiki/XWikiLogin"
        + "?email=mytest%40unit.test&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("XWiki.XWikiLogin"), eq("login"), eq(
        "email=mytest%40unit.test&ac=" + validkey), same(context))).andReturn(
            expectedActivationLink);
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(false).atLeastOnce();
    replayDefault();
    assertEquals(expectedActivationLink, passwdRecValidCmd.getActivationLink(toAdr, validkey));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject() throws Exception {
    String dictSubjectValue = "expected Rendered Subject {0}";
    String expectedRenderedSubject = "expected Rendered Subject www.unit.test";
    ((TestMessageTool) context.getMessageTool()).injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY,
        dictSubjectValue);
    expect(mockWebUtilsService.getMessageTool(eq("de"))).andReturn(context.getMessageTool());
    expect(request.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(expectedRenderedSubject, passwdRecValidCmd.getValidationEmailSubject(null, "de",
        "en"));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject_defLang() throws Exception {
    String dictSubjectValue = "expected Rendered Subject {0}";
    String expectedRenderedSubject = "expected Rendered Subject www.unit.test";
    String dicMailSubjectKey = PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY;
    ((TestMessageTool) context.getMessageTool()).injectMessage(dicMailSubjectKey, dictSubjectValue);
    XWikiMessageTool mockMessageTool = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(mockWebUtilsService.getMessageTool(eq("de"))).andReturn(mockMessageTool);
    expect(mockMessageTool.get(eq(dicMailSubjectKey), isA(List.class))).andReturn(
        dicMailSubjectKey);
    expect(mockWebUtilsService.getMessageTool(eq("en"))).andReturn(context.getMessageTool());
    expect(request.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(expectedRenderedSubject, passwdRecValidCmd.getValidationEmailSubject(null, "de",
        "en"));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject_defLang_null() throws Exception {
    String dicMailSubjectKey = PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY;
    XWikiMessageTool mockMessageTool = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(mockWebUtilsService.getMessageTool(eq("de"))).andReturn(mockMessageTool);
    expect(mockMessageTool.get(eq(dicMailSubjectKey), isA(List.class))).andReturn(
        dicMailSubjectKey);
    expect(mockWebUtilsService.getMessageTool((String) isNull())).andReturn(null).anyTimes();
    expect(request.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(dicMailSubjectKey, passwdRecValidCmd.getValidationEmailSubject(null, "de", null));
    verifyDefault();
  }

  @Test
  public void testGetFromEmailAdr_null() {
    String sender = "";
    String from = "from@mail.com";
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    replayDefault();
    sender = passwdRecValidCmd.getFromEmailAdr(sender, null);
    assertEquals(from, sender);
    verifyDefault();
  }

  @Test
  public void testSetValidationInfoInContext() throws XWikiException {
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    String to = "to@mail.com";
    String validkey = "validkey123";
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    passwdRecValidCmd.setValidationInfoInContext(to, validkey);
    assertNotNull(context.get("vcontext"));
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
    assertEquals(expectedLink, vcontext.get("activationLink"));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetNewValidationTokenForUser_XWikiGuest_deprecated() throws XWikiException {
    DocumentReference guestUserRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGuest");
    expect(xwiki.exists(eq(guestUserRef), same(context))).andReturn(false).once();
    replayDefault();
    passwdRecValidCmd.injected_webUtilsService = null;
    assertNull(passwdRecValidCmd.getNewValidationTokenForUser("XWiki.XWikiGuest", context));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetNewValidationTokenForUser_XWikiGuest() throws XWikiException {
    DocumentReference guestUserRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGuest");
    expect(xwiki.exists(eq(guestUserRef), same(context))).andReturn(false).once();
    replayDefault();
    assertNull(passwdRecValidCmd.getNewValidationTokenForUser(guestUserRef));
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_deprecated() throws XWikiException {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    expect(mockWebUtilsService.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDoc, context);
    verifyDefault(celSendMail, renderer);
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage() throws XWikiException {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    expect(doc.getTranslatedDocument(eq("de"), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault(celSendMail, renderer);
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_fallbackToCelements2web_deprecated() throws XWikiException {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    expect(mockWebUtilsService.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentCel2WebDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDoc, context);
    verifyDefault(celSendMail, renderer);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToCelements2web() throws XWikiException {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentCel2WebDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    expect(doc.getTranslatedDocument(eq("de"), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault(celSendMail, renderer);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToDisk() throws XWikiException {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    expect(xwiki.getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(context))).andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(false);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    DocumentReference defaultAccountActivation = new DocumentReference(context.getDatabase(),
        "Mails", "AccountActivationMail");
    expect(mockWebUtilsService.renderInheritableDocument(eq(defaultAccountActivation), eq("de"),
        (String) isNull())).andReturn(content);
    ((TestMessageTool) context.getMessageTool()).injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY, title);
    expect(mockWebUtilsService.getMessageTool(eq("de"))).andReturn(context.getMessageTool());
    expect(request.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault(celSendMail);
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_overwrittenSender_deprecated() throws XWikiException {
    String from = "sender@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    expect(mockWebUtilsService.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentCel2WebDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    BaseObject sendObj = new BaseObject();
    DocumentReference sendRef = new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass");
    sendObj.setStringValue("emailFrom", from);
    expect(doc.getXObject(eq(sendRef))).andReturn(sendObj);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDoc, context);
    verifyDefault(celSendMail, renderer);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToCelements2web_overwrittenSender()
      throws XWikiException {
    String adminLang = "en";
    String from = "sender@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.exists(eq(contentCel2WebDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(contentCel2WebDocRef), same(context))).andReturn(doc);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.isFromCache()).andReturn(true).once();
    doc.setFromCache(false);
    expectLastCall().once();
    expect(doc.getTranslatedDocument(eq("de"), same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    BaseObject sendObj = new BaseObject();
    DocumentReference sendRef = new DocumentReference(context.getDatabase(), "Celements2",
        "FormMailClass");
    sendObj.setStringValue("emailFrom", from);
    expect(doc.getXObject(eq(sendRef))).andReturn(sendObj);
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(xwiki.getRenderingEngine()).andReturn(renderer);
    expect(renderer.interpretText(eq(title), same(doc), same(context))).andReturn(title);
    CelSendMail celSendMail = createMock(CelSendMail.class);
    celSendMail.setFrom(eq(from));
    expectLastCall();
    celSendMail.setTo(eq(to));
    expectLastCall();
    celSendMail.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMail.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMail.setSubject(eq(title));
    expectLastCall();
    celSendMail.setReplyTo((String) isNull());
    expectLastCall();
    celSendMail.setCc((String) isNull());
    expectLastCall();
    celSendMail.setBcc((String) isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"), eq("email=to%40mail.com&ac="
        + validkey), same(context))).andReturn(expectedLink).once();
    expect(mockWebUtilsService.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault(celSendMail, renderer);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault(celSendMail, renderer);
  }

  @Test
  public void test_recoverPassword_NPE() throws Exception {
    expect(getWikiMock().getDefaultLanguage(same(context))).andReturn("en").anyTimes();
    DocumentReference accountDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "testUser");
    expect(getWikiMock().exists(eq(accountDocRef), same(context))).andReturn(true);
    expect(getWikiMock().getDocument(eq(accountDocRef), same(context))).andThrow(
        new XWikiException());
    String expectedMsg = "recovery failed";
    getMessageToolStub().injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_PASSWORD_RECOVERY_FAILED, expectedMsg);
    expect(mockWebUtilsService.getAdminMessageTool()).andReturn(
        context.getMessageTool()).anyTimes();
    replayDefault();
    assertEquals(expectedMsg, passwdRecValidCmd.recoverPassword("testUser", "inputParam"));
    verifyDefault();
  }
}
