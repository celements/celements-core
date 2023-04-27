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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.EntityReferenceValueProvider;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.ICellsClassConfig;
import com.celements.cells.classes.PageDepCellConfigClass;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class PageDependentDocumentReferenceCommand {

  public static final String PDC_DEFAULT_CONTENT_NAME = "PDC-Default_Content";
  public static final String PDC_WIKIDEFAULT_SPACE_NAME = "PDC-WikiDefault";
  /**
   * @deprecated instead use {@link PageDepCellConfigClass}
   */
  @Deprecated
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_SPACE = ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE;
  /**
   * @deprecated instead use {@link PageDepCellConfigClass}
   */
  @Deprecated
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_DOC = ICellsClassConfig.PAGE_DEP_CELL_CONFIG_CLASS_DOC;
  /**
   * @deprecated instead use {@link PageDepCellConfigClass}
   */
  @Deprecated
  public static final String PROPNAME_SPACE_NAME = "space_name";
  /**
   * @deprecated instead use {@link PageDepCellConfigClass}
   */
  @Deprecated
  public static final String PROPNAME_IS_INHERITABLE = "is_inheritable";

  private static final Logger LOGGER = LoggerFactory.getLogger(
      PageDependentDocumentReferenceCommand.class);

  PageLayoutCommand pageLayoutCmd;

  private SpaceReference currentLayoutRef;

  IWebUtilsService inject_WebUtilsService;

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
    LOGGER.debug("getDocumentReference: document [" + docRef + "] cellDocRef [" + cellDocRef
        + "] context language [" + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      return getDependentDocumentReference(docRef, cellDocRef);
    }
    LOGGER.info("getDocumentReference: isCurrentDoc returning " + docRef);
    return docRef;
  }

  /**
   * @deprecated since 2.29.0 instead use getDocumentReference(DocumentReference,
   *             DocumentReference, boolean)
   */
  @Deprecated
  public DocumentReference getDocumentReference(XWikiDocument document,
      DocumentReference cellDocRef, boolean isInheritable, XWikiContext context) {
    return getDocumentReference(document.getDocumentReference(), cellDocRef, isInheritable);
  }

  public DocumentReference getDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef, boolean isInheritable) {
    LOGGER.debug("getDocumentReference: document [" + docRef + "] cellDocRef [" + cellDocRef
        + "] isInheritable [" + isInheritable + "] context language [" + getContext().getLanguage()
        + "].");
    if (!isCurrentDocument(cellDocRef)) {
      return getDependentDocumentReference(docRef, cellDocRef, isInheritable);
    }
    LOGGER.info("getDocumentReference: isCurrentDoc returning " + docRef);
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

  public XWikiDocument getDocument(XWikiDocument document, DocumentReference cellDocRef)
      throws XWikiException {
    LOGGER.debug("getDocument: document [" + document.getDocumentReference() + "] cellDocRef ["
        + cellDocRef + "] context language [" + getContext().getLanguage() + "].");
    if (!isCurrentDocument(cellDocRef)) {
      DocumentReference dependentDocRef = getDependentDocumentReference(
          document.getDocumentReference(), cellDocRef);
      return getContext().getWiki().getDocument(dependentDocRef, getContext());
    }
    return document;
  }

  List<String> getDependentDocList(DocumentReference docRef, String depDocSpace) {
    List<DocumentReference> docParentList = getWebUtilsService().getDocumentParentsList(docRef,
        true);
    List<String> depDocList = new ArrayList<>(docParentList.size());
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
  public XWikiDocument getTranslatedDocument(XWikiDocument document, DocumentReference cellDocRef,
      XWikiContext context) throws XWikiException {
    return getTranslatedDocument(document, cellDocRef);
  }

  public XWikiDocument getTranslatedDocument(XWikiDocument document, DocumentReference cellDocRef)
      throws XWikiException {
    LOGGER.debug("getTranslatedDocument: document [" + document.getDocumentReference()
        + "] cellDocRef [" + cellDocRef + "] context language [" + getContext().getLanguage()
        + "].");
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
    LOGGER.debug("getDependentDocumentReference: docRef '{}' cellDocRef '{}' isInheritable '{}'"
        + " depDocSpaceRef '{}'.", docRef, cellDocRef, isInheritable, depDocSpaceRef);
    if (isInheritable) {
      List<String> depDocList = getDependentDocList(docRef, depDocSpaceRef.getName());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getDependentDocumentReference: inheritable for '{}' depDocList '{}'. ",
            docRef, Arrays.deepToString(depDocList.toArray(new String[0])));
      } else {
        LOGGER.info("getDependentDocumentReference: inheritable for '{}'. ", docRef);
      }
      XWikiDocument pageDepDoc = new InheritorFactory().getContentInheritor(depDocList,
          getContext()).getDocument();
      if (pageDepDoc != null) {
        return pageDepDoc.getDocumentReference();
      } else {
        final DocumentReference defaultDPCdocRef = getDependentDefaultDocumentReference(docRef,
            cellDocRef);
        LOGGER.debug("getDependentDocumentReference: inheritable result was null."
            + " Fallback to default '{}'", defaultDPCdocRef);
        return defaultDPCdocRef;
      }
    } else {
      return new DocumentReference(docRef.getName(), depDocSpaceRef);
    }
  }

  public DocumentReference getDependentDefaultDocumentReference(DocumentReference docRef,
      DocumentReference cellDocRef) {
    DocumentReference spaceDefault = getSpaceDefaultDocRef(docRef, cellDocRef);
    List<String> depDefaultDocList = new ArrayList<>();
    depDefaultDocList.add(getRefSerializer().serialize(spaceDefault));
    depDefaultDocList.add(getRefSerializer().serialize(getWikiDefaultDocRef(docRef, cellDocRef)));
    if (getCurrentLayoutRef() != null) {
      DocumentReference layoutDefault = getLayoutDefaultDocRef(getCurrentLayoutRef(), cellDocRef);
      LOGGER.trace("getDependentDefaultDocumentReference: layout default docRef '{}'",
          layoutDefault);
      if (layoutDefault != null) {
        depDefaultDocList.add(getRefSerializer().serialize(layoutDefault));
      }
    } else {
      LOGGER.trace("getDependentDefaultDocumentReference: no current layout reference found.");
    }
    XWikiDocument pageDepDoc = new InheritorFactory().getContentInheritor(depDefaultDocList,
        getContext()).getDocument();
    if (pageDepDoc != null) {
      LOGGER.debug("getDependentDefaultDocumentReference: docList '{}', pageDepDoc '{}'",
          depDefaultDocList, pageDepDoc.getDocumentReference());
      return pageDepDoc.getDocumentReference();
    } else {
      LOGGER.debug("getDependentDefaultDocumentReference: docList '{}', no default doc found!"
          + " Using spaceDefault", depDefaultDocList);
      // XXX What should be the default?
      // For now using spaceDefault for backwards compatibility
      return spaceDefault;
    }
  }

  public void setCurrentLayoutRef(SpaceReference currentLayoutRef) {
    LOGGER.info("setCurrentLayoutRef: '{}'", currentLayoutRef);
    this.currentLayoutRef = currentLayoutRef;
  }

  SpaceReference getCurrentLayoutRef() {
    if (currentLayoutRef == null) {
      currentLayoutRef = getPageLayoutCmd().getPageLayoutForCurrentDoc();
      LOGGER.info("getCurrentLayoutRef: '{}'", currentLayoutRef);
    }
    return currentLayoutRef;
  }

  public DocumentReference getLayoutDefaultDocRef(SpaceReference currLayoutRef,
      DocumentReference cellDocRef) {
    String depCellSpace = getDepCellSpace(cellDocRef);
    return getLayoutDefaultDocRef(currLayoutRef, depCellSpace);
  }

  public DocumentReference getLayoutDefaultDocRef(SpaceReference currLayoutRef,
      String depCellSpace) {
    if ((depCellSpace != null) && (!"".equals(depCellSpace))) {
      return new DocumentReference(depCellSpace + "-"
          + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME, currLayoutRef);
    }
    return null;
  }

  public DocumentReference getWikiDefaultDocRef(String depCellSpaceSuffix) {
    return getSpaceDefaultDocRef(getDependentWikiSpaceRef(depCellSpaceSuffix));
  }

  public DocumentReference getWikiDefaultDocRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    return getSpaceDefaultDocRef(getDependentWikiSpaceRef(docRef, cellDocRef));
  }

  public DocumentReference getSpaceDefaultDocRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    return getSpaceDefaultDocRef(getDependentDocumentSpaceRef(docRef, cellDocRef));
  }

  public DocumentReference getSpaceDefaultDocRef(SpaceReference depDocumentSpaceRef) {
    return new DocumentReference(PDC_DEFAULT_CONTENT_NAME, depDocumentSpaceRef);
  }

  /**
   * @deprecated since 2.29.0 instead use getDependentDocumentSpace(DocumentReference,
   *             DocumentReference)
   */
  @Deprecated
  public String getDependentDocumentSpace(XWikiDocument document, DocumentReference cellDocRef,
      XWikiContext context) {
    DocumentReference docRef = document.getDocumentReference();
    return getDependentDocumentSpaceRef(docRef, cellDocRef).getName();
  }

  public SpaceReference getDependentDocumentSpaceRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    SpaceReference spaceRef;
    if (!"".equals(getDepCellSpace(cellDocRef))) {
      SpaceReference curSpaceRef = getCurrentDocumentSpaceRef(docRef);
      spaceRef = new SpaceReference(curSpaceRef.getName() + "_" + getDepCellSpace(cellDocRef),
          (WikiReference) curSpaceRef.getParent());
    } else {
      LOGGER.warn("getDependentDocumentSpace: fallback to currentDocument. Please"
          + " check with isCurrentDocument method before calling"
          + " getDependentDocumentSpace!");
      spaceRef = getCurrentDocumentSpaceRef(docRef);
    }
    return spaceRef;
  }

  public SpaceReference getDependentWikiSpaceRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    SpaceReference spaceRef;
    String depCellSpace = getDepCellSpace(cellDocRef);
    spaceRef = getDependentWikiSpaceRef(depCellSpace);
    if (spaceRef == null) {
      LOGGER.warn("getDependentDocumentSpace: fallback to currentDocument. Please"
          + " check with isCurrentDocument method before calling"
          + " getDependentDocumentSpace!");
      spaceRef = getCurrentDocumentSpaceRef(docRef);
    }
    return spaceRef;
  }

  private SpaceReference getDependentWikiSpaceRef(String depCellSpaceSuffix) {
    SpaceReference spaceRef = null;
    if ((depCellSpaceSuffix != null) && !"".equals(depCellSpaceSuffix)) {
      String spaceName = PDC_WIKIDEFAULT_SPACE_NAME + "_" + depCellSpaceSuffix;
      spaceRef = new SpaceReference(spaceName, new WikiReference(getContext().getDatabase()));
    }
    return spaceRef;
  }

  SpaceReference getCurrentDocumentSpaceRef(DocumentReference docRef) {
    LOGGER.info("getCurrentDocumentSpaceRef for document [" + docRef + "].");
    SpaceReference spaceRef;
    List<SpaceReference> currentSpaces = docRef.getSpaceReferences();
    if (currentSpaces.size() > 0) {
      spaceRef = currentSpaces.get(0);
    } else {
      spaceRef = new SpaceReference(getConfigProvider().getDefaultValue(EntityType.SPACE),
          new WikiReference(getContext().getDatabase()));
      LOGGER.warn("getCurrentDocumentSpaceRef: no space reference for current Document" + " ["
          + docRef + "] found. Fallback to default [" + spaceRef + "].");
    }
    return spaceRef;
  }

  boolean isCurrentDocument(DocumentReference cellDocRef) {
    return "".equals(getDepCellSpace(cellDocRef));
  }

  public String getDepCellSpace(DocumentReference cellDocRef) {
    return getDepCellValue(cellDocRef, PageDepCellConfigClass.FIELD_SPACE_NAME).orElse("");
  }

  /**
   * @deprecated since 2.29.0 instead use isInheritable(DocumentReference)
   */
  @Deprecated
  public boolean isInheritable(DocumentReference cellDocRef, XWikiContext context) {
    return isInheritable(cellDocRef);
  }

  public boolean isInheritable(DocumentReference cellDocRef) {
    return getDepCellValue(cellDocRef, PageDepCellConfigClass.FIELD_IS_ACTIVE).orElse(false);
  }

  private <T> Optional<T> getDepCellValue(DocumentReference cellDocRef, ClassField<T> field) {
    return XWikiObjectFetcher.on(getModelAccess().getOrCreateDocument(cellDocRef))
        .filter(PageDepCellConfigClass.CLASS_REF)
        .fetchField(field)
        .stream().findFirst();
  }

  /**
   * @deprecated since 2.29.0 instead use getPageDepCellConfigClassDocRef()
   */
  @Deprecated
  public DocumentReference getPageDepCellConfigClassDocRef(XWikiContext context) {
    return getPageDepCellConfigClassDocRef();
  }

  /**
   * @deprecated since 2.81 instead use
   *             {@link ICellsClassConfig#getPageDepCellConfigClassRef(String)}
   */
  @Deprecated
  public DocumentReference getPageDepCellConfigClassDocRef() {
    DocumentReference pageDepConfigClassRef = new DocumentReference(getContext().getDatabase(),
        PAGE_DEP_CELL_CONFIG_CLASS_SPACE, PAGE_DEP_CELL_CONFIG_CLASS_DOC);
    return pageDepConfigClassRef;
  }

  private ICellsClassConfig getCellsClassConfig() {
    return Utils.getComponent(ICellsClassConfig.class);
  }

  private IWebUtilsService getWebUtilsService() {
    if (inject_WebUtilsService != null) {
      return inject_WebUtilsService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
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
    return getWebUtilsService().getRefDefaultSerializer();
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
