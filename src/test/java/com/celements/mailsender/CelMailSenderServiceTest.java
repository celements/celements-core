package com.celements.mailsender;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.plugin.cmd.IMailObjectRole;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.web.Utils;

public class CelMailSenderServiceTest extends AbstractBridgedComponentTestCase {

  private CelMailSenderService mailSender;

  @Before
  public void CelMailSenderServiceTest_setUp() throws Exception {
    mailSender = (CelMailSenderService) Utils.getComponent(
        IMailSenderRole.class);
  }

  @Test
  public void testGetNewMailObject_perLookup() {
    assertNotNull(mailSender.getNewMailObject());
    assertNotSame(mailSender.getNewMailObject(), mailSender.getNewMailObject());
  }

  @Test
  public void testSendMail() {
    IMailObjectRole mockMailObject = createMockAndAddToDefault(IMailObjectRole.class);
    mailSender.internal_injectedMailObject(mockMailObject);
    String from = "testfrom";
    String replyTo = "testreplyTo";
    String to = "testto";
    String cc = "testcc";
    String bcc = "testbcc";
    String subject = "testsubject";
    String htmlContent = "testHtmlContent";
    String textContent = "testTextContent";
    Attachment mockAttachment = createMockAndAddToDefault(Attachment.class);
    List<Attachment> attachments = Arrays.asList(mockAttachment);
    Map<String, String> others = new HashMap<String, String>();
    mockMailObject.setFrom(eq(from));
    expectLastCall().once();
    mockMailObject.setReplyTo(eq(replyTo));
    expectLastCall().once();
    mockMailObject.setTo(eq(to));
    expectLastCall().once();
    mockMailObject.setCc(eq(cc));
    expectLastCall().once();
    mockMailObject.setBcc(eq(bcc));
    expectLastCall().once();
    mockMailObject.setSubject(eq(subject));
    expectLastCall().once();
    mockMailObject.setHtmlContent(eq(htmlContent), eq(false));
    expectLastCall().once();
    mockMailObject.setTextContent(eq(textContent));
    expectLastCall().once();
    mockMailObject.setAttachments(same(attachments));
    expectLastCall().once();
    mockMailObject.setOthers(same(others));
    expectLastCall().once();
    expect(mockMailObject.sendMail()).andReturn(1);
    replayDefault();
    assertEquals(1, mailSender.sendMail(from, replyTo, to, cc, bcc, subject, htmlContent,
        textContent, attachments, others));
    verifyDefault();
  }

  @Test
  public void testSendMail_latin1() {
    IMailObjectRole mockMailObject = createMockAndAddToDefault(IMailObjectRole.class);
    mailSender.internal_injectedMailObject(mockMailObject);
    String from = "testfrom";
    String replyTo = "testreplyTo";
    String to = "testto";
    String cc = "testcc";
    String bcc = "testbcc";
    String subject = "testsubject";
    String htmlContent = "testHtmlContent";
    String textContent = "testTextContent";
    Attachment mockAttachment = createMockAndAddToDefault(Attachment.class);
    List<Attachment> attachments = Arrays.asList(mockAttachment);
    Map<String, String> others = new HashMap<String, String>();
    mockMailObject.setFrom(eq(from));
    expectLastCall().once();
    mockMailObject.setReplyTo(eq(replyTo));
    expectLastCall().once();
    mockMailObject.setTo(eq(to));
    expectLastCall().once();
    mockMailObject.setCc(eq(cc));
    expectLastCall().once();
    mockMailObject.setBcc(eq(bcc));
    expectLastCall().once();
    mockMailObject.setSubject(eq(subject));
    expectLastCall().once();
    mockMailObject.setHtmlContent(eq(htmlContent), eq(true));
    expectLastCall().once();
    mockMailObject.setTextContent(eq(textContent));
    expectLastCall().once();
    mockMailObject.setAttachments(same(attachments));
    expectLastCall().once();
    mockMailObject.setOthers(same(others));
    expectLastCall().once();
    expect(mockMailObject.sendMail()).andReturn(1);
    replayDefault();
    assertEquals(1, mailSender.sendMail(from, replyTo, to, cc, bcc, subject, htmlContent,
        textContent, attachments, others, true));
    verifyDefault();
  }

}
