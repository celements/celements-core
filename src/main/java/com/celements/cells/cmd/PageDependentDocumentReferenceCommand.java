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
package com.celements.cells.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.inheritor.InheritorFactory;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PageDependentDocumentReferenceCommand {

  public static final String PDC_DEFAULT_CONTENT_NAME = "PDC-Default_Content";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_SPACE = "Celements";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_DOC = "PageDepCellConfigClass";

  public static final String PROPNAME_SPACE_NAME = "space_name";
  public static final String PROPNAME_IS_INHERITABLE = "is_inheritable";

  private static Log mLogger = LogFactory.getFactory().getInstance(
      PageDependentDocumentReferenceCommand.class);

  IWebUtils webUtils = WebUtils.getInstance();

  public DocumentReference getDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) {
    if (!isCurrentDocument(document, cellDocRef, context)) {
      return getDependentDocumentReference(document, cellDocRef, context);
    }
    return document.getDocumentReference();
  }

  public DocumentReference getDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, boolean isInheritable, XWikiContext context) {
    if (!isCurrentDocument(document, cellDocRef, context)) {
      return getDependentDocumentReference(document, cellDocRef, isInheritable, context);
    }
    return document.getDocumentReference();
  }

  public XWikiDocument getDocument(XWikiDocument document, DocumentReference cellDocRef,
      XWikiContext context) throws XWikiException {
    if (!isCurrentDocument(document, cellDocRef, context)) {
      DocumentReference dependentDocRef = getDependentDocumentReference(document,
          cellDocRef, context);
      return context.getWiki().getDocument(dependentDocRef, context);
    }
    return document;
  }

  List<String> getDependentDocList(String fullName, String depDocSpace,
      XWikiContext context) {
    List<String> docParentList = webUtils.getDocumentParentsList(fullName,
        true, context);
    List<String> depDocList = new ArrayList<String>(docParentList.size());
    for (String parent : docParentList) {
      depDocList.add(depDocSpace + parent.substring(parent.indexOf('.')));
    }
    return depDocList;
  }

  public XWikiDocument getTranslatedDocument(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) throws XWikiException {
    if (!isCurrentDocument(document, cellDocRef, context)) {
      return getDocument(document, cellDocRef, context).getTranslatedDocument(context);
    }
    return document;
  }

  DocumentReference getDependentDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) {
    return getDependentDocumentReference(document, cellDocRef, isInheritable(cellDocRef,
        context), context);
  }

  DocumentReference getDependentDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, boolean isInheritable, XWikiContext context) {
    String depDocSpace = getDependentDocumentSpace(document, cellDocRef, context);
    if (isInheritable) {
      List<String> depDocList = getDependentDocList(document.getFullName(), depDocSpace,
          context);
      mLogger.debug("getDependentDocumentReference: inheritable for ["
          + document.getDocumentReference() + "]. ");
      XWikiDocument pageDepDoc = new InheritorFactory().getContentInheritor(depDocList,
          context).getDocument();
      if (pageDepDoc != null) {
        return pageDepDoc.getDocumentReference();
      } else {
        mLogger.debug("getDependentDocumentReference: inheritable result was null."
            + " Fallback to [" + depDocSpace + "." + PDC_DEFAULT_CONTENT_NAME + "]");
        return new DocumentReference(context.getDatabase(), depDocSpace, 
            PDC_DEFAULT_CONTENT_NAME);
      }
    } else {
      return new DocumentReference(context.getDatabase(), depDocSpace, 
          document.getDocumentReference().getName());
    }
  }

  public String getDependentDocumentSpace(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) {
    String spaceName;
    try {
      if (!"".equals(getDepCellSpace(cellDocRef, context))) {
        spaceName = getCurrentDocumentSpaceName(document, context) + "_"
          + getDepCellSpace(cellDocRef, context);
      } else {
        mLogger.warn("getDependentDocumentSpace: fallback to currentDocument. Please"
            + " check with isCurrentDocument method before calling"
            + " getDependentDocumentSpace!");
        spaceName = getCurrentDocumentSpaceName(document, context);
      }
    } catch (XWikiException exp) {
      spaceName = getCurrentDocumentSpaceName(document, context);
      mLogger.error("getDependentDocumentSpace: Failed to get getDepCellSpace from ["
          + cellDocRef + "] assuming" + " [" + spaceName + "] for document space.", exp);
    }
    return spaceName;
  }

  String getCurrentDocumentSpaceName(XWikiDocument document, XWikiContext context
      ) {
    mLogger.info("getCurrentDocumentSpaceName for document ["
        + document.getDocumentReference() + "].");
    String spaceName;
    List<SpaceReference> currentSpaces = document.getDocumentReference(
        ).getSpaceReferences();
    if (currentSpaces.size() > 0) {
      spaceName = currentSpaces.get(0).getName();
    } else {
      spaceName = context.getWiki().getDefaultSpace(context);
      mLogger.warn("getCurrentDocumentSpaceName: no space reference for current Document"
          + " [" + document.getDocumentReference() + "] found. Fallback to default ["
          + spaceName + "].");
    }
    return spaceName;
  }

  boolean isCurrentDocument(XWikiDocument document, DocumentReference cellDocRef,
      XWikiContext context) {
    try {
      return "".equals(getDepCellSpace(cellDocRef, context));
    } catch (XWikiException exp) {
      mLogger.error("Failed to get PageDepCellConfigClass object from [" + cellDocRef
          + "].", exp);
      // return true, because without config we are unable to determine the document
      return true;
    }
  }

  String getDepCellSpace(DocumentReference cellDocRef, XWikiContext context
      ) throws XWikiException {
    BaseObject cellConfObj = getDepCellXObject(cellDocRef, context);
    if (cellConfObj != null) {
      String spaceName = cellConfObj.getStringValue(PROPNAME_SPACE_NAME);
      if (spaceName != null) {
        return spaceName;
      }
    }
    return "";
  }

  public boolean isInheritable(DocumentReference cellDocRef, XWikiContext context) {
    try {
      BaseObject cellConfObj = getDepCellXObject(cellDocRef, context);
      if (cellConfObj != null) {
        return (cellConfObj.getIntValue(PROPNAME_IS_INHERITABLE, 0) != 0);
      }
    } catch (XWikiException exp) {
      mLogger.error("Faild to check if isInheritable for ["
          + cellDocRef.getLastSpaceReference() + "." + cellDocRef.getName() + "].", exp);
    }
    return false;
  }

  private BaseObject getDepCellXObject(DocumentReference cellDocRef, XWikiContext context)
      throws XWikiException {
    BaseObject cellConfObj = context.getWiki().getDocument(cellDocRef, context
        ).getXObject(getPageDepCellConfigClassDocRef(context));
    return cellConfObj;
  }

  public DocumentReference getPageDepCellConfigClassDocRef(XWikiContext context) {
    DocumentReference pageDepConfigClassRef = new DocumentReference(context.getDatabase(),
        PAGE_DEP_CELL_CONFIG_CLASS_SPACE, PAGE_DEP_CELL_CONFIG_CLASS_DOC);
    return pageDepConfigClassRef;
  }

}
