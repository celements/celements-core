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
package com.celements.web.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.captcha.CaptchaVerifier;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLifecycleException;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class CaptchaServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private CaptchaService captchaService;
  private XWikiRequest requestMock;
  private CaptchaVerifier origImgCaptchaVerifier;
  private ComponentDescriptor<CaptchaVerifier> captchaVerifyerDesc;
  private HttpSession sessionMock;
  private String sessionId;
  private CaptchaVerifier imgCaptchaVerifierMock;

  @Before
  public void setUp_CelementsWebScriptServiceTest() throws Exception {
    context = getContext();
    requestMock = createMockAndAddToDefault(XWikiRequest.class);
    context.setRequest(requestMock);
    sessionId = "AD87ND789S1L08";
    sessionMock = createMockAndAddToDefault(HttpSession.class);
    expect(requestMock.getSession()).andReturn(sessionMock).anyTimes();
    expect(sessionMock.getId()).andReturn(sessionId).anyTimes();
    captchaService = (CaptchaService) Utils.getComponent(ICaptchaServiceRole.class);
    setupImageCaptchaVerifierMock();
  }

  public void setupImageCaptchaVerifierMock() throws Exception,
      ComponentLifecycleException {
    origImgCaptchaVerifier = Utils.getComponent(CaptchaVerifier.class, "image");
    captchaVerifyerDesc = getComponentManager().getComponentDescriptor(
        CaptchaVerifier.class, "image");
    getComponentManager().release(origImgCaptchaVerifier);
    imgCaptchaVerifierMock = createMockAndAddToDefault(CaptchaVerifier.class);
    getComponentManager().registerComponent(captchaVerifyerDesc, imgCaptchaVerifierMock);
    expect(imgCaptchaVerifierMock.getUserId(requestMock)).andReturn(sessionId).anyTimes();
  }

  @After
  public void tearDown_CelementsWebScriptService() throws Exception {
    getComponentManager().unregisterComponent(CaptchaVerifier.class, "image");
    getComponentManager().registerComponent(captchaVerifyerDesc, origImgCaptchaVerifier);
  }

  @Test
  public void testCheckCaptcha_singleCall_true() throws Exception {
    String expectedAnwser = "chetuck";
    expect(requestMock.get(eq("captcha_answer"))).andReturn(expectedAnwser).atLeastOnce();
    expect(requestMock.get(eq("captcha_type"))).andReturn("image").atLeastOnce();
    expect(requestMock.get(eq("captcha_id"))).andReturn(sessionId).atLeastOnce();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(expectedAnwser))
        ).andReturn(true).once();
    replayDefault();
    assertTrue(captchaService.checkCaptcha());
    verifyDefault();
  }

  @Test
  public void testCheckCaptcha_singleCall_false() throws Exception {
    String wrongAnwser = "chetuck";
    expect(requestMock.get(eq("captcha_answer"))).andReturn(wrongAnwser).atLeastOnce();
    expect(requestMock.get(eq("captcha_type"))).andReturn("image").atLeastOnce();
    expect(requestMock.get(eq("captcha_id"))).andReturn(sessionId).atLeastOnce();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(wrongAnwser))
        ).andReturn(false).once();
    replayDefault();
    assertFalse(captchaService.checkCaptcha());
    verifyDefault();
  }

  @Test
  public void testCheckCaptcha_doubleCall_true() throws Exception {
    String expectedAnwser = "chetuck";
    expect(requestMock.get(eq("captcha_answer"))).andReturn(expectedAnwser).atLeastOnce();
    expect(requestMock.get(eq("captcha_type"))).andReturn("image").atLeastOnce();
    expect(requestMock.get(eq("captcha_id"))).andReturn(sessionId).atLeastOnce();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(expectedAnwser))
        ).andReturn(true).once();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(expectedAnwser))
        ).andReturn(false).anyTimes();
    replayDefault();
    assertTrue(captchaService.checkCaptcha());
    // check answer caching
    assertTrue(captchaService.checkCaptcha());
    verifyDefault();
  }

  @Test
  public void testCheckCaptcha_doubleCall_false() throws Exception {
    String expectedAnwser = "chetuck";
    expect(requestMock.get(eq("captcha_answer"))).andReturn(expectedAnwser).atLeastOnce();
    expect(requestMock.get(eq("captcha_type"))).andReturn("image").atLeastOnce();
    expect(requestMock.get(eq("captcha_id"))).andReturn(sessionId).atLeastOnce();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(expectedAnwser))
        ).andReturn(false).once();
    expect(imgCaptchaVerifierMock.isAnswerCorrect(eq(sessionId), eq(expectedAnwser))
        ).andReturn(false).anyTimes();
    replayDefault();
    assertFalse(captchaService.checkCaptcha());
    // check answer caching
    assertFalse(captchaService.checkCaptcha());
    verifyDefault();
  }

}
