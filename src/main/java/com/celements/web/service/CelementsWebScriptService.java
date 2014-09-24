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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

import com.celements.appScript.IAppScriptService;
import com.celements.navigation.cmd.DeleteMenuItemCommand;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.rendering.RenderCommand;
import com.celements.rteConfig.IRTEConfigTemplateRole;
import com.celements.sajson.Builder;
import com.celements.validation.IFormValidationServiceRole;
import com.celements.validation.ValidationType;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.plugin.cmd.ImageMapCommand;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.SkinConfigObjCommand;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDeletedDocument;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.Util;

@Component("celementsweb")
public class CelementsWebScriptService implements ScriptService {

  public static final String IMAGE_MAP_COMMAND = "com.celements.web.ImageMapCommand";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebScriptService.class);

  @Requirement
  QueryManager queryManager;

  @Requirement
  IAppScriptService appScriptService;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IFormValidationServiceRole formValidationService;

  @Requirement
  ITreeNodeCache treeNodeCacheService;

  @Requirement
  ITreeNodeService treeNodeService;

  @Requirement
  IRTEConfigTemplateRole rteConfigTemplateService;

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

  public String getScriptNameFromDocRef(DocumentReference docRef) {
    return appScriptService.getScriptNameFromDocRef(docRef);
  }

  public String getAppScriptTemplatePath(String scriptName) {
    return appScriptService.getAppScriptTemplatePath(scriptName);
  }

  public boolean isAppScriptOverwriteDocRef(DocumentReference docRef) {
    return appScriptService.isAppScriptOverwriteDocRef(docRef);
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
    String ret;
    if(isAppScriptRequest()) {
      LOGGER.debug("getCurrentPageURL: AppScript for query '" + queryString + "'");
      ret = getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      LOGGER.debug("getCurrentPageURL: query '" + queryString + "'");
      ret = Util.escapeURL("?" + queryString);
    }
    LOGGER.debug("getCurrentPageURL: ret '" + ret + "' for query '" + queryString + "'");
    return ret;
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
    String docFN = webUtilsService.getRefLocalSerializer().serialize(docRef);
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

  public String getHumanReadableSize(long bytes, boolean si) {
    return getHumanReadableSize(bytes, si, getContext().getLanguage());
  }

  public String getHumanReadableSize(long bytes, boolean si, String language) {
    return getHumanReadableSize(bytes, si, getLocal(language));
  }

  public String getHumanReadableSize(long bytes, boolean si, Locale locale) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
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

  public String getAttachmentURLPrefix() {
    return new AttachmentURLCommand().getAttachmentURLPrefix();
  }

  public String getAttachmentURLPrefix(String action) {
    return new AttachmentURLCommand().getAttachmentURLPrefix(action);
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

  public String renderCelementsDocument(DocumentReference elementDocRef,
      boolean preserveVelocityContext) {
    return renderCelementsDocument(elementDocRef, getContext().getLanguage(), "view",
        true);
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
    return renderCelementsDocument(elementDocRef, lang, renderMode, false);
  }

  public String renderCelementsDocument(DocumentReference elementDocRef, String lang,
      String renderMode, boolean preserveVelocityContext) {
    try {
      if (preserveVelocityContext) {
        return getCelementsRenderCmd().renderCelementsDocumentPreserveVelocityContext(
            elementDocRef, lang, renderMode);
      } else {
        return getCelementsRenderCmd().renderCelementsDocument(elementDocRef, lang,
            renderMode);
      }
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

  public String renderDocument(DocumentReference docRef,
      DocumentReference includeDocRef) {
    LOGGER.trace("renderDocument: docRef [" + docRef + "] and includeDocRef ["
      + includeDocRef +  "].");
    return new RenderCommand().renderDocument(docRef, includeDocRef);
  }

  public String renderDocument(DocumentReference docRef, String lang) {
    LOGGER.trace("renderDocument: lang [" + lang + "] docRef [" + docRef + "].");
    return new RenderCommand().renderDocument(docRef, lang);
  }

  public String renderDocument(DocumentReference docRef,
      DocumentReference includeDocRef, String lang) {
    LOGGER.trace("renderDocument: lang [" + lang + "] docRef [" + docRef
        + "] and includeDocRef [" + includeDocRef + "].");
    return new RenderCommand().renderDocument(docRef, includeDocRef, lang);
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

  public boolean existsInheritableDocument(DocumentReference docRef) {
    return webUtilsService.existsInheritableDocument(docRef, getContext().getLanguage());
  }

  public boolean existsInheritableDocument(DocumentReference docRef, String lang) {
    return webUtilsService.existsInheritableDocument(docRef, lang);
  }

  public String renderInheritableDocument(DocumentReference docRef) {
    return renderInheritableDocument(docRef, getContext().getLanguage());
  }

  public String renderInheritableDocument(DocumentReference docRef, String lang) {
    try {
      return webUtilsService.renderInheritableDocument(docRef, lang);
    } catch (XWikiException exp) {
      LOGGER.error("renderInheritableDocument: Failed to render inheritable [" + docRef
          + "] in lang [" + lang + "].");
    }
    return "";
  }

  public boolean useNewButtons() {
    int wikiConfig = getContext().getWiki().getXWikiPreferenceAsInt("useNewButtons",
        "celements.usenewbuttons", 0, getContext());
    return getContext().getWiki().getSpacePreferenceAsInt("useNewButtons", wikiConfig,
        getContext()) == 1;
  }

  public String getDefaultAdminLanguage() {
    return webUtilsService.getDefaultAdminLanguage();
  }

  public String getDefaultLanguage() {
    return webUtilsService.getDefaultLanguage();
  }

  public String getDefaultLanguage(String spaceName) {
    return webUtilsService.getDefaultLanguage(spaceName);
  }

  public List<String> getDeletedDocuments() {
    return getDeletedDocuments("", true);
  }

  public List<String> getDeletedDocuments(String orderby, boolean hideOverwritten) {
    List<String> resultList = Collections.emptyList();
    try {
      Query query = queryManager.createQuery(getDeletedDocsHql(orderby, hideOverwritten),
          Query.HQL);
      resultList = query.execute();
    } catch (QueryException queryExp) {
      LOGGER.error("Failed to parse or execute deletedDocs hql query.", queryExp);
    }
    return resultList;
  }

  private String getDeletedDocsHql(String orderby, boolean hideOverwritten) {
    String deletedDocsHql = "select distinct ddoc.fullName"
        + " from XWikiDeletedDocument as ddoc";
    if (hideOverwritten) {
      deletedDocsHql += " where ddoc.fullName not in (select doc.fullName from"
          + " XWikiDocument as doc)";
    }
    if (!"".equals(orderby)) {
      deletedDocsHql += " order by " + orderby;
    }
    return deletedDocsHql;
  }

  public double getWaitDaysBeforeDelete() {
    String waitdays;
    XWikiConfig config = getContext().getWiki().getConfig();
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
        waitdays = config.getProperty("xwiki.store.recyclebin.adminWaitDays", "0");
    } else {
        waitdays = config.getProperty("xwiki.store.recyclebin.waitDays", "7");
    }
    return Double.parseDouble(waitdays);
  }

  /**
   * permanentlyEmptyTrash delete all documents after waitDays and minWaitDays
   * @return
   */
  public Integer permanentlyEmptyTrash(int waitDays) {
    Calendar beforeWaiteDaysCal = Calendar.getInstance();
    beforeWaiteDaysCal.add(Calendar.DATE, -waitDays);
    Date delBeforeDate = beforeWaiteDaysCal.getTime();
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
      int countDeleted = 0;
      for (String fullName : getDeletedDocuments()) {
        try {
          for (XWikiDeletedDocument delDoc : getContext().getWiki().getDeletedDocuments(
              fullName, "", getContext())) {
            int seconds = (int) (getWaitDaysBeforeDelete() * 24 * 60 * 60 + 0.5);
            Calendar cal = Calendar.getInstance();
            cal.setTime(delDoc.getDate());
            cal.add(Calendar.SECOND, seconds);
            boolean isAfterMinWaitDays = cal.before(Calendar.getInstance());
            if (isAfterMinWaitDays && delDoc.getDate().before(delBeforeDate)) {
              XWikiDocument doc = getContext().getWiki().getDocument(
                  webUtilsService.resolveDocumentReference(fullName), getContext());
              getContext().getWiki().getRecycleBinStore().deleteFromRecycleBin(doc,
                  delDoc.getId(), getContext(), true);
              countDeleted++;
            }
          }
        } catch (XWikiException exp) {
          LOGGER.error("Failed to delete document [" + fullName + "] in wiki ["
              + getContext().getDatabase() + "].", exp);
        }
      }
      return countDeleted;
    } else {
      LOGGER.error("deleting document trash needs admin rights.");
    }
    return null;
  }

  public List<Object> getDeletedAttachments() {
    List<Object> resultList = Collections.emptyList();
    try {
      Query query = queryManager.createQuery(getDeletedAttachmentsHql(), Query.HQL);
      resultList = query.execute();
    } catch (QueryException queryExp) {
      LOGGER.error("Failed to parse or execute deletedAttachments hql query.", queryExp);
    }
    return resultList;
  }

  private String getDeletedAttachmentsHql() {
    return "select datt.id from DeletedAttachment as datt order by datt.filename asc";
  }

  /**
   * 
   * @return empty map means the validation has been successful. Otherwise validation
   *          messages are returned for invalid fields.
   */
  public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
    return formValidationService.validateRequest();
  }

  public com.xpn.xwiki.api.Object getSkinConfigObj() {
    BaseObject skinConfigObj = new SkinConfigObjCommand().getSkinConfigObj();
    if (skinConfigObj != null) {
      return skinConfigObj.newObjectApi(skinConfigObj, getContext());
    } else {
      return null;
    }
  }

  public com.xpn.xwiki.api.Object getSkinConfigObj(String fallbackClassName) {
    BaseObject skinConfigObj = new SkinConfigObjCommand().getSkinConfigObj(
        fallbackClassName);
    if (skinConfigObj != null) {
      return skinConfigObj.newObjectApi(skinConfigObj, getContext());
    } else {
      return null;
    }
  }

  public com.xpn.xwiki.api.Object getSkinConfigFieldInheritor(String fallbackClassName,
      String key) {
    BaseCollection skinConfigBaseColl = new SkinConfigObjCommand(
        ).getSkinConfigFieldInheritor(fallbackClassName).getObject(key);
    if ((skinConfigBaseColl != null) && (skinConfigBaseColl instanceof BaseObject)) {
      BaseObject skinConfigObj = (BaseObject)skinConfigBaseColl;
      return skinConfigObj.newObjectApi(skinConfigObj, getContext());
    } else {
      return null;
    }
  }
  
  public boolean addFileToFileBaseTag(DocumentReference fileDocRef, String fileName, 
      DocumentReference tagDocRef) {
    return addFileToFileBaseTag(fileDocRef.getLastSpaceReference().getName() + "." 
        + fileDocRef.getName(), fileName, tagDocRef);
  }
  
  public boolean addFileToFileBaseTag(String fileDocFullName, String fileName, 
      DocumentReference tagDocRef) {
    //FIXME not all tag documents have a page type: who cares? deprecated? migration?
//    DocumentReference pageTypeDocRef = webUtilsService.resolveDocumentReference(
//        "Celements2.PageType");
    DocumentReference tagClassDocRef = webUtilsService.resolveDocumentReference(
        "Classes.FilebaseTag");
    String tagValue = fileDocFullName + "/" + fileName;
    try {
      XWikiDocument tagDoc = getContext().getWiki().getDocument(tagDocRef, getContext());
      if(/*(tagDoc.getXObject(pageTypeDocRef, "page_type", "FileBaseTag", false) != null)
          && */(tagDoc.getXObject(tagClassDocRef, "attachment", tagValue, false) == null)) {
        BaseObject obj = tagDoc.newXObject(tagClassDocRef, getContext());
        obj.setStringValue("attachment", tagValue);
        getContext().getWiki().saveDocument(tagDoc, getContext());
        return true;
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Could not get tag Document", xwe);
    }
    return false;
  }

  public boolean getUserAdminShowLoginName() {
    boolean showLoginName = (getContext().getWiki().getXWikiPreferenceAsInt(
        "celUserAdminShowLoginName", "celements.administration.showloginname", 0,
        getContext()) != 0);
    return (webUtilsService.isAdvancedAdmin() || showLoginName);
  }

  public String getPossibleLogins() {
    return new PossibleLoginsCommand().getPossibleLogins();
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml) {
    return webUtilsService.cleanupXHTMLtoHTML5(xhtml);
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml, DocumentReference docRef) {
    return webUtilsService.cleanupXHTMLtoHTML5(xhtml, docRef);
  }

  @Deprecated
  public String cleanupXHTMLtoHTML5(String xhtml, SpaceReference layoutRef) {
    return webUtilsService.cleanupXHTMLtoHTML5(xhtml, layoutRef);
  }

  /**
   * Cache should maintain itself. Thus this flushMenuItemCache should not be called
   * anymore.
   * 
   * @deprecated
   */
  @Deprecated
  public void flushMenuItemCache() {
    treeNodeCacheService.flushMenuItemCache();
  }

  public List<com.xpn.xwiki.api.Object> getRTETemplateList() {
    try {
      List<BaseObject> rteTemplateList = rteConfigTemplateService.getRTETemplateList();
      List<com.xpn.xwiki.api.Object> rteTemplateListExternal =
          new ArrayList<com.xpn.xwiki.api.Object>();
      for(BaseObject rteTmpl : rteTemplateList) {
        rteTemplateListExternal.add(rteTmpl.newObjectApi(rteTmpl, getContext()));
      }
      return rteTemplateListExternal;
    } catch (XWikiException exp) {
      LOGGER.error("getRTETemplateList failed.", exp);
    }
    return Collections.emptyList();
  }

  public EntityReference getParentReference(DocumentReference docRef) {
    return treeNodeService.getParentReference(docRef);
  }

  public void moveTreeDocAfter(DocumentReference moveDocRef,
      DocumentReference insertAfterDocRef) {
    try {
      treeNodeService.moveTreeDocAfter(moveDocRef, insertAfterDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get moveDoc [" + moveDocRef + "]", exp);
    }
  }
  
  /**
   * TODO Move after Refactoring to WebUtilScriptService
   */
  public boolean isHighDate(Date date) {
    if ((date != null) && date.compareTo(IWebUtilsService.DATE_HIGH) >= 0) {
      return true;
    } 
    return false;
  }

}
