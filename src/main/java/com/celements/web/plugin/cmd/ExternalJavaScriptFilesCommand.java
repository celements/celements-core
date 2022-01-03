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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.sajson.JsonBuilder;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ExternalJavaScriptFilesCommand {

  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS = JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE
      + "." + JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC;

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ExternalJavaScriptFilesCommand.class);

  class JsFileEntry {

    String jsFileUrl;
    boolean defer = false;

    JsFileEntry(String jsFileUrl) {
      this.jsFileUrl = jsFileUrl;
    }

    JsFileEntry(String jsFileUrl, boolean defer) {
      this.jsFileUrl = jsFileUrl;
      this.defer = defer;
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

  private final XWikiContext context;
  private final Set<JsFileEntry> extJSfileSet = new LinkedHashSet<>();
  private final Set<String> extJSAttUrlSet = new HashSet<>();
  private final Set<String> extJSnotFoundSet = new LinkedHashSet<>();
  private boolean displayedAll = false;
  private AttachmentURLCommand attUrlCmd_injected = null;
  private IPageTypeResolverRole ptResolver_injected = null;

  public ExternalJavaScriptFilesCommand(XWikiContext context) {
    this.context = context;
  }

  public String addLazyExtJSfile(String jsFile) {
    return addLazyExtJSfile(jsFile, null);
  }

  public String addLazyExtJSfile(String jsFile, String action) {
    return addLazyExtJSfile(jsFile, action, null);
  }

  public String addLazyExtJSfile(String jsFile, String action, String params) {
    String attUrl;
    if (!StringUtils.isEmpty(action)) {
      attUrl = getAttUrlCmd().getAttachmentURL(jsFile, action, context);
    } else {
      attUrl = getAttUrlCmd().getAttachmentURL(jsFile, context);
    }
    if (!StringUtils.isEmpty(params)) {
      if (attUrl.indexOf("?") > -1) {
        attUrl += "&" + params;
      } else {
        attUrl += "?" + params;
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

  public String addExtJSfileOnceDefer(String jsFile) {
    return addExtJSfileOnce(jsFile, null, true);
  }

  public String addExtJSfileOnce(String jsFile) {
    return addExtJSfileOnce(jsFile, null);
  }

  public String addExtJSfileOnce(String jsFile, String action) {
    return addExtJSfileOnce(jsFile, action, false);
  }

  public String addExtJSfileOnce(String jsFile, String action, boolean defer) {
    return addExtJSfileOnce(jsFile, action, defer, null);
  }

  public String addExtJSfileOnce(String jsFile, String action, String params) {
    return addExtJSfileOnce(jsFile, action, false, params);
  }

  public String addExtJSfileOnce(String jsFile, String action, boolean defer, String params) {
    if (!extJSAttUrlSet.contains(jsFile)) {
      if (getAttUrlCmd().isAttachmentLink(jsFile) || getAttUrlCmd().isOnDiskLink(jsFile)) {
        extJSAttUrlSet.add(jsFile);
      }
      String attUrl;
      if (!StringUtils.isEmpty(action)) {
        attUrl = getAttUrlCmd().getAttachmentURL(jsFile, action, context);
      } else {
        attUrl = getAttUrlCmd().getAttachmentURL(jsFile, context);
      }
      if (!StringUtils.isEmpty(params)) {
        if (attUrl.indexOf("?") > -1) {
          attUrl += "&" + params;
        } else {
          attUrl += "?" + params;
        }
      }
      return addExtJSfileOnceInternal(jsFile, attUrl, defer);
    }
    return "";
  }

  private String addExtJSfileOnceInternal(String jsFile, String jsFileUrl, boolean defer) {
    String jsIncludes2 = "";
    if (jsFileUrl == null) {
      JsFileEntry jsFileEntry = new JsFileEntry(jsFile);
      if (!jsFileHasBeenSeen(jsFileEntry)) {
        extJSnotFoundSet.add(jsFile);
        jsIncludes2 = buildNotFoundWarning(jsFile);
      }
    } else {
      JsFileEntry jsFileEntry = new JsFileEntry(jsFileUrl, defer);
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

  AttachmentURLCommand getAttUrlCmd() {
    if (attUrlCmd_injected != null) {
      return attUrlCmd_injected;
    }
    return new AttachmentURLCommand();
  }

  void injectAttUrlCmd(AttachmentURLCommand attUrlCmd) {
    attUrlCmd_injected = attUrlCmd;
  }

  void injectPageTypeResolver(IPageTypeResolverRole ptResolver) {
    ptResolver_injected = ptResolver;
  }

  void injectDisplayAll(boolean displayedAll) {
    this.displayedAll = displayedAll;
  }

  String getExtStringForJsFile(JsFileEntry jsFile) {
    return "<script" + (jsFile.defer ? " defer" : "") + " type=\"text/javascript\" src=\""
        + StringEscapeUtils.escapeHtml(jsFile.jsFileUrl) + "\"></script>";
  }

  public String getAllExternalJavaScriptFiles() {
    if ((context != null) && (context.getDoc() != null)) {
      VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
      if ((vcontext != null) && vcontext.containsKey("skin_doc")) {
        try {
          addAllExtJSfilesFromDoc(getModelAccess().getDocument(((Document) vcontext.get(
              "skin_doc")).getDocumentReference()));
        } catch (DocumentNotExistsException nExExp) {
          LOGGER.info("addJSFiles from skin_doc failed.", nExExp);
        }
      }
      try {
        addAllExtJSfilesFromDoc(getModelAccess().getDocument(new DocumentReference(
            context.getDatabase(), "XWiki", "XWikiPreferences")));
      } catch (DocumentNotExistsException nExExp) {
        LOGGER.info("addJSFiles from XWiki.XWikiPreferences failed.", nExExp);
      }
      String curSpaceName = context.getDoc().getDocumentReference().getLastSpaceReference()
          .getName();
      try {
        addAllExtJSfilesFromDoc(getModelAccess().getDocument(new DocumentReference(
            context.getDatabase(), curSpaceName, "WebPreferences")));
      } catch (DocumentNotExistsException nExExp) {
        LOGGER.info("addJSFiles from current space '{}' WebPreferences failed.", curSpaceName,
            nExExp);
      }
      PageTypeReference pageTypeRef = getPageTypeResolver().resolvePageTypeRefForCurrentDoc();
      try {
        addAllExtJSfilesFromDoc(getModelAccess().getDocument(getObjectPageTypeUtils()
            .getDocRefForPageType(pageTypeRef)));
      } catch (DocumentNotExistsException exp) {
        LOGGER.info("Could not get Document with docRef {} ",
            getObjectPageTypeUtils().getDocRefForPageType(pageTypeRef), exp);
      }
      addAllExtJSfilesFromDoc(new PageLayoutCommand().getLayoutPropDoc());
      addAllExtJSfilesFromDoc(context.getDoc());
    }
    notifyExtJavaScriptFileListener();
    String jsIncludes = "";
    for (JsFileEntry jsFile : extJSfileSet) {
      jsIncludes += getExtStringForJsFile(jsFile) + "\n";
    }
    for (String jsFile : extJSnotFoundSet) {
      jsIncludes += buildNotFoundWarning(jsFile) + "\n";
    }
    displayedAll = true;
    return jsIncludes;
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

  private String addAllExtJSfilesFromDoc(XWikiDocument doc) {
    String jsIncludes2 = "";
    for (String jsFile : getJavaScriptExternalFilePaths(doc)) {
      String addJSinclude = addExtJSfileOnce(jsFile);
      if (!"".equals(addJSinclude)) {
        jsIncludes2 = jsIncludes2 + addJSinclude + "\n";
      }
    }
    return jsIncludes2;
  }

  @NotNull
  private List<String> getJavaScriptExternalFilePaths(@Nullable XWikiDocument doc) {
    if (doc == null) {
      return Collections.emptyList();
    }
    List<BaseObject> javaScriptFiles = doc.getXObjects(
        getOldCoreClasses().getJavaScriptExternalFilesClassRef(
            doc.getDocumentReference().getWikiReference().getName()));
    List<String> jsFiles = new ArrayList<>();
    if (javaScriptFiles != null) {
      for (BaseObject filepathObj : javaScriptFiles) {
        if (!Strings.isNullOrEmpty(filepathObj.getStringValue("filepath"))) {
          jsFiles.add(filepathObj.getStringValue("filepath"));
        }
      }
    }
    return jsFiles;
  }

  private IPageTypeResolverRole getPageTypeResolver() {
    if (ptResolver_injected != null) {
      return ptResolver_injected;
    }
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private XObjectPageTypeUtilsRole getObjectPageTypeUtils() {
    return Utils.getComponent(XObjectPageTypeUtilsRole.class);
  }

  private OldCoreClasses getOldCoreClasses() {
    return (OldCoreClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.oldCoreClasses");
  }

}
