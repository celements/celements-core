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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

public class CSSString extends CSS {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(CSSString.class);

  private String file;
  /* existing media: all, aural, braille, embossed, handheld, print, projection, screen, tty, tv */
  private String media;
  private boolean isContentCSS;

  public CSSString(String file, XWikiContext context){
    super(context);
    boolean contentCSS = (file.endsWith("-content.css")
        || file.endsWith("_content.css"));
    initFields(file, "all", contentCSS);
  }
  
  public CSSString(String file, String media, XWikiContext context){
    super(context);
    initFields(file, media, false);
  }
  
  public CSSString(String file, String media, boolean isContentCSS, XWikiContext context){
    super(context);
    initFields(file, media, isContentCSS);
  }
  
  private void initFields(String file, String media, boolean isContentCSS){
    this.file = file;
    this.media = media;
    this.isContentCSS = isContentCSS;
  }
  
  public String getCSS(XWikiContext context){
    return getURLFromString(getCssBasePath(), context);
  }
  
  public String getMedia(){
    return (media != null)?media:"";
  }
  
  public boolean isContentCSS(){
    return isContentCSS;
  }

  @Override
  public Attachment getAttachment() {
    if (isAttachment()) {
      try {
        XWikiDocument attDoc = context.getWiki().getDocument(getWebUtils(
            ).getPageFullName(file), context);
        XWikiAttachment att = attDoc.getAttachment(getWebUtils().getAttachmentName(file));
        return new Attachment(new Document(attDoc, context), att, context);
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    }
    return null;
  }

  @Override
  public boolean isAttachment() {
    return getWebUtils().isAttachmentLink(file);
  }

  @Override
  public String getCssBasePath() {
    return (file != null)?file:"";
  }
}
