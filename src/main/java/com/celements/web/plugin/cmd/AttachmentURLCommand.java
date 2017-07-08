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

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.web.service.LastStartupTimeStampRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class AttachmentURLCommand {

  private static Logger LOGGER = LoggerFactory.getLogger(AttachmentURLCommand.class);

  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentURL(link, getDefaultAction(), context);
  }

  protected String getDefaultAction() {
    return getContext().getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", getContext());
  }

  public String getAttachmentURLPrefix() {
    return getAttachmentURLPrefix(getDefaultAction());
  }

  public String getAttachmentURLPrefix(String action) {
    XWikiURLFactory urlf = getContext().getURLFactory();
    return urlf.createResourceURL("", true, getContext()).toString().replace("/skin/", "/" + action
        + "/");
  }

  public String getAttachmentURL(String link, String action, XWikiContext context) {
    String url = link;
    if (isAttachmentLink(link)) {
      String attName = getAttachmentName(link);
      try {
        XWikiDocument doc = context.getWiki().getDocument(getPageFullName(link), context);
        XWikiAttachment att = getAttachmentService().getAttachmentNameEqual(doc, attName);
        url = doc.getAttachmentURL(attName, action, context);
        url += "?version=" + getLastStartupTimeStamp().getLastChangedTimeStamp(att.getDate());
      } catch (XWikiException exp) {
        LOGGER.error("Error getting attachment URL for doc " + getPageFullName(link) + " and file "
            + attName, exp);
        url = link;
      } catch (AttachmentNotExistsException anee) {
        LOGGER.info("Attachment not found for link [{}] and action [{}]", link, action, anee);
        return null;
      }
    } else if (isOnDiskLink(link)) {
      String path = link.trim().substring(1);
      url = context.getWiki().getSkinFile(path, true, context).replace("/skin/", "/" + action
          + "/");
      url += "?version=" + getLastStartupTimeStamp().getFileModificationDate(path);
    }
    if (url.startsWith("?")) {
      url = context.getDoc().getURL("view", context) + url;
    }
    return url;
  }

  private LastStartupTimeStampRole getLastStartupTimeStamp() {
    return Utils.getComponent(LastStartupTimeStampRole.class);
  }

  public String getAttachmentName(String link) {
    return link.split(";")[1];
  }

  public String getPageFullName(String link) {
    return link.split(";")[0];
  }

  public boolean isAttachmentLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
      isAttachmentLink = link.matches(regex);
    }
    return isAttachmentLink;
  }

  public boolean isOnDiskLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "^:[/\\w\\-\\.]*";
      isAttachmentLink = link.trim().matches(regex);
    }
    return isAttachmentLink;
  }

  public String getExternalAttachmentURL(String fileName, String action, XWikiContext context) {
    try {
      return context.getURLFactory().getServerURL(context).toExternalForm() + getAttachmentURL(
          fileName, action, context);
    } catch (MalformedURLException exp) {
      LOGGER.error("Failed to getServerURL.", exp);
    }
    return "";
  }

  private IAttachmentServiceRole getAttachmentService() {
    return Utils.getComponent(IAttachmentServiceRole.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

}
