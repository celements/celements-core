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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

import one.util.streamex.StreamEx;

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

  private final Set<JsFileEntry> extJSfileSet = new LinkedHashSet<>();
  private final Set<String> extJSAttUrlSet = new HashSet<>();
  private final Set<String> extJSnotFoundSet = new LinkedHashSet<>();
  private boolean displayedAll = false;
  private static final Supplier<BeanClassDefConverter<BaseObject, JsFileEntry>> JS_FILE_ENTRY_CONVERTER = Suppliers
      .memoize(ExternalJavaScriptFilesCommand::jsFileEntryConverter);

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

  public String addLazyExtJSfile(String jsFile) {
    return addLazyExtJSfile(new ExtJsFileParameter()
        .setJsFile(jsFile));
  }

  public String addLazyExtJSfile(String jsFile, String action) {
    return addLazyExtJSfile(new ExtJsFileParameter()
        .setJsFile(jsFile)
        .setAction(action));
  }

  public String addLazyExtJSfile(String jsFile, String action, String params) {
    return addLazyExtJSfile(new ExtJsFileParameter()
        .setJsFile(jsFile)
        .setAction(action)
        .setParams(params));
  }

  String addLazyExtJSfile(ExtJsFileParameter extJsFileParams) {
    String attUrl;
    if (!StringUtils.isEmpty(extJsFileParams.getAction())) {
      attUrl = getAttUrlCmd(extJsFileParams.getAttUrlCmd()).getAttachmentURL(
          extJsFileParams.getJsFile(),
          extJsFileParams.getAction(), getModelContext().getXWikiContext());
    } else {
      attUrl = getAttUrlCmd(extJsFileParams.getAttUrlCmd()).getAttachmentURL(
          extJsFileParams.getJsFile(),
          getModelContext().getXWikiContext());
    }
    if (!StringUtils.isEmpty(extJsFileParams.getParams())) {
      if (attUrl.indexOf("?") > -1) {
        attUrl += "&" + extJsFileParams.getParams();
      } else {
        attUrl += "?" + extJsFileParams.getParams();
      }
    }
    JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.openDictionary();
    jsonBuilder.addProperty("fullURL", attUrl);
    jsonBuilder.addProperty("initLoad", true);
    jsonBuilder.closeDictionary();
    return "<span class='cel_lazyloadJS' style='display: none;'>" + jsonBuilder.getJSON()
        + "</span>";
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(String jsFile) {
    return addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(jsFile));
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(String jsFile, String action) {
    return addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(jsFile)
        .setAction(action));
  }

  /**
   * @deprecated since 5.4 instead use {@link addExtJSfileOnce(ExtJsFileParameter)}
   */
  @Deprecated
  @NotNull
  public String addExtJSfileOnce(String jsFile, String action, String params) {
    return addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(jsFile)
        .setAction(action)
        .setParams(params));
  }

  @NotNull
  public String addExtJSfileOnce(ExtJsFileParameter extJsFileParams) {
    if (!extJSAttUrlSet.contains(extJsFileParams.getJsFile())) {
      if (getAttUrlCmd(extJsFileParams.getAttUrlCmd()).isAttachmentLink(extJsFileParams.getJsFile())
          || getAttUrlCmd(extJsFileParams.getAttUrlCmd())
              .isOnDiskLink(extJsFileParams.getJsFile())) {
        extJSAttUrlSet.add(extJsFileParams.getJsFile());
      }
      String attUrl;
      if (!StringUtils.isEmpty(extJsFileParams.getAction())) {
        attUrl = getAttUrlCmd(extJsFileParams.getAttUrlCmd()).getAttachmentURL(
            extJsFileParams.getJsFile(),
            extJsFileParams.getAction(),
            getModelContext().getXWikiContext());
      } else {
        attUrl = getAttUrlCmd(extJsFileParams.getAttUrlCmd()).getAttachmentURL(
            extJsFileParams.getJsFile(),
            getModelContext().getXWikiContext());
      }
      if (!StringUtils.isEmpty(extJsFileParams.getParams())) {
        if (attUrl.indexOf("?") > -1) {
          attUrl += "&" + extJsFileParams.getParams();
        } else {
          attUrl += "?" + extJsFileParams.getParams();
        }
      }
      return Strings.nullToEmpty(addExtJSfileOnceInternal(extJsFileParams.getJsFile(),
          attUrl, extJsFileParams.getLoadMode()));
    }
    return "";
  }

  private String addExtJSfileOnceInternal(String jsFile, String jsFileUrl, JsLoadMode loadMode) {
    LOGGER.info("addExtJSfileOnceInternal: jsFile [{}] jsFileUrl [{}] loadMode [{}]", jsFile,
        jsFileUrl, loadMode);
    String jsIncludes2 = "";
    if (jsFileUrl == null) {
      JsFileEntry jsFileEntry = new JsFileEntry().addFilepath(jsFile);
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        extJSnotFoundSet.add(jsFile);
        jsIncludes2 = buildNotFoundWarning(jsFile);
      }
    } else {
      JsFileEntry jsFileEntry = new JsFileEntry().addFilepath(jsFileUrl)
          .addLoadMode(loadMode);
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        jsIncludes2 = getExtStringForJsFile(jsFileEntry);
        extJSfileSet.add(jsFileEntry);
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
    return "<script"
        + ((jsFile.getLoadMode() != JsLoadMode.SYNC)
            ? " " + jsFile.getLoadMode().toString().toLowerCase()
            : "")
        + " type=\"text/javascript\" src=\"" + StringEscapeUtils.escapeHtml(jsFile.getFilepath())
        + "\"></script>";
  }

  public String getAllExternalJavaScriptFiles() {
    return getAllExternalJavaScriptFiles(null);
  }

  String getAllExternalJavaScriptFiles(@Nullable AttachmentURLCommand attUrlCmd) {
    getDocRefsStream()
        .forEachOrdered(docRef -> addAllExtJSfilesFromDocRef(docRef, attUrlCmd));
    notifyExtJavaScriptFileListener();

    final StringBuilder jsIncludesBuilder = new StringBuilder();
    jsIncludesBuilder.append(Stream.concat(
        extJSfileSet.stream().map(this::getExtStringForJsFile),
        extJSnotFoundSet.stream().map(this::buildNotFoundWarning))
        .collect(Collectors.joining("\n")));
    jsIncludesBuilder.append("\n");
    displayedAll = true;
    return jsIncludesBuilder.toString();
  }

  private Stream<DocumentReference> getDocRefsStream() {
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
      @Nullable AttachmentURLCommand attUrlCmd) {
    checkNotNull(docRef);
    try {
      XWikiObjectFetcher.on(getModelAccess().getDocument(docRef))
          .filter(getJavaScriptExternalFilesClassRef())
          .stream()
          .map(ExternalJavaScriptFilesCommand.JS_FILE_ENTRY_CONVERTER.get())
          .filter(JsFileEntry::isValid)
          .forEachOrdered(jsFile -> addExtJSfileOnce(
              new ExtJsFileParameter()
                  .setJsFile(jsFile.getFilepath())
                  .setLoadMode(jsFile.getLoadMode())
                  .setAttUrlCmd(attUrlCmd)));
    } catch (DocumentNotExistsException nExExp) {
      LOGGER.info("addJSFiles from [{}] failed.", docRef, nExExp);
    }
  }

  @NotNull
  AttachmentURLCommand getAttUrlCmd(@Nullable AttachmentURLCommand attUrlCmd) {
    return Optional.ofNullable(attUrlCmd).orElse(new AttachmentURLCommand());
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
