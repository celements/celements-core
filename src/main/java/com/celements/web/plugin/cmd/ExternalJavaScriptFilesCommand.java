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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.cmd.PageTypeCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ExternalJavaScriptFilesCommand {

  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  public static final String JAVA_SCRIPT_EXTERNAL_FILES_CLASS = 
    JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE + "." + JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC;

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      ExternalJavaScriptFilesCommand.class);

  private XWikiContext context;
  private Set<String> extJSfileSet;
  private List<String> extJSfileList;
  private List<String> extJSnotFoundList;
  private boolean displayedAll = false;
  private AttachmentURLCommand attUrlCmd_injected = null;
  
  public ExternalJavaScriptFilesCommand(XWikiContext context) {
    this.context = context;
    extJSfileSet = new HashSet<String>();
    extJSfileList = new Vector<String>();
    extJSnotFoundList = new Vector<String>();
  }

  public String addExtJSfileOnce(String jsFile) {
    return addExtJSfileOnce_internal(jsFile, getAttUrlCmd().getAttachmentURL(jsFile,
        context));
  }

  public String addExtJSfileOnce(String jsFile, String action) {
    return addExtJSfileOnce_internal(jsFile, getAttUrlCmd().getAttachmentURL(jsFile,
        action, context));
  }

  private String addExtJSfileOnce_internal(String jsFile, String jsFileUrl) {
    String jsIncludes2 = "";
    if (jsFileUrl == null) {
      if (!extJSfileSet.contains(jsFile)) {
        jsIncludes2 = "<!-- WARNING: js-file not found: " + jsFile + "-->";
        extJSfileSet.add(jsFile);
        extJSnotFoundList.add(jsIncludes2);
      }
    } else {
      if (!extJSfileSet.contains(jsFileUrl)) {
        jsIncludes2 = getExtStringForJsFile(jsFileUrl);
        extJSfileSet.add(jsFileUrl);
        extJSfileList.add(jsFileUrl);
      }
    }
    if(!displayedAll) {
      jsIncludes2 = "";
    }
    return jsIncludes2;
  }

  AttachmentURLCommand getAttUrlCmd() {
    if(attUrlCmd_injected  != null) {
      return attUrlCmd_injected;
    }
    return new AttachmentURLCommand();
  }
  
  void injectAttUrlCmd(AttachmentURLCommand attUrlCmd) {
    attUrlCmd_injected = attUrlCmd;
  }
  
  void injectDisplayAll(boolean displayedAll) {
    this.displayedAll = displayedAll;
  }

  private String getExtStringForJsFile(String jsFile) {
    return "<script type=\"text/javascript\" src=\"" + jsFile + "\"></script>";
  }

  public String getAllExternalJavaScriptFiles() throws XWikiException {
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    if ((vcontext != null) && vcontext.containsKey("skin_doc")) {
      addAllExtJSfilesFromDoc(context.getWiki().getDocument(getWebUtils(
          ).resolveDocumentReference(((Document)vcontext.get("skin_doc")).getFullName()),
          context));
    }
    addAllExtJSfilesFromDoc(context.getWiki().getDocument(new DocumentReference(
        context.getDatabase(), "XWiki", "XWikiPreferences"), context));
    addAllExtJSfilesFromDoc(context.getWiki().getDocument(new DocumentReference(
        context.getDatabase(), context.getDoc().getDocumentReference(
            ).getLastSpaceReference().getName(), "WebPreferences"), context));
    addAllExtJSfilesFromDoc(context.getDoc());
    XWikiDocument pagetype = getPageTypeDoc(context.getDoc());
    if(pagetype != null){
      addAllExtJSfilesFromDoc(pagetype);
    }
    notifyExtJavaScriptFileListener();
    String jsIncludes = "";
    for (String jsFile : extJSfileList) {
      jsIncludes += getExtStringForJsFile(jsFile) + "\n";
    }
    for (String jsFileWarning : extJSnotFoundList) {
      jsIncludes += jsFileWarning + "\n";
    }
    displayedAll = true;
    return jsIncludes;
  }

  private void notifyExtJavaScriptFileListener() {
    Map<String, IExtJSFilesListener> listenerMap = getListenerMap();
    for (String jsfListenerKey : listenerMap.keySet()) {
      listenerMap.get(jsfListenerKey).beforeAllExtFinish(this);
    }
  }

  private Map<String, IExtJSFilesListener> getListenerMap() {
    try {
      return Utils.getComponentManager().lookupMap(IExtJSFilesListener.class);
    } catch (ComponentLookupException exp) {
      LOGGER.error("Failed to get IExtJSFilesListener components.", exp);
    }
    return Collections.emptyMap();
  }

  private String addAllExtJSfilesFromDoc(XWikiDocument doc) {
    String jsIncludes2 = "";
    for (String jsFile : getJavaScriptExternalFilePaths(doc)) {
      String addJSinclude = addExtJSfileOnce(jsFile);
      if(!"".equals(addJSinclude)) {
        jsIncludes2 = jsIncludes2 + addJSinclude + "\n";
      }
    }
    return jsIncludes2;
  }

  private XWikiDocument getPageTypeDoc(XWikiDocument doc) throws XWikiException{
  LOGGER.trace("entering with doc: '" + ((doc != null)?doc.getDocumentReference():"null")
      + "'");
    XWikiDocument pagetypeDoc = null;
    BaseObject obj = doc.getXObject(new DocumentReference(context.getDatabase(),
        PageTypeCommand.PAGE_TYPE_CLASS_SPACE, PageTypeCommand.PAGE_TYPE_CLASS_DOC));
    LOGGER.debug("Celements2.PageType object: '" + obj + "'");
    if((obj != null) && (obj instanceof BaseObject)){
      String pagetypeName = obj.getStringValue("page_type");
      LOGGER.debug("PageType name is: '" + pagetypeName + "'");
      if((pagetypeName != null) && (!pagetypeName.equals(""))){
        pagetypeDoc = context.getWiki().getDocument(getWebUtils(
            ).resolveDocumentReference(new PageTypeCommand().completePageTypeDocName(
                pagetypeName)), context);
      }
    }
  
    LOGGER.trace("ending. PageType is: '"
        + ((pagetypeDoc != null)?pagetypeDoc.getDocumentReference():"null") + "'");
    return pagetypeDoc;
  }
  
  private List<String> getJavaScriptExternalFilePaths(XWikiDocument doc) {
    List<BaseObject> javaScriptFiles = doc.getXObjects(new DocumentReference(
        context.getDatabase(), JAVA_SCRIPT_EXTERNAL_FILES_CLASS_SPACE,
        JAVA_SCRIPT_EXTERNAL_FILES_CLASS_DOC));
    Vector<String> jsFiles = new Vector<String>();
    if (javaScriptFiles != null) {
      for(Object filepath : javaScriptFiles) {
        if ((filepath != null) && (filepath instanceof BaseObject)) {
          BaseObject filepathObj = (BaseObject) filepath;
          if(!"".equals(filepathObj.getStringValue("filepath"))) {
            jsFiles.add(filepathObj.getStringValue("filepath"));
          }
        }
      }
    }
    return jsFiles;
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
