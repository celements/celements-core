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

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.xwiki.context.Execution;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.web.Utils;

/**
 * TODO make an API class but which is given back to velocity calls.
 *
 * @author edoardo
 */
@NotThreadSafe
public abstract class CSS extends Api {

  private static XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public CSS() {
    super(getContext());
  }

  @Deprecated
  public CSS(XWikiContext context) {
    super(context);
  }

  @Deprecated
  protected IWebUtils utils;

  protected AttachmentURLCommand attURLcmd;

  public String displayInclude(XWikiContext context) {
    String cssPath = getCSS(context);
    if (cssPath != null) {
      String media = getMedia();
      if (StringUtils.isBlank(media)) {
        media = "all";
      }
      return "<link rel=\"" + (isAlternate() ? "alternate " : "") + "stylesheet\" " + "title=\""
          + getTitle() + "\" media=\"" + media + "\" type=\"text/css\" " + "href=\"" + cssPath
          + "\" />\n";
    } else {
      return "<!-- WARNING: css file not found: " + getCssBasePath() + " -->\n";
    }
  }

  public String toString(XWikiContext context) {
    return getMedia() + " - " + getCSS(context);
  }

  public String getCSS() {
    return getCSS(context);
  }

  public abstract String getCSS(XWikiContext context);

  public abstract boolean isAlternate();

  public abstract String getTitle();

  public abstract String getMedia();

  public abstract boolean isContentCSS();

  public abstract boolean isAttachment();

  public abstract Attachment getAttachment();

  public abstract String getCssBasePath();

  protected String getURLFromString(String str, XWikiContext context) {
    return getAttachmentURLcmd().getAttachmentURL(str, context);
  }

  /**
   * for Tests only !!!
   **/
  @Deprecated
  void testInjectUtils(IWebUtils utils) {
    this.utils = utils;
  }

  /**
   * @deprecated instead use WebUtilsService directly
   */
  @Deprecated
  protected IWebUtils getWebUtils() {
    if (utils == null) {
      utils = WebUtils.getInstance();
    }
    return utils;
  }

  /**
   * for Tests only !!!
   **/
  void testInjectAttURLcmd(AttachmentURLCommand attURLcmd) {
    this.attURLcmd = attURLcmd;
  }

  protected AttachmentURLCommand getAttachmentURLcmd() {
    if (attURLcmd == null) {
      attURLcmd = new AttachmentURLCommand();
    }
    return attURLcmd;
  }

  protected IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  protected IAttachmentServiceRole getAttachmentService() {
    return Utils.getComponent(IAttachmentServiceRole.class);
  }

}
