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
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DocHeaderTitleCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(DocHeaderTitleCommand.class);

  MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();

  /**
   * deprecated since 2.41.0 use getDocHeaderTitle(DocumentReference docRef) instead
   */
  @Deprecated
  public String getDocHeaderTitle(String fullName, XWikiContext context) {
    DocumentReference docRef = getWebUtils().resolveDocumentReference(fullName);
    return getDocHeaderTitle(docRef);
  }

  public String getDocHeaderTitle(DocumentReference docRef) {
    String docHeaderTitle = "";
    try {
      XWikiDocument theDoc = getContext().getWiki().getDocument(docRef, getContext());
      XWikiDocument theTDoc = theDoc.getTranslatedDocument(getContext());
      BaseObject docTitelObj = theDoc.getXObject(getWebUtils().resolveDocumentReference(
          "Content.Title"));
      if ((theTDoc.getTitle() != null) && !"".equals(theTDoc.getTitle())) {
        docHeaderTitle = theTDoc.getTitle();
      } else if ((theDoc.getTitle() != null) && !"".equals(theDoc.getTitle())) {
        docHeaderTitle = theDoc.getTitle();
      } else if ((docTitelObj != null) && (docTitelObj.getStringValue("title") != null)
          && (!"".equals(docTitelObj.getStringValue("title")))) {
        docHeaderTitle = getContext().getWiki().getRenderingEngine().renderText(
            docTitelObj.getStringValue("title"), theDoc, getContext());
      } else {
        docHeaderTitle = menuNameCmd.getMultilingualMenuNameOnly(
            docRef.getLastSpaceReference().getName() + "." + docRef.getName(),
            getContext().getLanguage(), false, getContext());
      }
      if (!"".equals(getContext().getWiki().getSpacePreference("title",
          docRef.getLastSpaceReference().getName(), "", getContext()))) {
        docHeaderTitle = docHeaderTitle + getContext().getWiki().parseContent(
            getContext().getWiki().getSpacePreference("title",
                docRef.getLastSpaceReference().getName(), "", getContext()), getContext());
      }
    } catch (Exception exp) {
      mLogger.error(exp);
    }
    return docHeaderTitle;
  }

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }
}
