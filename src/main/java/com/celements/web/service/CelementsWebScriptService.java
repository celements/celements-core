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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

import com.celements.appScript.IAppScriptService;
import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.filebase.FileBaseScriptService;
import com.celements.lastChanged.ILastChangedRole;
import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.celements.metatag.BaseObjectMetaTagProvider;
import com.celements.model.access.ModelAccessScriptService;
import com.celements.navigation.cmd.DeleteMenuItemCommand;
import com.celements.navigation.service.ITreeNodeCache;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.navigation.service.TreeNodeScriptService;
import com.celements.pagetype.service.PageTypeScriptService;
import com.celements.rendering.RenderCommand;
import com.celements.rteConfig.IRTEConfigTemplateRole;
import com.celements.sajson.Builder;
import com.celements.sajson.JsonScriptService;
import com.celements.validation.ValidationType;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.plugin.cmd.CelementsRightsCommand;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.plugin.cmd.DocHeaderTitleCommand;
import com.celements.web.plugin.cmd.DocMetaTagsCmd;
import com.celements.web.plugin.cmd.FormObjStorageCommand;
import com.celements.web.plugin.cmd.ImageMapCommand;
import com.celements.web.plugin.cmd.ParseObjStoreCommand;
import com.celements.web.plugin.cmd.PlainTextCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.RenameCommand;
import com.celements.web.plugin.cmd.ResetProgrammingRightsCommand;
import com.celements.web.plugin.cmd.SkinConfigObjCommand;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDeletedDocument;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.Utils;

@Component("celementsweb")
public class CelementsWebScriptService implements ScriptService {

  private static final String CEL_GLOBALVAL_PREFIX = "celements.globalvalues.";

  private static final String IMAGE_MAP_COMMAND = "com.celements.web.ImageMapCommand";

  private static final String CEL_SUPPORTLINK_URL = "cel_supportLink_url";

  private static final Logger LOGGER = LoggerFactory.getLogger(CelementsWebScriptService.class);

  @Requirement
  QueryManager queryManager;

  @Requirement
  IAppScriptService appScriptService;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  ConfigurationSource configSource;

  @Requirement("legacyskin")
  ScriptService legacySkinScriptService;

  @Requirement
  ITreeNodeCache treeNodeCacheService;

  @Requirement
  ITreeNodeService treeNodeService;

  @Requirement("treeNode")
  ScriptService treeNodeScriptService;

  @Requirement
  IRTEConfigTemplateRole rteConfigTemplateService;

  @Requirement
  IClassesCompositorComponent classesComp;

  @Requirement
  IMandatoryDocumentCompositorRole mandatoryDocComp;

  @Requirement("deprecated")
  ScriptService deprecatedUsage;

  @Requirement
  ILastChangedRole lastChangedSrv;

  @Requirement
  LastStartupTimeStampRole lastStartupTimeStamp;

  @Requirement
  Execution execution;

  @Requirement("xwikiproperties")
  private ConfigurationSource xwikiPropertiesSource;

  /**
   * Property containing the version value in the {@link #VERSION_FILE} file.
   */
  private static final String VERSION_FILE_PROPERTY = "version";

  private HashMap<String, String> versionMap = new HashMap<>();

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
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
    if (isAppScriptRequest()) {
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
    LOGGER.trace("convertToPlainText called on celementsweb script service for [" + htmlContent
        + "].");
    return new PlainTextCommand().convertToPlainText(htmlContent);
  }

  /**
   * @deprecated since 4.0, instead use {@link JsonScriptService#newBuilder()}
   */
  @Deprecated
  public Builder getNewJSONBuilder() {
    return new Builder();
  }

