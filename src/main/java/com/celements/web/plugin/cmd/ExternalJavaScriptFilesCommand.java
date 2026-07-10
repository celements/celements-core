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
package com.celements.web.plugin.cmd;

import static com.celements.javascript.JsLoadMode.*;
import static com.google.common.base.Preconditions.*;
import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.FrontendResourceResolver;
import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.javascript.JsFileEntry;
import com.celements.javascript.JsIsRteContent;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.objects.BaseObject;

import one.util.streamex.StreamEx;

@NotThreadSafe
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalJavaScriptFilesCommand {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ExternalJavaScriptFilesCommand.class);

  private final LayoutServiceRole layoutService;
  private final IPageTypeResolverRole pageTypeResolver;
  private final IModelAccessFacade modelAccess;
  private final ModelContext modelContext;
  private final XObjectPageTypeUtilsRole objectPageTypeUtils;
  private final IWebUtilsService webUtilsService;
  private final AttachmentURLCommand attUrlCommand;
  private final FrontendResourceResolver frontendResolver;
  private final CssCommand cssCommand;
  private final BeanClassDefConverter<BaseObject, JsFileEntry> jsFileEntryConverter;

  private final Set<JsFileEntry> extJSfileSet = new LinkedHashSet<>();
  private final Set<String> extJSAttUrlSet = new LinkedHashSet<>();
  private final Set<String> extJSnotFoundSet = new LinkedHashSet<>();
  private boolean displayedAll = false;
  private boolean collectedAll = false;

  @Inject
  public ExternalJavaScriptFilesCommand(
      LayoutServiceRole layoutService,
      IPageTypeResolverRole pageTypeResolver,
      IModelAccessFacade modelAccess,
      ModelContext modelContext,
      XObjectPageTypeUtilsRole objectPageTypeUtils,
      IWebUtilsService webUtilsService,
      AttachmentURLCommand attUrlCommand,
      CssCommand cssCommand,
      FrontendResourceResolver frontendResolver,
      @Named(XObjectBeanConverter.NAME) BeanClassDefConverter<BaseObject, JsFileEntry> converter,
      @Named(JavaScriptExternalFilesClass.CLASS_DEF_HINT) ClassDefinition jsExtClassDef) {
    this.attUrlCommand = attUrlCommand;
    this.frontendResolver = frontendResolver;
    this.cssCommand = cssCommand;
    this.jsFileEntryConverter = converter;
    this.layoutService = layoutService;
    this.pageTypeResolver = pageTypeResolver;
    this.modelAccess = modelAccess;
    this.modelContext = modelContext;
    this.objectPageTypeUtils = objectPageTypeUtils;
    this.webUtilsService = webUtilsService;
    converter.initialize(jsExtClassDef);
    converter.initialize(new ReflectiveInstanceSupplier<>(JsFileEntry.class));
  }

  public Stream<String> streamExtJsFiles() {
    return extJSAttUrlSet.stream();
  }

  /**
   * @deprecated since 5.4 instead use {@link getLazySpanTag(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@NotEmpty String jsFile) {
    return getLazyLoadTag(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .setLazyLoad(true)
        .build());
  }

  /**
   * @deprecated since 5.4 instead use {@link getLazySpanTag(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@NotEmpty String jsFile, @Nullable String action) {
    return getLazyLoadTag(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .setAction(action)
        .setLazyLoad(true)
        .build());
  }

  /**
   * @deprecated since 5.4 instead use {@link getLazySpanTag(ExtJsFileParameter)}
   */
  @Deprecated
  public String addLazyExtJSfile(@NotEmpty String jsFile, @Nullable String action,
      @Nullable String params) {
    return getLazyLoadTag(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .setAction(action)
        .setQueryString(params)
        .setLazyLoad(true)
        .build());
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(@NotEmpty String jsFile) {
    return addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .build());
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(@NotEmpty String jsFile, @Nullable String action) {
    return addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .setAction(action)
        .build());
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(@NotEmpty String jsFile, @Nullable String action,
      @Nullable String params) {
    return addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(jsFile)
        .setAction(action)
        .setQueryString(params)
        .build());
  }

  /**
   * @param extJsFileParams
   * @return span-tag or script-tag (once) depending on lazyLoad parameter
   */
  @NotNull
  public String includeExtJsFile(@NotNull ExtJsFileParameter extJsFileParams) {
    LOGGER.info("includeExtJsFile {}", extJsFileParams);
    if (extJsFileParams.isLazyLoad()) {
      return getLazyLoadTag(extJsFileParams);
    } else {
      return addExtJSfileOnce(extJsFileParams);
    }
  }

  @NotEmpty
  public String getLazyLoadTag(@NotNull ExtJsFileParameter extJsFileParams) {
    return "<cel-lazy-load-js src=\"" + generateUrl(extJsFileParams).orElse("")
        + "\" loadMode=\"" + extJsFileParams.getLoadMode() + "\"></cel-lazy-load-js>";
  }

  @NotNull
  private Optional<String> generateUrl(@NotNull ExtJsFileParameter extJsFileParams) {
    return attUrlCommand.getAttachmentURL(
        extJsFileParams.getJsFile(),
        extJsFileParams.getAction().orElse(null),
        extJsFileParams.getQueryString().orElse(null))
        .map(UriComponents::toUriString);
  }

  @NotNull
  public String addExtJSfileOnce(@NotNull ExtJsFileParameter extJsFileParams) {
    if (!extJSAttUrlSet.contains(extJsFileParams.getJsFile())) {
      if (attUrlCommand.isAttachmentLink(extJsFileParams.getJsFile())
          || attUrlCommand.isOnDiskLink(extJsFileParams.getJsFile())) {
        extJSAttUrlSet.add(extJsFileParams.getJsFile());
        includeFrontendCss(extJsFileParams.getJsFile());
      }
      return generateScriptTagOnce(extJsFileParams, generateUrl(extJsFileParams).orElse(null));
    } else {
      LOGGER.debug("addExtJSfileOnce: skip already added {}", extJsFileParams.getJsFile());
    }
    return "";
  }

  @NotNull
  private String generateScriptTagOnce(@NotNull ExtJsFileParameter extJsFileParams,
      @Nullable String jsFileUrl) {
    LOGGER.info("generateScriptTagOnce: extJsFileParams [{}] jsFileUrl [{}]", extJsFileParams,
        jsFileUrl);
    String jsIncludes2 = "";
    JsFileEntry jsFileEntry = extJsFileParams.getJsFileEntry();
    if (jsFileUrl == null) {
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        extJSnotFoundSet.add(jsFileEntry.getFilepath());
        jsIncludes2 = buildNotFoundWarning(jsFileEntry.getFilepath());
      } else {
        LOGGER.debug("generateScriptTagOnce jsFileUrl == null: skip already seen {}", jsFileEntry);
      }
    } else {
      jsFileEntry.setFilepath(jsFileUrl);
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        jsIncludes2 = getExtStringForJsFile(jsFileEntry);
        extJSfileSet.add(jsFileEntry);
      } else {
        LOGGER.debug("generateScriptTagOnce jsFileUrl != null: skip already seen {}", jsFileEntry);
      }
    }
    if (!displayedAll) {
      jsIncludes2 = "";
    } else if (!jsIncludes2.isEmpty()) {
      jsIncludes2 = resolveCssIncludes(extJsFileParams.getJsFile()) + jsIncludes2;
    }
    return jsIncludes2;
  }

  private String resolveCssIncludes(String jsFile) {
    return frontendResolver.get(jsFile.trim()).stream()
        .flatMap(resource -> resource.cssPaths().stream())
        .map(attUrlCommand::getDiskFileUrl)
        .map(this::getCssLink)
        .collect(joining());
  }

  private void includeFrontendCss(String jsFile) {
    if (frontendResolver.isFrontendSource(jsFile.trim())) {
      cssCommand.includeCSSPage(jsFile.trim());
    }
  }

  private String getCssLink(String cssUrl) {
    return "<link rel=\"stylesheet\" title=\"\" media=\"all\" type=\"text/css\" href=\""
        + StringEscapeUtils.escapeHtml(cssUrl) + "\" />\n";
  }

  private String buildNotFoundWarning(String jsFile) {
    return "<!-- WARNING: js-file not found: " + jsFile + "-->";
  }

  private boolean jsFileHasBeenSeen(JsFileEntry jsFile) {
    return extJSfileSet.contains(jsFile) || extJSnotFoundSet.contains(jsFile.getFilepath());
  }

  void injectDisplayAll(boolean displayedAll) {
    this.displayedAll = displayedAll;
  }

  String getExtStringForJsFile(JsFileEntry jsFile) {
    var loadMode = Optional.ofNullable(jsFile.getLoadMode())
        .filter(mode -> (mode == ASYNC) || ((mode == DEFER) && !jsFile.isModule()));
    return "<script "
        + loadMode.map(mode -> mode.toString().toLowerCase() + " ").orElse("")
        + "type=\"" + (jsFile.isModule() ? "module" : "text/javascript")
        + "\" src=\"" + StringEscapeUtils.escapeHtml(jsFile.getFilepath())
        + "\"></script>";
  }

  public List<JsFileEntry> getAllRteContentJsFiles() {
    return getExtJsFileStream()
        .filter(fs -> fs.isRteContent() != JsIsRteContent.NO)
        .collect(Collectors.toList());
  }

  public String getAllExternalJavaScriptFiles() {
    getExtJsFileStream();
    notifyExtJavaScriptFileListener();
    final StringBuilder jsIncludesBuilder = generateJsImportString();
    displayedAll = true;
    return jsIncludesBuilder.toString();
  }

  private Stream<JsFileEntry> getExtJsFileStream() {
    ensureCollectAllJsExtFile();
    return extJSfileSet.stream();
  }

  private void ensureCollectAllJsExtFile() {
    if (!collectedAll) {
      streamDocRefs2CollectJsExtFileObj().forEachOrdered(this::addAllExtJSfilesFromDocRef);
      collectedAll = true;
    }
  }

  private StringBuilder generateJsImportString() {
    final StringBuilder jsIncludesBuilder = new StringBuilder();
    StreamEx.of(getExtJsFileStream()
        .filter(fs -> fs.isRteContent() != JsIsRteContent.ONLY)
        .map(this::getExtStringForJsFile))
        .append(extJSnotFoundSet.stream().map(this::buildNotFoundWarning))
        .forEach(tag -> jsIncludesBuilder.append(tag).append("\n"));
    return jsIncludesBuilder;
  }

  private Stream<DocumentReference> streamDocRefs2CollectJsExtFileObj() {
    return StreamEx.of(getSkinDocRef())
        .append(getXWikiPreferencesDocRef())
        .append(getCurrentSpacePreferencesDocRef())
        .append(getCurrentPageTypeDocRef())
        .append(getLayoutPropDocRef())
        .append(getCurrentDocRef());
  }

  private Stream<DocumentReference> getCurrentDocRef() {
    return StreamEx.of(modelContext.getCurrentDocRef().toJavaUtil());
  }

  private @NotNull Stream<DocumentReference> getLayoutPropDocRef() {
    return StreamEx.of(layoutService.getLayoutPropDocRefForCurrentDoc());
  }

  private @NotNull DocumentReference getCurrentPageTypeDocRef() {
    return objectPageTypeUtils.getDocRefForPageType(
        pageTypeResolver.resolvePageTypeRefForCurrentDoc());
  }

  private Stream<DocumentReference> getCurrentSpacePreferencesDocRef() {
    return StreamEx.of(modelContext.getCurrentSpaceRef().toJavaUtil()
        .map(spaceRef -> RefBuilder.from(spaceRef).doc("WebPreferences").build(
            DocumentReference.class)));
  }

  private @NotNull DocumentReference getXWikiPreferencesDocRef() {
    return RefBuilder.from(modelContext.getWikiRef()).space("XWiki")
        .doc("XWikiPreferences").build(DocumentReference.class);
  }

  private Stream<DocumentReference> getSkinDocRef() {
    return StreamEx.of(Optional.ofNullable(modelContext.getXWikiContext())
        .map(xcontext -> (VelocityContext) xcontext.get("vcontext"))
        .filter(vcontext -> vcontext.containsKey("skin_doc"))
        .map(vcontext -> ((Document) vcontext.get("skin_doc")).getDocumentReference()));
  }

  private void notifyExtJavaScriptFileListener() {
    Map<String, IExtJSFilesListener> listenerMap = getListenerMap();
    for (IExtJSFilesListener jsfListener : listenerMap.values()) {
      jsfListener.beforeAllExtFinish(this);
    }
  }

  private Map<String, IExtJSFilesListener> getListenerMap() {
    try {
      return webUtilsService.lookupMap(IExtJSFilesListener.class);
    } catch (ComponentLookupException exp) {
      LOGGER.error("Failed to get IExtJSFilesListener components.", exp);
    }
    return Collections.emptyMap();
  }

  void addAllExtJSfilesFromDocRef(@NotNull DocumentReference docRef) {
    checkNotNull(docRef);
    try {
      XWikiObjectFetcher.on(modelAccess.getDocument(docRef))
          .filter(JavaScriptExternalFilesClass.CLASS_REF)
          .stream()
          .map(jsFileEntryConverter)
          .filter(JsFileEntry::isValid)
          .forEachOrdered(jsFile -> addExtJSfileOnce(
              new ExtJsFileParameter.Builder()
                  .setJsFileEntry(jsFile)
                  .build()));
    } catch (DocumentNotExistsException nExExp) {
      LOGGER.info("addAllExtJSfilesFromDocRef skipping [{}] because: not exist.", docRef);
    }
  }
}
