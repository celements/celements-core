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
package com.celements.web.plugin.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.menu.MenuScriptService;
import com.celements.navigation.NavigationApi;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.TreeNodeScriptService;
import com.celements.pagetype.IPageType;
import com.celements.pagetype.PageTypeApi;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.celements.rendering.RenderCommand;
import com.celements.rteConfig.RTEConfig;
import com.celements.sajson.Builder;
import com.celements.validation.ValidationType;
import com.celements.web.contextmenu.ContextMenuBuilderApi;
import com.celements.web.contextmenu.ContextMenuItemApi;
import com.celements.web.css.CSS;
import com.celements.web.plugin.CelementsWebPlugin;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.celements.web.plugin.cmd.CelementsRightsCommand;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.celements.web.plugin.cmd.CssCommand;
import com.celements.web.plugin.cmd.DocFormCommand;
import com.celements.web.plugin.cmd.DocHeaderTitleCommand;
import com.celements.web.plugin.cmd.FormObjStorageCommand;
import com.celements.web.plugin.cmd.ISynCustom;
import com.celements.web.plugin.cmd.LastStartupTimeStamp;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.plugin.cmd.ParseObjStoreCommand;
import com.celements.web.plugin.cmd.RenameCommand;
import com.celements.web.plugin.cmd.ResetProgrammingRightsCommand;
import com.celements.web.service.ActionScriptService;
import com.celements.web.service.AppScriptScriptService;
import com.celements.web.service.AuthenticationScriptService;
import com.celements.web.service.CSSScriptService;
import com.celements.web.service.CaptchaScriptService;
import com.celements.web.service.CelMailScriptService;
import com.celements.web.service.CelementsWebScriptService;
import com.celements.web.service.ContextMenuScriptService;
import com.celements.web.service.DocFormScriptService;
import com.celements.web.service.EditorSupportScriptService;
import com.celements.web.service.EmptyCheckScriptService;
import com.celements.web.service.FileBaseScriptService;
import com.celements.web.service.IPrepareVelocityContext;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.service.ImageScriptService;
import com.celements.web.service.JSScriptService;
import com.celements.web.service.LayoutScriptService;
import com.celements.web.service.LegacySkinScriptService;
import com.celements.web.service.NextFreeDocScriptService;
import com.celements.web.service.RTEConfigScriptService;
import com.celements.web.service.WebUtilsScriptService;
import com.celements.web.service.WebUtilsService;
import com.celements.web.utils.DocumentCreationWorkerControlApi;
import com.celements.web.utils.SuggestBaseClass;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class CelementsWebPluginApi extends Api {

  public static final String CELEMENTS_CSSCOMMAND = "com.celements.web.CssCommand";

  /**
   * @Deprecated: 
   *  
   */
  @Deprecated
  public static final String JAVA_SCRIPT_FILES_COMMAND_KEY =
      JSScriptService.JAVA_SCRIPT_FILES_COMMAND_KEY;

  public static final String CELEMENTS_PAGE_LAYOUT_COMMAND =
      "com.celements.web.PageLayoutCommand";

  private static final String _DOC_FORM_COMMAND_OBJECT = "com.celements.DocFormCommand";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebPluginApi.class);

  private CelementsWebPlugin plugin;

  /**
   * @deprecated since ???NEXTRELEASE???
   */
  @Deprecated
  public CelementsWebPluginApi(
      CelementsWebPlugin plugin,
      XWikiContext context) {
    super(context);
    setPlugin(plugin);
  }

  /**
   * @deprecated since ???NEXTRELEASE???
   */
  @Deprecated
  //FIXME must check programming Rights!!!
  public CelementsWebPlugin getPlugin(){
    return plugin;
  }

  /**
   * @deprecated since ???NEXTRELEASE???
   */
  @Deprecated
  //FIXME must not be public! why do we need it in the API class anyway?
  public void setPlugin(CelementsWebPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * @deprecated since 2.2 instead use TreeNodeCache
   * Do not call flushCache for MenuItem changes anymore.  
   * The TreeNodeDocument change listener take care of flushing the cache if needed.
   */
  @Deprecated
  public void flushCache() {
    LOGGER.warn("flushCache called. Do not call flushCache for MenuItem "
        + " changes anymore. The TreeNodeDocument change listener take care of flushing "
        + " the cache if needed.");
  }

  /**
   * @deprecated since 2.2 instead use CelementsWebScriptService
   */
  @Deprecated
  public String getLastStartupTimeStamp() {
    return getScriptService().getLastStartupTimeStamp();
  }

  public boolean resetLastStartupTimeStamp() {
    if (hasProgrammingRights()) {
      new LastStartupTimeStamp().resetLastStartupTimeStamp();
      return true;
    }
    return false;
  }

  /**
   * @deprecated since 2.11.4 instead use contextMenu script service
   */
  @Deprecated
  public ContextMenuBuilderApi getContextMenuBuilder() {
    return getContextMenuScriptService().getContextMenuBuilder();
  }

  /**
   * @deprecated since 2.11.4 instead use contextMenu script service
   */
  @Deprecated
  public String getAllContextMenuCSSClassesAsJSON() {
    return getContextMenuScriptService().getAllContextMenuCSSClassesAsJSON();
  }

  /**
   * @deprecated since 2.33.0
   */
  @Deprecated
  public ContextMenuItemApi getWrapper(com.xpn.xwiki.api.Object menuItem,
      String elemId) {
    return new ContextMenuItemApi(menuItem, elemId, context);
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public NavigationApi createNavigation() {
    return getTreeNodeScriptService().createNavigation();
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public void enableMappedMenuItems() {
    getTreeNodeScriptService().enableMappedMenuItems();
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public int getMaxConfiguredNavigationLevel() {
    return getTreeNodeScriptService().getMaxConfiguredNavigationLevel();
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public boolean isTreeNode(DocumentReference docRef) {
    return getTreeNodeScriptService().isTreeNode(docRef);
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public boolean isNavigationEnabled(String configName) {
    return getTreeNodeScriptService().isNavigationEnabled(configName);
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public NavigationApi getNavigation(String configName) {
    return getTreeNodeScriptService().getNavigation(configName);
  }

  /**
   * @deprecated since 2.2 use treeNode script service instead
   */
  @Deprecated
  public String includeNavigation(String configName) {
    return getTreeNodeScriptService().includeNavigation(configName);
  }

  /**
   * @deprecated since 2.2 use getSubNodesForParentRef in treeNode script service instead
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace) {
    return plugin.getSubMenuItemsForParent(parent, menuSpace, "", context);
  }

  /**
   * @deprecated since 2.2 use getSubNodesForParentRef in treeNode script service instead
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart) {
    return plugin.getSubMenuItemsForParent(parent, menuSpace, menuPart, context);
  }

  /**
   * @deprecated since 2.24.0 instead use getSubNodesForParentRef(EntityReference)
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace) {
    return WebUtils.getInstance().getSubNodesForParent(parent, menuSpace, "", context);
  }

  /**
   * @deprecated since 2.2 instead use {@link TreeNodeScriptService
   * #getSubNodesForParentRef(EntityReference)}
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParentRef(EntityReference parentRef) {
    return getTreeNodeScriptService().getSubNodesForParent(parentRef, "");
  }

  /**
   * @deprecated since 2.24.0 instead use getSubNodesForParentRef(EntityReference, String)
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart) {
    return WebUtils.getInstance().getSubNodesForParent(parent, menuSpace, menuPart,
        context);
  }

  /**
   * @deprecated since 2.2 instead use {@link TreeNodeScriptService
   * #getSubNodesForParent(EntityReference, String)}
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(EntityReference parentRef, String menuPart) {
    return getTreeNodeScriptService().getSubNodesForParent(parentRef, menuPart);
  }

  /**
   * @deprecated since 2.33.0 use treeNode script service instead
   */
  @Deprecated
  public int queryCount() {
    return getTreeNodeScriptService().queryCount();
  }

  /**
   * @deprecated since 2.33.0 instead use {@link CelementsWebScriptService
   * #getDocMetaTags(String, String)}
   */
  @Deprecated
  public Map<String, String> getDocMetaTags(String language, String defaultLanguage) {
    return getScriptService().getDocMetaTags(language, defaultLanguage);
  }

  /**
   * @Deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsService
   * #getAttachmentListSortedSpace(String, String, boolean, int, int)}
   */
  @Deprecated
  public List<Attachment> getAttachmentListSortedSpace(String spaceName,
      String comparator, boolean imagesOnly, int start, int nb
      ) throws ClassNotFoundException {
    return getWebUtilsService().getAttachmentListSortedSpace(spaceName, comparator, 
        imagesOnly, start, nb);
  }

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator
      ) throws ClassNotFoundException{
    return getWebUtilsScriptService().getAttachmentListSorted(doc, comparator);
  }

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return getWebUtilsScriptService().getAttachmentListSorted(doc, comparator, imagesOnly,
        start, nb);
  }

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly) throws ClassNotFoundException{
    return getWebUtilsScriptService().getAttachmentListSortedAsJSON(doc, comparator,
        imagesOnly);
  }

  /**
   * @deprecated since 2.33.0 use WebUtilsScriptService
   */
  @Deprecated
  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return getWebUtilsScriptService().getAttachmentListSortedAsJSON(doc, comparator,
        imagesOnly, start, nb);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link ImageScriptService
   * #getRandomImages(String, int)}
   */
  @Deprecated
  public List<Attachment> getRandomImages(String fullName,
      int num) throws ClassNotFoundException{
    return getImageScriptService().getRandomImages(fullName, num);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getMessageTool(String)}
   */
  @Deprecated
  public XWikiMessageTool getMessageTool(String adminLanguage) {
    return getWebUtilsScriptService().getMessageTool(adminLanguage);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? always celements3 mode by now
   */
  @Deprecated
  public String getVersionMode() {
    return plugin.getVersionMode(context);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link JSScriptService
   * #getAllExternalJavaScriptFiles()}
   */
  @Deprecated
  public String getAllExternalJavaScriptFiles() throws XWikiException {
    return getJSScriptService().getAllExternalJavaScriptFiles();
  }

  /**
   * @deprecated since 2.11.3 instead use {@link CelementsWebScriptService
   * #addImageMapConfig(String)}
   */
  @Deprecated
  public void addImageMapConfig(String configName) {
    getScriptService().addImageMapConfig(configName);
  }

  /**
   * @deprecated since 2.11.3 instead use {@link CelementsWebScriptService
   * #displayImageMapConfigs()}
   */
  @Deprecated
  public String displayImageMapConfigs() {
    return getScriptService().displayImageMapConfigs();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link JSScriptService
   * #addExtJSfileOnce(String)}
   */
  @Deprecated
  public String addExtJSfileOnce(String jsFile) {
    return getJSScriptService().addExtJSfileOnce(jsFile);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link JSScriptService
   * #addExtJSfileOnce(String, String)}
   */
  @Deprecated
  public String addExtJSfileOnce(String jsFile, String action) {
    return getJSScriptService().addExtJSfileOnce(jsFile, action);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getUsernameForUserData(String)}
   */
  @Deprecated
  public String getUsernameForUserData(String login) {
    return getAuthenticationScriptService().getUsernameForUserData(login);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getUsernameForUserData(String, String)}
   */
  @Deprecated
  public String getUsernameForUserData(String login, String possibleLogins) {
    return getAuthenticationScriptService().getUsernameForUserData(login, possibleLogins);
  }

  public int getNextObjPageId(String spacename, String classname, String propertyName) throws XWikiException{
    String sql = ", BaseObject as obj, IntegerProperty as art_id";
    sql += " where obj.name=doc.fullName";
    sql += " and obj.className='" + classname + "'";
    sql += " and doc.space='" + spacename + "' and obj.id = art_id.id.id";
    sql += " and art_id.id.name='" + propertyName + "' order by art_id.value desc";

    int nextId = 1;

    List<XWikiDocument> docs = context.getWiki().getStore().searchDocuments(sql, context);
    if(docs.size() > 0){
      nextId = 1 + docs.get(0).getObject(classname).getIntValue(propertyName);
    }

    return nextId;
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getPasswordHash(String, String)}
   */
  @Deprecated
  public String encryptString(String encoding, String str){
    return getAuthenticationScriptService().getPasswordHash(encoding, str);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getPasswordHash(String)}
   */
  @Deprecated
  public String encryptString(String str){
    return getAuthenticationScriptService().getPasswordHash(str);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #sendNewValidation(String, String)}
   */
  @Deprecated
  public boolean sendNewValidation(String user, String possibleFields) {
    return getAuthenticationScriptService().sendNewValidation(user, possibleFields);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #sendNewValidation(String, String, DocumentReference)}
   */
  @Deprecated
  public void sendNewValidation(String user, String possibleFields,
      DocumentReference mailContentDocRef) {
    getAuthenticationScriptService().sendNewValidation(user, possibleFields, 
        mailContentDocRef);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getNewValidationTokenForUser()}
   */
  @Deprecated
  public String getNewValidationTokenForUser() {
    return getAuthenticationScriptService().getNewValidationTokenForUser();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getNewCelementsTokenForUser()}
   */
  @Deprecated
  public String getNewCelementsTokenForUser() {
    return getAuthenticationScriptService().getNewCelementsTokenForUser();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getNewCelementsTokenForUser(Boolean)}
   */
  @Deprecated
  public String getNewCelementsTokenForUser(Boolean guestPlus) {
    return getAuthenticationScriptService().getNewCelementsTokenForUser(guestPlus);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getNewCelementsTokenForUser(Boolean, int)}
   */
  @Deprecated
  public String getNewCelementsTokenForUser(Boolean guestPlus, int minutesValid) {
    return getAuthenticationScriptService().getNewCelementsTokenForUser(guestPlus, 
        minutesValid);
  }

  //  /**
  //   * If a template in the template dir on disk is parsed the hasProbrammingRights will
  //   * return false, because the sdoc and idoc are NULL. hasCelProgrammingRights in contrast
  //   * returns true in this case.
  //   * TODO: Check if there are other cases in which the sdoc AND idoc are null. Check
  //   * TODO: if there is a better way to recognize that a template from disk is rendered.
  //   *
  //   * @return
  //   */
  //  public boolean hasCelProgrammingRights() {
  //    return (hasProgrammingRights()
  //        || ((context.get("sdoc") == null) && (context.get("idoc") == null)));
  //  }

  private CssCommand getCssCmd() {
    if (!context.containsKey(CELEMENTS_CSSCOMMAND)) {
      context.put(CELEMENTS_CSSCOMMAND, new CssCommand());
    }
    return (CssCommand) context.get(CELEMENTS_CSSCOMMAND);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #getAllCSS()}
   */
  @Deprecated
  public List<CSS> getAllCSS() throws XWikiException{
    return getCSSScriptService().getAllCSS();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #displayAllCSS()}
   */
  @Deprecated
  public String displayAllCSS() throws XWikiException{
    return getCSSScriptService().displayAllCSS();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #getRTEContentCSS()}
   */
  @Deprecated
  public List<CSS> getRTEContentCSS() throws XWikiException{
    return getCSSScriptService().getRTEContentCSS();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #includeCSSPage(String)}
   */
  @Deprecated
  public void includeCSSPage(String css) {
    getCSSScriptService().includeCSSPage(css);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #includeCSSAfterPreferences(String)}
   */
  @Deprecated
  public void includeCSSAfterPreferences(String css) throws XWikiException{
    getCSSScriptService().includeCSSAfterPreferences(css);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CSSScriptService
   * #includeCSSAfterSkin(String)}
   */
  @Deprecated
  public void includeCSSAfterSkin(String css){
    getCSSScriptService().includeCSSAfterSkin(css);
  }

  /**
   * @deprecated since 2.9.4 instead use {@link EmptyCheckScriptService
   * #isEmptyRTEDocument(DocumentReference)}
   **/
  @Deprecated
  public boolean isEmptyRTEDocument(String fullName) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("usage of deprecated isEmptyRTEDocument(String) on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    return getEmptyCheckService().isEmptyRTEDocument(getWebUtilsService(
        ).resolveDocumentReference(fullName));
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link EmptyCheckScriptService
   * #isEmptyRTEDocument(DocumentReference)}
   */
  @Deprecated  
  public boolean isEmptyRTEDocument(DocumentReference documentRef) {
    return getEmptyCheckService().isEmptyRTEDocument(documentRef);
  }

  public String getEmailAdressForCurrentUser() {
    return plugin.getEmailAdressForUser(context.getUser(), context);
  }

  public String getEmailAdressForUser(String username) {
    if (hasProgrammingRights()) {
      return plugin.getEmailAdressForUser(username, context);
    } else {
      return null;
    }
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #activateAccount(String)}
   */
  @Deprecated
  public Map<String, String> activateAccount(String activationCode) throws XWikiException{
    return getAuthenticationScriptService().activateAccount(activationCode);
  }

  /**
   * Returns a list of all parent for a specified doc
   * 
   * @param fullName
   * @param includeDoc
   * @return List of all parents, starting at the specified doc (bottom up)
   * 
   * @deprecated since 2.41.0 instead use {@link WebUtilsScriptService
   * #getDocumentParentsDocRefList(DocumentReference, boolean)}
   */
  @Deprecated
  public List<String> getDocumentParentsList(String fullName, boolean includeDoc) {
    return WebUtils.getInstance().getDocumentParentsList(fullName, includeDoc,
        context);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getDocumentParentsDocRefList(DocumentReference, boolean)}
   * 
   * Returns a list of all parent for a specified doc
   * 
   * @param fullName
   * @param includeDoc
   * @return List of all parents, starting at the specified doc (bottom up)
   */
  @Deprecated
  public List<DocumentReference> getDocumentParentsDocRefList(DocumentReference docRef,
      boolean includeDoc) {
    return getWebUtilsScriptService().getDocumentParentsDocRefList(docRef, includeDoc);
  }

  /**
   * provides a pageTypeApi for the celements document <code>fullname</code>.
   * e.g. getPageType(fullName).getPageType() provides the PageType name given
   * by the pageType-object on the fullname.
   * 
   * @param fullName of the celements document
   * @return
   * @throws XWikiException
   * 
   * @deprecated since 2.21.0 instead use
   *             com.celements.pagetype.service.PageTypeScriptService.getPageTypeConfig()
   */
  @Deprecated
  public IPageType getPageType(String fullName) throws XWikiException {
    return new PageTypeApi(fullName, context);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LegacySkinScriptService
   * #getSkinConfigObj()}
   */
  @Deprecated
  public com.xpn.xwiki.api.Object getSkinConfigObj() {
    return getLegacySkinScriptService().getSkinConfigObj();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelMailScriptService
   * #sendMail(String, String, String, String, String, String, String, String, List, Map)}
   */
  @Deprecated
  public int sendMail(
      String from, String replyTo,
      String to, String cc, String bcc,
      String subject, String htmlContent, String textContent,
      List<Attachment> attachments, Map<String, String> others) {
    return getCelMailScriptService().sendMail(from, replyTo, to, cc, bcc, subject, 
        htmlContent, textContent, attachments, others);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getAttachmentsForDocs(List)}
   */
  @Deprecated
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN) {
    return getWebUtilsScriptService().getAttachmentsForDocs(docsFN);
  }

  // TODO Propabely can be removed as soon as all installations are on xwiki 2+
  @Deprecated
  public int sendLatin1Mail(
      String from, String replyTo,
      String to, String cc, String bcc,
      String subject, String htmlContent, String textContent,
      List<Attachment> attachments, Map<String, String> others){
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("usage of deprecated sendLatin1Mail on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    return plugin.sendMail(from, replyTo, to, cc, bcc, subject, htmlContent, textContent,
        attachments, others, true, context);
  }
  
  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link NextFreeDocScriptService
   * #getNextTitledPageDocRef(String, String)}
   */
  @Deprecated
  public DocumentReference getNextTitledPageDocRef(String space, String title) {
    return getNextFreeDocScriptService().getNextTitledPageDocRef(space, title);
  }

  /**
   * @deprecated since 2.30.0 instead use
   *             {@link NextFreeDocScriptService#getNextTitledPageDocRef(String, String)}
   */
  @Deprecated
  public String getNextTitledPageFullName(String space, String title) {
    return this.getWebUtilsService().getRefLocalSerializer().serialize(
        this.getNextTitledPageDocRef(space, title));
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link NextFreeDocScriptService
   * #getNextUntitledPageFullName(String)}
   */
  public String getNextUntitledPageFullName(String space) {
    return getNextFreeDocScriptService().getNextUntitledPageFullName(space);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link NextFreeDocScriptService
   * #getNextUntitledPageName(String)}
   */
  public String getNextUntitledPageName(String space) {
    return getNextFreeDocScriptService().getNextUntitledPageName(space);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LegacySkinScriptService
   * #showRightPanels()}
   */
  @Deprecated
  public int showRightPanels() {
    return getLegacySkinScriptService().showRightPanels();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LegacySkinScriptService
   * #showLeftPanels()}
   */
  @Deprecated
  public int showLeftPanels() {
    return getLegacySkinScriptService().showLeftPanels();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LegacySkinScriptService
   * #getRightPanels()}
   */
  @Deprecated
  public List<String> getRightPanels() {
    return getLegacySkinScriptService().getRightPanels();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LegacySkinScriptService
   * #getLeftPanels()}
   */
  @Deprecated
  public List<String> getLeftPanels() {
    return getLegacySkinScriptService().getLeftPanels();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getDocSectionAsJSON(String, DocumentReference, int)}
   */
  @Deprecated
  public String getDocSectionAsJSON(String regex, String fullName, int part
      ) throws XWikiException {
    return getWebUtilsScriptService().getDocSectionAsJSON(regex, getWebUtilsService(
        ).resolveDocumentReference(fullName), part);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #countSections(String, DocumentReference)}
   */
  @Deprecated
  public int countSections(String regex, String fullName) throws XWikiException {
    return getWebUtilsScriptService().countSections(regex, getWebUtilsService(
        ).resolveDocumentReference(fullName));
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public long getMillisecsForEarlyBirdDate(Date date) {
    return getSynCustom().getMillisecsForEarlyBirdDate(date);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public String getFormatedEarlyBirdDate(Date date, String format) {
    return getSynCustom().getFormatedEarlyBirdDate(date, format);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getAllowedLanguages()}
   */
  @Deprecated
  public List<String> getAllowedLanguages() {
    return getWebUtilsScriptService().getAllowedLanguages();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getAllowedLanguages(String)}
   */
  @Deprecated
  public List<String> getAllowedLanguages(String spaceName) {
    return getWebUtilsScriptService().getAllowedLanguages(spaceName);
  }

  public int createUser() throws XWikiException {
    return plugin.createUser(true, context);
  }

  public int createUser(boolean validate) throws XWikiException {
    return plugin.createUser(validate, context);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getUniqueValidationKey()}
   */
  @Deprecated
  public String getUniqueValidationKey() throws XWikiException {
    return getAuthenticationScriptService().getUniqueValidationKey();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #recoverPassword()}
   */
  @Deprecated
  public String recoverPassword() throws XWikiException {
    return getAuthenticationScriptService().recoverPassword();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #recoverPassword(String)}
   */
  @Deprecated
  public String recoverPassword(String account) throws XWikiException {
    return getAuthenticationScriptService().recoverPassword(account);
  }

  /**
   * @deprecated since 2.18.0 use instead velocity $datetool.format(format, date)
   */
  @Deprecated
  public Date parseDate(String date, String format) {
    return getWebUtilsService().parseDate(date, format);
  }

  @Deprecated
  private ISynCustom getSynCustom() {
    return (ISynCustom) Utils.getComponent(ScriptService.class, "syncustom");
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public void processRegistrationsWithoutCallback(List<String> recipients) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of processRegistrationsWithoutCallback on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    getSynCustom().processRegistrationsWithoutCallback(recipients);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public void paymentCallback() throws XWikiException {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of paymentCallback on [" + docRef.getWikiReference()
        + ":" + docRef.getLastSpaceReference() + "." + docRef.getName() + "].");
    getSynCustom().paymentCallback();
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public void sendCallbackNotificationMail(Map<String, String[]> data,
      List<String> recipients) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of sendCallbackNotificationMail on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    getSynCustom().sendCallbackNotificationMail(data, recipients);
  }

  public boolean isEmptyRTEString(String rteContent) {
    return getDefaultEmptyCheckService().isEmptyRTEString(rteContent);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getParentSpace()}
   */
  @Deprecated
  public String getParentSpace() {
    return getWebUtilsScriptService().getParentSpace();
  }

  public List<DocumentReference> getRTEConfigsList() {
    return new RTEConfig().getRTEConfigsList();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link RTEConfigScriptService
   * #getRTEConfigField(String)}
   */
  public String getRTEConfigField(String name) {
    return getRTEConfigScriptService().getRTEConfigField(name);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getJSONContent(Document)}
   */
  public String getJSONContent(Document contentDoc) {
    return getWebUtilsScriptService().getJSONContent(contentDoc);
  }

  /**
   * 
   * @param authorDocName
   * @return returns the name of the user in the form "lastname, first name"
   * @throws XWikiException
   * 
   * @Deprecated since 2.18.0 instead use {@link WebUtilsScriptService
   * #getUserNameForDocRef(DocumentReference)}
   */
  @Deprecated
  public String getUserNameForDocName(String authorDocName) throws XWikiException{
    return this.getUserNameForDocRef(getWebUtilsService().resolveDocumentReference(
        authorDocName));
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getUserNameForDocRef(DocumentReference)}
   * 
   * @param authorDocName
   * @return returns the name of the user in the form "lastname, first name"
   */
  @Deprecated
  public String getUserNameForDocRef(DocumentReference userDocRef) {
    return getWebUtilsScriptService().getUserNameForDocRef(userDocRef);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link WebUtilsScriptService
   * #getMajorVersion(DocumentReference)}
   */
  @Deprecated
  public String getMajorVersion(Document doc) {
    return getWebUtilsScriptService().getMajorVersion(doc);
  }

  /**
   * @deprecated since 2.11.5 use $services.celMenu.getMenuBar() instead
   */
  @Deprecated
  public MenuScriptService getMenuBar() {
    return (MenuScriptService) Utils.getComponent(ScriptService.class, "celMenu");
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link DocFormScriptService
   * #updateDocFromMap(String, Map)}
   */
  @Deprecated
  public Set<Document> updateDocFromMap(String fullname, Map<String, ?> map
      ) throws XWikiException {
    return getDocFormScriptService().updateDocFromMap(fullname, map);
  }

  private DocFormCommand getDocFormCommand() {
    if (context.get(_DOC_FORM_COMMAND_OBJECT) == null) {
      context.put(_DOC_FORM_COMMAND_OBJECT, new DocFormCommand());
    }
    return (DocFormCommand) context.get(_DOC_FORM_COMMAND_OBJECT);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link DocFormScriptService
   * #updateDocFromRequest()}
   */
  @Deprecated
  public Set<Document> updateDocFromRequest() throws XWikiException {
    return getDocFormScriptService().updateDocFromRequest();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link DocFormScriptService
   * #updateDocFromRequest(String)}
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public Set<Document> updateDocFromRequest(String fullname
      ) throws XWikiException {
    return getDocFormScriptService().updateDocFromRequest(fullname);
  }

  /**
   * 
   * @param attachToDoc
   * @param fieldName
   * @param userToken
   * @return
   * @deprecated since 2.14.0  because upload failes if xwiki guest does not have
   *             view rights on document
   */
  @Deprecated
  public int tokenBasedUpload(Document attachToDoc, String fieldName, String userToken) {
    try {
      return plugin.tokenBasedUpload(attachToDoc, fieldName, userToken, context);
    } catch (XWikiException exp) {
      LOGGER.error("token based attachment upload failed: ", exp);
    }
    return 0;
  }
  
  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link FileBaseScriptService
   * #tokenBasedUpload(String, String, String)}
   */
  @Deprecated
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken) {
    return getFileBaseScriptService().tokenBasedUpload(attachToDocFN, fieldName, 
        userToken);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link FileBaseScriptService
   * #tokenBasedUpload(String, String, String, Boolean)}
   */
  @Deprecated
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken,
      Boolean createIfNotExists) {
    return getFileBaseScriptService().tokenBasedUpload(attachToDocFN, fieldName, 
        userToken, createIfNotExists);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #checkAuthByToken(String)}
   * 
   * Check authentication from logincredential and password and set according persitent
   * login information If it fails user is unlogged
   * 
   * @param userToken token for user
   * @return null if failed, non null XWikiUser if sucess
   * @throws XWikiException
   */
  @Deprecated
  public XWikiUser checkAuthByToken(String userToken) throws XWikiException {
    return getAuthenticationScriptService().checkAuthByToken(userToken);
  }
  
  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #checkAuth(String, String, String, String)}
   * 
   * Check authentication from logincredential and password and set according persitent
   * login information If it fails user is unlogged
   * 
   * @param username logincredential to check
   * @param password password to check
   * @param rememberme "1" if you want to remember the login accross navigator restart
   * @return null if failed, non null XWikiUser if sucess
   * @throws XWikiException
   */
  @Deprecated
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins) throws XWikiException {
    return getAuthenticationScriptService().checkAuth(logincredential, password, 
        rememberme, possibleLogins);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #checkAuth(String, String, String, String, boolean)}
   * 
   * Check authentication from logincredential and password and set according persitent
   * login information If it fails user is unlogged
   * 
   * @param username logincredential to check
   * @param password password to check
   * @param rememberme "1" if you want to remember the login accross navigator restart
   * @param noRedirect supress auto redirect to xredirect parameter
   * @return null if failed, non null XWikiUser if sucess
   * @throws XWikiException
   */
  @Deprecated
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, boolean noRedirect) throws XWikiException {
    return getAuthenticationScriptService().checkAuth(logincredential, password, rememberme, possibleLogins,
        noRedirect);
  }

  /**
   * @deprecated: since ???NEXTRELEASE??? instead use {@link EditorSupportScriptService
   * #validateField(String, String, String)}, Note: the validateField Method has an other
   * return parameter (Map<ValidationType, Set<String>> instead of String)
   *
   * @return null means the validation has been successful. Otherwise the
   *         validation message configured in the class is returned.
   */
  @Deprecated
  public String validateField(String className, String fieldName, String value) {
    return getDocFormCommand().validateField(className, fieldName, value, context);
  }

  /**
   * @deprecated since 2.2.0 instead use celementsweb script service
   */
  @Deprecated
  public Map<String, String> validateRequest() {
    Map<String, String> ret = new HashMap<String, String>();
    Map<String, Map<ValidationType, Set<String>>> validateMap = getScriptService(
        ).validateRequest();
    for (String key : validateMap.keySet()) {
      Set<String> set = validateMap.get(key).get(ValidationType.ERROR);
      if ((set != null) && (set.size() > 0)) {
        ret.put(key, set.iterator().next());
      }
    }
    return ret;
  }

  /**
   * TODO: Vielleicht direkt TreeNodeService verwenden
   * @return
   */
  private PageLayoutCommand getPageLayoutCmd() {
    if (!context.containsKey(CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      context.put(CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) context.get(CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #renderPageLayout(SpaceReference)}
   */
  @Deprecated
  public String renderPageLayout(SpaceReference spaceRef) {
    return getLayoutScriptService().renderPageLayout(spaceRef);
  }

  public String renderCelementsDocumentWithLayout(DocumentReference docRef,
      SpaceReference layoutSpaceRef) {
    XWikiDocument oldContextDoc = context.getDoc();
    LOGGER.debug("renderCelementsDocumentWithLayout for docRef [" + docRef
        + "] and layoutSpaceRef [" + layoutSpaceRef + "] overwrite oldContextDoc ["
        + oldContextDoc.getDocumentReference() + "].");
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    try {
      XWikiDocument newContextDoc = context.getWiki().getDocument(docRef, context);
      context.setDoc(newContextDoc);
      vcontext.put("doc", newContextDoc.newDocument(context));
      return getPageLayoutCmd().renderPageLayout(layoutSpaceRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get docRef document to renderCelementsDocumentWithLayout.",
          exp);
    } finally {
      context.setDoc(oldContextDoc);
      vcontext.put("doc", oldContextDoc.newDocument(context));
    }
    return "";
  }

  public SpaceReference getCurrentRenderingLayout() {
    return getPageLayoutCmd().getCurrentRenderingLayout();
  }

  /**
   * @deprecated since 2.18.0 instead use {@link LayoutScriptService
   * #renderPageLayout(SpaceReference)}
   */
  @Deprecated
  public String renderPageLayout(String spaceName) {
    return this.renderPageLayout(getWebUtilsService().resolveSpaceReference(spaceName));
  }

  /**
   * @deprecated since 2.18.0 instead use {@link LayoutScriptService
   * #getPageLayoutForDoc(DocumentReference)}
   */
  @Deprecated
  public String getPageLayoutForDoc(String fullName) {
    return this.getPageLayoutForDoc(
        getWebUtilsService().resolveDocumentReference(fullName));
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #getPageLayoutForDoc(DocumentReference)}
   */
  @Deprecated
  public String getPageLayoutForDoc(DocumentReference docRef) {
    return getLayoutScriptService().getPageLayoutForDoc(docRef);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #renderPageLayout()}
   */
  @Deprecated
  public String renderPageLayout() {
    return getLayoutScriptService().renderPageLayout();
  }

  /**
   * @deprecated since 2.18.0 instead use addTranslation(DocumentReference, String)
   */
  @Deprecated
  public boolean addTranslation(String fullName, String language) {
    return new AddTranslationCommand().addTranslation(fullName, language, context);
  }

  public boolean addTranslation(DocumentReference docRef, String language) {
    return new AddTranslationCommand().addTranslation(docRef, language);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public float getBMI() {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of getBMI on [" + docRef.getWikiReference() + ":"
        + docRef.getLastSpaceReference() + "." + docRef.getName() + "].");
    return getSynCustom().getBMI();
  }

  public List<String> renameSpace(String spaceName, String newSpaceName) {
    return new RenameCommand().renameSpace(spaceName, newSpaceName, context);
  }

  public boolean renameDoc(String fullName, String newDocName) {
    return new RenameCommand().renameDoc(fullName, newDocName, context);
  }

  public List<String> getSupportedAdminLanguages() {
    return plugin.getSupportedAdminLanguages();
  }

  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    return plugin.writeUTF8Response(filename, renderDocFullName, context);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public int countObjsWithField(String fullName, String className, String fieldName,
      String value, String valueEnd) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of countObjsWithField on [" + docRef.getWikiReference()
        + ":" + docRef.getLastSpaceReference() + "." + docRef.getName() + "].");
    return getSynCustom().countObjsWithField(fullName, className, fieldName, value,
        valueEnd);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public Map<String, Integer> getRegistrationStatistics(Document mappingDoc,
      String congressName) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of getRegistrationStatistics on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    return getSynCustom().getRegistrationStatistics(mappingDoc, congressName);
  }

  /**
   * @deprecated since 2.10 use syncustom script service direcly instead
   */
  @Deprecated
  public Map<String, String> getExportMapping(String mappingStr, String congress) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("deprecated usage of getExportMapping on [" + docRef.getWikiReference()
        + ":" + docRef.getLastSpaceReference() + "." + docRef.getName() + "].");
    return getSynCustom().getExportMapping(mappingStr, congress);
  }

  public String getCelementsWebAppVersion() {
    DocumentReference centralAppDocRef = new DocumentReference("celements2web", "XApp",
        "XWikiApplicationCelements2web");
    DocumentReference xappClassDocRef = new DocumentReference("celements2web",
        "XAppClasses", "XWikiApplicationClass");
    try {
      XWikiDocument appReceiptDoc = context.getWiki().getDocument(centralAppDocRef,
          context);
      BaseObject appClassObj = appReceiptDoc.getXObject(xappClassDocRef);
      if (appClassObj != null) {
        return appClassObj.getStringValue("appversion");
      }
    } catch (XWikiException exp) {
      LOGGER.warn("Failed to get celementsWeb Application scripts version.", exp);
    }
    return "N/A";
  }

  public String getCelementsWebCoreVersion() {
    return context.getWiki().Param("com.celements.version");
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #getActivePageLayouts()}
   */
  @Deprecated
  public Map<String,String> getActivePageLayouts() {
    return getLayoutScriptService().getActivePageLayouts();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #getAllPageLayouts()}
   */
  @Deprecated
  public Map<String,String> getAllPageLayouts() {
    return getLayoutScriptService().getAllPageLayouts();
  }

  @SuppressWarnings("unchecked")
  public boolean isFormFilled() {
    return plugin.isFormFilled(context.getRequest().getParameterMap(),
        Collections.<String>emptySet());
  }

  @SuppressWarnings("unchecked")
  public boolean isFormFilled(String excludeFields) {
    Set<String> excludeSet = new HashSet<String>();
    for (String field : excludeFields.split(",")) {
      if(!"".equals(field.trim()) && (field.trim().length() > 0)) {
        excludeSet.add(field);
      }
    }
    return plugin.isFormFilled(context.getRequest().getParameterMap(), excludeSet);
  }

  public boolean resetProgrammingRights() {
    if (hasAdminRights()) {
      return new ResetProgrammingRightsCommand().resetCelements2webRigths(context);
    } else {
      LOGGER.warn("user [" + context.getUser() + "] tried to reset programming rights,"
          + " but has no admin rights.");
    }
    return false;
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #createNewLayout(String)}
   */
  @Deprecated
  public String createNewLayout(String layoutSpaceName) {
    return getLayoutScriptService().createNewLayout(layoutSpaceName);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #deleteLayout(String)}
   */
  @Deprecated
  public boolean deleteLayout(String layoutSpaceName) {
    return getLayoutScriptService().deleteLayout(layoutSpaceName);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #getPageLayoutApiForName(String)}
   */
  @Deprecated
  public PageLayoutApi getPageLayoutApiForName(String layoutSpaceName) {
    return getLayoutScriptService().getPageLayoutApiForName(layoutSpaceName);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link TreeNodeScriptService
   * #navReorderSave(String, String)}
   */
  @Deprecated
  public String navReorderSave(String fullName, String structureJSON) {
    return getTreeNodeScriptService().navReorderSave(fullName, structureJSON);
  }

  /**
   * @deprecated since 2.34.0 instead use {@link LayoutScriptService
   * #layoutExists(SpaceReference)}
   */
  @Deprecated
  public boolean layoutExists(String layoutSpaceName) {
    return this.layoutExists(getWebUtilsService().resolveSpaceReference(
        layoutSpaceName));
  }

  /**
   * @deprecated since 2.34.0 instead use {@link LayoutScriptService
   * #layoutExists(SpaceReference)}
   */
  @Deprecated
  public boolean layoutExists(SpaceReference layoutSpaceRef) {
    return getLayoutScriptService().layoutExists(layoutSpaceRef);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #layoutEditorAvailable()}
   */
  @Deprecated
  public boolean layoutEditorAvailable() {
    return getLayoutScriptService().layoutEditorAvailable();
  }

  /**
   * @deprecated since 2.21.0 instead use page type script service
   *      com.celements.pagetype.service.PageTypeScriptService.getAllPageTypes()
   */
  @Deprecated
  public List<String> getAllPageTypes() {
    return getPageTypesByCategories(Arrays.asList("", "pageType"), false);
  }

  /**
   * @deprecated since 2.21.0 instead use page type script service
   *      com.celements.pagetype.service.PageTypeScriptService.getAvailablePageTypes()
   */
  @Deprecated
  public List<String> getAvailablePageTypes() {
    return getPageTypesByCategories(Arrays.asList("", "pageType"), true);
  }

  /**
   * @deprecated since 2.21.0 instead use page type script service
   *      com.celements.pagetype.service.PageTypeScriptService.getPageTypesByCategories()
   */
  @Deprecated
  public List<String> getPageTypesByCategories(List<String> catList,
      boolean onlyVisible) {
    return new GetPageTypesCommand().getPageTypesForCategories(
        new HashSet<String>(catList), onlyVisible, context);
  }

  /**
   * @deprecated since 2.21.0 instead use page type script service
   *      com.celements.pagetype.service.PageTypeScriptService.getAllCellTypes()
   */
  @Deprecated
  public List<String> getAllCellTypes() {
    return getPageTypesByCategories(Arrays.asList("celltype"), false);
  }

  /**
   * @deprecated since 2.21.0 instead use page type script service
   *      com.celements.pagetype.service.PageTypeScriptService.getAvailableCellTypes()
   */
  @Deprecated
  public List<String> getAvailableCellTypes() {
    return getPageTypesByCategories(Arrays.asList("celltype"), true);
  }

  /**
   * renderCelementsDocument
   * @param elementFullName
   * @return
   * 
   * @deprecated since 2.11.2 use {@link CelementsWebScriptService
   * #renderCelementsDocument(DocumentReference)}instead
   */
  @Deprecated
  public String renderCelementsDocument(String elementFullName) {
    return renderCelementsDocument(elementFullName, "view");
  }

  /**
   * 
   * @param elementFullName
   * @param renderMode
   * @return
   * 
   * @deprecated since 2.11.2 use {@link CelementsWebScriptService
   * #renderCelementsDocument(DocumentReference, String)}
   */
  @Deprecated
  public String renderCelementsDocument(String elementFullName, String renderMode) {
    return getScriptService().renderCelementsDocument(getWebUtilsService(
        ).resolveDocumentReference(elementFullName), renderMode);
  }

  /**
   * @deprecated since 2.11.7 instead use {@link CelementsWebScriptService
   * #renderCelementsDocument(DocumentReference)}
   */
  @Deprecated
  public String renderCelementsDocument(DocumentReference elementDocRef) {
    return getScriptService().renderCelementsDocument(elementDocRef);
  }

  /**
   * @deprecated since 2.11.7 instead use {@link CelementsWebScriptService
   * #renderCelementsDocument(DocumentReference, String)}
   */
  @Deprecated
  public String renderCelementsDocument(DocumentReference elementDocRef,
      String renderMode) {
    return getScriptService().renderCelementsDocument(elementDocRef, renderMode);
  }

  /**
   * @deprecated since 2.17.0
   *             instead use {@link CelementsWebScriptService#renderDocument(Document)}
   */
  @Deprecated
  public String renderDocument(Document renderDoc) {
    return getScriptService().renderDocument(renderDoc);
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderDocument in {@link CelementsWebScriptService
   * #renderDocument(Document, boolean, List)}
   */
  @Deprecated
  public String renderDocument(Document renderDoc, boolean removePre,
      List<String> rendererNameList) {
    return getScriptService().renderDocument(renderDoc, removePre, rendererNameList);
  }

  private RenderCommand getCelementsRenderCmd() {
    RenderCommand renderCommand = new RenderCommand();
    renderCommand.setDefaultPageType("RichText");
    return renderCommand;
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderCelementsDocument in CelementsWebScriptService
   */
  @Deprecated
  public String renderCelementsDocument(Document renderDoc) {
    return renderCelementsDocument(renderDoc, "view");
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderCelementsDocument in CelementsWebScriptService
   */
  @Deprecated
  public String renderCelementsDocument(Document renderDoc, String renderMode) {
    return getScriptService().renderCelementsDocument(renderDoc, renderMode);
  }

  public String getEditURL(Document doc) {
    if(!context.getWiki().exists(doc.getDocumentReference(), context)
        || !isValidLanguage() || !isTranslationAvailable(doc, context.getLanguage())) {
      return doc.getURL("edit", "language=" + getWebUtilsService().getDefaultLanguage());
    } else {
      return doc.getURL("edit", "language=" + context.getLanguage());
    }
  }

  public boolean isTranslationAvailable(Document doc, String language) {
    try {
      return doc.getTranslationList().contains(language);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get TranslationList for [" + doc.getFullName() + "].",
          exp);
      return (language.equals(getWebUtilsService().getDefaultLanguage())
          && context.getWiki().exists(doc.getDocumentReference(), context));
    }
  }

  public boolean isValidLanguage() {
    return getAllowedLanguages().contains(context.getLanguage());
  }

  /**
   * @deprecated since ???NEXTRELEASE??? without replacement
   */
  @Deprecated
  public DocumentCreationWorkerControlApi getTestDocumentCreationWorker() {
    return new DocumentCreationWorkerControlApi(context);
  }

  public String clearFileName(String fileName) {
    return context.getWiki().clearName(fileName, false, true, context);
  }

  /**
   * @deprecated since 2.41.0
   *             instead use getDocHeaderTitle(DocumentReference)
   */
  @Deprecated
  public String getDocHeaderTitle(String fullName) {
    return new DocHeaderTitleCommand().getDocHeaderTitle(fullName, context);
  }
  
  public String getDocHeaderTitle(DocumentReference docRef) {
    return new DocHeaderTitleCommand().getDocHeaderTitle(docRef);
  }

  public void logDeprecatedVelocityScript(String logMessage) {
    LOGGER.warn("deprecated usage of velocity Script: " + logMessage);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #isValidUserJSON(String, String, String, List)}
   */
  @Deprecated
  public String isValidUserJSON(String username, String password, String memberOfGroup,
      List<String> returnGroupMemberships) {
    return getAuthenticationScriptService().isValidUserJSON(username, password,
        memberOfGroup, returnGroupMemberships);
  }

  /**
   * @deprecated since 2.14.0 use syncustom script service direcly instead
   */
  @Deprecated
  public boolean congressRegistrationPlausibility() {
    return getSynCustom().congressRegistrationPlausibility();
  }

  /**
   * @deprecated since 2.14.0 use syncustom script service direcly instead
   */
  @Deprecated
  public boolean congressRegistrationPlausibility(Document document) {
    return getSynCustom().congressRegistrationPlausibility(document);
  }
  
  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelementsWebScriptService
   * #newObjectForFormStorage(Document, String)}
   */
  @Deprecated
  public com.xpn.xwiki.api.Object newObjectForFormStorage(Document storageDoc,
      String className) {
    return getScriptService().newObjectForFormStorage(storageDoc, className);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CaptchaScriptService
   * #checkCaptcha()}
   */
  @Deprecated   
  public boolean checkCaptcha() {
    return getCaptchaScriptService().checkCaptcha();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CaptchaScriptService
   * #getCaptchaId()}
   */
  @Deprecated 
  public String getCaptchaId() {
    return getCaptchaScriptService().getCaptchaId();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelementsWebScriptService
   * #getObjStoreOptionsMap(String)}
   * 
   * Get the options (checkbox and radio buttons) saved using the ObjectSorage Action as
   * a Map.
   * @param options The String saved in the store object
   * @return Map containing all the
   */
  @Deprecated
  public Map<String, String> getObjStoreOptionsMap(String options) {
    return getScriptService().getObjStoreOptionsMap(options);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link LayoutScriptService
   * #useXWikiLoginLayout()}
   */
  @Deprecated 
  public boolean useXWikiLoginLayout() {
    return getLayoutScriptService().useXWikiLoginLayout();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getLogoutRedirectURL()}
   */
  @Deprecated
  public String getLogoutRedirectURL() {
    return getAuthenticationScriptService().getLogoutRedirectURL();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #getLoginRedirectURL()}
   */
  @Deprecated
  public String getLoginRedirectURL() {
    return getAuthenticationScriptService().getLoginRedirectURL();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelementsWebScriptService
   * #isCelementsRights(String)}
   */
  @Deprecated
  public boolean isCelementsRights(String fullName) {
    return getScriptService().isCelementsRights(fullName);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link ActionScriptService
   * #executeAction(Document)}
   */
  @Deprecated
  public boolean executeAction(Document actionDoc) {
    return getActionScriptService().executeAction(actionDoc);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link ActionScriptService
   * #executeAction(Document, Map)}
   */
  @Deprecated
  public boolean executeAction(Document actionDoc,
      Map<String, List<Object>> fakeRequestMap) {
    return getActionScriptService().executeAction(actionDoc, fakeRequestMap);
  }

  /**
   * API to check rights on a document for a given user or group
   * 
   * @deprecated since ???NEXTRELEASE??? instead use {@link AuthenticationScriptService
   * #hasAccessLevel(String, String, boolean, String)}
   * 
   * @param level right to check (view, edit, comment, delete)
   * @param user user or group for which to check the right
   * @param isUser true for users and false for group
   * @param docname document on which to check the rights
   * @return true if right is granted/false if not
   */
  @Deprecated
  public boolean hasAccessLevel(String level, String user, boolean isUser,
      String docname) {
    return getAuthenticationScriptService().hasAccessLevel(level, user, isUser, 
        getWebUtilsService().resolveDocumentReference(docname));
  }

  /**
   * @deprecated since 2.11.6 instead use celementsweb script service
   */
  @Deprecated
  public String getSkinFile(String fileName) {
    return getScriptService().getSkinFile(fileName);
  }

  /**
   * @deprecated since 2.11.6 instead use celementsweb script service
   */
  @Deprecated
  public String getSkinFile(String fileName, String action) {
    return getScriptService().getSkinFile(fileName, action);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link EditorSupportScriptService
   * #getSuggestBaseClass(DocumentReference, String)}
   */
  @Deprecated
  public SuggestBaseClass getSuggestBaseClass(DocumentReference classreference,
      String fieldname) {
    return getEditorSupportScriptService().getSuggestBaseClass(classreference, fieldname);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link EditorSupportScriptService
   * #getSuggestList(DocumentReference, String, String)}
   */
  @Deprecated
  public List<Object> getSuggestList(DocumentReference classRef, String fieldname,
      String input) {
    return getEditorSupportScriptService().getSuggestList(classRef, fieldname, input);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link EditorSupportScriptService
   * #getSuggestList(DocumentReference, String, List, String, String, String, int))}
   */
  @Deprecated
  public List<Object> getSuggestList(DocumentReference classRef, String fieldname,
      List<String> excludes, String input, String firstCol, String secCol, int limit) {
    return getEditorSupportScriptService().getSuggestList(classRef, fieldname, excludes, 
        input, firstCol, secCol, limit);
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelementsWebScriptService
   * #getDefaultSpace()}
   */
  @Deprecated
  public String getDefaultSpace() {
    return getScriptService().getDefaultSpace();
  }

  /**
   * @deprecated since ???NEXTRELEASE??? instead use {@link CelementsWebScriptService
   * #checkClasses()}
   */
  @Deprecated
  public void checkClasses() {
    getScriptService().checkClasses();
  }

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    return getEmptyCheckService().getNextNonEmptyChildren(documentRef);
  }

  /**
   * @deprecated since 2.11.2 instead use {@link ImageScriptService
   * #getRandomImages(String, int)}
   */
  @Deprecated
  public boolean useImageAnimations() {
    return getImageScriptService().useImageAnimations();
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #getAppScriptURL(String)}
   */
  @Deprecated
  public String getAppScriptURL(String scriptName) {
    return getAppScriptScriptService().getAppScriptURL(scriptName);
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #getAppScriptURL(String, String)}
   */
  @Deprecated
  public String getAppScriptURL(String scriptName, String queryString) {
    return getAppScriptScriptService().getAppScriptURL(scriptName, queryString);
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #isAppScriptCurrentPage(String)}
   */
  @Deprecated
  public boolean isAppScriptCurrentPage(String scriptName) {
    return getAppScriptScriptService().isAppScriptCurrentPage(scriptName);
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #getScriptNameFromURL()}
   */
  @Deprecated
  public String getScriptNameFromURL() {
    return getAppScriptScriptService().getScriptNameFromURL();
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #isAppScriptRequest()}
   */
  @Deprecated
  public boolean isAppScriptRequest() {
    return getAppScriptScriptService().isAppScriptRequest();
  }

  /**
   * @deprecated since 2.11.2 instead use {@link AppScriptScriptService
   * #getCurrentPageURL(String)}
   */
  @Deprecated
  public String getCurrentPageURL(String queryString) {
    return getAppScriptScriptService().getCurrentPageURL(queryString);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String convertToPlainText(String htmlContent) {
    return getScriptService().convertToPlainText(htmlContent);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public Builder getNewJSONBuilder() {
    return getScriptService().getNewJSONBuilder();
  }

  private CelementsWebScriptService getScriptService() {
    return (CelementsWebScriptService) Utils.getComponent(ScriptService.class,
        "celementsweb");
  }

  private WebUtilsScriptService getWebUtilsScriptService() {
    return (WebUtilsScriptService) Utils.getComponent(ScriptService.class, "webUtils");
  }

  private ContextMenuScriptService getContextMenuScriptService() {
    return (ContextMenuScriptService) Utils.getComponent(ScriptService.class,
        "contextMenu");
  }

  private TreeNodeScriptService getTreeNodeScriptService() {
    return (TreeNodeScriptService) Utils.getComponent(ScriptService.class, "treeNode");
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private IPrepareVelocityContext getPrepareVelocityContextService() {
    return Utils.getComponent(IPrepareVelocityContext.class);
  }

  private DefaultStringEntityReferenceSerializer getEntitySerializer() {
    return ((DefaultStringEntityReferenceSerializer)Utils.getComponent(
        EntityReferenceSerializer.class));
  }

  private IEmptyCheckRole getEmptyCheckService() {
    return Utils.getComponent(IEmptyCheckRole.class);
  }

  private IDefaultEmptyDocStrategyRole getDefaultEmptyCheckService() {
    return Utils.getComponent(IDefaultEmptyDocStrategyRole.class);
  }

  private AuthenticationScriptService getAuthenticationScriptService() {
    return (AuthenticationScriptService) Utils.getComponent(ScriptService.class, 
        "authentication");
  }
  
  private EditorSupportScriptService getEditorSupportScriptService() {
    return (EditorSupportScriptService) Utils.getComponent(ScriptService.class, 
        "editorsupport");
  }
  
  private ActionScriptService getActionScriptService() {
    return (ActionScriptService) Utils.getComponent(ScriptService.class, 
        "action");
  }
  
  private LayoutScriptService getLayoutScriptService() {
    return (LayoutScriptService) Utils.getComponent(ScriptService.class, 
        "layout");
  }
  
  private CaptchaScriptService getCaptchaScriptService() {
    return (CaptchaScriptService) Utils.getComponent(ScriptService.class, 
        "captcha");
  }
  
  private CSSScriptService getCSSScriptService() {
    return (CSSScriptService) Utils.getComponent(ScriptService.class, 
        "css");
  }
  
  private LegacySkinScriptService getLegacySkinScriptService() {
    return (LegacySkinScriptService) Utils.getComponent(ScriptService.class, 
        "legacyskin");
  }
  
  private DocFormScriptService getDocFormScriptService() {
    return (DocFormScriptService) Utils.getComponent(ScriptService.class, 
        "docform");
  }
  
  private FileBaseScriptService getFileBaseScriptService() {
    return (FileBaseScriptService) Utils.getComponent(ScriptService.class, 
        "filebase");
  }
  
  private RTEConfigScriptService getRTEConfigScriptService() {
    return (RTEConfigScriptService) Utils.getComponent(ScriptService.class, 
        "rteconfig");
  }
  
  private NextFreeDocScriptService getNextFreeDocScriptService() {
    return (NextFreeDocScriptService) Utils.getComponent(ScriptService.class, 
        "nextfreedoc");
  }
  
  private CelMailScriptService getCelMailScriptService() {
    return (CelMailScriptService) Utils.getComponent(ScriptService.class, 
        "celmail");
  }
  
  private ImageScriptService getImageScriptService() {
    return (ImageScriptService) Utils.getComponent(ScriptService.class, 
        "image");
  }
  
  private JSScriptService getJSScriptService() {
    return (JSScriptService) Utils.getComponent(ScriptService.class, 
        "javascript");
  }
  
  private AppScriptScriptService getAppScriptScriptService() {
    return (AppScriptScriptService) Utils.getComponent(ScriptService.class, 
        "appscript");
  }
}