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

import com.celements.captcha.ICaptchaServiceRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated instead use ICaptchaServiceRole
 */
@Deprecated
public class CaptchaCommand {

  /**
   * @deprecated instead use ICaptchaServiceRole.checkCaptcha
   */
  @Deprecated
  public boolean checkCaptcha(XWikiContext context) {
    return Utils.getComponent(ICaptchaServiceRole.class).checkCaptcha();
  }

  /**
   * @deprecated instead use ICaptchaServiceRole.getCaptchaId
   */
  @Deprecated
  public String getCaptchaId(XWikiContext context) {
    return Utils.getComponent(ICaptchaServiceRole.class).getCaptchaId();
  }
}
