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
package com.celements.pagetype;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

/**
 * TODO merge/move with/to XObjectPageTypeConfig or PageTypeService
 */
public class PageType {

  /**
   * @deprecated since 2.18.0 instead use PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS
   */
  @Deprecated
  public static final String PAGE_TYPE_PROPERTIES = PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS;

  /**
   * @deprecated since 2.29.1 instead use pageTypeDocRef
   */
  @Deprecated
  private String pageTypeFN;

  private DocumentReference pageTypeDocRef;

  /**
   * @deprecated since 2.29.1 instead use PageType(DocumentReference)
   */
  @Deprecated
  public PageType(String pageTypeFN) {
    this.pageTypeFN = pageTypeFN;
    this.pageTypeDocRef = getModelUtils().resolveRef(pageTypeFN, DocumentReference.class);
  }

  public PageType(DocumentReference pageTypeDocRef) {
    this.pageTypeDocRef = pageTypeDocRef;
    this.pageTypeFN = getModelUtils().serializeRefLocal(pageTypeDocRef);
  }

  public String getConfigName(XWikiContext context) {
    DocumentReference pageTypeDocRef = getModelUtils().resolveRef(getFullName(),
        DocumentReference.class);
    return pageTypeDocRef.getName();
  }

  public XWikiDocument getTemplateDocument(XWikiContext context) throws XWikiException {
    XWikiDocument templateDoc = null;

    if (context.getWiki().exists(pageTypeFN, context)) {
      templateDoc = context.getWiki().getDocument(pageTypeFN, context);
    } else {
      templateDoc = context.getWiki().getDocument("celements2web:" + pageTypeFN, context);
    }

    return templateDoc;
  }

  /**
   * @deprecated since 2.29.1 instead use DocumentReference getDocumentReference()
   */
  @Deprecated
  public String getFullName() {
    return pageTypeFN;
  }

  public DocumentReference getDocumentReference() {
    return pageTypeDocRef;
  }

  public BaseObject getPageTypeProperties(XWikiContext context) {
    try {
      return getTemplateDocument(context).getObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    } catch (XWikiException e) {
      return null;
    }
  }

  public boolean hasPageTitle(XWikiContext context) {
    BaseObject ptPropObj = getPageTypeProperties(context);
    return ((ptPropObj != null) && (ptPropObj.getIntValue("haspagetitle") == 1));
  }

  public boolean showFrame(XWikiContext context) {
    if (!"login".equals(context.getAction()) && (getPageTypeProperties(context) != null)
        && (getPageTypeProperties(context).getIntValue("show_frame") == 0)) {
      return false;
    }
    return true;
  }

  public List<String> getCategories(XWikiContext context) {
    if (!"".equals(getCategoryString(context))) {
      return Arrays.asList(getCategoryString(context).split(","));
    }
    return Collections.emptyList();
  }

  public String getCategoryString(XWikiContext context) {
    if (getPageTypeProperties(context) != null) {
      return getPageTypeProperties(context).getStringValue("category");
    }
    return "";
  }

  public String getRenderTemplate(String renderMode, XWikiContext context) throws XWikiException {
    String specView = getRenderTemplateForRenderMode(renderMode, context);
    return specView;
  }

  String getRenderTemplateForRenderMode(String renderMode, XWikiContext context)
      throws XWikiException {
    String specView = null;
    if (getPageTypeProperties(context) != null) {
      specView = getPageTypeProperties(context).getStringValue("page_" + renderMode);
      specView = resolveTemplatePath(specView, context);
    }
    return specView;
  }

  // TODO check where to move to. RenderCommand or PageTypeTemplateResolver?
  public String resolveTemplatePath(String specView, XWikiContext context) {
    // TODO replace implementation with WebUtils getInheritedTemplatedPath
    if ((specView != null) && (specView.trim().length() > 0) && !context.getWiki().exists(specView,
        context)) {
      if (!specView.startsWith("celements2web:") && context.getWiki().exists("celements2web:"
          + specView, context)) {
        specView = "celements2web:" + specView;
      } else {
        specView = ":" + specView.replaceAll("celements2web:", "");
      }
    }
    return specView;
  }

  public String getPrettyName(XWikiContext context) {
    if (getPageTypeProperties(context) != null) {
      return getPageTypeProperties(context).getStringValue("type_name");
    }
    return "";
  }

  ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
