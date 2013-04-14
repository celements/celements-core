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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.EntityReferenceValueProvider;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.inheritor.InheritorFactory;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PageDependentDocumentReferenceCommand {

  public static final String PDC_DEFAULT_CONTENT_NAME = "PDC-Default_Content";
  public static final String PDC_WIKIDEFAULT_SPACE_NAME = "PDC-WikiDefault";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_SPACE = "Celements";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_DOC = "PageDepCellConfigClass";

  public static final String PROPNAME_SPACE_NAME = "space_name";
  public static final String PROPNAME_IS_INHERITABLE = "is_inheritable";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      PageDependentDocumentReferenceCommand.class);
  PageLayoutCommand pageLayoutCmd;

  /**
   * @deprecated since 2.29.0 instead use getDocumentReference(DocumentReference,
   *             DocumentReference)
   */
  @Deprecated
  public DocumentReference getDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) {
    return getDocumentReference(document.getDocumentReference(), cellDocRef);
  }

  public DocumentReference getDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef) {
    LOGGER.debug("getDocumentReference: document [" + docRef
        + "] cellDocRef [" + cellDocRef + "] context language ["
        + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      return getDependentDocumentReference(docRef, cellDocRef);
    }
    return docRef;
  }

  /**
   * @deprecated since 2.29.0 instead use getDocumentReference(DocumentReference,
   *             DocumentReference, boolean)
   */
  @Deprecated
  public DocumentReference getDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, boolean isInheritable, XWikiContext context) {
    return getDocumentReference(document.getDocumentReference(), cellDocRef,
        isInheritable);
  }

  public DocumentReference getDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef, boolean isInheritable) {
    LOGGER.debug("getDocumentReference: document [" + docRef
        + "] cellDocRef [" + cellDocRef + "] isInheritable [" + isInheritable
        + "] context language [" + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      return getDependentDocumentReference(docRef, cellDocRef,
          isInheritable);
    }
    return docRef;
  }

  /**
   * @deprecated since 2.29.0 instead use getDocument(XWikiDocument, DocumentReference)
   */
  @Deprecated
  public XWikiDocument getDocument(XWikiDocument document, DocumentReference cellDocRef,
      XWikiContext context) throws XWikiException {
    return getDocument(document, cellDocRef);
  }

  public XWikiDocument getDocument(XWikiDocument document, DocumentReference cellDocRef
      ) throws XWikiException {
    LOGGER.debug("getDocument: document [" + document.getDocumentReference()
        + "] cellDocRef [" + cellDocRef + "] context language ["
        + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      DocumentReference dependentDocRef = getDependentDocumentReference(
          document.getDocumentReference(), cellDocRef);
      return getContext().getWiki().getDocument(dependentDocRef, getContext());
    }
    return document;
  }

  List<String> getDependentDocList(DocumentReference docRef, String depDocSpace) {
    List<DocumentReference> docParentList = getWebUtilsService().getDocumentParentsList(
        docRef, true);
    List<String> depDocList = new ArrayList<String>(docParentList.size());
    for (DocumentReference parentRef : docParentList) {
      depDocList.add(depDocSpace + "." + parentRef.getName());
    }
    return depDocList;
  }

  /**
   * @deprecated since 2.29.0 instead use getTranslatedDocument(XWikiDocument,
   *             DocumentReference)
   */
  @Deprecated
  public XWikiDocument getTranslatedDocument(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) throws XWikiException {
    return getTranslatedDocument(document, cellDocRef);
  }

  public XWikiDocument getTranslatedDocument(XWikiDocument document,
        DocumentReference cellDocRef) throws XWikiException {
    LOGGER.debug("getTranslatedDocument: document [" + document.getDocumentReference()
        + "] cellDocRef [" + cellDocRef + "] context language ["
        + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      XWikiDocument tdoc = getDocument(document, cellDocRef).getTranslatedDocument(
          getContext().getLanguage(), getContext());
      LOGGER.trace("getTranslatedDocument returning tdoc [" + tdoc.getDocumentReference()
          + "] lang [" + tdoc.getLanguage() + "," + tdoc.getDefaultLanguage() + "].");
      return tdoc;
    }
    return document;
  }

  DocumentReference getDependentDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef) {
    return getDependentDocumentReference(docRef, cellDocRef, isInheritable(cellDocRef));
  }

  DocumentReference getDependentDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef, boolean isInheritable) {
    SpaceReference depDocSpaceRef = getDependentDocumentSpaceRef(docRef, cellDocRef);
    if (isInheritable) {
      List<String> depDocList = getDependentDocList(docRef, depDocSpaceRef.getName());
      LOGGER.debug("getDependentDocumentReference: inheritable for [" + docRef + "]. ");
      XWikiDocument pageDepDoc = new InheritorFactory().getContentInheritor(depDocList,
          getContext()).getDocument();
      if (pageDepDoc != null) {
        return pageDepDoc.getDocumentReference();
      } else {
        LOGGER.debug("getDependentDocumentReference: inheritable result was null."
            + " Fallback to [" + depDocSpaceRef + "." + PDC_DEFAULT_CONTENT_NAME + "]");
        return getDependentDefaultDocumentReference(docRef, cellDocRef);
      }
    } else {
      return new DocumentReference(docRef.getName(), depDocSpaceRef);
    }
  }

  public DocumentReference getDependentDefaultDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef) {
    DocumentReference spaceDefault = new DocumentReference(PDC_DEFAULT_CONTENT_NAME,
        getDependentDocumentSpaceRef(docRef, cellDocRef));
    DocumentReference wikiDefault = new DocumentReference(PDC_DEFAULT_CONTENT_NAME,
        getDependentWikiSpaceRef(docRef, cellDocRef));
    List<String> depDefaultDocList = new ArrayList<String>();
    depDefaultDocList.add(getRefSerializer().serialize(spaceDefault));
    depDefaultDocList.add(getRefSerializer().serialize(wikiDefault));
    SpaceReference currLayoutRef = getPageLayoutCmd().getPageLayoutForCurrentDoc();
    if (currLayoutRef != null) {
      try {
        DocumentReference layoutDefault = new DocumentReference(getDepCellSpace(
            cellDocRef) + "-"
            + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME,
            currLayoutRef);
        depDefaultDocList.add(getRefSerializer().serialize(layoutDefault));
      } catch (XWikiException exp) {
        LOGGER.warn("Failed to get layoutDefault because unable to get depCellSpace for ["
            + cellDocRef + "] omitting layout default.", exp);
      }
    }
    XWikiDocument pageDepDoc = new InheritorFactory().getContentInheritor(
        depDefaultDocList, getContext()).getDocument();
    if (pageDepDoc != null) {
      return pageDepDoc.getDocumentReference();
    } else {
      //XXX What should be the default?
      //For now using spaceDefault for backwards compatibility
      return spaceDefault;
    }
  }

  /**
   * @deprecated since 2.29.0 instead use
   *             getDependentDocumentSpace(DocumentReference, DocumentReference)
   */
  @Deprecated
  public String getDependentDocumentSpace(XWikiDocument document,
      DocumentReference cellDocRef, XWikiContext context) {
    DocumentReference docRef = document.getDocumentReference();
    return getDependentDocumentSpaceRef(docRef, cellDocRef).getName();
  }

  public SpaceReference getDependentDocumentSpaceRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    SpaceReference spaceName;
    try {
      if (!"".equals(getDepCellSpace(cellDocRef))) {
        spaceName = (SpaceReference) getCurrentDocumentSpaceRef(docRef).clone();
        spaceName.setName(spaceName.getName() + "_" + getDepCellSpace(cellDocRef));
      } else {
        LOGGER.warn("getDependentDocumentSpace: fallback to currentDocument. Please"
            + " check with isCurrentDocument method before calling"
            + " getDependentDocumentSpace!");
        spaceName = getCurrentDocumentSpaceRef(docRef);
      }
    } catch (XWikiException exp) {
      spaceName = getCurrentDocumentSpaceRef(docRef);
      LOGGER.error("getDependentDocumentSpace: Failed to get getDepCellSpace from ["
          + cellDocRef + "] assuming" + " [" + spaceName + "] for document space.", exp);
    }
    return spaceName;
  }

  public SpaceReference getDependentWikiSpaceRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    SpaceReference spaceRef;
    try {
      if (!"".equals(getDepCellSpace(cellDocRef))) {
        String spaceName = PDC_WIKIDEFAULT_SPACE_NAME + "_" + getDepCellSpace(cellDocRef);
        spaceRef = new SpaceReference(spaceName,
            (WikiReference)getCurrentDocumentSpaceRef(docRef).getParent());
      } else {
        LOGGER.warn("getDependentDocumentSpace: fallback to currentDocument. Please"
            + " check with isCurrentDocument method before calling"
            + " getDependentDocumentSpace!");
        spaceRef = getCurrentDocumentSpaceRef(docRef);
      }
    } catch (XWikiException exp) {
      spaceRef = getCurrentDocumentSpaceRef(docRef);
      LOGGER.error("getDependentDocumentSpace: Failed to get getDepCellSpace from ["
          + cellDocRef + "] assuming" + " [" + spaceRef + "] for document space.", exp);
    }
    return spaceRef;
  }

  SpaceReference getCurrentDocumentSpaceRef(DocumentReference docRef) {
    LOGGER.info("getCurrentDocumentSpaceName for document [" + docRef + "].");
    SpaceReference spaceRef;
    List<SpaceReference> currentSpaces = docRef.getSpaceReferences();
    if (currentSpaces.size() > 0) {
      spaceRef = currentSpaces.get(0);
    } else {
      spaceRef = new SpaceReference(getConfigProvider().getDefaultValue(EntityType.SPACE),
          new WikiReference(getContext().getDatabase()));
      LOGGER.warn("getCurrentDocumentSpaceName: no space reference for current Document"
          + " [" + docRef + "] found. Fallback to default ["
          + spaceRef + "].");
    }
    return spaceRef;
  }

  boolean isCurrentDocument(DocumentReference cellDocRef) {
    try {
      return "".equals(getDepCellSpace(cellDocRef));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get PageDepCellConfigClass object from [" + cellDocRef
          + "].", exp);
      // return true, because without config we are unable to determine the document
      return true;
    }
  }

  String getDepCellSpace(DocumentReference cellDocRef) throws XWikiException {
    BaseObject cellConfObj = getDepCellXObject(cellDocRef);
    if (cellConfObj != null) {
      String spaceName = cellConfObj.getStringValue(PROPNAME_SPACE_NAME);
      if (spaceName != null) {
        return spaceName;
      }
    }
    return "";
  }

  /**
   * @deprecated since 2.29.0 instead use isInheritable(DocumentReference)
   */
  @Deprecated
  public boolean isInheritable(DocumentReference cellDocRef, XWikiContext context) {
    return isInheritable(cellDocRef);
  }

  public boolean isInheritable(DocumentReference cellDocRef) {
    try {
      BaseObject cellConfObj = getDepCellXObject(cellDocRef);
      if (cellConfObj != null) {
        return (cellConfObj.getIntValue(PROPNAME_IS_INHERITABLE, 0) != 0);
      }
    } catch (XWikiException exp) {
      LOGGER.error("Faild to check if isInheritable for ["
          + cellDocRef.getLastSpaceReference() + "." + cellDocRef.getName() + "].", exp);
    }
    return false;
  }

  private BaseObject getDepCellXObject(DocumentReference cellDocRef)
      throws XWikiException {
    BaseObject cellConfObj = getContext().getWiki().getDocument(cellDocRef, getContext()
        ).getXObject(getPageDepCellConfigClassDocRef());
    return cellConfObj;
  }

  /**
   * @deprecated since 2.29.0 instead use getPageDepCellConfigClassDocRef()
   */
  @Deprecated
  public DocumentReference getPageDepCellConfigClassDocRef(XWikiContext context) {
    return getPageDepCellConfigClassDocRef();
  }

  public DocumentReference getPageDepCellConfigClassDocRef() {
    DocumentReference pageDepConfigClassRef = new DocumentReference(
        getContext().getDatabase(), PAGE_DEP_CELL_CONFIG_CLASS_SPACE,
        PAGE_DEP_CELL_CONFIG_CLASS_DOC);
    return pageDepConfigClassRef;
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }
  
  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  private EntityReferenceValueProvider getConfigProvider() {
    return Utils.getComponent(EntityReferenceValueProvider.class);
  } 

  PageLayoutCommand getPageLayoutCmd() {
    if (this.pageLayoutCmd == null) {
      this.pageLayoutCmd = new PageLayoutCommand();
    }
    return this.pageLayoutCmd;
  }

  private EntityReferenceSerializer<String> getRefSerializer() {
    return getWebUtilsService().getRefLocalSerializer();
  }

}
