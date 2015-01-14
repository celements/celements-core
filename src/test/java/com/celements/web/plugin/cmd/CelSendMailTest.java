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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.plugin.mailsender.Mail;
import com.xpn.xwiki.plugin.mailsender.MailSenderPluginApi;

public class CelSendMailTest extends AbstractBridgedComponentTestCase {
  XWikiContext context;
  CelSendMail sendMail;
  
  @Before
  public void setUp_CelSendMailTest() throws Exception {
    context = getContext();
    sendMail = new CelSendMail();
  }

  @Test
  public void testSetFrom() {
    String from = "test@synventis.com";
    sendMail.setFrom(from);
    assertEquals(from, sendMail.getMailObject().getFrom());
  }

  @Test
  public void testSetReplyTo() {
    String replyTo = "test@synventis.com";
    sendMail.setReplyTo(replyTo);
    assertEquals(replyTo, sendMail.getMailObject().getHeaders().get("reply-to"));
  }

  @Test
  public void testSetReplyTo_null() {
    sendMail.setReplyTo(null);
    assertNull(sendMail.getMailObject().getHeaders().get("reply-to"));
  }

  @Test
  public void testSetReplyTo_empty() {
    sendMail.setReplyTo(" \n ");
    assertNull(sendMail.getMailObject().getHeaders().get("reply-to"));
  }

  @Test
  public void testSetTo() {
    String to = "test@synventis.com";
    sendMail.setTo(to);
    assertEquals(to, sendMail.getMailObject().getTo());
  }

  @Test
  public void testSetCc() {
    String cc = "test@synventis.com";
    sendMail.setCc(cc);
    assertEquals(cc, sendMail.getMailObject().getCc());
  }

  @Test
  public void testSetBcc() {
    String bcc = "test@synventis.com";
    sendMail.setBcc(bcc);
    assertEquals(bcc, sendMail.getMailObject().getBcc());
  }

  @Test
  public void testSetSubject() {
    String subject = "It's the Subject!";
    sendMail.setSubject(subject);
    assertEquals(subject, sendMail.getMailObject().getSubject());
  }

  @Test
  @Deprecated
  public void testSetHtmlContent_deprecated() {
    CelSendMail sendMail = new CelSendMail(context);
    String html = "<h2>HTML Content!</h2>";
    String text = "It's plain.\r\n\r\nHTML Content!";
    TestMessageTool msgTool = (TestMessageTool) context.getMessageTool();
    msgTool.injectMessage("cel_plain_text_mail", "It's plain.");
    replayDefault();
    sendMail.setHtmlContent(html, false);
    verifyDefault();
    assertEquals(html, sendMail.getMailObject().getHtmlPart());
    assertEquals(text, sendMail.getMailObject().getTextPart());
  }

  @Test
  public void testSetHtmlContent() {
    CelSendMail sendMail = new CelSendMail();
    String html = "<h2>HTML Content!</h2>";
    String text = "It's plain.\r\n\r\nHTML Content!";
    TestMessageTool msgTool = (TestMessageTool) context.getMessageTool();
    msgTool.injectMessage("cel_plain_text_mail", "It's plain.");
    replayDefault();
    sendMail.setHtmlContent(html, false);
    verifyDefault();
    assertEquals(html, sendMail.getMailObject().getHtmlPart());
    assertEquals(text, sendMail.getMailObject().getTextPart());
  }

  @Test
  public void testSetTextContent() {
    String text = "Plain Text Content.";
    sendMail.setTextContent(text);
    assertEquals(text, sendMail.getMailObject().getTextPart());
  }

  @Test
  public void testSetAttachments() {
    Attachment att1 = createMock(Attachment.class);
    Attachment att2 = createMock(Attachment.class);
    Attachment att3 = createMock(Attachment.class);
    List<Attachment> list = new ArrayList<Attachment>();
    list.add(att1);
    list.add(att2);
    list.add(att3);
    sendMail.setAttachments(list);
    assertEquals(list, sendMail.getMailObject().getAttachments());
  }

  @Test
  public void testSetOthers() {
    String replyTo = "test@synventis.com";
    String addInfo = "something else";
    Map<String, String> map = new HashMap<String, String>();
    map.put("reply-to", replyTo);
    map.put("added-info", addInfo);
    sendMail.setOthers(map);
    assertEquals(replyTo, sendMail.getMailObject().getHeaders().get("reply-to"));
    assertEquals(addInfo, sendMail.getMailObject().getHeaders().get("added-info"));
  }

  @Test
  public void testSendMail_mailNull() {
    assertEquals(-999, sendMail.sendMail());
  }

  @Test
  public void testSendMail() {
    XWiki xwiki = getWikiMock();
    MailSenderPluginApi mailPlugin = createMockAndAddToDefault(MailSenderPluginApi.class);
    expect(xwiki.getPluginApi(eq("mailsender"), same(context))).andReturn(mailPlugin);
    expect(mailPlugin.sendMail(isA(Mail.class), isA(CelMailConfiguration.class))
        ).andReturn(1);
    sendMail.injectMail(new Mail());
    expect(xwiki.getXWikiPreference(eq("smtp_server"),
        eq("celements.mail.default.smtp_server"), eq(""), same(context))).andReturn(""
            ).anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("smtp_port"),
        eq("celements.mail.default.smtp_port"), eq(25), same(context))).andReturn(25
            ).anyTimes();
    expect(xwiki.getXWikiPreference(eq("admin_email"),
        eq("celements.mail.default.admin_email"), eq(""), same(context))).andReturn(""
            ).anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_username"),
        eq("celements.mail.default.smtp_server_username"), eq(""), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("smtp_server_password"),
        eq("celements.mail.default.smtp_server_password"), eq(""), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("javamail_extra_props"),
        eq("celements.mail.default.javamail_extra_props"), eq(""), same(context))
        ).andReturn("").anyTimes();
    replayDefault();
    assertEquals(1, sendMail.sendMail());
    verifyDefault();
  }
  
  @Test
  public void testGetMailObject_notNull() {
    Mail mail = sendMail.getMailObject();
    assertNotNull(mail);
  }
  
  @Test
  public void testGetMailObject_injected() {
    Mail injectMail = createMock(Mail.class);
    sendMail.injectMail(injectMail);
    Mail mail = sendMail.getMailObject();
    assertSame(injectMail, mail);
  }

}
