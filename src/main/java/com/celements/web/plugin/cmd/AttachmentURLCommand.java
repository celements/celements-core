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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class AttachmentURLCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      AttachmentURLCommand.class);

  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentURL(link, getDefaultAction(context), context);
  }

  protected String getDefaultAction(XWikiContext context) {
    return context.getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", context);
  }

  public String getAttachmentURL(String link, String action, XWikiContext context) {
    String url = link;
    if(isAttachmentLink(link)) {
      String attName = getAttachmentName(link);
      try {
        XWikiDocument doc = context.getWiki().getDocument(getPageFullName(link), context);
        if (doc.getAttachment(attName) == null) {
          return null;
        }
        url = doc.getAttachmentURL(attName, action, context);
        url += "?version=" + new LastStartupTimeStamp().getLastChangedTimeStamp(
            doc.getAttachment(attName).getDate());
      } catch (XWikiException exp) {
        mLogger.error("Error getting attachment URL for doc " + getPageFullName(link)
            + " and file " + attName, exp);
        url = link;
      }
    } else if(isOnDiskLink(link)) {
      String path = link.trim().substring(1);
      url = context.getWiki().getSkinFile(path, true, context).replace("/skin/", "/"
          + action + "/");
      url += "?version=" + new LastStartupTimeStamp().getFileModificationDate(path,
          context);
    }
    if (url.startsWith("?")) {
      url = context.getDoc().getURL("view", context) + url;
    }
    return url;
  }

  public String getAttachmentName(String link) {
    return link.split(";")[1];
  }

  public String getPageFullName(String link) {
    return link.split(";")[0];
  }

  public boolean isAttachmentLink(String link) {
    boolean isAttachmentLink = false;
    if(link != null) {
      String regex = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
      isAttachmentLink = link.matches(regex);
    }
    return isAttachmentLink;
  }

  public boolean isOnDiskLink(String link) {
    boolean isAttachmentLink = false;
    if(link != null) {
      String regex = "^:[/\\w\\-\\.]*";
      isAttachmentLink = link.trim().matches(regex);
    }
    return isAttachmentLink;
  }

}
