package com.celements.pagetype.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.celements.pagetype.PageTypeClasses;
import com.celements.pagetype.PageTypeReference;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class PageTypeResolverService implements IPageTypeResolverRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      PageTypeResolverService.class);

  @Requirement("celements.celPageTypeClasses")
  IClassCollectionRole pageTypeClasses;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IPageTypeRole pageTypeService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  private PageTypeClasses getPageTypeClasses() {
    return (PageTypeClasses) pageTypeClasses;
  }

  public PageTypeReference getPageTypeRefForCurrentDoc() {
    return getPageTypeRefForDocWithDefault(getContext().getDoc());
  }

  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc) {
    PageTypeReference pageTypeRef = getPageTypeRefForDoc(doc);
    if (pageTypeRef == null) {
      pageTypeRef = getDefaultPageTypeRefForDoc(doc.getDocumentReference());
    }
    return pageTypeRef;
  }

  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc,
      PageTypeReference defaultPTRef) {
    PageTypeReference pageTypeRef = getPageTypeRefForDoc(doc);
    if (pageTypeRef == null) {
      pageTypeRef = defaultPTRef;
    }
    LOGGER.debug("getPageTypeRefForDocWithDefault: for [" + doc.getDocumentReference()
        + "] returning [" + pageTypeRef + "].");
    return pageTypeRef;
  }

  public PageTypeReference getPageTypeRefForDocWithDefault(DocumentReference docRef) {
    try {
      XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
      return getPageTypeRefForDocWithDefault(doc);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get XWikiDocument for [" + docRef + "].", exp);
    }
    return getDefaultPageTypeRefForDoc(docRef);
  }

  public PageTypeReference getDefaultPageTypeRefForDoc(DocumentReference docRef) {
    FieldInheritor inheritor = new InheritorFactory().getConfigFieldInheritor(
        getPageTypeClasses().getPageTypeClassRef(getContext().getDatabase()), docRef);
    String defPageTypeName = inheritor.getStringValue(PageTypeClasses.PAGE_TYPE_FIELD,
        "RichText");
    PageTypeReference pageTypeRef = pageTypeService.getPageTypeRefByConfigName(
        defPageTypeName);
    if (pageTypeRef != null) {
      return pageTypeRef;
    }
    return pageTypeService.getPageTypeRefByConfigName("RichText");
  }

  public PageTypeReference getPageTypeRefForDoc(XWikiDocument checkDoc) {
    BaseObject pageTypeObj = getPageTypeObject(checkDoc);
    if (pageTypeObj != null) {
      String pageType = pageTypeObj.getStringValue(PageTypeClasses.PAGE_TYPE_FIELD);
      return pageTypeService.getPageTypeRefByConfigName(pageType);
    }
    return null;
  }

  public BaseObject getPageTypeObject(XWikiDocument checkDoc) {
    if((checkDoc != null) && checkDoc.isNew()
        && (webUtilsService.getWikiTemplateDocRef() != null)) {
      checkDoc = webUtilsService.getWikiTemplateDoc();
    }
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        checkDoc.getDocumentReference().getLastSpaceReference().getParent().getName());
    if ((checkDoc != null) && (checkDoc.getXObjects(pageTypeClassRef) != null)
        && (checkDoc.getXObjects(pageTypeClassRef).size() > 0)) {
      BaseObject pageTypeObj = checkDoc.getXObject(pageTypeClassRef);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("getPageTypeObject: page type object for class [" + pageTypeClassRef
            + "] found for [" + checkDoc + "] with object details: "
            + pageTypeObj.toXMLString());
      } else {
        LOGGER.debug("getPageTypeObject: page type object for class [" + pageTypeClassRef
            + "] found for [" + checkDoc + "].");
      }
      return pageTypeObj;
    }
    LOGGER.debug("getPageTypeObject: no page type object for class [" + pageTypeClassRef
          + "] found for [" + checkDoc + "].");
    return null;
  }

}