  public boolean deleteMenuItem(DocumentReference docRef) {
    String docFN = webUtilsService.getRefLocalSerializer().serialize(docRef);
    try {
      if (getContext().getWiki().getRightService().hasAccessLevel("edit", getContext().getUser(),
          docFN, getContext())) {
        return new DeleteMenuItemCommand().deleteMenuItem(docRef);
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check 'edit' access rights for user [" + getContext().getUser()
          + "] on document [" + docFN + "]");
    }
    return false;
  }

  public Date getLastUpdatedDate() {
    return lastChangedSrv.getLastUpdatedDate();
  }

  public Date getLastUpdatedDate(SpaceReference spaceRef) {
    return lastChangedSrv.getLastUpdatedDate(spaceRef);
  }

  public Date getLastUpdatedDate(List<SpaceReference> spaceRefList) {
    return lastChangedSrv.getLastUpdatedDate(spaceRefList);
  }

  public List<Object[]> getLastChangedDocuments(int numEntries) {
    return lastChangedSrv.getLastChangedDocuments(numEntries);
  }

  /**
   * @deprecated instead use List<String[]> getLastChangedDocuments(int, SpaceReference)
   */
  @Deprecated
  public List<Object[]> getLastChangedDocuments(int numEntries, String space) {
    return lastChangedSrv.getLastChangedDocuments(numEntries, space);
  }

  public List<Object[]> getLastChangedDocuments(int numEntries, SpaceReference spaceRef) {
    return lastChangedSrv.getLastChangedDocuments(numEntries, spaceRef);
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
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    NumberFormat decimalFormat = NumberFormat.getInstance(locale);
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

  /**
   * @deprecated instead use {@link ModelAccessScriptService}
   */
  @Deprecated
  public Document createDocument(DocumentReference newDocRef) {
    return createDocument(newDocRef, null);
  }

  /**
   * @deprecated instead use {@link ModelAccessScriptService} and {@link PageTypeScriptService}
   */
  @Deprecated
  public Document createDocument(DocumentReference newDocRef, String pageType) {
    LOGGER.trace("create new document for [" + newDocRef + "] and pageType [" + pageType + "].");
    XWikiDocument theNewDoc = new CreateDocumentCommand().createDocument(newDocRef, pageType);
    if (theNewDoc != null) {
      LOGGER.debug("created new document for [" + newDocRef + "] and pageType [" + pageType + "].");
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
    return new AttachmentURLCommand().getExternalAttachmentURL(fileName, action, getContext());
  }

  private RenderCommand getCelementsRenderCmd() {
    RenderCommand renderCommand = new RenderCommand();
    renderCommand.setDefaultPageType("RichText");
    return renderCommand;
  }

  public String renderCelementsDocument(DocumentReference elementDocRef,
      boolean preserveVelocityContext) {
    return renderCelementsDocument(elementDocRef, getContext().getLanguage(), "view", true);
  }

  public String renderCelementsDocument(DocumentReference elementDocRef) {
    return renderCelementsDocument(elementDocRef, getContext().getLanguage(), "view");
  }

  public String renderCelementsDocument(DocumentReference elementDocRef, String renderMode) {
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
        return getCelementsRenderCmd().renderCelementsDocumentPreserveVelocityContext(elementDocRef,
            lang, renderMode);
      } else {
        return getCelementsRenderCmd().renderCelementsDocument(elementDocRef, lang, renderMode);
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
    // we must not get here for !getService().isAppScriptRequest()
    if ("view".equals(getContext().getAction()) && renderDoc.isNew()) {
      LOGGER.info("renderCelementsDocument: Failed to get xwiki document for [{}] no rendering"
          + " applied.", renderDoc.getFullName());
      return "";
    } else {
      return renderCelementsDocument(renderDoc.getDocumentReference(), renderDoc.getLanguage(),
          renderMode);
    }
  }

  public String renderDocument(DocumentReference docRef) {
    LOGGER.trace("renderDocument: docRef [{}].", docRef);
    return new RenderCommand().renderDocument(docRef);
  }

  public String renderDocument(DocumentReference docRef, DocumentReference includeDocRef) {
    LOGGER.trace("renderDocument: docRef [{}] and includeDocRef [{}].", docRef, includeDocRef);
    return new RenderCommand().renderDocument(docRef, includeDocRef);
  }

  public String renderDocument(DocumentReference docRef, String lang) {
    LOGGER.trace("renderDocument: lang [{}] docRef [{}].", lang, docRef);
    return new RenderCommand().renderDocument(docRef, lang);
  }

  public String renderDocument(DocumentReference docRef, DocumentReference includeDocRef,
      String lang) {
    LOGGER.trace("renderDocument: lang [{}] docRef [{}] and includeDocRef [{}].", lang, docRef,
        includeDocRef);
    return new RenderCommand().renderDocument(docRef, includeDocRef, lang);
  }

  public String renderDocument(Document renderDoc) {
    LOGGER.trace("renderDocument: renderDocLang [{}] renderDoc [{}].", renderDoc.getLanguage(),
        renderDoc.getDocumentReference());
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
      LOGGER.error("renderCelementsDocument: Failed to render [{}] lang [{}].", docRef, lang, exp);
    }
    return "";
  }

  public String renderDocument(Document renderDoc, boolean removePre,
      List<String> rendererNameList) {
    return renderDocument(renderDoc.getDocumentReference(), renderDoc.getLanguage(), removePre,
        rendererNameList);
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

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
  public String getDefaultLanguage() {
    return webUtilsService.getDefaultLanguage();
  }

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
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
    String deletedDocsHql = "select distinct ddoc.fullName" + " from XWikiDeletedDocument as ddoc";
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
   *
   * @return
   */
  public Integer permanentlyEmptyTrash(int waitDays) {
    Calendar beforeWaiteDaysCal = Calendar.getInstance();
    beforeWaiteDaysCal.add(Calendar.DATE, -waitDays);
    Date delBeforeDate = beforeWaiteDaysCal.getTime();
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
      int countDeleted = 0;
      for (String fullName : getDeletedDocuments("", false)) {
        try {
          for (XWikiDeletedDocument delDoc : getContext().getWiki().getDeletedDocuments(fullName,
              "", getContext())) {
            int seconds = (int) ((getWaitDaysBeforeDelete() * 24 * 60 * 60) + 0.5);
            Calendar cal = Calendar.getInstance();
            cal.setTime(delDoc.getDate());
            cal.add(Calendar.SECOND, seconds);
            boolean isAfterMinWaitDays = cal.before(Calendar.getInstance());
            if (isAfterMinWaitDays && delDoc.getDate().before(delBeforeDate)) {
              XWikiDocument doc = getContext().getWiki().getDocument(
                  webUtilsService.resolveDocumentReference(fullName), getContext());
              getContext().getWiki().getRecycleBinStore().deleteFromRecycleBin(doc, delDoc.getId(),
                  getContext(), true);
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
   * permanentlyEmptyAttachmentTrash delete all documents after waitDays and minWaitDays
   *
   * @return
   */
  public boolean permanentlyEmptyAttachmentTrash(int waitDays) {
    int result = 0;
    Calendar beforeWaiteDaysCal = Calendar.getInstance();
    beforeWaiteDaysCal.add(Calendar.DATE, -waitDays);
    Date delBeforeDate = beforeWaiteDaysCal.getTime();
    if (getContext().getWiki().getRightService().hasAdminRights(getContext())) {
      try {
        Session sess = getNewHibSession(getContext());
        Transaction transaction = sess.beginTransaction();
        org.hibernate.Query query = sess.createSQLQuery("delete from xwikiattrecyclebin"
            + " where XDA_DATE < :deleteBeforeDate");
        query.setParameter("deleteBeforeDate", delBeforeDate);
        result = query.executeUpdate();
        LOGGER.info("deleted [{}] attachments in database [{}].", result,
            getContext().getDatabase());
        transaction.commit();
      } catch (XWikiException exp) {
        LOGGER.error("permanentlyEmptyAttachmentTrash: failed to get a hibernate session. ", exp);
      }
    } else {
      LOGGER.error("deleting document trash needs admin rights.");
    }
    return (result > 0);
  }

  private Session getNewHibSession(XWikiContext context) throws XWikiException {
    Session session = context.getWiki().getHibernateStore().getSessionFactory().openSession();
    context.getWiki().getHibernateStore().setDatabase(session, context);
    return session;
  }

  /**
   * @deprecated since 2.59 instead use {@link EditorSupportScriptService #validateRequest()}
   * @return empty map means the validation has been successful. Otherwise validation messages are
   *         returned for invalid fields.
   */
  @Deprecated
  public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
    return getEditorSupportScriptService().validateRequest();
  }

  /**
   * getLastStartupTimeStamp to solve browser caching issues with files on disk e.g. tinymce
   *
   * @return
   */
  public String getLastStartupTimeStamp() {
    return lastStartupTimeStamp.getLastStartupTimeStamp();
  }

  /**
   * @deprecated since 4.0 {@link BaseObjectMetaTagProvider} collects all the tags on its own
   */
  @Deprecated
  public Map<String, String> getDocMetaTags(String language, String defaultLanguage) {
    return new DocMetaTagsCmd().getDocMetaTags(language, defaultLanguage, getContext());
  }

  /**
   * @deprecated since 2.59 instead use {@link LegacySkinScriptService #getSkinConfigObj()}
   */
  @Deprecated
  public com.xpn.xwiki.api.Object getSkinConfigObj() {
    return ((LegacySkinScriptService) legacySkinScriptService).getSkinConfigObj();
  }

  public com.xpn.xwiki.api.Object getSkinConfigObj(String fallbackClassName) {
    BaseObject skinConfigObj = new SkinConfigObjCommand().getSkinConfigObj(fallbackClassName);
    if (skinConfigObj != null) {
      return skinConfigObj.newObjectApi(skinConfigObj, getContext());
    } else {
      return null;
    }
  }

  public com.xpn.xwiki.api.Object getSkinConfigFieldInheritor(String fallbackClassName,
      String key) {
    BaseCollection skinConfigBaseColl = new SkinConfigObjCommand().getSkinConfigFieldInheritor(
        fallbackClassName).getObject(key);
    if ((skinConfigBaseColl != null) && (skinConfigBaseColl instanceof BaseObject)) {
      BaseObject skinConfigObj = (BaseObject) skinConfigBaseColl;
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
    // FIXME not all tag documents have a page type: who cares? deprecated? migration?
    // DocumentReference pageTypeDocRef = webUtilsService.resolveDocumentReference(
    // "Celements2.PageType");
    DocumentReference tagClassDocRef = webUtilsService.resolveDocumentReference(
        "Classes.FilebaseTag");
    String tagValue = fileDocFullName + "/" + fileName;
    try {
      XWikiDocument tagDoc = getContext().getWiki().getDocument(tagDocRef, getContext());
      if (/*
           * (tagDoc.getXObject(pageTypeDocRef, "page_type", "FileBaseTag", false) != null) &&
           */(tagDoc.getXObject(tagClassDocRef, "attachment", tagValue, false) == null)) {
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
   * The context.isMainWiki method is broken for virtual usage. This is a replacement.
   */
  public boolean isMainWiki() {
    return getContext() == null ? null : isMainWiki(getContext().getDatabase());
  }

  /**
   * The context.isMainWiki method is broken for virtual usage. This is a replacement.
   */
  public boolean isMainWiki(String wikiName) {
    return getContext() == null ? null
        : (!getContext().getWiki().isVirtualMode() || (wikiName == null ? getMainWikiName() == null
            : wikiName.equalsIgnoreCase(getMainWikiName())));
  }

  /**
   * The context.getMainXWiki method is broken for virtual usage. This is a replacement. return
   * schema = wiki.Param("xwiki.db");
   */
  public String getMainWikiName() {
    return (getContext() != null ? getContext().getWiki().Param("xwiki.db") : null);
  }

  /**
   * Cache should maintain itself. Thus this flushMenuItemCache should not be called anymore.
   *
   * @deprecated
   */
  @Deprecated
  public void flushMenuItemCache() {
    treeNodeCacheService.flushMenuItemCache();
  }

  public void checkClasses() {
    classesComp.checkClasses();
  }

  public boolean isClassCollectionActivated(String name) {
    return classesComp.isActivated(name);
  }

  public void checkMandatoryDocuments() {
    mandatoryDocComp.checkAllMandatoryDocuments();
  }

  public String getDefaultSpace() {
    return getContext().getWiki().getDefaultSpace(getContext());
  }

  public boolean isCelementsRights(DocumentReference docRef) {
    return new CelementsRightsCommand().isCelementsRights(
        getWebUtilsService().getRefDefaultSerializer().serialize(docRef), getContext());
  }

  public Map<String, String> getObjStoreOptionsMap(String options) {
    return (new ParseObjStoreCommand()).getObjStoreOptionsMap(options, getContext());
  }

  public com.xpn.xwiki.api.Object newObjectForFormStorage(Document storageDoc, String className) {
    if (hasProgrammingRights()) {
      BaseObject newStoreObj = new FormObjStorageCommand().newObject(storageDoc.getDocument(),
          className, getContext());
      if (newStoreObj != null) {
        return newStoreObj.newObjectApi(newStoreObj, getContext());
      }
    }
    return null;
  }

  /**
   * @deprecated since 2.65.0 instead use logDeprecatedVelocityScript in
   *             DeprecatedUsageScriptService: $services.deprecated.logVelocityScript
   */
  @Deprecated
  public void logDeprecatedVelocityScript(String logMessage) {
    ((DeprecatedUsageScriptService) deprecatedUsage).logVelocityScript(logMessage);
  }

  public String getDocHeaderTitle(DocumentReference docRef) {
    return new DocHeaderTitleCommand().getDocHeaderTitle(docRef);
  }

  /**
   * @deprecated since 2.59.1 instead use clearFileName in FileBaseScriptService
   */
  @Deprecated
  public String clearFileName(String fileName) {
    return ((FileBaseScriptService) Utils.getComponent(ScriptService.class,
        "filebase")).clearFileName(fileName);
  }

  public boolean isTranslationAvailable(Document doc, String language) {
    try {
      return doc.getTranslationList().contains(language);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get TranslationList for [" + doc.getFullName() + "].", exp);
      return (language.equals(getWebUtilsService().getDefaultLanguage())
          && getContext().getWiki().exists(doc.getDocumentReference(), getContext()));
    }
  }

  public String getEditURL(Document doc) {
    if (!getContext().getWiki().exists(doc.getDocumentReference(), getContext())
        || !isValidLanguage() || !isTranslationAvailable(doc, getContext().getLanguage())) {
      return doc.getURL("edit", "language=" + getWebUtilsService().getDefaultLanguage());
    } else {
      return doc.getURL("edit", "language=" + getContext().getLanguage());
    }
  }

  public boolean isValidLanguage() {
    return getWebUtilsScriptService().getAllowedLanguages().contains(getContext().getLanguage());
  }

  public boolean resetProgrammingRights() {
    if (hasAdminRights()) {
      return new ResetProgrammingRightsCommand().resetCelements2webRigths(getContext());
    } else {
      LOGGER.warn("user [" + getContext().getUser()
          + "] tried to reset programming rights, but has no admin rights.");
    }
    return false;
  }

  /**
   * @deprecated instead use versions directory and getVersion(String)
   */
  @Deprecated
  public String getCelementsWebCoreVersion() {
    return getContext().getWiki().Param("com.celements.version");
  }

  public String getCelementsMainAppName() {
    return xwikiPropertiesSource.getProperty("celements.appname", "");
  }

  public String getCelementsWebAppVersion() {
    DocumentReference centralAppDocRef = new DocumentReference("celements2web", "XApp",
        "XWikiApplicationCelements2web");
    DocumentReference xappClassDocRef = new DocumentReference("celements2web", "XAppClasses",
        "XWikiApplicationClass");
    try {
      XWikiDocument appReceiptDoc = getContext().getWiki().getDocument(centralAppDocRef,
          getContext());
      BaseObject appClassObj = appReceiptDoc.getXObject(xappClassDocRef);
      if (appClassObj != null) {
        return appClassObj.getStringValue("appversion");
      }
    } catch (XWikiException exp) {
      LOGGER.warn("Failed to get celementsWeb Application scripts version.", exp);
    }
    return "N/A";
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and write the
   * exception in a log-file
   */
  public int getNextObjPageId(SpaceReference spaceRef, DocumentReference classRef,
      String propertyName) throws XWikiException {
    String sql = ", BaseObject as obj, IntegerProperty as art_id";
    sql += " where obj.name=doc.fullName";
    sql += " and obj.className='" + getWebUtilsService().getRefLocalSerializer().serialize(classRef)
        + "'";
    sql += " and doc.space='" + spaceRef.getName() + "' and obj.id = art_id.id.id";
    sql += " and art_id.id.name='" + propertyName + "' order by art_id.value desc";
    int nextId = 1;
    List<XWikiDocument> docs = getContext().getWiki().getStore().searchDocuments(sql, getContext());
    if (docs.size() > 0) {
      nextId = 1 + docs.get(0).getXObject(classRef).getIntValue(propertyName);
    }
    return nextId;
  }

  public String getEmailAdressForCurrentUser() {
    return getCelementsWebService().getEmailAdressForUser(
        getWebUtilsService().resolveDocumentReference(getContext().getUser()));
  }

  public String getEmailAdressForUser(String username) {
    if (hasProgrammingRights()) {
      return getCelementsWebService().getEmailAdressForUser(
          getWebUtilsService().resolveDocumentReference(username));
    } else {
      return null;
    }
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and write the
   * exception in a log-file
   */
  public int createUser() throws XWikiException {
    return getCelementsWebService().createUser(true);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and write the
   * exception in a log-file
   */
  public int createUser(boolean validate) throws XWikiException {
    return getCelementsWebService().createUser(validate);
  }

  public boolean addTranslation(DocumentReference docRef, String language) {
    return new AddTranslationCommand().addTranslation(docRef, language);
  }

  public List<String> renameSpace(String spaceName, String newSpaceName) {
    return new RenameCommand().renameSpace(spaceName, newSpaceName, getContext());
  }

  public boolean renameDoc(DocumentReference docRef, String newDocName) {
    return new RenameCommand().renameDoc(getWebUtilsService().getRefDefaultSerializer().serialize(
        docRef), newDocName, getContext());
  }

  public List<String> getSupportedAdminLanguages() {
    return getCelementsWebService().getSupportedAdminLanguages();
  }

  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    return getCelementsWebService().writeUTF8Response(filename, renderDocFullName);
  }

  public boolean resetLastStartupTimeStamp() {
    if (hasProgrammingRights()) {
      lastStartupTimeStamp.resetLastStartupTimeStamp();
      return true;
    }
    return false;
  }

  public List<com.xpn.xwiki.api.Object> getRTETemplateList() {
    try {
      List<BaseObject> rteTemplateList = rteConfigTemplateService.getRTETemplateList();
      List<com.xpn.xwiki.api.Object> rteTemplateListExternal = new ArrayList<>();
      for (BaseObject rteTmpl : rteTemplateList) {
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

  public void setGlobalContextValue(String key, Object value) {
    LOGGER.debug("setGlobalContextValue: key '{}', value '{}'", key, value);
    execution.getContext().setProperty(CEL_GLOBALVAL_PREFIX + key, value);
  }

  public Object getGlobalContextValue(String key) {
    Object value = execution.getContext().getProperty(CEL_GLOBALVAL_PREFIX + key);
    LOGGER.debug("getGlobalContextValue: key '{}', value '{}'", key, value);
    return value;
  }

  public boolean useMultiselect() {
    return "1".equals(getContext().getWiki().getSpacePreference("celMultiselect",
        "celements.celMultiselect", "0", getContext()));
  }

  /**
   * @deprecated since 2.64.0 instead use TreeNodeScriptService.moveTreeDocAfter
   */
  @Deprecated
  public void moveTreeDocAfter(DocumentReference moveDocRef, DocumentReference insertAfterDocRef) {
    getTreeNodeScriptService().moveTreeDocAfter(moveDocRef, insertAfterDocRef);
  }

  private boolean hasAdminRights() {
    return getContext().getWiki().getRightService().hasAdminRights(getContext());
  }

  private boolean hasProgrammingRights() {
    return getContext().getWiki().getRightService().hasProgrammingRights(getContext());
  }

  private EditorSupportScriptService getEditorSupportScriptService() {
    return (EditorSupportScriptService) Utils.getComponent(ScriptService.class, "editorsupport");
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private ICelementsWebServiceRole getCelementsWebService() {
    return Utils.getComponent(ICelementsWebServiceRole.class);
  }

  private WebUtilsScriptService getWebUtilsScriptService() {
    return (WebUtilsScriptService) Utils.getComponent(ScriptService.class, "webUtils");
  }

  /**
   * TODO Move after Refactoring to WebUtilScriptService
   */
  public boolean isHighDate(Date date) {
    if ((date != null) && (date.compareTo(IWebUtilsService.DATE_HIGH) >= 0)) {
      return true;
    }
    return false;
  }

  public boolean hasGoogleAnalytics() {
    return !StringUtils.isEmpty(getGoogleAnalytics());
  }

  public String getGoogleAnalytics() {
    return getContext().getWiki().getXWikiPreference("celGoogleAnalyticsAccount", "", getContext());
  }

  private TreeNodeScriptService getTreeNodeScriptService() {
    return (TreeNodeScriptService) treeNodeScriptService;
  }

  public String getVersion(String systemComponentName) {
    if (!this.versionMap.containsKey(systemComponentName)) {
      String versionFile = "/WEB-INF/versions/" + systemComponentName + ".properties";
      try {
        InputStream is = getContext().getWiki().getResourceAsStream(versionFile);
        XWikiConfig properties = new XWikiConfig(is);
        this.versionMap.put(systemComponentName, properties.getProperty(VERSION_FILE_PROPERTY));
      } catch (MalformedURLException | XWikiException exp) {
        // Failed to retrieve the version, log a warning and default to "Unknown"
        LOGGER.warn("Failed to retrieve XWiki's version from [" + versionFile + "], using the ["
            + VERSION_FILE_PROPERTY + "] property.", exp);
        return "Unknown version";
      }
    }
    return this.versionMap.get(systemComponentName);
  }

  public Joiner getJoiner(String separator) {
    return Joiner.on(Strings.nullToEmpty(separator));
  }

  public Splitter getSplitter(String separator) {
    return Splitter.on(Strings.nullToEmpty(separator));
  }

  @NotNull
  public String getSupportLinkURL() {
    String url = webUtilsService.getAdminMessageTool().get(CEL_SUPPORTLINK_URL);
    if (url.equals(CEL_SUPPORTLINK_URL)) {
      url = configSource.getProperty(CEL_SUPPORTLINK_URL + "_" + webUtilsService.getAdminLanguage(),
          "");
      if (url.isEmpty()) {
        url = configSource.getProperty(CEL_SUPPORTLINK_URL, "");
      }
    }
    return url;
  }

  /**
   * @deprecated since 3.1, only intended for internal usage
   */
  @Deprecated
  public String encodeUrlToUtf8(String urlStr) {
    return getCelementsWebService().encodeUrlToUtf8(urlStr);
  }

  public void sendRedirect(String urlStr) {
    getCelementsWebService().sendRedirect(urlStr);
  }

}
