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
package com.celements.web.css;

import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.web.Utils;

/**
 * TODO make an API class but which is given back to velocity calls.
 * @author edoardo
 *
 */
public abstract class CSS extends Api {

  public CSS(XWikiContext context) {
    super(context);
  }

  protected IWebUtils utils;

  public String displayInclude(XWikiContext context){
    String cssPath = getCSS(context);
    if (cssPath != null) {
      return "<link rel=\"stylesheet\" media=\"" + getMedia() + "\" type=\"text/css\" href=\"" + cssPath + "\" />\n";
    } else {
      return "<!-- WARNING: css file not found: " + getCssBasePath() + " -->\n";
    }
  }
  
  public String toString(XWikiContext context){
    return getMedia() + " - " + getCSS(context);
  }

  public String getCSS() {
    return getCSS(context);
  }

  public abstract String getCSS(XWikiContext context);

  public abstract String getMedia();

  public abstract boolean isContentCSS();

  public abstract boolean isAttachment();

  public abstract Attachment getAttachment();

  public abstract String getCssBasePath();

  protected String getURLFromString(String str, XWikiContext context) {
    return getWebUtils().getAttachmentURL(str, context);
  }
  
  /**
   *  for Tests only !!!
   **/
  void testInjectUtils(IWebUtils utils) {
    this.utils = utils;
  }

  protected IWebUtils getWebUtils() {
    if (utils == null) {
      utils = WebUtils.getInstance();
    }
    return utils;
  }

  protected IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}