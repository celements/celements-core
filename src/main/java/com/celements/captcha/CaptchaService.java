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
package com.celements.captcha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.captcha.CaptchaVerifier;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

/**
 * 
 * @author fabian
 * since 2.51.0
 *
 */
@Component
public class CaptchaService implements ICaptchaServiceRole {

  private static Log _LOGGER = LogFactory.getFactory().getInstance(CaptchaService.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  @Override 
  public boolean checkCaptcha() {
    String answer = getContext().getRequest().get("captcha_answer");
    if((answer != null) && (answer.length() > 0)) {
        try {
          _LOGGER.info("Checking answer for user id '" 
              + getCaptchaVerifier().getUserId(getContext().getRequest()) + "'");
          String anwserCacheKey = "captcha_" + getCaptchaType() + "_anwserCache";
          if (!getContext().containsKey(anwserCacheKey)) {
            Boolean isAnswerCorrect = getCaptchaVerifier().isAnswerCorrect(getContext(
                ).getRequest().get("captcha_id"), answer);
            getContext().put(anwserCacheKey, isAnswerCorrect);
          }
          return (Boolean) getContext().get(anwserCacheKey);
        } catch (Exception e) {
          _LOGGER.error("Exception while attempting to verify captcha", e);
        }
    }
    return false;
  }

  private CaptchaVerifier getCaptchaVerifier() {
    return Utils.getComponent(CaptchaVerifier.class, getCaptchaType());
  }

  public String getCaptchaType() {
    return getContext().getRequest().get("captcha_type");
  }

  @Override 
  public String getCaptchaId() {
    return getCaptchaId("image");
  }

  @Override 
  public String getCaptchaId(String captchaType) {
    CaptchaVerifier cv = Utils.getComponent(CaptchaVerifier.class, captchaType);
    try {
      String userId = cv.getUserId(getContext().getRequest());
      _LOGGER.info("Captcha user id is [" + userId + "]");
      return userId;
    } catch (Exception e) {
      _LOGGER.error("Exception while attempting to verify captcha", e);
    }
    return "";
  }

}