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

import static com.google.common.base.MoreObjects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class PageTypeResolverService implements IPageTypeResolverRole {

  private static Logger LOGGER = LoggerFactory.getLogger(PageTypeResolverService.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private IPageTypeRole pageTypeService;

  @Requirement(PageTypeClass.CLASS_DEF_HINT)
  private ClassDefinition pageTypeClassDef;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  @Override
  public PageTypeReference getPageTypeRefForCurrentDoc() {
    XWikiDocument doc = firstNonNull(webUtilsService.getWikiTemplateDoc(), context.getDoc());
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
    PageTypeReference pageTypeRef;
    try {
      XWikiDocument doc = modelAccess.getDocument(docRef);
      pageTypeRef = getPageTypeRefForDocWithDefault(doc);
    } catch (DocumentNotExistsException notExistsExp) {
      pageTypeRef = getDefaultPageTypeRefForDoc(docRef);
    }
    return pageTypeRef;
  }

  @Override
  public PageTypeReference getDefaultPageTypeRefForDoc(DocumentReference docRef) {
    FieldInheritor inheritor = new InheritorFactory().getConfigFieldInheritor(
        pageTypeClassDef.getClassReference().getDocRef(docRef.getWikiReference()), docRef);
    String defPageTypeName = inheritor.getStringValue(IPageTypeClassConfig.PAGE_TYPE_FIELD,
        "RichText");
    PageTypeReference pageTypeRef = pageTypeService.getPageTypeRefByConfigName(defPageTypeName);
    if (pageTypeRef != null) {
      return pageTypeRef;
    }
    return pageTypeService.getPageTypeRefByConfigName("RichText");
  }

  @Override
  public Optional<PageTypeReference> resolvePageTypeReference(XWikiDocument doc) {
    return pageTypeService.getPageTypeReference(getPageTypeFetcher(doc).fetchField(
        PageTypeClass.FIELD_PAGE_TYPE).first().or(""));
  }

  @Override
  @Deprecated
  public PageTypeReference getPageTypeRefForDoc(XWikiDocument doc) {
    if (doc != null) {
      return resolvePageTypeReference(doc).orNull();
    }
    return null;
  }

  @Override
  @Deprecated
  public BaseObject getPageTypeObject(XWikiDocument doc) {
    BaseObject pageTypeObj = null;
    if (doc != null) {
      pageTypeObj = getPageTypeFetcher(doc).first().orNull();
    }
    return pageTypeObj;
  }

  private XWikiObjectFetcher getPageTypeFetcher(XWikiDocument doc) {
    if (useTemplateDoc(doc)) {
      doc = webUtilsService.getWikiTemplateDoc();
    }
    XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(doc).filter(pageTypeClassDef);
    if (LOGGER.isTraceEnabled() && fetcher.exists()) {
      LOGGER.trace("getPageTypeFetcher - for [{}] with object [{}] details: {}", doc,
          fetcher.first().get(), fetcher.first().get().toXMLString());
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getPageTypeFetcher - for [{}]: {}", doc, fetcher.first().orNull());
    }
    return fetcher;
  }

  private boolean useTemplateDoc(XWikiDocument doc) {
    return doc.isNew() && (context.getDoc() != null) && doc.getDocumentReference().equals(
        context.getDoc().getDocumentReference())
        && (webUtilsService.getWikiTemplateDocRef() != null);
  }

}
