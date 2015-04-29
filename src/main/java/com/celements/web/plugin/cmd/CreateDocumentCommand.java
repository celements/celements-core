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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageTypeClasses;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CreateDocumentCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CreateDocumentCommand.class);

  IWebUtils webUtils = WebUtils.getInstance();

  IWebUtilsService injected_webService;

  /**
   * createDocument creates a new document if it does not exist
   * @param docRef
   * @param pageType
   * @return
   */
  public XWikiDocument createDocument(DocumentReference docRef, String pageType) {
    try {
      return createDocument(docRef, pageType, true);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document [" + docRef + "].", exp);
      return null;
    }
  }

  /**
   * createDocument creates a new document if it does not exist
   * @param docRef
   * @param pageType
   * @param withSave
   * @return
   * @throws XWikiException 
   */
  public XWikiDocument createDocument(DocumentReference docRef, String pageType, 
      boolean withSave) throws XWikiException {
    if (!getContext().getWiki().exists(docRef, getContext())) {
      XWikiDocument theNewDoc = getContext().getWiki().getDocument(docRef, getContext());
      initNewXWikiDocument(theNewDoc);
      String pageTypeStr = "";
      if (pageType != null) {
        DocumentReference pageTypeClassRef = new DocumentReference(
            docRef.getWikiReference().getName(), PageTypeClasses.PAGE_TYPE_CLASS_SPACE,
            PageTypeClasses.PAGE_TYPE_CLASS_DOC);
        BaseObject pageTypeObj = theNewDoc.getXObject(pageTypeClassRef, true,
            getContext());
        pageTypeObj.setStringValue("page_type", pageType);
        pageTypeStr = pageType + "-";
      }
      if (withSave) {
        getContext().getWiki().saveDocument(theNewDoc, "init " + pageTypeStr + "document", 
            false, getContext());
        LOGGER.debug("saved '" + theNewDoc + "'");
      } else {
        LOGGER.debug("skipped saving '" + theNewDoc + "'");
      }
      return theNewDoc;
    } else {
      LOGGER.warn("Failed to create new Document [" + docRef
          + "] because it already exists");
    }
    return null;
  }

  void initNewXWikiDocument(XWikiDocument theNewDoc) {
    Date creationDate = new Date();
    theNewDoc.setDefaultLanguage(getWebService().getDefaultLanguage());
    theNewDoc.setLanguage("");
    theNewDoc.setCreationDate(creationDate);
    theNewDoc.setContentUpdateDate(creationDate);
    theNewDoc.setDate(creationDate);
    theNewDoc.setCreator(getContext().getUser());
    theNewDoc.setAuthor(getContext().getUser());
    theNewDoc.setTranslation(0);
    theNewDoc.setContent("");
    theNewDoc.setMetaDataDirty(true);
    LOGGER.info("initNewXWikiDocument:  doc ["
        + theNewDoc.getDocumentReference() + "], defaultLang ["
        + theNewDoc.getDefaultLanguage() + "] isNew saving");
  }

  private IWebUtilsService getWebService() {
    if (injected_webService != null) {
      return injected_webService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }
}
