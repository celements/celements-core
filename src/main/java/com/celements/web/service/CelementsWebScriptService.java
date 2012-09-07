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
package com.celements.web.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

import com.celements.appScript.IAppScriptService;
import com.celements.navigation.cmd.DeleteMenuItemCommand;
import com.celements.rendering.RenderCommand;
import com.celements.sajson.Builder;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.plugin.cmd.ImageMapCommand;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celementsweb")
public class CelementsWebScriptService implements ScriptService {

  public static final String IMAGE_MAP_COMMAND = "com.celements.web.ImageMapCommand";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebScriptService.class);

  @Requirement("local")
  EntityReferenceSerializer<String> modelSerializer;

  @Requirement
  QueryManager queryManager;

  @Requirement
  IAppScriptService appScriptService;

  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public boolean hasDocAppScript(String scriptName) {
    return appScriptService.hasDocAppScript(scriptName);
  }

  public boolean hasLocalAppScript(String scriptName) {
    return appScriptService.hasLocalAppScript(scriptName);
  }

  public boolean hasCentralAppScript(String scriptName) {
    return appScriptService.hasCentralAppScript(scriptName);
  }

  public DocumentReference getAppScriptDocRef(String scriptName) {
    return appScriptService.getAppScriptDocRef(scriptName);
  }

  public DocumentReference getLocalAppScriptDocRef(String scriptName) {
    return appScriptService.getLocalAppScriptDocRef(scriptName);
  }

  public DocumentReference getCentralAppScriptDocRef(String scriptName) {
    return appScriptService.getCentralAppScriptDocRef(scriptName);
  }

  public String getAppScriptTemplatePath(String scriptName) {
    return appScriptService.getAppScriptTemplatePath(scriptName);
  }

  public boolean isAppScriptAvailable(String scriptName) {
    return appScriptService.isAppScriptAvailable(scriptName);
  }

  public String getAppScriptURL(String scriptName) {
    return appScriptService.getAppScriptURL(scriptName);
  }

  public String getAppScriptURL(String scriptName, String queryString) {
    return appScriptService.getAppScriptURL(scriptName, queryString);
  }

  public boolean isAppScriptCurrentPage(String scriptName) {
    return appScriptService.isAppScriptCurrentPage(scriptName);
  }

  public String getScriptNameFromURL() {
    return appScriptService.getScriptNameFromURL();
  }

  public boolean isAppScriptRequest() {
    return appScriptService.isAppScriptRequest();
  }

  public String getCurrentPageURL(String queryString) {
    if(isAppScriptRequest()) {
      return getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      return "?" + queryString;
    }
  }

  public String convertToPlainText(String htmlContent) {
    LOGGER.trace("convertToPlainText called on celementsweb script service for ["
        + htmlContent + "].");
    return new PlainTextCommand().convertToPlainText(htmlContent);
  }

  public Builder getNewJSONBuilder() {
    return new Builder();
  }

  public boolean deleteMenuItem(DocumentReference docRef) {
    String docFN = modelSerializer.serialize(docRef);
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("edit",
          getContext().getUser(), docFN, getContext())) {
        return new DeleteMenuItemCommand().deleteMenuItem(docRef);
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check 'edit' access rights for user ["
          + getContext().getUser() + "] on document [" + docFN + "]");
    }
    return false;
  }

  public List<String[]> getLastChangedDocuments(int numEntries) {
    return getLastChangedDocuments(numEntries, "");
  }

  //TODO write unit tests
  public List<String[]> getLastChangedDocuments(int numEntries, String space) {
    String xwql = "select doc.fullName, doc.language from XWikiDocument doc";
    boolean hasSpaceRestriction = (!"".equals(space));
    if (hasSpaceRestriction) {
      xwql = xwql + " where doc.space = :spaceName";
    }
    xwql = xwql + " order by doc.date desc";
    Query query;
    try {
      query = queryManager.createQuery(xwql, Query.XWQL);
      if (hasSpaceRestriction) {
        query = query.bindValue("spaceName", space);
      }
      return query.setLimit(numEntries).execute();
    } catch (QueryException exp) {
      LOGGER.error("Failed to create whats-new query for space [" + space + "].", exp);
    }
    return Collections.emptyList();
  }

  public String getHumanReadableSize(int bytes, boolean si) {
    return getHumanReadableSize(bytes, si, getContext().getLanguage());
  }

  public String getHumanReadableSize(int bytes, boolean si, String language) {
    return getHumanReadableSize(bytes, si, getLocal(language));
  }

  public String getHumanReadableSize(int bytes, boolean si, Locale locale) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    NumberFormat decimalFormat = DecimalFormat.getInstance(locale);
    decimalFormat.setMaximumFractionDigits(1);
    decimalFormat.setMinimumFractionDigits(1);
    return String.format("%s %sB", decimalFormat.format(bytes / Math.pow(unit, exp)), pre);
  }

  public Locale getLocal(String language) {
    return new Locale(language);
  }

  public Locale getLocal(String language, String country) {
    return new Locale(language, country);
  }

  public Document createDocument(DocumentReference newDocRef) {
    return createDocument(newDocRef, null);
  }

  public Document createDocument(DocumentReference newDocRef, String pageType) {
    LOGGER.trace("create new document for [" + newDocRef + "] and pageType [" + pageType
        + "].");
    XWikiDocument theNewDoc = new CreateDocumentCommand().createDocument(newDocRef,
        pageType);
    if (theNewDoc != null) {
      LOGGER.debug("created new document for [" + newDocRef + "] and pageType ["
          + pageType + "].");
      return theNewDoc.newDocument(getContext());
    }
    return null;
  }

  public List<String> getImageUseMaps(String rteContent) {
    return getImageMapCommand().getImageUseMaps(rteContent);
  }

  private ImageMapCommand getImageMapCommand() {
    if (getContext().get(IMAGE_MAP_COMMAND) == null) {
      getContext().put(IMAGE_MAP_COMMAND, new ImageMapCommand(getContext()));
    }
    return (ImageMapCommand) getContext().get(IMAGE_MAP_COMMAND);
  }

  public void addImageMapConfig(String configName) {
    getImageMapCommand().addMapConfig(configName);
  }
  
  public String displayImageMapConfigs() {
    return getImageMapCommand().displayAllImageMapConfigs();
  }

  public String getSkinFile(String fileName) {
    return new AttachmentURLCommand().getAttachmentURL(fileName, getContext());
  }

  public String getSkinFile(String fileName, String action) {
    return new AttachmentURLCommand().getAttachmentURL(fileName, action, getContext());
  }

  public String getSkinFileExternal(String fileName, String action) {
    return new AttachmentURLCommand().getExternalAttachmentURL(fileName, action,
        getContext());
  }

  private RenderCommand getCelementsRenderCmd() {
    RenderCommand renderCommand = new RenderCommand();
    renderCommand.setDefaultPageType("RichText");
    return renderCommand;
  }

  public String renderCelementsDocument(DocumentReference elementDocRef) {
    return renderCelementsDocument(elementDocRef, getContext().getLanguage(), "view");
  }

  public String renderCelementsDocument(DocumentReference elementDocRef,
      String renderMode) {
    return renderCelementsDocument(elementDocRef, getContext().getLanguage(), renderMode);
  }

  public String renderCelementsDocument(DocumentReference elementDocRef, String lang,
      String renderMode) {
    try {
      return getCelementsRenderCmd().renderCelementsDocument(elementDocRef, lang,
          renderMode);
    } catch (XWikiException exp) {
      LOGGER.error("renderCelementsDocument: Failed to render " + elementDocRef, exp);
    }
    return "";
  }

  public String renderCelementsDocument(Document renderDoc) {
    return renderCelementsDocument(renderDoc, "view");
  }

  public String renderCelementsDocument(Document renderDoc, String renderMode) {
    //we must not get here for !getService().isAppScriptRequest()
    if ("view".equals(getContext().getAction()) && renderDoc.isNew()) {
      LOGGER.info("renderCelementsDocument: Failed to get xwiki document for"
          + renderDoc.getFullName() + " no rendering applied.");
      return "";
    } else {
      return renderCelementsDocument(renderDoc.getDocumentReference(),
          renderDoc.getLanguage(), renderMode);
    }
  }

  public String renderDocument(DocumentReference docRef) {
    LOGGER.trace("renderDocument: docRef [" + docRef + "].");
    return new RenderCommand().renderDocument(docRef);
  }

  public String renderDocument(DocumentReference docRef, String lang) {
    LOGGER.trace("renderDocument: lang [" + lang + "] docRef [" + docRef + "].");
    return new RenderCommand().renderDocument(docRef, lang);
  }

  public String renderDocument(Document renderDoc) {
    LOGGER.trace("renderDocument: renderDocLang [" + renderDoc.getLanguage()
        + "] renderDoc [" + renderDoc.getDocumentReference() + "].");
    return new RenderCommand().renderDocument(renderDoc.getDocumentReference(),
        renderDoc.getLanguage());
  }

  public String renderDocument(DocumentReference docRef, String lang, boolean removePre,
      List<String> rendererNameList) {
    try {
      RenderCommand renderCommand = new RenderCommand();
      renderCommand.initRenderingEngine(rendererNameList);
      return renderCommand.renderDocument(docRef, lang);
    } catch (XWikiException exp) {
      LOGGER.error("renderCelementsDocument: Failed to render ["
          + docRef + "] lang ["+ lang + "].", exp);
    }
    return "";
  }

  public String renderDocument(Document renderDoc, boolean removePre,
      List<String> rendererNameList) {
    return renderDocument(renderDoc.getDocumentReference(), renderDoc.getLanguage(),
        removePre, rendererNameList);
  }

}
