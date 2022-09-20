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
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@NotThreadSafe
public class CSSBaseObject extends CSS {

  private static final Logger LOGGER = LoggerFactory.getLogger(CSSBaseObject.class);

  private BaseObject obj;

  private AttachmentURLCommand attURLcmd = new AttachmentURLCommand();

  @Deprecated
  public CSSBaseObject(BaseObject obj, XWikiContext context) {
    super(context);
    this.obj = obj;
  }

  public CSSBaseObject(BaseObject obj) {
    super();
    this.obj = obj;
  }

  @Override
  public String getCSS(XWikiContext context) {
    return getURLFromString(getCssBasePath(), context);
  }

  @Override
  public boolean isAlternate() {
    if (obj != null) {
      return obj.getIntValue("alternate", -1) == 1;
    } else {
      return false;
    }
  }

  @Override
  public String getTitle() {
    if (obj != null) {
      return obj.getStringValue("title");
    } else {
      return "";
    }
  }

  @Override
  public String getMedia() {
    if (obj != null) {
      return obj.getStringValue("media");
    } else {
      return "";
    }
  }

  @Override
  public boolean isContentCSS() {
    if ((obj != null) && ((obj.getIntValue("is_rte_content") == 1) || obj.getStringValue(
        "cssname").endsWith("-content.css") || obj.getStringValue("cssname").endsWith(
            "_content.css"))) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Attachment getAttachment() {
    if (isAttachment()) {
      String cssName = getCssBasePath();
      DocumentReference addDocRef = getWebUtilsService().resolveDocumentReference(
          attURLcmd.getPageFullName(cssName));
      LOGGER.debug("getAttachment for [" + cssName + "].");
      try {
        XWikiDocument attDoc = context.getWiki().getDocument(addDocRef, context);
        XWikiAttachment att = getAttachmentService().getAttachmentNameEqual(attDoc,
            attURLcmd.getAttachmentName(obj.getStringValue("cssname")));
        return getAttachmentService().getApiAttachment(att);
      } catch (XWikiException xwe) {
        LOGGER.error("Exception getting attachment document.", xwe);
      } catch (AttachmentNotExistsException anee) {
        LOGGER.warn("Couldn't find CSS [{}] on doc [{}]", obj.getStringValue("cssname"), addDocRef,
            anee);
      } catch (NoAccessRightsException e) {
        LOGGER.error("No rights to view CSS [{}] on doc [{}]", obj.getStringValue("cssname"),
            addDocRef, e);
      }
    }
    return null;
  }

  @Override
  public boolean isAttachment() {
    return attURLcmd.isAttachmentLink(obj.getStringValue("cssname"));
  }

  BaseObject getObject() {
    return obj;
  }

  @Override
  public String getCssBasePath() {
    String str = "";
    if (obj != null) {
      str = obj.getStringValue("cssname");
      if ((str != null) && !"".equals(str) && !str.contains(":") && !str.startsWith("/")) {
        str = obj.getDocumentReference().getWikiReference().getName() + ":" + str;
      }
    }
    return str;
  }

}
