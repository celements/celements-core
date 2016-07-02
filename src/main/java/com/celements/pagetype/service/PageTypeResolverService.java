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
package com.celements.pagetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class PageTypeResolverService implements IPageTypeResolverRole {

  private static Logger LOGGER = LoggerFactory.getLogger(PageTypeResolverService.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IPageTypeRole pageTypeService;

  @Requirement
  IPageTypeClassConfig pageTypeClassCfg;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public PageTypeReference getPageTypeRefForCurrentDoc() {
    XWikiDocument doc = getContext().getDoc();
    if ((getContext().getRequest() != null) && ("inline".equals(getContext().getAction()))) {
      String templName = getContext().getRequest().get("template");
      if (!Strings.isNullOrEmpty(templName)) {
        try {
          DocumentReference docRef = webUtilsService.resolveDocumentReference(templName);
          doc = modelAccess.getDocument(docRef);
          LOGGER.debug("getPageTypeRefForCurrentDoc: creating new document, getting page type from "
              + "template {}", docRef);
        } catch (DocumentNotExistsException exp) {
          LOGGER.warn("Exception while getting template doc '{}", templName, exp);
        }
      }
    }
    return getPageTypeRefForDocWithDefault(doc);
  }

  @Override
  public PageTypeReference getPageTypeRefForDocWithDefault(XWikiDocument doc) {
    if (doc != null) {
      PageTypeReference pageTypeRef = getPageTypeRefForDoc(doc);
      if (pageTypeRef == null) {
        pageTypeRef = getDefaultPageTypeRefForDoc(doc.getDocumentReference());
      }
      return pageTypeRef;
    }
    return null;
  }

  @Override
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

  @Override
  public PageTypeReference getPageTypeRefForDocWithDefault(DocumentReference docRef) {
    try {
      XWikiDocument doc = modelAccess.getDocument(docRef);
      return getPageTypeRefForDocWithDefault(doc);
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Failed to get XWikiDocument for [" + docRef + "].", exp);
    }
    return getDefaultPageTypeRefForDoc(docRef);
  }

  @Override
  public PageTypeReference getDefaultPageTypeRefForDoc(DocumentReference docRef) {
    FieldInheritor inheritor = new InheritorFactory().getConfigFieldInheritor(
        pageTypeClassCfg.getPageTypeClassRef(docRef.getWikiReference()), docRef);
    String defPageTypeName = inheritor.getStringValue(IPageTypeClassConfig.PAGE_TYPE_FIELD,
        "RichText");
    PageTypeReference pageTypeRef = pageTypeService.getPageTypeRefByConfigName(defPageTypeName);
    if (pageTypeRef != null) {
      return pageTypeRef;
    }
    return pageTypeService.getPageTypeRefByConfigName("RichText");
  }

  @Override
  public PageTypeReference getPageTypeRefForDoc(XWikiDocument checkDoc) {
    BaseObject pageTypeObj = getPageTypeObject(checkDoc);
    if (pageTypeObj != null) {
      String pageType = pageTypeObj.getStringValue(IPageTypeClassConfig.PAGE_TYPE_FIELD);
      return pageTypeService.getPageTypeRefByConfigName(pageType);
    }
    return null;
  }

  @Override
  public BaseObject getPageTypeObject(XWikiDocument checkDoc) {
    if (checkDoc != null) {
      if (checkDoc.isNew() && (webUtilsService.getWikiTemplateDocRef() != null)) {
        checkDoc = webUtilsService.getWikiTemplateDoc();
      }
      DocumentReference pageTypeClassRef = pageTypeClassCfg.getPageTypeClassRef(
          checkDoc.getDocumentReference().getWikiReference());
      if ((checkDoc.getXObjects(pageTypeClassRef) != null) && (checkDoc.getXObjects(
          pageTypeClassRef).size() > 0)) {
        BaseObject pageTypeObj = checkDoc.getXObject(pageTypeClassRef);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("getPageTypeObject: page type object for class [" + pageTypeClassRef
              + "] found for [" + checkDoc + "] with object details: " + pageTypeObj.toXMLString());
        } else {
          LOGGER.debug("getPageTypeObject: page type object for class [" + pageTypeClassRef
              + "] found for [" + checkDoc + "].");
        }
        return pageTypeObj;
      }
      LOGGER.debug("getPageTypeObject: no page type object for class [" + pageTypeClassRef
          + "] found for [" + checkDoc + "].");
    } else {
      LOGGER.warn("getPageTypeObject: checkDoc parameter is null!");
    }
    return null;
  }

}
