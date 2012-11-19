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
package com.celements.rendering;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class RenderCommand {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(RenderCommand.class);

  private PageTypeReference defaultPageTypeRef = null;

  private XWikiRenderingEngine renderingEngine;

  IPageTypeRole injectedPageTypeService;

  IPageTypeResolverRole injectedPageTypeResolver;

  private static XWikiRenderingEngine defaultRenderingEngine;

  /**
   * @Deprecated since 2.18.0 instead use new RenderCommand() 
   */
  @Deprecated
  public RenderCommand(XWikiContext context) {}
  
  public RenderCommand() {}

  private XWikiContext getContext() {
    return (XWikiContext)getExecutionContext().getProperty("xwikicontext");
  }

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  /**
   * @deprecated since 2.21.0 instead use setDefaultPageTypeReference
   */
  @Deprecated
  public void setDefaultPageType(String defaultPageType) {
    this.defaultPageTypeRef = getPageTypeService().getPageTypeRefByConfigName(
        defaultPageType);
  }

  public void setDefaultPageTypeReference(PageTypeReference defaultPageTypeRef) {
    this.defaultPageTypeRef = defaultPageTypeRef;
  }

  public String renderCelementsCell(DocumentReference elemDocRef) throws XWikiException {
    XWikiDocument cellDoc = getTemplateDoc(elemDocRef);
    return renderCelementsDocument(cellDoc, "view");
  }

  /**
   * renderCelementsCell
   * 
   * @param elementFullName
   * @return
   * @throws XWikiException
   * 
   * @Deprecated since 2.17.0 instead use renderCelementsCell(DocumentReference) 
   */
  @Deprecated
  public String renderCelementsCell(String elementFullName) throws XWikiException {
    XWikiDocument cellDoc = getTemplateDoc(getWebUtilsService().resolveDocumentReference(
        elementFullName));
    return renderCelementsDocument(cellDoc, "view");
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String renderMode
      ) throws XWikiException {
      return renderCelementsDocument(elemDocRef, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String lang,
      String renderMode) throws XWikiException {
    XWikiDocument cellDoc = getContext().getWiki().getDocument(elemDocRef, getContext());
    return renderCelementsDocument(cellDoc, lang, renderMode);
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String renderMode
      ) throws XWikiException {
    return renderCelementsDocument(cellDoc, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String lang,
      String renderMode) throws XWikiException {
    LOGGER.trace("renderCelementsDocument: cellDoc [" + cellDoc.getDocumentReference()
        + "] lang [" + lang + "] renderMode [" + renderMode + "].");
    String cellDocFN = getRefSerializer().serialize(cellDoc.getDocumentReference());
    if (getContext().getWiki().getRightService().hasAccessLevel(renderMode,
        getContext().getUser(), cellDocFN, getContext())) {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      vcontext.put("celldoc", cellDoc.newDocument(getContext()));
      PageTypeReference cellTypeRef = getPageTypeResolver(
          ).getPageTypeRefForDocWithDefault(cellDoc, defaultPageTypeRef);
      IPageTypeConfig cellType = null;
      if (cellTypeRef != null) {
        cellType = getPageTypeService().getPageTypeConfigForPageTypeRef(cellTypeRef);
      }
      String renderTemplatePath = getRenderTemplatePath(cellType, cellDocFN,
          renderMode);
      String templateContent;
      XWikiDocument templateDoc = getContext().getDoc();
      if (renderTemplatePath.startsWith(":")) {
        String templatePath = getTemplatePathOnDisk(renderTemplatePath);
        try {
          templateContent = getContext().getWiki().getResourceContent(templatePath);
        } catch (IOException exp) {
          LOGGER.debug("Exception while parsing template [" + templatePath + "].", exp);
          return "";
        }
      } else {
        DocumentReference renderTemplateDocRef = getWebUtilsService(
            ).resolveDocumentReference(renderTemplatePath);
        templateDoc = getTemplateDoc(renderTemplateDocRef);
        templateContent = getTranslatedContent(templateDoc, lang);
      }
      return getRenderingEngine().renderText(templateContent,
          templateDoc, getContext().getDoc(), getContext());
    } else {
      return "";
    }
  }

  public String renderDocument(DocumentReference docRef) {
    return renderDocument(docRef, getContext().getLanguage());
  }

  public String renderDocument(DocumentReference docRef, String lang) {
    LOGGER.debug("renderDocument for lang  [" + lang + "] and docref [" + docRef + "].");
    try {
      XWikiDocument xwikidoc = getContext().getWiki().getDocument(docRef, getContext());
      return renderDocument(xwikidoc, lang);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get translated document for [" + docRef + "] in [" + lang
          + "].", exp);
    }
    return "";
  }

  public String renderDocument(XWikiDocument document) throws XWikiException {
    return renderDocument(document, document.getLanguage());
  }

  public String renderDocument(XWikiDocument document, String lang
      ) throws XWikiException {
    LOGGER.debug("renderDocument for lang  [" + lang + "] and doc ["
        + document.getDocumentReference() + "].");
    return getRenderingEngine().renderText(getTranslatedContent(document, lang), document,
        getContext());
  }

  XWikiRenderingEngine getRenderingEngine() throws XWikiException {
    if (this.renderingEngine == null) {
      this.renderingEngine = getDefaultRenderingEngine(getContext());
    }
    return this.renderingEngine;
  }

  static XWikiRenderingEngine getDefaultRenderingEngine(XWikiContext context
      ) throws XWikiException {
    if (defaultRenderingEngine == null) {
      defaultRenderingEngine = initRenderingEngine(Arrays.asList("velocity", "groovy"),
          context);
    }
    return defaultRenderingEngine;
  }

  static XWikiRenderingEngine initRenderingEngine(List<String> rendererNames,
      XWikiContext context) throws XWikiException {
    return new DefaultCelementsRenderingEngine(rendererNames, context);
  }

  public XWikiRenderingEngine initRenderingEngine(List<String> rendererNames
      ) throws XWikiException {
    return RenderCommand.initRenderingEngine(rendererNames, getContext());
  }

  public void setRenderingEngine(XWikiRenderingEngine renderingEngine) {
      this.renderingEngine = renderingEngine;
  }

  String getTranslatedContent(XWikiDocument templateDoc, String lang
      ) throws XWikiException {
    LOGGER.debug("getTranslatedContent for lang  [" + lang + "] and templateDoc ["
        + templateDoc.getDocumentReference() + "].");
    String translatedContent = templateDoc.getTranslatedContent(lang, getContext());
    if (!getRenderingEngine().getRendererNames().contains("xwiki")) {
      return translatedContent.replaceAll("\\{pre\\}|\\{/pre\\}", "");
    } else {
      return translatedContent;
    }
  }

  XWikiDocument getTemplateDoc(DocumentReference templateDocRef) throws XWikiException {
    return getContext().getWiki().getDocument(templateDocRef, getContext());
  }

  String getTemplatePathOnDisk(String renderTemplatePath) {
    return renderTemplatePath.replaceAll("^:(Templates\\.)?", "/templates/celTemplates/"
        ) + ".vm";
  }

  String getRenderTemplatePath(IPageTypeConfig cellType, String cellDocFN,
      String renderMode) throws XWikiException {
    LOGGER.trace("getRenderTemplatePath: for cellDoc [" + cellDocFN + "] with cellType ["
        + (cellType != null ? cellType.getName() : "null") + "] and renderMode ["
        + renderMode + "].");
    if (cellType != null) {
      String renderTemplateFullName = cellType.getRenderTemplateForRenderMode(renderMode);
      if ((renderTemplateFullName != null) && !"".equals(renderTemplateFullName)) {
        LOGGER.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
            + cellType.getName() + "] and renderTemplate ["
            + renderTemplateFullName + "].");
        return renderTemplateFullName;
      }
      LOGGER.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
          + cellType.getName() + "] using content of cellDoc [" + cellDocFN + "].");
    }
    return cellDocFN;
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  IPageTypeResolverRole getPageTypeResolver() {
    if (injectedPageTypeResolver != null) {
      return injectedPageTypeResolver;
    }
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  IPageTypeRole getPageTypeService() {
    if (injectedPageTypeService != null) {
      return injectedPageTypeService;
    }
    return Utils.getComponent(IPageTypeRole.class);
  }

  private DefaultStringEntityReferenceSerializer getRefSerializer() {
    return (DefaultStringEntityReferenceSerializer) Utils.getComponent(
        EntityReferenceSerializer.class);
  }

}
