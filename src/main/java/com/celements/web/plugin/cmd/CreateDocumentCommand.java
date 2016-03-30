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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * 
 * @deprecated instead use {@link IModelAccessFacade#createDocument(DocumentReference)}
 * and {@link IPageTypeRole#setPageType(XWikiDocument, PageTypeReference)}
 *
 */
@Deprecated
public class CreateDocumentCommand {

  private static Logger LOGGER = LoggerFactory.getLogger(CreateDocumentCommand.class);

  /**
   * @deprecated use {@link IModelAccessFacade#createDocument(DocumentReference)}
   * and {@link IPageTypeRole#setPageType(XWikiDocument, PageTypeReference)}
   * 
   * createDocument creates a new document if it does not exist
   * @param docRef
   * @param pageType
   * @return
   */
  @Deprecated
  public XWikiDocument createDocument(DocumentReference docRef, String pageType) {
    try {
      return createDocument(docRef, pageType, true);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document [" + docRef + "].", exp);
      return null;
    }
  }

  /**
   * @deprecated use {@link IModelAccessFacade#createDocument(DocumentReference)}
   * and {@link IPageTypeRole#setPageType(XWikiDocument, PageTypeReference)}
   * <br>
   * createDocument creates a new document if it does not exist
   * @param docRef
   * @param pageType
   * @param withSave
   * @return
   * @throws XWikiException 
   */
  @Deprecated
  public XWikiDocument createDocument(DocumentReference docRef, String pageType, 
      boolean withSave) throws XWikiException {
    try {
      XWikiDocument doc = getModelAccess().createDocument(docRef);
      PageTypeReference ptRef = getPageTypeService().getPageTypeRefByConfigName(
          Strings.nullToEmpty(pageType));
      String pageTypeStr = "";
      if (ptRef != null) {
        getPageTypeService().setPageType(doc, ptRef);
        pageTypeStr = pageType + "-";
      }
      if (withSave) {
        getModelAccess().saveDocument(doc, "init " + pageTypeStr + "document", false);
        LOGGER.debug("saved '" + doc + "'");
      } else {
        LOGGER.debug("skipped saving '" + doc + "'");
      }
      return doc;
    } catch (DocumentLoadException | DocumentSaveException exc) {
      throw new XWikiException(0, 0, "Load/Save failed", exc);
    } catch (DocumentAlreadyExistsException exc) {
      LOGGER.info("doc already exists '{}'", docRef);
    }
    return null;
  }

  private IPageTypeRole getPageTypeService() {
    return Utils.getComponent(IPageTypeRole.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }
  
}
