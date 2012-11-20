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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.menu.MenuScriptService;
import com.celements.navigation.NavContextMenuApi;
import com.celements.navigation.NavigationApi;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.ReorderSaveCommand;
import com.celements.pagetype.IPageType;
import com.celements.pagetype.PageTypeApi;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.celements.rendering.RenderCommand;
import com.celements.sajson.Builder;
import com.celements.web.contextmenu.ContextMenuBuilderApi;
import com.celements.web.contextmenu.ContextMenuItemApi;
import com.celements.web.css.CSS;
import com.celements.web.plugin.CelementsWebPlugin;
import com.celements.web.plugin.RTEConfig;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.celements.web.plugin.cmd.CaptchaCommand;
import com.celements.web.plugin.cmd.CelementsRightsCommand;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.celements.web.plugin.cmd.CssCommand;
import com.celements.web.plugin.cmd.DocFormCommand;
import com.celements.web.plugin.cmd.DocHeaderTitleCommand;
import com.celements.web.plugin.cmd.DocMetaTagsCmd;
import com.celements.web.plugin.cmd.EmptyCheckCommand;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
import com.celements.web.plugin.cmd.FormObjStorageCommand;
import com.celements.web.plugin.cmd.ISynCustom;
import com.celements.web.plugin.cmd.LastStartupTimeStamp;
import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.plugin.cmd.ParseObjStoreCommand;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.RemoteUserValidator;
import com.celements.web.plugin.cmd.RenameCommand;
import com.celements.web.plugin.cmd.ResetProgrammingRightsCommand;
import com.celements.web.plugin.cmd.SuggestListCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.CelementsWebScriptService;
import com.celements.web.service.ContextMenuScriptService;
import com.celements.web.service.IPrepareVelocityContext;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.celements.web.utils.DocumentCreationWorkerControlApi;
import com.celements.web.utils.SuggestBaseClass;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class CelementsWebPluginApi extends Api {

  public static final String CELEMENTS_CSSCOMMAND = "com.celements.web.CssCommand";

  public static final String JAVA_SCRIPT_FILES_COMMAND_KEY =
    "com.celements.web.ExternalJavaScriptFilesCommand";
  
  public static final String CELEMENTS_PAGE_LAYOUT_COMMAND =
    "com.celements.web.PageLayoutCommand";

  private static final String _DOC_FORM_COMMAND_OBJECT = "com.celements.DocFormCommand";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsWebPluginApi.class);
  
  private CelementsWebPlugin plugin;

  private EmptyCheckCommand emptyCheckCmd = new EmptyCheckCommand();

  public CelementsWebPluginApi(
      CelementsWebPlugin plugin,
      XWikiContext context) {
    super(context);
    setPlugin(plugin);
  }

  //FIXME must check programming Rights!!!
  public CelementsWebPlugin getPlugin(){
    return plugin;
  }

  //FIXME must not be public! why do we need it in the API class anyway?
  public void setPlugin(CelementsWebPlugin plugin) {
    this.plugin = plugin;
  }

  public void flushCache() {
    plugin.flushCache(context);
  }

  /**
   * getLastStartupTimeStamp
   * 
   *  to solve browser caching issues with files on disk e.g. tinymce
   * @return
   */
  public String getLastStartupTimeStamp(){
    return new LastStartupTimeStamp().getLastStartupTimeStamp();
  }

  /**
   * @deprecated since 2.11.4 instead use contextMenu script service
   */
  @Deprecated
  public ContextMenuBuilderApi getContextMenuBuilder() {
    return getCMService().getContextMenuBuilder();
  }

  /**
   * @deprecated since 2.11.4 instead use contextMenu script service
   */
  @Deprecated
  public String getAllContextMenuCSSClassesAsJSON() {
    return getCMService().getAllContextMenuCSSClassesAsJSON();
  }

  public ContextMenuItemApi getWrapper(com.xpn.xwiki.api.Object menuItem,
      String elemId) {
    return new ContextMenuItemApi(menuItem, elemId, context);
  }

  public NavContextMenuApi getNavContextMenu() {
    return NavContextMenuApi.getNavContextMenu(context);
  }

  public NavigationApi createNavigation() {
    return NavigationApi.createNavigation(context);
  }
  
  public void enableMappedMenuItems() {
    plugin.enableMappedMenuItems(context);
  }

  public int getMaxConfiguredNavigationLevel() {
    return WebUtils.getInstance().getMaxConfiguredNavigationLevel(context);
  }

  public boolean isNavigationEnabled(String configName) {
    NavigationApi nav = NavigationApi.createNavigation(context);
    nav.loadConfigByName(configName);
    return nav.isNavigationEnabled();
  }

  public NavigationApi getNavigation(String configName) {
    NavigationApi nav = NavigationApi.createNavigation(context);
    nav.loadConfigByName(configName);
    return nav;
  }

  public String includeNavigation(String configName) {
    return getNavigation(configName).includeNavigation();
  }

  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace) {
    return plugin.getSubMenuItemsForParent(parent, menuSpace, "", context);
  }

  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart) {
    return plugin.getSubMenuItemsForParent(parent, menuSpace, menuPart, context);
  }

  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace) {
    return WebUtils.getInstance().getSubNodesForParent(parent, menuSpace, "", context);
  }

  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart) {
    return WebUtils.getInstance().getSubNodesForParent(parent, menuSpace, menuPart,
        context);
  }

  public int queryCount() {
    return plugin.queryCount();
  }

  public Map<String, String> getDocMetaTags(String language,
      String defaultLanguage) {
    return new DocMetaTagsCmd().getDocMetaTags(language, defaultLanguage, context);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator
      ) throws ClassNotFoundException{
    return getWebUtilsService().getAttachmentListSorted(doc, comparator);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return getWebUtilsService().getAttachmentListSorted(doc, comparator, imagesOnly,
        start, nb);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly) throws ClassNotFoundException{
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator, 
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return getWebUtilsService().getAttachmentListSortedAsJSON(doc, comparator, 
        imagesOnly, start, nb);
  }

  public List<Attachment> getRandomImages(String fullName,
      int num) throws ClassNotFoundException{
      return WebUtils.getInstance().getRandomImages(fullName, num, context);
  }

  public XWikiMessageTool getMessageTool(String adminLanguage) {
    return getWebUtilsService().getMessageTool(adminLanguage);
  }

  public String getVersionMode() {
    return plugin.getVersionMode(context);
  }
  
  public String getAllExternalJavaScriptFiles() throws XWikiException {
    return getExtJavaScriptFileCmd().getAllExternalJavaScriptFiles();
  }

  private ExternalJavaScriptFilesCommand getExtJavaScriptFileCmd() {
    if (context.get(JAVA_SCRIPT_FILES_COMMAND_KEY) == null) {
      context.put(JAVA_SCRIPT_FILES_COMMAND_KEY, new ExternalJavaScriptFilesCommand(
          context));
    }
    return (ExternalJavaScriptFilesCommand) context.get(JAVA_SCRIPT_FILES_COMMAND_KEY);
  }
  
  /**
   * @deprecated since 2.11.3 instead use celementsweb script service
   */
  @Deprecated
  public void addImageMapConfig(String configName) {
    getService().addImageMapConfig(configName);
  }
  
  /**
   * @deprecated since 2.11.3 instead use celementsweb script service
   */
  @Deprecated
  public String displayImageMapConfigs() {
    return getService().displayImageMapConfigs();
  }

  public String addExtJSfileOnce(String jsFile) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile);
  }

  public String addExtJSfileOnce(String jsFile, String action) {
    return getExtJavaScriptFileCmd().addExtJSfileOnce(jsFile, action);
  }

  public String getUsernameForUserData(String login) {
    String possibleLogins = plugin.getPossibleLogins(context);
    String account = "";
    try {
      LOGGER.debug("executing getUsernameForUserData in plugin");
      account = new UserNameForUserDataCommand().getUsernameForUserData(login,
          possibleLogins, context);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get usernameForUserData for login [" + login
          + "] and possibleLogins [" + possibleLogins + "].", exp);
    }
    return account;
  }

  public String getUsernameForUserData(String login, String possibleLogins) {
    String account = "";
    if(hasProgrammingRights() || hasAdminRights()) {
      try {
        LOGGER.debug("executing getUsernameForUserData in plugin");
        account = new UserNameForUserDataCommand().getUsernameForUserData(login,
            possibleLogins, context);
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get usernameForUserData for login [" + login
            + "] and possibleLogins [" + possibleLogins + "].", exp);
      }
    } else {
      LOGGER.debug("missing ProgrammingRights for [" + context.get("sdoc")
          + "]: getUsernameForUserData cannot be executed!");
    }
    return account;
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
  
  public String encryptString(String encoding, String str){
    return plugin.encryptString(encoding, str);
  }
  
  public String encryptString(String str){
    return encryptString("hash:SHA-512:", str);
  }
  
  public void sendNewValidation(String user, String possibleFields) {
    if ((hasAdminRights() || hasProgrammingRights()) && (user != null)
        && (user.trim().length() > 0)) {
      LOGGER.debug("sendNewValidation for user [" + user + "].");
      try {
        new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user,
            possibleFields);
      } catch (XWikiException exp) {
        LOGGER.error("sendNewValidation: failed.", exp);
      }
    }
  }
  
  public void sendNewValidation(String user, String possibleFields,
      DocumentReference mailContentDocRef) {
    if ((hasAdminRights() || hasProgrammingRights()) && (user != null)
        && (user.trim().length() > 0)) {
      LOGGER.debug("sendNewValidation for user [" + user + "] using mail ["
          + mailContentDocRef + "].");
      try {
        new PasswordRecoveryAndEmailValidationCommand().sendNewValidation(user,
            possibleFields, mailContentDocRef);
      } catch (XWikiException exp) {
        LOGGER.error("sendNewValidation: failed.", exp);
      }
    } else {
      LOGGER.warn("sendNewValidation: new validation email for user [" + user
          + "] not sent.");
    }
  }
  
  public String getNewValidationTokenForUser() {
    if(hasProgrammingRights() && (context.getUser() != null)) {
      try {
        return new PasswordRecoveryAndEmailValidationCommand(
            ).getNewValidationTokenForUser(context.getUser(), context);
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: "
            + context.getUser(), exp);
      }
    }
    return null;
  }
  
  public String getNewCelementsTokenForUser() {
    return getNewCelementsTokenForUser(false);
  }
  
  public String getNewCelementsTokenForUser(Boolean guestPlus) {
    if(context.getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand(
            ).getNewCelementsTokenForUserWithAuthentication(context.getUser(), guestPlus,
                context);
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: "
            + context.getUser(), exp);
      }
    }
    return null;
  }

  public String getNewCelementsTokenForUser(Boolean guestPlus, int minutesValid) {
    if(context.getUser() != null) {
      try {
        return new NewCelementsTokenForUserCommand(
            ).getNewCelementsTokenForUserWithAuthentication(context.getUser(), guestPlus,
                minutesValid, context);
      } catch (XWikiException exp) {
        LOGGER.error("Failed to create new validation Token for user: "
            + context.getUser(), exp);
      }
    }
    return null;
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

  public List<CSS> getAllCSS() throws XWikiException{
    return getCssCmd().getAllCSS(context);
  }
  
  public String displayAllCSS() throws XWikiException{
    return getCssCmd().displayAllCSS(context);
  }

  public List<CSS> getRTEContentCSS() throws XWikiException{
    return getCssCmd().getRTEContentCSS(context);
  }
  
  public void includeCSSPage(String css) {
    getCssCmd().includeCSSPage(css, context);
  }

  public void includeCSSAfterPreferences(String css) throws XWikiException{
    getCssCmd().includeCSSAfterPreferences(css, context);
  }
  
  public void includeCSSAfterSkin(String css){
    getCssCmd().includeCSSAfterSkin(css, context);
  }

  /**
   * @deprecated since 2.9.4 use instead isEmptyRTEDocument(DocumentReference)
   **/
  @Deprecated
  public boolean isEmptyRTEDocument(String fullName) {
    DocumentReference docRef = context.getDoc().getDocumentReference();
    LOGGER.warn("usage of deprecated isEmptyRTEDocument(String) on ["
        + docRef.getWikiReference() + ":" + docRef.getLastSpaceReference() + "."
        + docRef.getName() + "].");
    return emptyCheckCmd.isEmptyRTEDocument(fullName, context);
  }
  
  public boolean isEmptyRTEDocument(DocumentReference documentRef) {
    return emptyCheckCmd.isEmptyRTEDocument(documentRef);
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
  
  public Map<String, String> activateAccount(String activationCode) throws XWikiException{
    Map<String, String> result = new HashMap<String, String>();
    //TODO Why do we need Programming rights here? and do we realy need them?
//    if(hasProgrammingRights()){
    result = plugin.activateAccount(activationCode, context);
//    }
    return result;
  }
  
  /**
   * Returns a list of all parent for a specified doc
   * 
   * @param fullName
   * @param includeDoc
   * @return List of all parents, starting at the specified doc (bottom up)
   */
  public List<String> getDocumentParentsList(String fullName,
      boolean includeDoc) {
    return WebUtils.getInstance().getDocumentParentsList(fullName, includeDoc,
        context);
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
  public IPageType getPageType(String fullName) throws XWikiException {
    return new PageTypeApi(fullName, context);
  }

  public com.xpn.xwiki.api.Object getSkinConfigObj() {
    BaseObject skinConfigObj = plugin.getSkinConfigObj(context);
    if (skinConfigObj != null) {
      return skinConfigObj.newObjectApi(skinConfigObj, context); 
    } else {
      return null;
    }
  }

  public int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others) {
    return plugin.sendMail(from, replyTo, to, cc, bcc, subject, htmlContent,
        textContent, attachments, others, context);
  }
  
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN) {
    List<Attachment> attachments = Collections.emptyList();
    if (hasProgrammingRights()){
      LOGGER.info("getAttachmentsForDocs: fetching attachments...");
      attachments = plugin.getAttachmentsForDocs(docsFN, context);      
    }
    else {
      LOGGER.info("getAttachmentsForDocs: no programming rights");
    }
    return attachments;
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
  
  public String getNextTitledPageFullName(String space, String title){
    return new NextFreeDocNameCommand().getNextTitledPageFullName(space, title, context);
  }
  
  public String getNextUntitledPageFullName(String space) {
    return new NextFreeDocNameCommand().getNextUntitledPageFullName(space, context);
  }

  public String getNextUntitledPageName(String space) {
    return new NextFreeDocNameCommand().getNextUntitledPageName(space, context);
  }

  public int showRightPanels() {
    return getPrepareVelocityContextService().showRightPanels();
  }

  public int showLeftPanels() {
    return getPrepareVelocityContextService().showLeftPanels();
  }

  public List<String> getRightPanels() {
    return getPrepareVelocityContextService().getRightPanels();
  }

  public List<String> getLeftPanels() {
    return getPrepareVelocityContextService().getLeftPanels();
  }

  public String getDocSectionAsJSON(String regex, String fullName, int part
      ) throws XWikiException {
    return WebUtils.getInstance().getDocSectionAsJSON(regex, fullName, part, context);
  }

  public int countSections(String regex, String fullName) throws XWikiException {
    return WebUtils.getInstance().countSections(regex, fullName, context);
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

  public List<String> getAllowedLanguages() {
    return getWebUtilsService().getAllowedLanguages();
  }
  
  public List<String> getAllowedLanguages(String spaceName) {
    return getWebUtilsService().getAllowedLanguages(spaceName);
  }
  
  public int createUser() throws XWikiException {
    return plugin.createUser(true, context);
  }
  
  public int createUser(boolean validate) throws XWikiException {
    return plugin.createUser(validate, context);
  }

  public String getUniqueValidationKey() throws XWikiException {
    return new NewCelementsTokenForUserCommand().getUniqueValidationKey(context);
  }
  
  public String recoverPassword() throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword();
  }
  
  public String recoverPassword(String account) throws XWikiException {
    return new PasswordRecoveryAndEmailValidationCommand().recoverPassword(account,
        account);
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
    return new EmptyCheckCommand().isEmptyRTEString(rteContent);
  }
  public String getParentSpace() {
    return getWebUtilsService().getParentSpace();
  }
  
  public String getRTEConfigField(String name) throws XWikiException {
    return RTEConfig.getInstance(context).getRTEConfigField(name, context);
  }
  
  public String getJSONContent(Document contentDoc) {
    return getWebUtilsService().getJSONContent(contentDoc.getDocument());
  }
  
  /**
   * 
   * @param authorDocName
   * @return returns the name of the user in the form "lastname, first name"
   * @throws XWikiException
   * 
   * @Deprecated since 2.18.0 instead use getUserNameForDocRef(DocumentReference)
   */
  @Deprecated
  public String getUserNameForDocName(String authorDocName) throws XWikiException{
    return WebUtils.getInstance().getUserNameForDocName(authorDocName, context);
  }
  
  /**
   * 
   * @param authorDocName
   * @return returns the name of the user in the form "lastname, first name"
   */
  public String getUserNameForDocRef(DocumentReference userDocRef) {
    try {
      return getWebUtilsService().getUserNameForDocRef(userDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document [" + userDocRef + "].", exp);
    }
    return "N/A";
  }
  
  public String getMajorVersion(Document doc) {
    return getWebUtilsService().getMajorVersion(doc.getDocument());
  }

  /**
   * @deprecated since 2.11.5 use $services.celMenu.getMenuBar() instead
   */
  @Deprecated
  public MenuScriptService getMenuBar() {
    return (MenuScriptService) Utils.getComponent(ScriptService.class, "celMenu");
  }

  public Set<Document> updateDocFromMap(String fullname, Map<String, ?> map
      ) throws XWikiException {
    Map<String, String[]> recompMap = new HashMap<String, String[]>();
    for (String key : map.keySet()) {
      if(map.get(key) instanceof String[]) {
        recompMap.put(key, (String[])map.get(key));
      } else if(map.get(key) instanceof String) {
        recompMap.put(key, new String[]{(String)map.get(key)});
      }
    }
    Set<Document> docs = new HashSet<Document>();
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(fullname,
        recompMap, context);
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(context));
    }
    return docs;
  }

  private DocFormCommand getDocFormCommand() {
    if (context.get(_DOC_FORM_COMMAND_OBJECT) == null) {
      context.put(_DOC_FORM_COMMAND_OBJECT, new DocFormCommand());
    }
    return (DocFormCommand) context.get(_DOC_FORM_COMMAND_OBJECT);
  }

  public Set<Document> updateDocFromRequest() throws XWikiException {
    return updateDocFromRequest(null);
  }
  
  @SuppressWarnings("unchecked")
  public Set<Document> updateDocFromRequest(String fullname
      ) throws XWikiException {
    Set<Document> docs = new HashSet<Document>();
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(fullname,
        context.getRequest().getParameterMap(), context);
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(context));
    }
    return docs;
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
      LOGGER.error("token based attachment upload failed: " + exp);
    }
    return 0;
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken) {
    return tokenBasedUpload(attachToDocFN, fieldName, userToken, false);
  }

  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken, 
      Boolean createIfNotExists) {
    try {
      return plugin.tokenBasedUpload(attachToDocFN, fieldName, userToken, 
          createIfNotExists, context);
    } catch (XWikiException exp) {
      LOGGER.error("token based attachment upload failed: " + exp);
    }
    return 0;
  }

  /**
   * Check authentication from logincredential and password and set according persitent
   * login information If it fails user is unlogged
   * 
   * @param userToken token for user
   * @return null if failed, non null XWikiUser if sucess
   * @throws XWikiException
   */
  public XWikiUser checkAuthByToken(String userToken) throws XWikiException {
    if(hasProgrammingRights()){
      LOGGER.debug("checkAuthByToken: executing checkAuthByToken in plugin");
      return plugin.checkAuthByToken(userToken, context);
    } else {
      LOGGER.debug("checkAuthByToken: missing ProgrammingRights for ["
          + context.get("sdoc") + "]: checkAuthByToken cannot be executed!");
    }
    return null;
  }

  /**
  * Check authentication from logincredential and password and set according persitent
  * login information If it fails user is unlogged
  * 
  * @param username logincredential to check
  * @param password password to check
  * @param rememberme "1" if you want to remember the login accross navigator restart
  * @return null if failed, non null XWikiUser if sucess
  * @throws XWikiException
  */
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins) throws XWikiException {
    return plugin.checkAuth(logincredential, password, rememberme, possibleLogins,
        context);
  }

  /**
   * 
   * @return null means the validation has been successful. Otherwise the
   *         validation message configured in the class is returned.
   */
  public String validateField(String className, String fieldName, String value) {
    return getDocFormCommand().validateField(className, fieldName, value, context);
  }
 
  /**
   * 
   * @return empty map means the validation has been successful. Otherwise the
   *         validation message configured in the class is returned for not
   *         validating fields.
   */
  public Map<String, String> validateRequest() {
    return getDocFormCommand().validateRequest(context);
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (!context.containsKey(CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      context.put(CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) context.get(CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  public String renderPageLayout(SpaceReference spaceRef) {
    return getPageLayoutCmd().renderPageLayout(spaceRef);
  }

  /**
   * @deprecated since 2.18.0 instead use renderPageLayout(SpaceReference)
   */
  @Deprecated
  public String renderPageLayout(String spaceName) {
    return getPageLayoutCmd().renderPageLayout(
        getWebUtilsService().resolveSpaceReference(spaceName));
  }

  /**
   * @deprecated since 2.18.0 instead use getPageLayoutForDoc(DocumentReference)
   */
  @Deprecated
  public String getPageLayoutForDoc(String fullName) {
    return getPageLayoutCmd().getPageLayoutForDoc(
        getWebUtilsService().resolveDocumentReference(fullName)).getName();
  }

  public String getPageLayoutForDoc(DocumentReference docRef) {
    return getPageLayoutCmd().getPageLayoutForDoc(docRef).getName();
  }

  public String renderPageLayout() {
    return getPageLayoutCmd().renderPageLayout();
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

  public Map<String,String> getActivePageLayouts() {
    return getPageLayoutCmd().getActivePageLyouts();
  }

  public Map<String,String> getAllPageLayouts() {
    return getPageLayoutCmd().getAllPageLayouts();
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

  public String createNewLayout(String layoutSpaceName) {
    return getPageLayoutCmd().createNew(getWebUtilsService().resolveSpaceReference(
        layoutSpaceName));
  }

  public boolean deleteLayout(String layoutSpaceName) {
    SpaceReference layoutSpaceRef = getWebUtilsService().resolveSpaceReference(
        layoutSpaceName);
    String layoutPropDocName = getEntitySerializer().serialize(getPageLayoutCmd(
        ).standardPropDocRef(layoutSpaceRef));
    try {
      if (hasAccessLevel("delete", layoutPropDocName)) {
        return getPageLayoutCmd().deleteLayout(layoutSpaceRef);
      } else {
        LOGGER.warn("NO delete rights on [" + layoutPropDocName
            + "] for user [" + context.getUser() + "].");
      }
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check delete rights on [" + layoutSpaceName + "] for user ["
          + context.getUser() + "].");
    }
    return false;
  }

  public PageLayoutApi getPageLayoutApiForName(String layoutSpaceName) {
    return new PageLayoutApi(getWebUtilsService().resolveSpaceReference(layoutSpaceName),
        context);
  }

  public String navReorderSave(String fullName, String structureJSON) {
    return new ReorderSaveCommand().reorderSave(fullName, structureJSON, context);
  }

  public boolean layoutExists(String layoutSpaceName) {
    return getPageLayoutCmd().layoutExists(getWebUtilsService().resolveSpaceReference(
        layoutSpaceName));
  }

  public boolean layoutEditorAvailable() {
    return getPageLayoutCmd().layoutEditorAvailable();
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
   * @deprecated since 2.11.2 use renderCelementsDocument(DocumentReference) instead
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
   * @deprecated since 2.11.2 use renderCelementsDocument(DocumentReference, String) instead
   */
  @Deprecated
  public String renderCelementsDocument(String elementFullName, String renderMode) {
    try {
      return getCelementsRenderCmd().renderCelementsDocument(context.getWiki(
          ).getDocument(elementFullName, context), renderMode);
    } catch (XWikiException exp) {
      LOGGER.error("renderCelementsDocument: Failed to render " + elementFullName, exp);
    }
    return "";
  }

  /**
   * @deprecated since 2.11.7 instead use celementsweb script service
   */
  @Deprecated
  public String renderCelementsDocument(DocumentReference elementDocRef) {
    return getService().renderCelementsDocument(elementDocRef);
  }

  /**
   * @deprecated since 2.11.7 instead use celementsweb script service
   */
  @Deprecated
  public String renderCelementsDocument(DocumentReference elementDocRef,
      String renderMode) {
    return getService().renderCelementsDocument(elementDocRef, renderMode);
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderDocument in CelementsWebScriptService
   */
  @Deprecated
  public String renderDocument(Document renderDoc) {
    return getService().renderDocument(renderDoc);
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderDocument in CelementsWebScriptService
   */
  @Deprecated
  public String renderDocument(Document renderDoc, boolean removePre,
      List<String> rendererNameList) {
    return getService().renderDocument(renderDoc, removePre, rendererNameList);
  }

  private RenderCommand getCelementsRenderCmd() {
    RenderCommand renderCommand = new RenderCommand();
    renderCommand.setDefaultPageType("RichText");
    return renderCommand;
  }

  public String renderCelementsDocument(Document renderDoc) {
    return renderCelementsDocument(renderDoc, "view");
  }

  /**
   * @deprecated since 2.17.0
   *             instead use renderCelementsDocument in CelementsWebScriptService
   */
  @Deprecated
  public String renderCelementsDocument(Document renderDoc, String renderMode) {
    return getService().renderCelementsDocument(renderDoc, renderMode);
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
  
  public DocumentCreationWorkerControlApi getTestDocumentCreationWorker() {
    return new DocumentCreationWorkerControlApi(context);
  }

  public String clearFileName(String fileName) {
    return context.getWiki().clearName(fileName, false, true, context);
  }

  public String getDocHeaderTitle(String fullName) {
    return new DocHeaderTitleCommand().getDocHeaderTitle(fullName, context);
  }

  public void logDeprecatedVelocityScript(String logMessage) {
    LOGGER.warn("deprecated usage of velocity Script: " + logMessage);
  }

  public String isValidUserJSON(String username, String password, String memberOfGroup, 
      List<String> returnGroupMemberships) {
    RemoteUserValidator validater = new RemoteUserValidator();
    if(hasProgrammingRights()) {
      return validater.isValidUserJSON(username, password, memberOfGroup, 
          returnGroupMemberships, context);
    }
    return null;
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
  
  public com.xpn.xwiki.api.Object newObjectForFormStorage(Document storageDoc,
      String className) {
    if (hasProgrammingRights()) {
      BaseObject newStoreObj = new FormObjStorageCommand().newObject(
          storageDoc.getDocument(), className, context);
      if (newStoreObj != null) {
        return newStoreObj.newObjectApi(newStoreObj, context);
      }
    }
    return null;
  }
  
  public boolean checkCaptcha() {
    return new CaptchaCommand().checkCaptcha(context);
  }
  
  public String getCaptchaId() {
    return new CaptchaCommand().getCaptchaId(context);
  }

  /**
   * Get the options (checkbox and radio buttons) saved using the ObjectSorage Action as
   * a Map.
   * @param options The String saved in the store object
   * @return Map containing all the 
   */
  public Map<String, String> getObjStoreOptionsMap(String options) {
    return (new ParseObjStoreCommand()).getObjStoreOptionsMap(options, context);
  }

  public boolean useXWikiLoginLayout() {
    return "1".equals(context.getWiki().getSpacePreference("xwikiLoginLayout",
        "celements.xwikiLoginLayout", "1", context));
  }

  public String getLogoutRedirectURL() {
    XWiki xwiki = context.getWiki();
    String logoutRedirectConf = xwiki.getSpacePreference("LogoutRedirect",
        "celements.logoutRedirect", xwiki.getDefaultSpace(context) + ".WebHome", context);
    String logoutRedirectURL = logoutRedirectConf;
    if (!logoutRedirectConf.startsWith("http://")
        && !logoutRedirectConf.startsWith("https://")) {
      logoutRedirectURL = xwiki.getURL(logoutRedirectConf, "view", "logout=1", context);
    }
    return logoutRedirectURL;
  }

  public String getLoginRedirectURL() {
    XWiki xwiki = context.getWiki();
    String loginRedirectConf = xwiki.getSpacePreference("LoginRedirect",
        "celements.loginRedirect", xwiki.getDefaultSpace(context) + ".WebHome", context);
    String loginRedirectURL = loginRedirectConf;
    if (!loginRedirectConf.startsWith("http://")
        && !loginRedirectConf.startsWith("https://")) {
      loginRedirectURL = xwiki.getURL(loginRedirectConf, "view", "", context);
    }
    return loginRedirectURL;
  }
  
  public boolean isCelementsRights(String fullName) {
    return new CelementsRightsCommand().isCelementsRights(fullName, context);
  }

  @SuppressWarnings("unchecked")
  public boolean executeAction(Document actionDoc) {
    return plugin.executeAction(actionDoc, context.getRequest().getParameterMap(),
        context.getDoc(), context);
  }

  /**
   * API to check rights on a document for a given user or group
   * 
   * @param level right to check (view, edit, comment, delete)
   * @param user user or group for which to check the right
   * @param isUser true for users and false for group
   * @param docname document on which to check the rights
   * @return true if right is granted/false if not
   */
  public boolean hasAccessLevel(String level, String user, boolean isUser,
      String docname) {
    try {
      //XXX add extended hasAccessLevel to interface asap
      return ((XWikiRightServiceImpl)context.getWiki().getRightService()).hasAccessLevel(
          level, user, docname, isUser, getXWikiContext());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * @deprecated since 2.11.6 instead use celementsweb script service
   */
  @Deprecated
  public String getSkinFile(String fileName) {
    return getService().getSkinFile(fileName);
  }

  /**
   * @deprecated since 2.11.6 instead use celementsweb script service
   */
  @Deprecated
  public String getSkinFile(String fileName, String action) {
    return getService().getSkinFile(fileName, action);
  }

  public SuggestBaseClass getSuggestBaseClass(DocumentReference classreference, 
      String fieldname) {
    return new SuggestBaseClass(classreference, fieldname, context);
  }

  public List<Object> getSuggestList(DocumentReference classRef, String fieldname, 
      String input) {
    return new SuggestListCommand().getSuggestList(classRef, fieldname, null, input, "",
        "", 0, context);
  }

  public List<Object> getSuggestList(DocumentReference classRef, String fieldname, 
      List<String> excludes, String input, String firstCol, String secCol, int limit) {
    return new SuggestListCommand().getSuggestList(classRef, fieldname, excludes, 
        input, firstCol, secCol, limit, context);
  }
  
  public String getDefaultSpace() {
    return context.getWiki().getDefaultSpace(context);
  }

  public void checkClasses()  {
    new CheckClassesCommand().checkClasses(context);
  }

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    return emptyCheckCmd.getNextNonEmptyChildren(documentRef);
  }

  public boolean useImageAnimations() {
    return "1".equals(context.getWiki().getSpacePreference("celImageAnimation",
        "celements.celImageAnimation", "0", context));
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String getAppScriptURL(String scriptName) {
    return getService().getAppScriptURL(scriptName);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String getAppScriptURL(String scriptName, String queryString) {
    return getService().getAppScriptURL(scriptName, queryString);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public boolean isAppScriptCurrentPage(String scriptName) {
    return getService().isAppScriptCurrentPage(scriptName);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String getScriptNameFromURL() {
    return getService().getScriptNameFromURL();
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public boolean isAppScriptRequest() {
    return getService().isAppScriptRequest();
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String getCurrentPageURL(String queryString) {
    return getService().getCurrentPageURL(queryString);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public String convertToPlainText(String htmlContent) {
    return getService().convertToPlainText(htmlContent);
  }

  /**
   * @deprecated since 2.11.2 instead use celementsweb script service
   */
  @Deprecated
  public Builder getNewJSONBuilder() {
    return getService().getNewJSONBuilder();
  }

  private CelementsWebScriptService getService() {
    return (CelementsWebScriptService) Utils.getComponent(ScriptService.class,
        "celementsweb");
  }

  private ContextMenuScriptService getCMService() {
    return (ContextMenuScriptService) Utils.getComponent(ScriptService.class,
        "contextMenu");
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

}
