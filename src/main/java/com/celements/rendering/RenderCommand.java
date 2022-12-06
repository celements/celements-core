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

import static com.celements.cells.CellRenderStrategy.*;
import static com.celements.common.MoreObjectsCel.*;
import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.google.common.base.Predicates.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.Contextualiser;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.classes.KeyValueClass;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class RenderCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderCommand.class);

  private PageTypeReference defaultPageTypeRef = null;

  private XWikiRenderingEngine renderingEngine;

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
    XWikiDocument cellDoc = getModelAccess().getOrCreateDocument(elemDocRef);
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
    XWikiDocument cellDoc = getModelAccess().getOrCreateDocument(getModelUtils()
        .resolveRef(elementFullName, DocumentReference.class));
    return renderCelementsDocument(cellDoc, "view");
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String renderMode)
      throws XWikiException {
    return renderCelementsDocument(elemDocRef, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocumentPreserveVelocityContext(DocumentReference elementDocRef,
      String lang, String renderMode) throws XWikiException {
    VelocityContext vContext = (VelocityContext) getVeloCtx().clone();
    return new Contextualiser()
        .withXWikiContext("vcontext", vContext)
        .withExecContext("velocityContext", vContext)
        .execute(rethrow(() -> renderCelementsDocument(elementDocRef, lang, renderMode)));
  }

  public String renderCelementsDocument(DocumentReference elemDocRef, String lang,
      String renderMode) throws XWikiException {
    XWikiDocument cellDoc = getModelAccess().getOrCreateDocument(elemDocRef);
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
      String template = getRenderTemplatePath(cellDoc, renderMode);
      return new Contextualiser()
          .withVeloContext("celldoc", cellDoc.newDocument(getContext()))
          .execute(rethrow(() -> renderTemplatePath(cellDoc, template, lang, "")));
    } else {
      if ((getContext() == null) || (getContext().get("vcontext") == null)) {
        LOGGER.error("Failed to renderCelementsDocument '{}', because velocity context "
            + " or context is null.", cellDoc.getDocumentReference());
      }
      return "";
    }
  }

  public String renderTemplatePath(String renderTemplatePath, String lang, String defLang)
      throws XWikiException {
    return renderTemplatePath(getContext().getDoc(), renderTemplatePath, lang, defLang);
  }

  public String renderTemplatePath(XWikiDocument cellDoc, String renderTemplatePath,
      String lang, String defLang) throws XWikiException {
    String renderedContent = "";
    String templateContent;
    Optional<XWikiDocument> templateDoc = getTemplateDoc(renderTemplatePath);
    if (!templateDoc.isPresent()) {
      templateContent = getWebUtilsService().getTranslatedDiscTemplateContent(renderTemplatePath,
          lang, defLang);
    } else {
      templateContent = getTranslatedContent(templateDoc.get(), lang);
    }
    if (!StringUtils.isEmpty(templateContent)) {
      renderedContent = getCellContextualiser(cellDoc).execute(rethrow(() -> getRenderingEngine()
          .renderText(templateContent, templateDoc.orElse(getContext().getDoc()),
              getContext().getDoc(), getContext())));
    } else {
      LOGGER.info("renderTemplatePath: skip rendering, empty template [{}]", renderTemplatePath);
    }
    return renderedContent;
  }

  private Optional<XWikiDocument> getTemplateDoc(String renderTemplatePath) {
    if (!renderTemplatePath.startsWith(":")) {
      return getModelAccess().getDocumentOpt(getModelUtils().resolveRef(
          renderTemplatePath, DocumentReference.class));
    }
    return Optional.empty();
  }

  private Contextualiser getCellContextualiser(XWikiDocument cellDoc) {
    Contextualiser contextualiser = new Contextualiser();
    Optional<String> scopeKey = getRenderScopeKey(cellDoc);
    LOGGER.debug("getCellContextualiser: cell [{}], scope [{}]", cellDoc, scopeKey);
    scopeKey.map(key -> key + EXEC_CTX_KEY_DOC_SUFFIX)
        .map(getExecutionContext()::getProperty)
        .flatMap(doc -> tryCast(doc, XWikiDocument.class))
        .ifPresent(contextualiser::withDoc);
    scopeKey.map(key -> key + EXEC_CTX_KEY_OBJ_NB_SUFFIX)
        .map(getExecutionContext()::getProperty)
        .ifPresent(nb -> contextualiser.withExecContext(EXEC_CTX_KEY_OBJ_NB, nb));
    return contextualiser;
  }

  private Optional<String> getRenderScopeKey(XWikiDocument cellDoc) {
    return Optional.ofNullable(cellDoc)
        .flatMap(doc -> XWikiObjectFetcher.on(doc)
            .filter(KeyValueClass.FIELD_KEY, "cell-render-scope")
            .fetchField(KeyValueClass.FIELD_VALUE)
            .stream().findFirst())
        .map(scope -> EXEC_CTX_KEY + "." + scope);
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
      XWikiDocument xwikidoc = getModelAccess().getOrCreateDocument(docRef);
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
      XWikiDocument contentdoc = getModelAccess().getOrCreateDocument(docRef);
      XWikiDocument includeDoc = getModelAccess().getOrCreateDocument(includeDocRef);
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

  String getRenderTemplatePath(XWikiDocument cellDoc, String renderMode) {
    String cellDocFN = getModelUtils().serializeRef(cellDoc.getDocumentReference());
    return Optional.ofNullable(getPageTypeResolver()
        .resolvePageTypeReference(cellDoc).toJavaUtil()
        .orElse(defaultPageTypeRef))
        .map(getPageTypeService()::getPageTypeConfigForPageTypeRef)
        .map(cellType -> cellType.getRenderTemplateForRenderMode(renderMode))
        .filter(not(String::isEmpty))
        .orElse(cellDocFN);
  }

  private VelocityContext getVeloCtx() {
    return (VelocityContext) getContext().get("vcontext");
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  IPageTypeResolverRole getPageTypeResolver() {
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  IPageTypeRole getPageTypeService() {
    return Utils.getComponent(IPageTypeRole.class);
  }

}
