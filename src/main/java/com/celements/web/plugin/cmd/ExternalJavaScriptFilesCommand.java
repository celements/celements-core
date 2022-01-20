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

import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.javascript.JsFileEntry;
import com.celements.javascript.JsLoadMode;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.sajson.JsonBuilder;
import com.celements.web.classes.CelementsClassDefinition;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Suppliers;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

import one.util.streamex.StreamEx;

@NotThreadSafe
public class ExternalJavaScriptFilesCommand {

  /**
   * @deprecated since 5.4 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC
   */
  @Deprecated
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  /**
   * @deprecated since 4.0 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE
   */
  @Deprecated
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  /**
   * @deprecated since 4.0 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS
   */
  @Deprecated
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS = JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE
      + "." + JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ExternalJavaScriptFilesCommand.class);
  private static final Supplier<BeanClassDefConverter<BaseObject, JsFileEntry>> JS_FILE_ENTRY_CONVERTER = Suppliers
      .memoize(ExternalJavaScriptFilesCommand::jsFileEntryConverter);

  private final Set<JsFileEntry> extJSfileSet = new LinkedHashSet<>();
  private final Set<String> extJSAttUrlSet = new HashSet<>();
  private final Set<String> extJSnotFoundSet = new LinkedHashSet<>();
  private boolean displayedAll = false;

  /**
   * @deprecated since 5.4 instead use {@link ExternalJavaScriptFilesCommand()}
   */
  @Deprecated
  public ExternalJavaScriptFilesCommand(XWikiContext context) {}

  public ExternalJavaScriptFilesCommand() {}

  private static BeanClassDefConverter<BaseObject, JsFileEntry> jsFileEntryConverter() {
    @SuppressWarnings("unchecked")
    BeanClassDefConverter<BaseObject, JsFileEntry> converter = Utils.getComponent(
        BeanClassDefConverter.class, XObjectBeanConverter.NAME);
    converter.initialize(Utils.getComponent(CelementsClassDefinition.class,
        JavaScriptExternalFilesClass.CLASS_DEF_HINT));
    converter.initialize(new ReflectiveInstanceSupplier<>(JsFileEntry.class));
    return converter;
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
    return getLazyLoadTag(extJsFileParams, null);
  }

  @NotEmpty
  String getLazyLoadTag(@NotNull ExtJsFileParameter extJsFileParams,
      @Nullable AttachmentURLCommand attUrlCmdMock) {
    final JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.openDictionary();
    jsonBuilder.addProperty("fullURL", generateUrl(extJsFileParams, attUrlCmdMock));
    jsonBuilder.addProperty("initLoad", true);
    jsonBuilder.closeDictionary();
    return "<span class='cel_lazyloadJS' style='display: none;'>" + jsonBuilder.getJSON()
        + "</span>";
  }

  @NotEmpty
  private String generateUrl(@NotNull ExtJsFileParameter extJsFileParams,
      @Nullable AttachmentURLCommand attUrlCmdMock) {
    String attUrl;
    final AttachmentURLCommand attUrlCmd = getAttUrlCmd(attUrlCmdMock);
    final Optional<String> action = extJsFileParams.getAction();
    if (action.isPresent()) {
      attUrl = attUrlCmd.getAttachmentURL(extJsFileParams.getJsFile(), action.get(),
          getModelContext().getXWikiContext());
    } else {
      attUrl = attUrlCmd.getAttachmentURL(extJsFileParams.getJsFile(),
          getModelContext().getXWikiContext());
    }
    final Optional<String> params = extJsFileParams.getQueryString();
    if (params.isPresent()) {
      if (attUrl.indexOf("?") > -1) {
        attUrl += "&" + params.get();
      } else {
        attUrl += "?" + params.get();
      }
    }
    return attUrl;
  }

  @NotNull
  public String addExtJSfileOnce(@NotNull ExtJsFileParameter extJsFileParams) {
    return addExtJSfileOnce(extJsFileParams, null);
  }

  @NotNull
  String addExtJSfileOnce(@NotNull ExtJsFileParameter extJsFileParams,
      @Nullable AttachmentURLCommand attUrlCmdMock) {
    if (!extJSAttUrlSet.contains(extJsFileParams.getJsFile())) {
      final AttachmentURLCommand attUrlCmd = getAttUrlCmd(attUrlCmdMock);
      if (attUrlCmd.isAttachmentLink(extJsFileParams.getJsFile())
          || attUrlCmd.isOnDiskLink(extJsFileParams.getJsFile())) {
        extJSAttUrlSet.add(extJsFileParams.getJsFile());
      }
      return generateScriptTagOnce(extJsFileParams, generateUrl(extJsFileParams, attUrlCmd));
    } else {
      LOGGER.debug("addExtJSfileOnce: skip already added {}", extJsFileParams.getJsFile());
    }
    return "";
  }

  @NotNull
  private String generateScriptTagOnce(@NotNull ExtJsFileParameter extJsFileParams,
      @NotEmpty String jsFileUrl) {
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
    }
    return jsIncludes2;
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
    return "<script" + ((jsFile.getLoadMode() != JsLoadMode.SYNC)
        ? " " + jsFile.getLoadMode().toString().toLowerCase()
        : "") + " type=\"text/javascript\" src=\""
        + StringEscapeUtils.escapeHtml(jsFile.getFilepath()) + "\"></script>";
  }

  public String getAllExternalJavaScriptFiles() {
    return getAllExternalJavaScriptFiles(null);
  }

  String getAllExternalJavaScriptFiles(@Nullable AttachmentURLCommand attUrlCmdMock) {
    streamDocRefs2CollectJsExtFileObj()
        .forEachOrdered(docRef -> addAllExtJSfilesFromDocRef(docRef, attUrlCmdMock));
    notifyExtJavaScriptFileListener();
    final StringBuilder jsIncludesBuilder = generateJsImportString();
    displayedAll = true;
    return jsIncludesBuilder.toString();
  }

  private StringBuilder generateJsImportString() {
    final StringBuilder jsIncludesBuilder = new StringBuilder();
    StreamEx.of(extJSfileSet.stream().map(this::getExtStringForJsFile))
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
    return StreamEx.of(getModelContext().getCurrentDocRef().toJavaUtil());
  }

  private @NotNull Stream<DocumentReference> getLayoutPropDocRef() {
    return StreamEx.of(getLayoutService().getLayoutPropDocRefForCurrentDoc());
  }

  private @NotNull DocumentReference getCurrentPageTypeDocRef() {
    return getObjectPageTypeUtils().getDocRefForPageType(
        getPageTypeResolver().resolvePageTypeRefForCurrentDoc());
  }

  private Stream<DocumentReference> getCurrentSpacePreferencesDocRef() {
    return StreamEx.of(getModelContext().getCurrentSpaceRef().toJavaUtil()
        .map(spaceRef -> RefBuilder.from(spaceRef).doc("WebPreferences").build(
            DocumentReference.class)));
  }

  private @NotNull DocumentReference getXWikiPreferencesDocRef() {
    return RefBuilder.from(getModelContext().getWikiRef()).space("XWiki")
        .doc("XWikiPreferences").build(DocumentReference.class);
  }

  private Stream<DocumentReference> getSkinDocRef() {
    return StreamEx.of(Optional.ofNullable(getModelContext().getXWikiContext())
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
      return Utils.getComponent(IWebUtilsService.class).lookupMap(IExtJSFilesListener.class);
    } catch (ComponentLookupException exp) {
      LOGGER.error("Failed to get IExtJSFilesListener components.", exp);
    }
    return Collections.emptyMap();
  }

  void addAllExtJSfilesFromDocRef(@NotNull DocumentReference docRef,
      @Nullable AttachmentURLCommand attUrlCmdMock) {
    checkNotNull(docRef);
    try {
      XWikiObjectFetcher.on(getModelAccess().getDocument(docRef))
          .filter(getJavaScriptExternalFilesClassRef())
          .stream()
          .map(ExternalJavaScriptFilesCommand.JS_FILE_ENTRY_CONVERTER.get())
          .filter(JsFileEntry::isValid)
          .forEachOrdered(jsFile -> addExtJSfileOnce(
              new ExtJsFileParameter.Builder()
                  .setJsFileEntry(jsFile)
                  .build(),
              attUrlCmdMock));
    } catch (DocumentNotExistsException nExExp) {
      LOGGER.info("addAllExtJSfilesFromDocRef skipping [{}] because: not exist.", docRef);
    }
  }

  @NotNull
  AttachmentURLCommand getAttUrlCmd(@Nullable AttachmentURLCommand attUrlCmdMock) {
    return Optional.ofNullable(attUrlCmdMock).orElse(new AttachmentURLCommand());
  }

  private @NotNull LayoutServiceRole getLayoutService() {
    return Utils.getComponent(LayoutServiceRole.class);
  }

  private IPageTypeResolverRole getPageTypeResolver() {
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private ModelContext getModelContext() {
    return Utils.getComponent(ModelContext.class);
  }

  private XObjectPageTypeUtilsRole getObjectPageTypeUtils() {
    return Utils.getComponent(XObjectPageTypeUtilsRole.class);
  }

  private ClassReference getJavaScriptExternalFilesClassRef() {
    return Utils
        .getComponent(CelementsClassDefinition.class, JavaScriptExternalFilesClass.CLASS_DEF_HINT)
        .getClassReference();
  }
}
