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

import static com.celements.auth.user.UserTestUtils.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.query.QueryException;

import com.celements.auth.IAuthenticationServiceRole;
import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.common.test.TestMessageTool;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class PasswordRecoveryAndEmailValidationCommandTest extends AbstractComponentTest {

  private final DocumentReference userDocRef = new ImmutableDocumentReference("db", "XWiki",
      "msladek");

  private PasswordRecoveryAndEmailValidationCommand cmd;

  private IModelAccessFacade modelAccessMock;
  private IWebUtilsService webUtilsMock;
  private UserService userServiceMock;
  private IAuthenticationServiceRole authServiceMock;
  private IMailObjectRole celSendMailMock;
  private XWikiRightService rightServiceMock;
  private XWikiRenderingEngine rendererMock;
  private XWikiRequest requestMock;

  @Before
  public void setUp_PasswordRecoveryAndEmailValidationCommandTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    userServiceMock = registerComponentMock(UserService.class);
    authServiceMock = registerComponentMock(IAuthenticationServiceRole.class);
    webUtilsMock = registerComponentMock(IWebUtilsService.class);
    celSendMailMock = registerComponentMock(IMailObjectRole.class);
    rightServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightServiceMock).anyTimes();
    rendererMock = createMockAndAddToDefault(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(rendererMock).anyTimes();
    requestMock = createMockAndAddToDefault(XWikiRequest.class);
    getContext().setRequest(requestMock);
    cmd = new PasswordRecoveryAndEmailValidationCommand();
  }

  @Test
  public void testGetDefaultMailDocRef() {
    DocumentReference defaultAccountActivation = new DocumentReference(getContext().getDatabase(),
        "Mails", "AccountActivationMail");
    replayDefault();
    assertEquals(defaultAccountActivation, cmd.getDefaultMailDocRef());
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailContent() throws Exception {
    DocumentReference defaultAccountActivation = new DocumentReference(getContext().getDatabase(),
        "Mails", "AccountActivationMail");
    String expectedRenderedContent = "expectedRenderedContent";
    expect(webUtilsMock.renderInheritableDocument(eq(defaultAccountActivation), eq("de"), eq(
        "en"))).andReturn(expectedRenderedContent);
    replayDefault();
    assertEquals(expectedRenderedContent, cmd.getValidationEmailContent(null, "de", "en"));
    verifyDefault();
  }

  @Test
  public void testGetActivationLink_Contentlogin_notRestricted() throws Exception {
    String toAdr = "mytest@unit.test";
    String validkey = "1j392k347";
    String expectedActivationLink = "http://www.unit-test.test/login"
        + "?email=mytest%40unit.test&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=mytest%40unit.test&ac=" + validkey), same(getContext()))).andReturn(
            expectedActivationLink);
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals(expectedActivationLink, cmd.getActivationLink(toAdr, validkey));
    verifyDefault();
  }

  @Test
  public void testGetActivationLink_Contentlogin_restricted() throws Exception {
    String toAdr = "mytest@unit.test";
    String validkey = "1j392k347";
    String expectedActivationLink = "http://www.unit-test.test/login/XWiki/XWikiLogin"
        + "?email=mytest%40unit.test&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("XWiki.XWikiLogin"), eq("login"), eq(
        "email=mytest%40unit.test&ac=" + validkey), same(getContext()))).andReturn(
            expectedActivationLink);
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(false).atLeastOnce();
    replayDefault();
    assertEquals(expectedActivationLink, cmd.getActivationLink(toAdr, validkey));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject() throws Exception {
    String dictSubjectValue = "expected Rendered Subject {0}";
    String expectedRenderedSubject = "expected Rendered Subject www.unit.test";
    ((TestMessageTool) getContext().getMessageTool()).injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY,
        dictSubjectValue);
    expect(webUtilsMock.getMessageTool(eq("de"))).andReturn(getContext().getMessageTool());
    expect(requestMock.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(expectedRenderedSubject, cmd.getValidationEmailSubject(null, "de", "en"));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject_defLang() throws Exception {
    String dictSubjectValue = "expected Rendered Subject {0}";
    String expectedRenderedSubject = "expected Rendered Subject www.unit.test";
    String dicMailSubjectKey = PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY;
    ((TestMessageTool) getContext().getMessageTool()).injectMessage(dicMailSubjectKey,
        dictSubjectValue);
    XWikiMessageTool mockMessageTool = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(webUtilsMock.getMessageTool(eq("de"))).andReturn(mockMessageTool);
    expect(mockMessageTool.get(eq(dicMailSubjectKey), isA(List.class))).andReturn(
        dicMailSubjectKey);
    expect(webUtilsMock.getMessageTool(eq("en"))).andReturn(getContext().getMessageTool());
    expect(requestMock.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(expectedRenderedSubject, cmd.getValidationEmailSubject(null, "de", "en"));
    verifyDefault();
  }

  @Test
  public void testGetValidationEmailSubject_defLang_null() throws Exception {
    String dicMailSubjectKey = PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY;
    XWikiMessageTool mockMessageTool = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(webUtilsMock.getMessageTool(eq("de"))).andReturn(mockMessageTool);
    expect(mockMessageTool.get(eq(dicMailSubjectKey), isA(List.class))).andReturn(
        dicMailSubjectKey);
    expect(webUtilsMock.getMessageTool((String) isNull())).andReturn(null).anyTimes();
    expect(requestMock.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    replayDefault();
    assertEquals(dicMailSubjectKey, cmd.getValidationEmailSubject(null, "de", null));
    verifyDefault();
  }

  @Test
  public void testGetFromEmailAdr_null() {
    String sender = "";
    String from = "from@mail.com";
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    replayDefault();
    sender = cmd.getFromEmailAdr(sender, null);
    assertEquals(from, sender);
    verifyDefault();
  }

  @Test
  public void testSetValidationInfoInContext() throws Exception {
    VelocityContext vcontext = new VelocityContext();
    getContext().put("vcontext", vcontext);
    String to = "to@mail.com";
    String validkey = "validkey123";
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    cmd.setValidationInfoInContext(to, validkey);
    assertNotNull(getContext().get("vcontext"));
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
    assertEquals(expectedLink, vcontext.get("activationLink"));
    verifyDefault();
  }

  @Test
  public void test_createNewValidationTokenForUser() throws Exception {
    String token = "asdf";
    User user = createMockAndAddToDefault(User.class);
    expect(userServiceMock.getUser(userDocRef)).andReturn(user);
    XWikiDocument userDoc = createAndExpectUserDoc(userDocRef);
    expect(user.getDocument()).andReturn(userDoc);
    expect(authServiceMock.getUniqueValidationKey()).andReturn(token);
    expect(modelAccessMock.setProperty(userDoc, XWikiUsersClass.FIELD_VALID_KEY, token)).andReturn(
        true);
    modelAccessMock.saveDocument(same(userDoc), anyObject(String.class));
    expectLastCall();

    replayDefault();
    assertEquals(token, cmd.createNewValidationTokenForUser(userDocRef));
    verifyDefault();
  }

  @Test
  public void test_createNewValidationTokenForUser_UserInstantiationException() throws Exception {
    Throwable cause = new UserInstantiationException("");
    expect(userServiceMock.getUser(userDocRef)).andThrow(cause);

    replayDefault();
    assertSame(cause, new ExceptionAsserter<CreatingValidationTokenFailedException>(
        CreatingValidationTokenFailedException.class) {

      @Override
      protected void execute() throws Exception {
        cmd.createNewValidationTokenForUser(userDocRef);
      }
    }.evaluate().getCause());
    verifyDefault();
  }

  @Test
  public void test_createNewValidationTokenForUser_QueryException() throws Exception {
    Throwable cause = new QueryException("", null, null);
    User user = createMockAndAddToDefault(User.class);
    expect(userServiceMock.getUser(userDocRef)).andReturn(user);
    XWikiDocument userDoc = createAndExpectUserDoc(userDocRef);
    expect(user.getDocument()).andReturn(userDoc);
    expect(authServiceMock.getUniqueValidationKey()).andThrow(cause);

    replayDefault();
    assertSame(cause, new ExceptionAsserter<CreatingValidationTokenFailedException>(
        CreatingValidationTokenFailedException.class) {

      @Override
      protected void execute() throws Exception {
        cmd.createNewValidationTokenForUser(userDocRef);
      }
    }.evaluate().getCause());
    verifyDefault();
  }

  @Test
  public void test_createNewValidationTokenForUser_DocumentSaveException() throws Exception {
    Throwable cause = new DocumentSaveException(userDocRef);
    String token = "asdf";
    User user = createMockAndAddToDefault(User.class);
    expect(userServiceMock.getUser(userDocRef)).andReturn(user);
    XWikiDocument userDoc = createAndExpectUserDoc(userDocRef);
    expect(user.getDocument()).andReturn(userDoc);
    expect(authServiceMock.getUniqueValidationKey()).andReturn(token);
    expect(modelAccessMock.setProperty(userDoc, XWikiUsersClass.FIELD_VALID_KEY, token)).andReturn(
        true);
    modelAccessMock.saveDocument(same(userDoc), anyObject(String.class));
    expectLastCall().andThrow(cause);

    replayDefault();
    assertSame(cause, new ExceptionAsserter<CreatingValidationTokenFailedException>(
        CreatingValidationTokenFailedException.class) {

      @Override
      protected void execute() throws Exception {
        cmd.createNewValidationTokenForUser(userDocRef);
      }
    }.evaluate().getCause());
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_deprecated() throws Exception {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    expect(webUtilsMock.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    VelocityContext vcontext = new VelocityContext();
    getContext().put("vcontext", vcontext);
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentDocRef))).andReturn(doc);
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();

    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDoc, getContext());
    verifyDefault();
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage() throws Exception {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    VelocityContext vcontext = new VelocityContext();
    getContext().put("vcontext", vcontext);
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentDocRef))).andReturn(doc);
    expect(doc.getTranslatedDocument(eq("de"), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();

    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault();
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_fallbackToCelements2web_deprecated() throws Exception {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    expect(webUtilsMock.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    getContext().put("vcontext", new VelocityContext());
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(false);
    expect(modelAccessMock.exists(eq(contentCel2WebDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentCel2WebDocRef))).andReturn(doc);
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();

    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDoc, getContext());
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToCelements2web() throws Exception {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    getContext().put("vcontext", new VelocityContext());
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(false);
    expect(modelAccessMock.exists(eq(contentCel2WebDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentCel2WebDocRef))).andReturn(doc);
    expect(doc.getTranslatedDocument(eq("de"), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    expect(doc.getXObject(eq(new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass")))).andReturn(null);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToDisk() throws Exception {
    String adminLang = "en";
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    getContext().put("vcontext", new VelocityContext());
    expect(getWikiMock().getXWikiPreference(eq("admin_email"), eq(
        CelMailConfiguration.MAIL_DEFAULT_ADMIN_EMAIL_KEY), eq(""), same(getContext()))).andReturn(
            from);
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(false);
    expect(modelAccessMock.exists(eq(contentCel2WebDocRef))).andReturn(false);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    DocumentReference defaultAccountActivation = new DocumentReference(getContext().getDatabase(),
        "Mails", "AccountActivationMail");
    expect(webUtilsMock.renderInheritableDocument(eq(defaultAccountActivation), eq("de"),
        (String) isNull())).andReturn(content);
    ((TestMessageTool) getContext().getMessageTool()).injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_ACOUNT_ACTIVATION_MAIL_SUBJECT_KEY, title);
    expect(webUtilsMock.getMessageTool(eq("de"))).andReturn(getContext().getMessageTool());
    expect(requestMock.getHeader(eq("host"))).andReturn("www.unit.test").anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @Test
  public void testSendValidationMessage_overwrittenSender_deprecated() throws Exception {
    String from = "sender@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    expect(webUtilsMock.resolveDocumentReference(eq(contentDoc))).andReturn(
        contentDocRef).anyTimes();
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    getContext().put("vcontext", new VelocityContext());
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(false);
    expect(modelAccessMock.exists(eq(contentCel2WebDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentCel2WebDocRef))).andReturn(doc);
    String adminLang = "en";
    expect(doc.getTranslatedDocument(eq(adminLang), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    BaseObject sendObj = new BaseObject();
    DocumentReference sendRef = new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass");
    sendObj.setStringValue("emailFrom", from);
    expect(doc.getXObject(eq(sendRef))).andReturn(sendObj);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDoc, getContext());
    verifyDefault();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToCelements2web_overwrittenSender()
      throws Exception {
    String adminLang = "en";
    String from = "sender@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    DocumentReference contentDocRef = new DocumentReference(getContext().getDatabase(), "Tools",
        "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web", "Tools",
        "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    getContext().put("vcontext", new VelocityContext());
    expect(modelAccessMock.exists(eq(contentDocRef))).andReturn(false);
    expect(modelAccessMock.exists(eq(contentCel2WebDocRef))).andReturn(true);
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(modelAccessMock.getDocument(eq(contentCel2WebDocRef))).andReturn(doc);
    expect(doc.getTranslatedDocument(eq("de"), same(getContext()))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(getContext()))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
    BaseObject sendObj = new BaseObject();
    DocumentReference sendRef = new DocumentReference(getContext().getDatabase(), "Celements2",
        "FormMailClass");
    sendObj.setStringValue("emailFrom", from);
    expect(doc.getXObject(eq(sendRef))).andReturn(sendObj);
    expect(rendererMock.interpretText(eq(title), same(doc), same(getContext()))).andReturn(title);
    celSendMailMock.setFrom(eq(from));
    expectLastCall();
    celSendMailMock.setTo(eq(to));
    expectLastCall();
    celSendMailMock.setHtmlContent(eq(content), eq(false));
    expectLastCall();
    celSendMailMock.setTextContent(eq(noHTML));
    expectLastCall();
    celSendMailMock.setSubject(eq(title));
    expectLastCall();
    celSendMailMock.setReplyTo((String) isNull());
    expectLastCall();
    celSendMailMock.setCc((String) isNull());
    expectLastCall();
    celSendMailMock.setBcc((String) isNull());
    expectLastCall();
    celSendMailMock.setAttachments((List<Attachment>) isNull());
    expectLastCall();
    celSendMailMock.setOthers((Map<String, String>) isNull());
    expectLastCall();
    expect(celSendMailMock.sendMail()).andReturn(1);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac=" + validkey;
    expect(getWikiMock().getExternalURL(eq("Content.login"), eq("view"), eq(
        "email=to%40mail.com&ac=" + validkey), same(getContext()))).andReturn(expectedLink).once();
    expect(webUtilsMock.getDefaultAdminLanguage()).andReturn(adminLang).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("Content.login"),
        same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    cmd.sendValidationMessage(to, validkey, contentDocRef, "de");
    verifyDefault();
  }

  @Test
  public void test_recoverPassword_UserInstantiationException() throws Exception {
    expect(getWikiMock().getDefaultLanguage(same(getContext()))).andReturn("en").anyTimes();
    DocumentReference userDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "testUser");
    expect(userServiceMock.getUser(userDocRef)).andThrow(new UserInstantiationException(""));
    String expectedMsg = "recovery failed";
    getMessageToolStub().injectMessage(
        PasswordRecoveryAndEmailValidationCommand.CEL_PASSWORD_RECOVERY_FAILED, expectedMsg);
    expect(webUtilsMock.getAdminMessageTool()).andReturn(getContext().getMessageTool()).anyTimes();
    replayDefault();
    assertEquals(expectedMsg, cmd.recoverPassword(userDocRef, "inputParam"));
    verifyDefault();
  }
}
