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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.captcha.CaptchaVerifier;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class CaptchaCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CaptchaCommand.class);

  public boolean checkCaptcha(XWikiContext context) {
    String answer = context.getRequest().get("captcha_answer");
    if((answer != null) && (answer.length() > 0)) {
      CaptchaVerifier cv = Utils.getComponent(CaptchaVerifier.class, context.getRequest(
          ).get("captcha_type"));
        try {
          mLogger.info("Checking answer for user id '" 
              + cv.getUserId(context.getRequest()) + "'");
          return cv.isAnswerCorrect(context.getRequest().get("captcha_id"), answer);
//        return cv.isAnswerCorrect(cv.getUserId(context.getRequest()), answer);
        } catch (Exception e) {
          mLogger.error("Exception while attempting to verify captcha", e);
        }
    }
    return false;
  }

  public String getCaptchaId(XWikiContext context) {
    CaptchaVerifier cv = Utils.getComponent(CaptchaVerifier.class, "image");
  try {
    mLogger.info("Captcha user id is '" + cv.getUserId(context.getRequest()) + "'");
    return cv.getUserId(context.getRequest());
  } catch (Exception e) {
    mLogger.error("Exception while attempting to verify captcha", e);
  }
    return "";
  }
}