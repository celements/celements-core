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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;

public class PasswordRecoveryAndEmailValidationCommandTest 
    extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private PasswordRecoveryAndEmailValidationCommand passwdRecValidCmd;

  @Before
  public void setUp_PasswordRecoveryAndEmailValidationCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    passwdRecValidCmd = new PasswordRecoveryAndEmailValidationCommand();
  }

  @Test
  public void testSetValidationInfoInContext() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    String to = "to@mail.com";
    String validkey = "validkey123";
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac="
      + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"),
        eq("email=to%40mail.com&ac=" + validkey), same(context))
        ).andReturn(expectedLink).once();
    replay(xwiki);
    passwdRecValidCmd.setValidationInfoInContext(to, validkey);
    assertNotNull(context.get("vcontext"));
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
    assertEquals(expectedLink, vcontext.get("activationLink"));
    verify(xwiki);
  }
  
  @Test
  public void testGetNewValidationTokenForUser_XWikiGuest() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    DocumentReference guestUserRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGuest");
    expect(xwiki.exists(eq(guestUserRef), same(context))).andReturn(false).once();
    replay(xwiki);
    assertNull(passwdRecValidCmd.getNewValidationTokenForUser("XWiki.XWikiGuest", context));
    verify(xwiki);
  }
    
  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage() throws XWikiException {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(),
        "Tools", "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    expect(xwiki.getXWikiPreference(eq("admin_email"), same(context)))
        .andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(true);
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(contentDocRef), same(context))).andReturn(doc);
    expect(doc.getTranslatedDocument(same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
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
    celSendMail.setReplyTo((String)isNull());
    expectLastCall();
    celSendMail.setCc((String)isNull());
    expectLastCall();
    celSendMail.setBcc((String)isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>)isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>)isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac="
      + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"),
        eq("email=to%40mail.com&ac=" + validkey), same(context))
        ).andReturn(expectedLink).once();
    replay(doc, celSendMail, renderer, xwiki);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDoc, context);
    verify(doc, celSendMail, renderer, xwiki);
    assertEquals(to, vcontext.get("email"));
    assertEquals(validkey, vcontext.get("validkey"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendValidationMessage_fallbackToCelements2web() throws XWikiException {
    String from = "from@mail.com";
    String to = "to@mail.com";
    String validkey = "validkey123";
    String contentDoc = "Tools.ActivationMail";
    DocumentReference contentDocRef = new DocumentReference(context.getDatabase(),
        "Tools", "ActivationMail");
    DocumentReference contentCel2WebDocRef = new DocumentReference("celements2web",
        "Tools", "ActivationMail");
    String content = "This is the mail content.";
    String noHTML = "";
    String title = "the title";
    context.put("vcontext", new VelocityContext());
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    expect(xwiki.getXWikiPreference(eq("admin_email"), same(context)))
        .andReturn(from);
    expect(xwiki.exists(eq(contentDocRef), same(context))).andReturn(false);
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(contentCel2WebDocRef), same(context))).andReturn(doc);
    expect(doc.getTranslatedDocument(same(context))).andReturn(doc).anyTimes();
    expect(doc.getRenderedContent(same(context))).andReturn(content);
    expect(doc.getTitle()).andReturn(title);
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
    celSendMail.setReplyTo((String)isNull());
    expectLastCall();
    celSendMail.setCc((String)isNull());
    expectLastCall();
    celSendMail.setBcc((String)isNull());
    expectLastCall();
    celSendMail.setAttachments((List<Attachment>)isNull());
    expectLastCall();
    celSendMail.setOthers((Map<String, String>)isNull());
    expectLastCall();
    expect(celSendMail.sendMail()).andReturn(1);
    passwdRecValidCmd.injectCelSendMail(celSendMail);
    String expectedLink = "http://myserver.ch/login?email=to%40mail.com&ac="
      + validkey;
    expect(xwiki.getExternalURL(eq("Content.login"), eq("view"),
        eq("email=to%40mail.com&ac=" + validkey), same(context))
        ).andReturn(expectedLink).once();
    replay(doc, celSendMail, renderer, xwiki);
    passwdRecValidCmd.sendValidationMessage(to, validkey, contentDoc, context);
    verify(doc, celSendMail, renderer, xwiki);
  }

}
