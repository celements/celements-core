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
package com.celements.web.plugin;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.auth.AccountActivationFailedException;
import com.celements.auth.IAuthenticationServiceRole;
import com.celements.mailsender.IMailSenderRole;
import com.celements.mandatory.CheckMandatoryDocuments;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.IPageType;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.celements.web.plugin.cmd.TokenBasedUploadCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.CelementsWebService;
import com.celements.web.service.ICelementsWebServiceRole;
import com.celements.web.service.IPrepareVelocityContext;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.service.WebUtilsService;
import com.celements.webform.ActionService;
import com.celements.webform.IActionServiceRole;
import com.celements.webform.IWebFormServiceRole;
import com.celements.webform.WebFormService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;

public class CelementsWebPlugin extends XWikiDefaultPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelementsWebPlugin.class);

  final String PARAM_XPAGE = "xpage";
  final String PARAM_CONF = "conf";
  final String PARAM_AJAX_MODE = "ajax_mode";
  final String PARAM_SKIN = "skin";
  final String PARAM_LANGUAGE = "language";
  final String PARAM_XREDIRECT = "xredirect";

  public CelementsWebPlugin(String name, String className, XWikiContext context) {
    super(name, className, context);
  }

  @Override
  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new CelementsWebPluginApi((CelementsWebPlugin) plugin, context);
  }

  @Override
  public String getName() {
    return getPrepareVelocityContextService().getVelocityName();
  }

  @Override
  public void init(XWikiContext context) {
    LOGGER.trace("init called database [" + context.getDatabase() + "]");
    super.init(context);
  }

  @Override
  public void virtualInit(XWikiContext context) {
    // TODO move to ApplicationReadyEvent listener after migration to xwiki 4
    LOGGER.trace("virtualInit called database [" + context.getDatabase() + "]");
    if ("1".equals(context.getWiki().Param("celements.classCollections.checkOnStart", "1"))) {
      new CheckClassesCommand().checkClasses();
    }
    if ("1".equals(context.getWiki().Param("celements.mandatory.checkOnStart", "1"))) {
      new CheckMandatoryDocuments().checkMandatoryDocuments();
    }
    super.virtualInit(context);
  }

  /**
   * @deprecated since 6.0 instead use TreeNodeCache
   */
  @Deprecated
  public int queryCount() {
    throw new UnsupportedOperationException(
        "CelementsWebPlugin queryCount is not supported anymore.");
  }

  /**
   * getSubMenuItemsForParent
   * get all submenu items of given parent document (by fullname).
   *
   * @param parent
   * @param menuSpace
   *          (default: $doc.space)
   * @param menuPart
   * @return (array of menuitems)
   * @deprecated since 6.0 no replacement
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(String parent, String menuSpace,
      String menuPart, XWikiContext context) {
    throw new UnsupportedOperationException(
        "CelementsWebPlugin getSubMenuItemsForParent is not supported anymore.");
  }

  public String getVersionMode(XWikiContext context) {
    String versionMode = context.getWiki().getSpacePreference("celements_version", context);
    if ("---".equals(versionMode)) {
      versionMode = context.getWiki().getXWikiPreference("celements_version", "celements2",
          context);
      if ("---".equals(versionMode)) {
        versionMode = "celements2";
      }
    }
    return versionMode;
  }

  /**
   * getUsernameForUserData
   *
   * @param login
   * @param possibleLogins
   * @param context
   * @return
   * @throws XWikiException
   * @deprecated since 2.14.0 use UserNameForUserDataCommand instead
   */
  @Deprecated
  public String getUsernameForUserData(String login, String possibleLogins, XWikiContext context)
      throws XWikiException {
    return new UserNameForUserDataCommand().getUsernameForUserData(login, possibleLogins, context);
  }

  /**
   * @param userToken
   * @param context
   * @return
   * @throws XWikiException
   * @deprecated since 2.14.0 use TokenLDAPAuthServiceImpl instead
   */
  @Deprecated
  public String getUsernameForToken(String userToken, XWikiContext context) throws XWikiException {

    String hashedCode = encryptString("hash:SHA-512:", userToken);
    String userDoc = "";

    if ((userToken != null) && (userToken.trim().length() > 0)) {

      String hql = ", BaseObject as obj, Classes.TokenClass as token where ";
      hql += "doc.space='XWiki' ";
      hql += "and obj.name=doc.fullName ";
      hql += "and token.tokenvalue=? ";
      hql += "and token.validuntil>=? ";
      hql += "and obj.id=token.id ";

      List<Object> parameterList = new Vector<>();
      parameterList.add(hashedCode);
      parameterList.add(new Date());

      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
      LOGGER.info("searching token and found " + users.size() + " with parameters "
          + Arrays.deepToString(parameterList.toArray()));
      if ((users == null) || (users.size() == 0)) {
        String db = context.getDatabase();
        context.setDatabase("xwiki");
        users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
        if ((users != null) && (users.size() == 1)) {
          users.add("xwiki:" + users.remove(0));
        }
        context.setDatabase(db);
      }
      int usersFound = 0;
      for (String tmpUserDoc : users) {
        if (!tmpUserDoc.trim().equals("")) {
          usersFound++;
          userDoc = tmpUserDoc;
        }
      }
      if (usersFound > 1) {
        LOGGER.warn("Found more than one user for token '" + userToken + "'");
        return null;
      }
    } else {
      LOGGER.warn("No valid token given");
    }
    return userDoc;
  }

  /**
   * @deprecated since 2.59 instead use {@link IAuthenticationServiceRole
   *             #getPasswordHash(String, String)}
   */
  @Deprecated
  public String encryptString(String encoding, String str) {
    return getAuthenticationService().getPasswordHash(encoding, str);
  }

  /**
   * @deprecated since 2.59 instead use {@link IAuthenticationServiceRole
   *             #activateAccount(String)}
   */
  @Deprecated
  public Map<String, String> activateAccount(String activationCode, XWikiContext context)
      throws XWikiException {
    try {
      return getAuthenticationService().activateAccount(activationCode);
    } catch (AccountActivationFailedException authExp) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_PLUGINS,
          XWikiException.ERROR_XWIKI_UNKNOWN, "activateAccount failed.", authExp);
    }
  }

  /**
   * @deprecated since 2.59 instead use {@link CelementsWebService
   *             #getEmailAdressForUser(DocumentReference)}
   */
  @Deprecated
  public String getEmailAdressForUser(String username, XWikiContext context) {
    return getCelementsWebService().getEmailAdressForUser(
        getWebUtilsService().resolveDocumentReference(username));
  }

  // TODO Delegation can be removed as soon as latin1 flag can be removed
  /**
   * @deprecated since 2.19.0 instead use IMailSenderRole service directly.
   */
  @Deprecated
  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others, XWikiContext context) {
    return sendMail(from, replyTo, to, cc, bcc, subject, htmlContent, textContent, attachments,
        others, false, context);
  }

  /**
   * @deprecated since 2.19.0 instead use IMailSenderRole service directly.
   */
  @Deprecated
  public int sendMail(String from, String replyTo, String to, String cc, String bcc, String subject,
      String htmlContent, String textContent, List<Attachment> attachments,
      Map<String, String> others, boolean isLatin1, XWikiContext context) {
    return getMailSenderService().sendMail(from, replyTo, to, cc, bcc, subject, htmlContent,
        textContent, attachments, others, isLatin1);
  }

  private IMailSenderRole getMailSenderService() {
    return Utils.getComponent(IMailSenderRole.class);
  }

  /**
   * @deprecated since 2.59 instead use {@link WebUtilsService
   *             #getAttachmentsForDocs(List)}
   */
  @Deprecated
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN, XWikiContext context) {
    return getWebUtilsService().getAttachmentsForDocs(docsFN);
  }

  /**
   * @deprecated since 2.11.7 instead use renderCelementsDocument
   *             on celementsweb scriptService
   */
  @Deprecated
  public String renderCelementsPageType(XWikiDocument doc, IPageType pageType, XWikiContext context)
      throws XWikiException {
    XWikiDocument viewTemplate = context.getWiki().getDocument(pageType.getRenderTemplate("view"),
        context);
    return context.getWiki().getRenderingEngine().renderDocument(viewTemplate, doc, context);
  }

  @Override
  public void beginRendering(XWikiContext context) {
    LOGGER.debug("start beginRendering: language [" + context.getLanguage() + "].");
    try {
      getPrepareVelocityContextService().prepareVelocityContext(context);
    } catch (RuntimeException exp) {
      LOGGER.error("beginRendering", exp);
      throw exp;
    }
    LOGGER.debug("end beginRendering: language [" + context.getLanguage() + "].");
  }

  @Override
  public void beginParsing(XWikiContext context) {
    LOGGER.debug("start beginParsing: language [" + context.getLanguage() + "].");
    try {
      getPrepareVelocityContextService().prepareVelocityContext(context);
    } catch (RuntimeException exp) {
      LOGGER.error("beginParsing", exp);
      throw exp;
    }
    LOGGER.debug("end beginParsing: language [" + context.getLanguage() + "].");
  }

  IPrepareVelocityContext getPrepareVelocityContextService() {
    return Utils.getComponent(IPrepareVelocityContext.class);
  }

  /**
   * @deprecated since 2.59 instead use {@link CelementsWebService
   *             #getUniqueNameValueRequestMap()}
   */
  @Deprecated
  public Map<String, String> getUniqueNameValueRequestMap(XWikiContext context) {
    return getCelementsWebService().getUniqueNameValueRequestMap();
  }

  /**
   * @deprecated since 2.59 instead use {@link CelementsWebService
   *             #createUser(boolean)}
   */
  @Deprecated
  public int createUser(boolean validate, XWikiContext context) throws XWikiException {
    return getCelementsWebService().createUser(validate);
  }

  /**
   * @deprecated since 2.33.0 instead use PossibleLoginsCommand
   */
  @Deprecated
  public String getPossibleLogins(XWikiContext context) {
    return new PossibleLoginsCommand().getPossibleLogins();
  }

  /**
   * @deprecated since 2.59 instead use {@link CelementsWebService
   *             #createUser(Map, String, boolean)}
   */
  @Deprecated
  public synchronized int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate, XWikiContext context) throws XWikiException {
    return getCelementsWebService().createUser(userData, possibleLogins, validate);
  }

  /**
   * @param attachToDoc
   * @param fieldName
   * @param userToken
   * @param context
   * @return
   * @throws XWikiException
   * @deprecated since 2.28.0 use TokenBasedUploadCommand instead
   */
  @Deprecated
  public int tokenBasedUpload(Document attachToDoc, String fieldName, String userToken,
      XWikiContext context) throws XWikiException {
    return new TokenBasedUploadCommand().tokenBasedUpload(attachToDoc, fieldName, userToken,
        context);
  }

  /**
   * @param attachToDocFN
   * @param fieldName
   * @param userToken
   * @param createIfNotExists
   * @param context
   * @return
   * @throws XWikiException
   * @deprecated since 2.28.0 use TokenBasedUploadCommand instead
   */
  @Deprecated
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken,
      Boolean createIfNotExists, XWikiContext context) throws XWikiException {
    return new TokenBasedUploadCommand().tokenBasedUpload(attachToDocFN, fieldName, userToken,
        createIfNotExists, context);
  }

  /**
   * @deprecated since 2.59 instead use {@link IAuthenticationServiceRole
   *             #checkAuth(String, String, String, String, Boolean)}
   */
  @Deprecated
  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, Boolean noRedirect, XWikiContext context) throws XWikiException {
    return getAuthenticationService().checkAuth(logincredential, password, rememberme,
        possibleLogins, noRedirect);
  }

  /**
   * @deprecated since 2.59 instead use {@link ITreeNodeService
   *             #enableMappedMenuItems()}
   */
  @Deprecated
  public void enableMappedMenuItems(XWikiContext context) {
    GetMappedMenuItemsForParentCommand cmd = new GetMappedMenuItemsForParentCommand();
    cmd.setIsActive(true);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY, cmd);
  }

  /**
   * @deprecated since 2.59 instead use {@link ActionService
   *             #executeAction(Document, Map, XWikiDocument, XWikiContext)}
   */
  @Deprecated
  public boolean executeAction(Document actionDoc, Map<String, String[]> request,
      XWikiDocument includingDoc, XWikiContext context) {
    return getActionService().executeAction(actionDoc, request, includingDoc, context);
  }

  /**
   * @deprecated since 2.59 instead use {@link CelementsWebService
   *             #writeUTF8Response(String, String)}
   */
  @Deprecated
  public boolean writeUTF8Response(String filename, String renderDocFullName,
      XWikiContext context) {
    return getCelementsWebService().writeUTF8Response(filename, renderDocFullName);
  }

  /**
   * @deprecated since 2.59 instead use {@link WebFormService
   *             #isFormFilled(Map, Set)}
   */
  @Deprecated
  public boolean isFormFilled(Map<String, String[]> parameterMap, Set<String> additionalFields) {
    return getWebFormService().isFormFilled(parameterMap, additionalFields);
  }

  boolean arrayContains(String[] array, String value) {
    Arrays.sort(array);
    return (Arrays.binarySearch(array, value) >= 0);
  }

  /**
   * @deprecated since 2.14.0 use IWebUtilsService instead
   */
  @Deprecated
  public String getDefaultLanguage(XWikiContext context) {
    return getWebUtilsService().getDefaultLanguage();
  }

  /**
   * addTranslation
   *
   * @param fullName
   * @param language
   * @param context
   * @return
   * @deprecated since 2.14.0 please use the AddTranslationCommand directly
   */
  @Deprecated
  public boolean addTranslation(String fullName, String language, XWikiContext context) {
    return new AddTranslationCommand().addTranslation(fullName, language, context);
  }

  private IAuthenticationServiceRole getAuthenticationService() {
    return Utils.getComponent(IAuthenticationServiceRole.class);
  }

  private IActionServiceRole getActionService() {
    return Utils.getComponent(IActionServiceRole.class);
  }

  private ICelementsWebServiceRole getCelementsWebService() {
    return Utils.getComponent(ICelementsWebServiceRole.class);
  }

  private IWebFormServiceRole getWebFormService() {
    return Utils.getComponent(IWebFormServiceRole.class);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }
}
