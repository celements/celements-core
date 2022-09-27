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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.ModelUtils;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class RenderCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderCommand.class);

  private PageTypeReference defaultPageTypeRef = null;

  private XWikiRenderingEngine renderingEngine;

  IPageTypeRole injectedPageTypeService;

  IPageTypeResolverRole injectedPageTypeResolver;

  private volatile static XWikiRenderingEngine defaultRenderingEngine;

  /**
   * @Deprecated since 2.18.0 instead use new RenderCommand()
   */
  @Deprecated
  public RenderCommand(XWikiContext context) {}

  public RenderCommand() {}

  private XWikiContext getContext() {
    return (XWikiContext) getExecutionContext().getProperty("xwikicontext");
  }

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  /**
   * @deprecated since 2.21.0 instead use setDefaultPageTypeReference
   */
  @Deprecated
  public void setDefaultPageType(String defaultPageType) {
    this.defaultPageTypeRef = getPageTypeService().getPageTypeRefByConfigName(defaultPageType);
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
   * @Deprecated since 2.17.0 instead use renderCelementsCell(DocumentReference)
   */
  @Deprecated
  public String renderCelementsCell(String elementFullName) throws XWikiException {
    XWikiDocument cellDoc = getTemplateDoc(getWebUtilsService().resolveDocumentReference(
        elementFullName));
    return renderCelementsDocument(cellDoc, "view");
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String renderMode)
      throws XWikiException {
    return renderCelementsDocument(elemDocRef, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocumentPreserveVelocityContext(DocumentReference elementDocRef,
      String lang, String renderMode) throws XWikiException {
    VelocityContext preservedVcontext = (VelocityContext) getExecutionContext().getProperty(
        "velocityContext");
    VelocityContext vContext = (VelocityContext) preservedVcontext.clone();
    getContext().put("vcontext", vContext);
    getExecutionContext().setProperty("velocityContext", vContext);
    try {
      return renderCelementsDocument(elementDocRef, lang, renderMode);
    } finally {
      getContext().put("vcontext", preservedVcontext);
      getExecutionContext().setProperty("velocityContext", preservedVcontext);
    }
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String lang,
      String renderMode) throws XWikiException {
    XWikiDocument cellDoc = getContext().getWiki().getDocument(elemDocRef, getContext());
    return renderCelementsDocument(cellDoc, lang, renderMode);
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String renderMode)
      throws XWikiException {
    return renderCelementsDocument(cellDoc, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String lang, String renderMode)
      throws XWikiException {
    LOGGER.debug("renderCelementsDocument: cellDoc [{}] lang [{}] renderMode [{}].",
        cellDoc.getDocumentReference(), lang, renderMode);
    String cellDocFN = getModelUtils().serializeRef(
        cellDoc.getDocumentReference());
    if ((getContext() != null) && (getContext().get("vcontext") != null)
        && getContext().getWiki().getRightService().hasAccessLevel(renderMode,
            getContext().getUser(), cellDocFN, getContext())) {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      vcontext.put("celldoc", cellDoc.newDocument(getContext()));
      PageTypeReference cellTypeRef = getPageTypeResolver()
          .resolvePageTypeReference(cellDoc).toJavaUtil()
          .orElse(defaultPageTypeRef);
      IPageTypeConfig cellType = null;
      if (cellTypeRef != null) {
        cellType = getPageTypeService().getPageTypeConfigForPageTypeRef(cellTypeRef);
      }
      return renderTemplatePath(getRenderTemplatePath(cellType, cellDocFN, renderMode), lang);
    } else {
      if ((getContext() == null) || (getContext().get("vcontext") == null)) {
        LOGGER.error("Failed to renderCelementsDocument '{}', because velocity context "
            + " or context is null.", cellDoc.getDocumentReference());
      }
      return "";
    }
  }

  public String renderTemplatePath(String renderTemplatePath, String lang) throws XWikiException {
    return renderTemplatePath(renderTemplatePath, lang, null);
  }

  public String renderTemplatePath(String renderTemplatePath, String lang, String defLang)
      throws XWikiException {
    String templateContent;
    XWikiDocument templateDoc = getContext().getDoc();
    if (renderTemplatePath.startsWith(":")) {
      templateContent = getWebUtilsService().getTranslatedDiscTemplateContent(renderTemplatePath,
          lang, defLang);
    } else {
      DocumentReference renderTemplateDocRef = getModelUtils().resolveRef(renderTemplatePath,
          DocumentReference.class);
      templateDoc = getTemplateDoc(renderTemplateDocRef);
      templateContent = getTranslatedContent(templateDoc, lang);
    }
    if (!StringUtils.isEmpty(templateContent)) {
      if (LOGGER.isDebugEnabled()) {
        VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
        Document cellDoc = (Document) vcontext.get("celldoc");
        LOGGER.debug("renderTemplatePath: cellDoc before [{}].", cellDoc);
      }
      LOGGER.trace("renderTemplatePath: template content for lang [{}] and context.language [{}]"
          + " is [{}", lang, getContext().getLanguage(), templateContent);
      String renderedContent = getRenderingEngine().renderText(templateContent, templateDoc,
          getContext().getDoc(), getContext());
      LOGGER.trace("renderTemplatePath: rendered content for lang [{}] and context.language [{}]"
          + " is [{}]", lang, getContext().getLanguage(), renderedContent);
      if (LOGGER.isDebugEnabled()) {
        VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
        Document cellDoc = (Document) vcontext.get("celldoc");
        LOGGER.debug("renderTemplatePath: cellDoc after [{}].", cellDoc);
      }
      return renderedContent;
    } else {
      LOGGER.info("renderTemplatePath: skip rendering, because empty Template [{}].",
          renderTemplatePath);
    }
    return "";
  }

  public String renderDocument(DocumentReference docRef) {
    return renderDocument(docRef, getContext().getLanguage());
  }

  public String renderDocument(DocumentReference docRef, DocumentReference includeDocRef) {
    return renderDocument(docRef, includeDocRef, getContext().getLanguage());
  }

  public String renderDocument(DocumentReference docRef, String lang) {
    LOGGER.debug("renderDocument for lang  [{}] and docref [{}].", lang, docRef);
    try {
      XWikiDocument xwikidoc = getContext().getWiki().getDocument(docRef, getContext());
      return renderDocument(xwikidoc, null, lang);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get translated document for [" + docRef + "] in [" + lang + "].",
          exp);
    }
    return "";
  }

  public String renderDocument(DocumentReference docRef, DocumentReference includeDocRef,
      String lang) {
    LOGGER.debug("renderDocument for lang  [{}] and docref [{}] and includeDocRef [{}].", lang,
        docRef, includeDocRef);
    try {
      XWikiDocument contentdoc = getContext().getWiki().getDocument(docRef, getContext());
      XWikiDocument includeDoc = getContext().getWiki().getDocument(includeDocRef, getContext());
      return renderDocument(contentdoc, includeDoc, lang);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get translated document for [{}] or includeDoc [{}].", docRef,
          includeDocRef, exp);
    }
    return "";
  }

  public String renderDocument(XWikiDocument document) throws XWikiException {
    return renderDocument(document, null, document.getLanguage());
  }

  /**
   * renderDocument renders the given document content for the requested language. The
   * includingdoc will be $doc in the velocity context during rendering.
   */
  public String renderDocument(XWikiDocument document, XWikiDocument includingdoc, String lang)
      throws XWikiException {
    LOGGER.debug("renderDocument for lang  [" + lang + "] and doc ["
        + document.getDocumentReference() + "].");
    String translatedContent = getTranslatedContent(document, lang);
    LOGGER.trace("translated content for lang [" + lang + "] and context.language ["
        + getContext().getLanguage() + "] is [" + translatedContent + "]");
    if (includingdoc == null) {
      includingdoc = getContext().getDoc();
    }
    String renderedContent = getRenderingEngine().renderText(translatedContent, document,
        includingdoc, getContext());
    LOGGER.trace("rendered content for lang [" + lang + "] and context.language ["
        + getContext().getLanguage() + "] is [" + renderedContent + "]");
    return renderedContent;
  }

  XWikiRenderingEngine getRenderingEngine() throws XWikiException {
    if (this.renderingEngine == null) {
      this.renderingEngine = getDefaultRenderingEngine(getContext());
    }
    return this.renderingEngine;
  }

  synchronized static XWikiRenderingEngine getDefaultRenderingEngine(XWikiContext context)
      throws XWikiException {
    if (defaultRenderingEngine == null) {
      defaultRenderingEngine = initRenderingEngine(Arrays.asList("velocity", "groovy"), context);
    }
    return defaultRenderingEngine;
  }

  static XWikiRenderingEngine initRenderingEngine(List<String> rendererNames, XWikiContext context)
      throws XWikiException {
    return new DefaultCelementsRenderingEngine(rendererNames, context);
  }

  public XWikiRenderingEngine initRenderingEngine(List<String> rendererNames)
      throws XWikiException {
    return RenderCommand.initRenderingEngine(rendererNames, getContext());
  }

  public void setRenderingEngine(XWikiRenderingEngine renderingEngine) {
    this.renderingEngine = renderingEngine;
  }

  String getTranslatedContent(XWikiDocument templateDoc, String lang) throws XWikiException {
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
    return getTemplatePathOnDisk(renderTemplatePath, null);
  }

  String getTemplatePathOnDisk(String renderTemplatePath, String lang) {
    String templateOnDiskPath = getWebUtilsService().getTemplatePathOnDisk(renderTemplatePath,
        lang);
    if (templateOnDiskPath.startsWith(":")) {
      templateOnDiskPath = renderTemplatePath.replaceAll("^:", "/templates/celTemplates/") + ".vm";
    }
    return templateOnDiskPath;
  }

  String getRenderTemplatePath(IPageTypeConfig cellType, String cellDocFN, String renderMode)
      throws XWikiException {
    LOGGER.trace("getRenderTemplatePath: for cellDoc [" + cellDocFN + "] with cellType ["
        + (cellType != null ? cellType.getName() : "null") + "] and renderMode [" + renderMode
        + "].");
    if (cellType != null) {
      String renderTemplateFullName = cellType.getRenderTemplateForRenderMode(renderMode);
      if ((renderTemplateFullName != null) && !"".equals(renderTemplateFullName)) {
        LOGGER.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
            + cellType.getName() + "] and renderTemplate [" + renderTemplateFullName + "].");
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

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
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

}
