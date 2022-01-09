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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

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

import com.celements.javascript.JsLoadMode;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.sajson.JsonBuilder;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ExternalJavaScriptFilesCommand.class);

  static final class JsFileEntry {

    String jsFileUrl;
    JsLoadMode loadMode = JsLoadMode.SYNC;

    JsFileEntry(String jsFileUrl) {
      this.jsFileUrl = jsFileUrl;
    }

    JsFileEntry(String jsFileUrl, JsLoadMode loadMode) {
      this.jsFileUrl = jsFileUrl;
      this.loadMode = loadMode;
    }

    @Override
    public int hashCode() {
      return jsFileUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof JsFileEntry)
          && Objects.equals(((JsFileEntry) obj).jsFileUrl, jsFileUrl);
    }

  }

  static final class ExtJsFileParameter {

    @NotNull
    private String jsFile;
    @Nullable
    private String action = null;
    @Nullable
    private String params = null;
    @Nullable
    private AttachmentURLCommand attUrlCmd = null;
    private JsLoadMode loadMode = JsLoadMode.SYNC;

    public ExtJsFileParameter setJsFile(@NotNull String jsFile) {
      checkNotNull(jsFile);
      this.jsFile = jsFile;
      return this;
    }

    public ExtJsFileParameter setAction(String action) {
      this.action = action;
      return this;
    }

    public ExtJsFileParameter setParams(String params) {
      this.params = params;
      return this;
    }

    public ExtJsFileParameter setAttUrlCmd(AttachmentURLCommand attUrlCmd) {
      this.attUrlCmd = attUrlCmd;
      return this;
    }

    public ExtJsFileParameter setLoadMode(JsLoadMode loadMode) {
      this.loadMode = loadMode;
      return this;
    }

  }

  private final Set<JsFileEntry> extJSfileSet = new LinkedHashSet<>();
  private final Set<String> extJSAttUrlSet = new HashSet<>();
  private final Set<String> extJSnotFoundSet = new LinkedHashSet<>();
  private boolean displayedAll = false;

  public ExternalJavaScriptFilesCommand() {}

  /**
   * @deprecated since 5.4 instead use {@link ExternalJavaScriptFilesCommand()}
   */
  @Deprecated
  public ExternalJavaScriptFilesCommand(XWikiContext context) {}

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
    if (!StringUtils.isEmpty(extJsFileParams.action)) {
      attUrl = getAttUrlCmd(extJsFileParams.attUrlCmd).getAttachmentURL(extJsFileParams.jsFile,
          extJsFileParams.action, getModelContext().getXWikiContext());
    } else {
      attUrl = getAttUrlCmd(extJsFileParams.attUrlCmd).getAttachmentURL(extJsFileParams.jsFile,
          getModelContext().getXWikiContext());
    }
    if (!StringUtils.isEmpty(extJsFileParams.params)) {
      if (attUrl.indexOf("?") > -1) {
        attUrl += "&" + extJsFileParams.params;
      } else {
        attUrl += "?" + extJsFileParams.params;
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

  @NotNull
  public String addExtJSfileOnceDefer(String jsFile) {
    return addExtJSfileOnce(jsFile, null, true);
  }

  @NotNull
  public String addExtJSfileOnce(String jsFile) {
    return addExtJSfileOnce(jsFile, null);
  }

  @NotNull
  public String addExtJSfileOnce(String jsFile, String action) {
    return addExtJSfileOnce(jsFile, action, false);
  }

  @NotNull
  public String addExtJSfileOnce(String jsFile, String action, boolean defer) {
    return addExtJSfileOnce(jsFile, action, defer, null);
  }

  @NotNull
  public String addExtJSfileOnce(String jsFile, String action, String params) {
    return addExtJSfileOnce(jsFile, action, false, params);
  }

  @NotNull
  public String addExtJSfileOnce(String jsFile, String action, boolean defer, String params) {
    return addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(jsFile)
        .setAction(action)
        .setLoadMode(JsLoadMode.DEFER)
        .setParams(params));
  }

  @NotNull
  String addExtJSfileOnce(ExtJsFileParameter extJsFileParams) {
    if (!extJSAttUrlSet.contains(extJsFileParams.jsFile)) {
      if (getAttUrlCmd(extJsFileParams.attUrlCmd).isAttachmentLink(extJsFileParams.jsFile)
          || getAttUrlCmd(extJsFileParams.attUrlCmd).isOnDiskLink(extJsFileParams.jsFile)) {
        extJSAttUrlSet.add(extJsFileParams.jsFile);
      }
      String attUrl;
      if (!StringUtils.isEmpty(extJsFileParams.action)) {
        attUrl = getAttUrlCmd(extJsFileParams.attUrlCmd).getAttachmentURL(extJsFileParams.jsFile,
            extJsFileParams.action,
            getModelContext().getXWikiContext());
      } else {
        attUrl = getAttUrlCmd(extJsFileParams.attUrlCmd).getAttachmentURL(extJsFileParams.jsFile,
            getModelContext().getXWikiContext());
      }
      if (!StringUtils.isEmpty(extJsFileParams.params)) {
        if (attUrl.indexOf("?") > -1) {
          attUrl += "&" + extJsFileParams.params;
        } else {
          attUrl += "?" + extJsFileParams.params;
        }
      }
      return Strings.nullToEmpty(addExtJSfileOnceInternal(extJsFileParams.jsFile,
          attUrl, extJsFileParams.loadMode));
    }
    return "";
  }

  private String addExtJSfileOnceInternal(String jsFile, String jsFileUrl, JsLoadMode loadMode) {
    String jsIncludes2 = "";
    if (jsFileUrl == null) {
      JsFileEntry jsFileEntry = new JsFileEntry(jsFile);
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        extJSnotFoundSet.add(jsFile);
        jsIncludes2 = buildNotFoundWarning(jsFile);
      }
    } else {
      JsFileEntry jsFileEntry = new JsFileEntry(jsFileUrl, loadMode);
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
    return extJSfileSet.contains(jsFile) || extJSnotFoundSet.contains(jsFile.jsFileUrl);
  }

  void injectDisplayAll(boolean displayedAll) {
    this.displayedAll = displayedAll;
  }

  String getExtStringForJsFile(JsFileEntry jsFile) {
    return "<script"
        + ((jsFile.loadMode != JsLoadMode.SYNC) ? " " + jsFile.loadMode.toString().toLowerCase()
            : "")
        + " type=\"text/javascript\" src=\"" + StringEscapeUtils.escapeHtml(jsFile.jsFileUrl)
        + "\"></script>";
  }

  public String getAllExternalJavaScriptFiles() {
    return getAllExternalJavaScriptFiles(null, null);
  }

  String getAllExternalJavaScriptFiles(@Nullable PageLayoutCommand pageLayoutCmdMock,
      @Nullable AttachmentURLCommand attUrlCmd) {
    getDocRefsStream(pageLayoutCmdMock)
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

  private Stream<DocumentReference> getDocRefsStream(
      @Nullable PageLayoutCommand pageLayoutCmdMock) {
    Builder<DocumentReference> docRefStreamBuilder = Stream.builder();
    getSkinDocRef().ifPresent(docRefStreamBuilder::accept);
    docRefStreamBuilder.add(getXWikiPreferencesDocRef());
    getCurrentSpacePreferencesDocRef().ifPresent(docRefStreamBuilder::accept);
    docRefStreamBuilder.add(getCurrentPageTypeDocRef());
    getLayoutPropDocRef(pageLayoutCmdMock).ifPresent(docRefStreamBuilder::accept);
    getCurrentDocRef().ifPresent(docRefStreamBuilder::accept);
    return docRefStreamBuilder.build();
  }

  private Optional<DocumentReference> getCurrentDocRef() {
    return getModelContext().getCurrentDocRef().toJavaUtil();
  }

  private @NotNull Optional<DocumentReference> getLayoutPropDocRef(
      @Nullable PageLayoutCommand pageLayoutCmd) {
    return Optional.ofNullable(getLayoutService(pageLayoutCmd).getLayoutPropDoc())
        .map(XWikiDocument::getDocumentReference);
  }

  private @NotNull DocumentReference getCurrentPageTypeDocRef() {
    return getObjectPageTypeUtils().getDocRefForPageType(
        getPageTypeResolver().resolvePageTypeRefForCurrentDoc());
  }

  private Optional<DocumentReference> getCurrentSpacePreferencesDocRef() {
    return getModelContext().getCurrentSpaceRef().toJavaUtil()
        .map(spaceRef -> RefBuilder.from(spaceRef).doc("WebPreferences").build(
            DocumentReference.class));
  }

  private @NotNull DocumentReference getXWikiPreferencesDocRef() {
    return RefBuilder.from(getModelContext().getWikiRef()).space("XWiki")
        .doc("XWikiPreferences").build(DocumentReference.class);
  }

  private Optional<DocumentReference> getSkinDocRef() {
    return Optional.ofNullable(getModelContext().getXWikiContext())
        .map(xcontext -> (VelocityContext) xcontext.get("vcontext"))
        .filter(vcontext -> vcontext.containsKey("skin_doc"))
        .map(vcontext -> ((Document) vcontext.get("skin_doc")).getDocumentReference());
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

  private void addAllExtJSfilesFromDocRef(@NotNull DocumentReference docRef,
      @Nullable AttachmentURLCommand attUrlCmd) {
    checkNotNull(docRef);
    try {
      XWikiObjectFetcher.on(getModelAccess().getDocument(docRef))
          .filter(new ClassReference(IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE,
              IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC))
          .stream()
          .map(filepathObj -> filepathObj.getStringValue("filepath"))
          .filter(Predicates.not(Strings::isNullOrEmpty))
          .forEachOrdered(jsFileUrl -> addExtJSfileOnce(
              new ExtJsFileParameter().setJsFile(jsFileUrl).setAttUrlCmd(attUrlCmd)));
    } catch (DocumentNotExistsException nExExp) {
      LOGGER.info("addJSFiles from [{}] failed.", docRef, nExExp);
    }
  }

  @NotNull
  AttachmentURLCommand getAttUrlCmd(@Nullable AttachmentURLCommand attUrlCmd) {
    return Optional.ofNullable(attUrlCmd).orElse(new AttachmentURLCommand());
  }

  private @NotNull PageLayoutCommand getLayoutService(@Nullable PageLayoutCommand pageLayoutCmd) {
    return Optional.ofNullable(pageLayoutCmd).orElse(new PageLayoutCommand());
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

}
