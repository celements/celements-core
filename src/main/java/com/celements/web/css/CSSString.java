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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@NotThreadSafe
public class CSSString extends CSS {

  private static final Logger LOGGER = LoggerFactory.getLogger(CSSString.class);

  private String file;
  private boolean alternate;
  private String title;
  /*
   * existing media: all, aural, braille, embossed, handheld, print, projection, screen,
   * tty, tv
   */
  private String media;
  private boolean isContentCSS;

  public CSSString(String file, XWikiContext context) {
    super(context);
    boolean contentCSS = (file.endsWith("-content.css") || file.endsWith("_content.css"));
    initFields(file, false, "", "all", contentCSS);
  }

  public CSSString(String file, String media, XWikiContext context) {
    super(context);
    initFields(file, false, "", media, false);
  }

  public CSSString(String file, boolean alternate, String title, String media, boolean isContentCSS,
      XWikiContext context) {
    super(context);
    initFields(file, alternate, title, media, isContentCSS);
  }

  private void initFields(String file, boolean alternate, String title, String media,
      boolean isContentCSS) {
    this.file = file;
    this.alternate = alternate;
    this.title = title;
    this.media = media;
    this.isContentCSS = isContentCSS;
  }

  @Override
  public String getCSS(XWikiContext context) {
    return getURLFromString(getCssBasePath(), context);
  }

  @Override
  public boolean isAlternate() {
    return alternate;
  }

  @Override
  public String getTitle() {
    return (title != null) ? title : "";
  }

  @Override
  public String getMedia() {
    return (media != null) ? media : "";
  }

  @Override
  public boolean isContentCSS() {
    return isContentCSS;
  }

  @Override
  public Attachment getAttachment() {
    if (isAttachment()) {
      String pageFN = getAttachmentURLcmd().getPageFullName(file);
      try {
        XWikiDocument attDoc = context.getWiki().getDocument(
            getWebUtilsService().resolveDocumentReference(pageFN), context);
        XWikiAttachment att = getAttachmentService().getAttachmentNameEqual(attDoc,
            getAttachmentURLcmd().getAttachmentName(file));
        return getAttachmentService().getApiAttachment(att);
      } catch (XWikiException xwe) {
        LOGGER.error("Exception getting attachment document.", xwe);
      } catch (AttachmentNotExistsException anee) {
        LOGGER.warn("Couldn't find attachment [{}] on doc [{}]", file, pageFN, anee);
      } catch (NoAccessRightsException e) {
        LOGGER.error("No rights to view attachment [{}] on doc [{}]", file, pageFN, e);
      }
    }
    return null;
  }

  @Override
  public boolean isAttachment() {
    return getAttachmentURLcmd().isAttachmentLink(file);
  }

  @Override
  public String getCssBasePath() {
    return (file != null) ? file : "";
  }

}
