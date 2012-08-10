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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.navigation.MenuItemNavigation;
import com.celements.navigation.MenuTypeRepository;
import com.celements.navigation.Navigation;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.web.pagetype.IPageType;
import com.celements.web.pagetype.PageTypeApi;
import com.celements.web.pagetype.PageTypeCommand;
import com.celements.web.pagetype.RenderCommand;
import com.celements.web.plugin.api.CelementsWebPluginApi;
import com.celements.web.plugin.cmd.AddTranslationCommand;
import com.celements.web.plugin.cmd.CelSendMail;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.UserNameForUserDataCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.token.NewCelementsTokenForUserCommand;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiResponse;

public class CelementsWebPlugin extends XWikiDefaultPlugin {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CelementsWebPlugin.class);

  private final static IWebUtils util = WebUtils.getInstance();

  final String PARAM_XPAGE = "xpage";
  final String PARAM_CONF = "conf";
  final String PARAM_AJAX_MODE = "ajax_mode";
  final String PARAM_SKIN = "skin";
  final String PARAM_LANGUAGE = "language";
  final String PARAM_XREDIRECT = "xredirect";

  private List<String> supportedAdminLangList;

  private CelSendMail injectedCelSendMail;

  public CelementsWebPlugin(
      String name, String className, XWikiContext context) {
    super(name, className, context);
    init(context);
  }

  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new CelementsWebPluginApi((CelementsWebPlugin) plugin, context);
  }

  public String getName() {
    mLogger.debug("Entered method getName");
    return "celementsweb";
  }

  public void flushCache() {
    //TODO: check if flushCache is called for changing a page MenuItem.
    mLogger.debug("Entered method flushCache");
  }

  public void flushCache(XWikiContext context) {
    util.flushMenuItemCache(context);
  }

  public void init(XWikiContext context) {
    addMenuTypeMenuItemToRepository();
    super.init(context);
  }

  public void virtualInit(XWikiContext context) {
    new CheckClassesCommand().checkClasses(context);
    super.virtualInit(context);
  }

  private void addMenuTypeMenuItemToRepository() {
    if (MenuTypeRepository.getInstance().put(Navigation.MENU_TYPE_MENUITEM,
        new MenuItemNavigation())) {
      mLogger.debug("Added MenuItemNavigation with key '"
          + Navigation.MENU_TYPE_MENUITEM + "' to the "
          + "MenuTypeRepository");
    }
  }

  public int queryCount() {
    return util.queryCount();
  }
  
  /**
   * getSubMenuItemsForParent
   * get all submenu items of given parent document (by fullname).
   * 
   * @param parent
   * @param menuSpace (default: $doc.space)
   * @param menuPart 
   * @return (array of menuitems)
   */
  public List<com.xpn.xwiki.api.Object> getSubMenuItemsForParent(
      String parent, String menuSpace, String menuPart, XWikiContext context) {
    return util.getSubMenuItemsForParent(parent, menuSpace, menuPart, context);
  }

  public String getVersionMode(XWikiContext context) {
    String versionMode = context.getWiki().getWebPreference("celements_version",
        context);
    if ("---".equals(versionMode)) {
      versionMode = context.getWiki().getXWikiPreference("celements_version",
          "celements2", context);
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
   * 
   * @deprecated since 2.14.0 use UserNameForUserDataCommand instead
   */
  @Deprecated
  public String getUsernameForUserData(String login, String possibleLogins,
      XWikiContext context) throws XWikiException{
    return new UserNameForUserDataCommand().getUsernameForUserData(login, possibleLogins,
        context);
  }

  /**
   * 
   * @param userToken
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @deprecated since 2.14.0 use TokenLDAPAuthServiceImpl instead
   */
  @Deprecated
  public String getUsernameForToken(String userToken, XWikiContext context
      ) throws XWikiException{
    
    String hashedCode = encryptString("hash:SHA-512:", userToken);
    String userDoc = "";
    
    if((userToken != null) && (userToken.trim().length() > 0)){
      
      String hql = ", BaseObject as obj, Classes.TokenClass as token where ";
      hql += "doc.space='XWiki' ";
      hql += "and obj.name=doc.fullName ";
      hql += "and token.tokenvalue=? ";
      hql += "and token.validuntil>=? ";
      hql += "and obj.id=token.id ";
      
      List<Object> parameterList = new Vector<Object>();
      parameterList.add(hashedCode);
      parameterList.add(new Date());
      
      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
      mLogger.info("searching token and found " + users.size() + " with parameters " + 
          Arrays.deepToString(parameterList.toArray()));
      if(users == null || users.size() == 0) {
        String db = context.getDatabase();
        context.setDatabase("xwiki");
        users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
        if(users != null && users.size() == 1) {
          users.add("xwiki:" + users.remove(0));
        }
        context.setDatabase(db);
      }
      int usersFound = 0;
      for (String tmpUserDoc : users) {
        if(!tmpUserDoc.trim().equals("")) {
          usersFound++;
          userDoc = tmpUserDoc;
        }
      }
      if(usersFound > 1){
        mLogger.warn("Found more than one user for token '" + userToken + "'");
        return null;
      }
    } else {
      mLogger.warn("No valid token given");
    }    
    return userDoc;
  }

  /**
   * 
   * @param accountName
   * @param guestPlus. if user is XWiki.XWikiGuest and guestPlus is true the account
   * XWiki.XWikiGuestPlus will be used to get the token.
   * @param context
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUser(String accountName,
      Boolean guestPlus, XWikiContext context) throws XWikiException {
    if (!"".equals(context.getRequest().getParameter("j_username"))
        && !"".equals(context.getRequest().getParameter("j_password"))) {
      mLogger.info("getNewCelementsTokenForUser: trying to authenticate  "
          + context.getRequest().getParameter("j_username"));
      Principal principal = context.getWiki().getAuthService().authenticate(
          context.getRequest().getParameter("j_username"),
          context.getRequest().getParameter("j_password"), context);
      if(principal != null) {
        mLogger.info("getNewCelementsTokenForUser: successfully autenthicated "
            + principal.getName());
        context.setUser(principal.getName());
        accountName = principal.getName();
      }
    }
    return new NewCelementsTokenForUserCommand().getNewCelementsTokenForUser(accountName,
        guestPlus, context);
  }

  public String encryptString(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }
  
  public Map<String, String> activateAccount(String activationCode,
      XWikiContext context) throws XWikiException{
    Map<String, String> userAccount = new HashMap<String, String>();
    String hashedCode = encryptString("hash:SHA-512:", activationCode);
    String username = new UserNameForUserDataCommand().getUsernameForUserData(hashedCode,
        "validkey", context);
    
    if((username != null) && !username.equals("")){
      String password = context.getWiki().generateRandomString(24);
      XWikiDocument doc = context.getWiki().getDocument(username, context);
      BaseObject obj = doc.getObject("XWiki.XWikiUsers");

//      obj.set("validkey", "", context);
      obj.set("active", "1", context);
      obj.set("force_pwd_change", "1", context);
      obj.set("password", password, context);
      
      context.getWiki().saveDocument(doc, context);
      
      userAccount.put("username", username);
      userAccount.put("password", password);
    }
    
    return userAccount;
  }

  public String getEmailAdressForUser(String username, XWikiContext context) {
    if (context.getWiki().exists(username, context)) {
      try {
        XWikiDocument doc = context.getWiki().getDocument(username, context);
        BaseObject obj = doc.getObject("XWiki.XWikiUsers");
        return obj.getStringValue("email");
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    }
    return null;
  }
  
  //TODO Delegation can be removed as soon as latin1 flag can be removed
  public int sendMail(
      String from, String replyTo, 
      String to, String cc, String bcc, 
      String subject, String htmlContent, String textContent, 
      List<Attachment> attachments, Map<String, String> others,
      XWikiContext context){
    return sendMail(from, replyTo, to, cc, bcc, subject, htmlContent, textContent,
        attachments, others, false, context);
  }
  
  public int sendMail(
        String from, String replyTo, 
        String to, String cc, String bcc, 
        String subject, String htmlContent, String textContent, 
        List<Attachment> attachments, Map<String, String> others, boolean isLatin1,
        XWikiContext context){
    CelSendMail sender = getCelSendMail(context);
    sender.setFrom(from);
    sender.setReplyTo(replyTo);
    sender.setTo(to);
    sender.setCc(cc);
    sender.setBcc(bcc);
    sender.setSubject(subject);
    sender.setHtmlContent(htmlContent, isLatin1);
    sender.setTextContent(textContent);
    sender.setAttachments(attachments);
    sender.setOthers(others);
    return sender.sendMail();
  }

  void injectCelSendMail(CelSendMail celSendMail) {
    this.injectedCelSendMail = celSendMail;
  }
  
  CelSendMail getCelSendMail(XWikiContext context) {
    if(injectedCelSendMail != null) {
      return injectedCelSendMail;
    }
    return new CelSendMail(context);
  }
    
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN, XWikiContext context) {
    List<Attachment> attachments = new ArrayList<Attachment>();
    for(String docFN : docsFN) {
      try {
        mLogger.info("getAttachmentsForDocs: processing doc " + docFN);
        for(XWikiAttachment xwikiAttachment : context.getWiki().getDocument(
            docFN, context).getAttachmentList()) {
          mLogger.info("getAttachmentsForDocs: adding attachment " + 
              xwikiAttachment.getFilename() + " to list.");
          attachments.add(new Attachment(context.getWiki().getDocument(
              docFN, context).newDocument(context), xwikiAttachment, context));
        }
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    }
    return attachments;
  }

  /**
   * @deprecated since 2.11.7 instead use renderCelementsDocument
   *             on celementsweb scriptService
   */
  @Deprecated
  public String renderCelementsPageType(XWikiDocument doc, IPageType pageType,
      XWikiContext context) throws XWikiException{
    XWikiDocument viewTemplate = context.getWiki().getDocument(
        pageType.getRenderTemplate("view"), context);
    return context.getWiki().getRenderingEngine(
        ).renderDocument(viewTemplate, doc, context);
  }

  public BaseObject getSkinConfigObj(XWikiContext context) {
    XWikiDocument doc = context.getDoc();
    try {
      XWiki xwiki = context.getWiki();
      XWikiDocument skinDoc = xwiki.getDocument(
          xwiki.getWebPreference("skin", context), context);
      String className = skinDoc.getObject("XWiki.XWikiSkins").getStringValue(
          "skin_config_class_name");
      BaseObject configObj = util.getConfigDocByInheritance(doc, className,
          context).getObject(className);
      return configObj;
    } catch(XWikiException e){
      mLogger.error(e);
    }
    return null;
  }

  @Override
  public void beginRendering(XWikiContext context) {
    try {
      initCelementsVelocity(context);
      initPanelsVelocity(context);
    } catch(RuntimeException exp) {
      mLogger.error("beginRendering", exp);
      throw exp;
    }
  }

  @Override
  public void beginParsing(XWikiContext context) {
    try {
      initCelementsVelocity(context);
      initPanelsVelocity(context);
    } catch(RuntimeException exp) {
      mLogger.error("beginParsing", exp);
      throw exp;
    }
  }

  void initPanelsVelocity(XWikiContext context) {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    if (vcontext != null) {
      if (!vcontext.containsKey("rightPanels")) {
        mLogger.debug("setting rightPanels in vcontext: " + getRightPanels(context));
        vcontext.put("rightPanels", getRightPanels(context));
      }
      if (!vcontext.containsKey("leftPanels")) {
        mLogger.debug("setting leftPanels in vcontext: " + getLeftPanels(context));
        vcontext.put("leftPanels", getLeftPanels(context));
      }
      if (!vcontext.containsKey("showRightPanels")) {
        vcontext.put("showRightPanels", Integer.toString(showRightPanels(context)));
      }
      if (!vcontext.containsKey("showLeftPanels")) {
        vcontext.put("showLeftPanels", Integer.toString(showLeftPanels(context)));
      }
      mLogger.debug("leftPanels [" + vcontext.get("leftPanels")
          + "] and rightPanels [" + vcontext.get("rightPanels")
          + "] after initPanelsVelocity.");
    }
  }

  void initCelementsVelocity(XWikiContext context) {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    if ((vcontext != null) && (context.getWiki() != null)) {
      if (!vcontext.containsKey(getName())) {
        vcontext.put(getName(), context.getWiki().getPluginApi(getName(), context));
      }
      if (!vcontext.containsKey("default_language")) {
        vcontext.put("default_language", getDefaultLanguage(context));
      }
      if (!vcontext.containsKey("langs")) {
        vcontext.put("langs", WebUtils.getInstance().getAllowedLanguages(context));
      }
      if (!vcontext.containsKey("hasedit")) {
        try {
          if (context.getDoc() != null) {
            vcontext.put("hasedit", context.getWiki(
                ).getRightService().hasAccessLevel("edit", context.getUser(),
                    context.getDoc().getFullName(), context));
          } else {
            vcontext.put("hasedit", new Boolean(false));
          }
        } catch (XWikiException exp) {
          mLogger.error("Failed to check edit Access Rights on "
              + context.getDoc().getFullName(), exp);
          vcontext.put("hasedit", new Boolean(false));
        }
      }
      if (!vcontext.containsKey("skin_doc")) {
        try {
          String skinDocName = context.getWiki().getSkin(context);
          Document skinDoc = context.getWiki().getDocument(skinDocName, context
              ).newDocument(context);
          vcontext.put("skin_doc", skinDoc);
        } catch (XWikiException e) {
          mLogger.error("Failed to get skin_doc");
        }
      }
      if (!vcontext.containsKey("isAdmin")) {
        vcontext.put("isAdmin", util.isAdminUser(context));
      }
      if (!vcontext.containsKey("isSuperAdmin")) {
        vcontext.put("isSuperAdmin", (util.isAdminUser(context)
            && context.getUser().startsWith("xwiki:")));
      }
      if (!vcontext.containsKey("admin_language")) {
        vcontext.put("admin_language", util.getAdminLanguage(context));
        mLogger.debug("added admin_language to vcontext: "
            + util.getAdminLanguage(context));
      }
      if (!vcontext.containsKey("adminMsg")) {
        vcontext.put("adminMsg", WebUtils.getInstance().getAdminMessageTool(
            context));
      }
      if (!vcontext.containsKey("celements2_skin")) {
        vcontext.put("celements2_skin", getCelementsSkinDoc(context));
      }
      if (!vcontext.containsKey("celements2_baseurl")
          && (getCelementsSkinDoc(context) != null)) {
        String celements2_baseurl = getCelementsSkinDoc(context).getURL("view");
        if (celements2_baseurl.indexOf("/",8) > 0) {
          vcontext.put("celements2_baseurl", celements2_baseurl.substring(0,
              celements2_baseurl.indexOf("/",8)));
        }
      }
      if (!vcontext.containsKey("page_type")) {
        vcontext.put("page_type", PageTypeCommand.getInstance().getPageType(context.getDoc(),
            context));
      }
      if (!vcontext.containsKey("tinyMCE_width")) {
        vcontext.put("tinyMCE_width", getRTEwidth(context));
      }
    }
  }

  String getRTEwidth(XWikiContext context) {
    int tinyMCEwidth = -1;
    String tinyMCEwidthStr = "";
    if (getCelementsSkinDoc(context) != null) {
      BaseObject pageTypeObj = PageTypeCommand.getInstance().getPageTypeObj(
          context.getDoc(), context).getPageTypeProperties(context);
      if (pageTypeObj != null) {
        tinyMCEwidth = pageTypeObj.getIntValue("rte_width", -1);
        tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
      }
      if (tinyMCEwidth < 0) {
        tinyMCEwidthStr = context.getWiki().getWebPreference("editbox_width", context);
        if ((tinyMCEwidthStr != null) && !"".equals(tinyMCEwidthStr)) {
          tinyMCEwidth = Integer.parseInt(tinyMCEwidthStr);
        }
      }
    }
    if (tinyMCEwidth < 0) {
      tinyMCEwidth = 453;
      tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
    }
    return tinyMCEwidthStr;
  }

  private Document getCelementsSkinDoc(XWikiContext context) {
    Document skinDoc = null;
    try {
      skinDoc = context.getWiki(
          ).getDocument("celements2web:XWiki.Celements2Skin", context
              ).newDocument(context);
    } catch (XWikiException exp) {
      mLogger.error("Failed to load celements2_skin"
          + " (celements2web:XWiki.Celements2Skin) ", exp);
    }
    return skinDoc;
  }


  public int showRightPanels(XWikiContext context) {
    if (showRightPanelsBoolean(context) && !getRightPanels(context).isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showRightPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showRightPanels");
  }

  public int showLeftPanels(XWikiContext context) {
    if (showLeftPanelsBoolean(context) && !getLeftPanels(context).isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showLeftPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showLeftPanels");
  }

  public List<String> getRightPanels(XWikiContext context) {
    if (showRightPanelsBoolean(context)) {
      return Arrays.asList(getPanelString(context, "rightPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  public List<String> getLeftPanels(XWikiContext context) {
    if (showLeftPanelsBoolean(context)) {
      return Arrays.asList(getPanelString(context, "leftPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  private boolean showPanelByConfigName(XWikiContext context,
      String configName) {
    if (isPageShowPanelOverwrite(configName, context.getDoc())) {
      return (1 == getPagePanelObj(configName, context.getDoc()
          ).getIntValue("show_panels"));
    } else if ((getPageTypeDoc(context) != null)
        && isPageShowPanelOverwrite(configName, getPageTypeDoc(context))) {
      boolean showPanels = (1 == getPagePanelObj(configName, getPageTypeDoc(context)
          ).getIntValue("show_panels"));
      mLogger.debug("using pagetype for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (isSpaceOverwrite(context)) {
      boolean showPanels = "1".equals(context.getWiki().getWebPreference(configName,
          getSpaceOverwrite(context), "0", context));
      mLogger.debug("using spaceover webPrefs for panels " + configName
          + "," + getSpaceOverwrite(context) +" -> "+ showPanels);
      return showPanels;
    } else if (isGlobalPref(context)) {
      boolean showPanels = ("1".equals(context.getWiki().getXWikiPreference(configName,
          context)));
      mLogger.debug("using globalPref for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (context.getWiki() != null) {
      boolean showPanels = ("1".equals(context.getWiki().getWebPreference(configName,
          context)));
      mLogger.debug("using webPrefs for panels " + configName + " -> "+ showPanels);
      return showPanels;
    }
    return false;
  }

  private XWikiDocument getPageTypeDoc(XWikiContext context) {
    if(context.getDoc() != null) {
      try {
        Document templateDocument = new PageTypeApi(
        context.getDoc().getFullName(), context).getTemplateDocument();
        XWikiDocument pageTypeDoc = context.getWiki().getDocument(
            templateDocument.getFullName(), context);
        mLogger.debug("getPageTypeDoc: pageTypeDoc=" + pageTypeDoc + " , "
            + templateDocument);
        return pageTypeDoc;
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    }
    return null;
  }

  private BaseObject getPagePanelObj(String configName, XWikiDocument theDoc) {
    if (theDoc != null) {
      return theDoc.getObject(CheckClassesCommand.CLASS_PANEL_CONFIG_CLASS, "config_name",
        getPanelType(configName), false);
    } else {
      return null;
    }
  }

  private boolean isPageShowPanelOverwrite(String configName,
      XWikiDocument theDoc) {
    try {
      return ((getPagePanelObj(configName, theDoc) != null)
         && (((BaseProperty)getPagePanelObj(configName, theDoc
             ).get("show_panels")).getValue() != null));
    } catch (XWikiException e) {
      mLogger.error(e);
      return false;
    }
  }

  private String getPanelType(String configName) {
    if ("showLeftPanels".equals(configName)) {
      return "leftPanels";
    } else if ("showRightPanels".equals(configName)) {
      return "rightPanels";
    } else {
      return configName;
    }
  }

  private boolean isSpaceOverwrite(XWikiContext context) {
    return ((getSpaceOverwrite(context) != null)
        && !"".equals(getSpaceOverwrite(context)));
  }

  private String getPanelString(XWikiContext context, String configName) {
    String panelsString = "";
    if(isGlobalPref(context)) {
      panelsString = context.getWiki().getXWikiPreference(configName, context);
    } else if(isPagePanelsOverwrite(configName, context.getDoc())) {
      panelsString = getPagePanelObj(configName, context.getDoc()
          ).getStringValue("panels");
    } else if((getPageTypeDoc(context) != null)
        && isPagePanelsOverwrite(configName, getPageTypeDoc(context))) {
      panelsString = getPagePanelObj(configName, getPageTypeDoc(context)
          ).getStringValue("panels");
    } else if(isSpaceOverwrite(context)) {
      panelsString = context.getWiki().getWebPreference(configName,
           getSpaceOverwrite(context), "", context);
    } else {
      panelsString = context.getWiki().getUserPreference(configName, context);
      mLogger.debug("else with panels in userPreferences: " + panelsString);
      if("".equals(panelsString)) {
         panelsString = context.getWiki().getWebPreference(configName, context);
         mLogger.debug("else with panels in webPreferences: " + panelsString);
      }
    }
    mLogger.debug("panels for config " + configName + " ; " + panelsString);
    return panelsString;
  }

  private boolean isPagePanelsOverwrite(String configName,
      XWikiDocument theDoc) {
    return ((getPagePanelObj(configName, theDoc) != null)
        && (!"".equals(getPagePanelObj(configName, theDoc
            ).getStringValue("panels"))));
  }

  private String getSpaceOverwrite(XWikiContext context) {
    if(context.getRequest() != null) {
      return context.getRequest().get("space");
    }
    return "";
  }

  private boolean isGlobalPref(XWikiContext context) {
    if ((context.getDoc() != null) && (context.getRequest() != null)) {
      return "XWiki.XWikiPreferences".equals(context.getDoc().getFullName())
          || "globaladmin".equals(context.getRequest().get("editor"));
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getUniqueNameValueRequestMap(XWikiContext context) {
    Map<String, String[]> params = context.getRequest().getParameterMap();
    Map<String, String> resultMap = new HashMap<String, String>();
    for (String key : params.keySet()) {
      if((params.get(key) != null) && (params.get(key).length > 0)) {
        resultMap.put(key, params.get(key)[0]);
      } else {
        resultMap.put(key, "");
      }
    }
    return resultMap;
  }
  
  public int createUser(boolean validate, XWikiContext context) throws XWikiException{
    String possibleLogins = context.getWiki().getXWikiPreference("cellogin", context);
    if((possibleLogins == null) || "".equals(possibleLogins)) {
      String db = context.getDatabase();
      context.setDatabase("celements2web");
      possibleLogins = context.getWiki().getXWikiPreference("cellogin", context);
      context.setDatabase(db);
      if((possibleLogins == null) || "".equals(possibleLogins)) {
        possibleLogins = "loginname";
      }
    }
    return createUser(getUniqueNameValueRequestMap(context), possibleLogins, validate, context);
  }
  
  @SuppressWarnings("deprecation")
  public synchronized int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate, XWikiContext context) throws XWikiException {
    String accountName = "";
    if(userData.containsKey("xwikiname")) {
      accountName = userData.get("xwikiname");
      userData.remove("xwikiname");
    } else {
      while(accountName.equals("") || context.getWiki().exists("XWiki." + accountName, context)){
        accountName = context.getWiki().generateRandomString(12);
      }
    }
    String validkey = "";
    int success = -1;
    if(areIdentifiersUnique(userData, possibleLogins, context)) {
      if(!userData.containsKey("password")) {
        String password = context.getWiki().generateRandomString(8);
        userData.put("password", password);
      }
      if(!userData.containsKey("validkey")) {
        validkey = getUniqueValidationKey(context);
        userData.put("validkey", validkey);
      } else {
        validkey = userData.get("validkey");
      }
      if(!userData.containsKey("active")) {
        userData.put("active", "0");
      }
      String content = "#includeForm(\"XWiki.XWikiUserSheet\")";
      
      //TODO as soon as all installations are on xwiki 1.8+ change to new method (using
      //     XWikiDocument.XWIKI10_SYNTAXID as additional parameter
      success = context.getWiki().createUser(accountName, userData, "XWiki.XWikiUsers",
          content, "edit", context);
    }
    
    if(success == 1){
      // Set rights on user doc
      XWikiDocument doc = context.getWiki().getDocument("XWiki." + accountName, context);
      List<BaseObject> rightsObjs = doc.getObjects("XWiki.XWikiRights");
      for (BaseObject rightObj : rightsObjs) {
        if(rightObj.getStringValue("groups").equals("")){
          rightObj.set("users", doc.getFullName(), context);
          rightObj.set("allow", "1", context);
          rightObj.set("levels", "view,edit,delete", context);
          rightObj.set("groups", "", context);
        } else{
          rightObj.set("users", "", context);
          rightObj.set("allow", "1", context);
          rightObj.set("levels", "view,edit,delete", context);
          rightObj.set("groups", "XWiki.XWikiAdminGroup", context);
        }
      }
      context.getWiki().saveDocument(doc, context);
      
      if(validate) {
        mLogger.info("send account validation mail with data: accountname='" + accountName
            + "', email='" + userData.get("email") + "', validkey='" + validkey + "'");
        try{
          new PasswordRecoveryAndEmailValidationCommand().sendValidationMessage(
              userData.get("email"), validkey, "Tools.AccountActivationMail", context);
        } catch(XWikiException e){
          mLogger.error("Exception while sending validation mail to '" + 
              userData.get("email") + "'", e);
        }
      }
    }
    return success;
  }

  private boolean areIdentifiersUnique(Map<String, String> userData, 
      String possibleLogins, XWikiContext context) throws XWikiException {
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if(!"".equals(key.trim()) && (("," + possibleLogins + ",").indexOf("," + key + ",") >= 0)) {
        String user = getUsernameForUserData(userData.get(key), possibleLogins, context);
        if((user == null) || (user.length() > 0)) { //user == "" means there is no such user
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  /**
   * getUniqueValidationKey
   * 
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @deprecated since 2.14.0 use NewCelementsTokenForUserCommand instead
   */
  @Deprecated
  public String getUniqueValidationKey(XWikiContext context)
      throws XWikiException {
    return new NewCelementsTokenForUserCommand().getUniqueValidationKey(context);
  }

  @Deprecated
  public int tokenBasedUpload(Document attachToDoc, String fieldName, String userToken,
      XWikiContext context) throws XWikiException {
    String username = getUsernameForToken(userToken, context);
    if((username != null) && !username.equals("")){
      mLogger.info("tokenBasedUpload: user " + username + " identified by userToken.");
      context.setUser(username);
      return attachToDoc.addAttachments(fieldName);
    } else {
      mLogger.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken, 
      Boolean createIfNotExists, XWikiContext context) throws XWikiException {
    String username = getUsernameForToken(userToken, context);
    if((username != null) && !username.equals("")){
      mLogger.info("tokenBasedUpload: user " + username + " identified by userToken.");
      context.setUser(username);
      XWikiDocument doc = context.getWiki().getDocument(attachToDocFN, context);
      if (createIfNotExists || context.getWiki().exists(attachToDocFN, context)) {
        mLogger.info("tokenBasedUpload: add attachment.");
        return doc.newDocument(context).addAttachments(fieldName);
      } else {
        mLogger.warn("tokenBasedUpload: document " + attachToDocFN + " does not exist.");
      }
    } else {
      mLogger.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }

  /**
   * 
   * @param userToken
   * @param context
   * @return
   * @throws XWikiException
   * 
   * @deprecated since 2.14.0 use TokenLDAPAuthServiceImpl instead
   */
  @Deprecated
  public XWikiUser checkAuthByToken(String userToken, XWikiContext context
      ) throws XWikiException {
    String username = getUsernameForToken(userToken, context);
    if((username != null) && !username.equals("")){
      mLogger.info("checkAuthByToken: user " + username + " identified by userToken.");
      context.setUser(username);
      return context.getXWikiUser();
    } else {
      mLogger.warn("checkAuthByToken: username could not be identified by token");
    }
    return null;
  }

  public XWikiUser checkAuth(String logincredential, String password,
        String rememberme, String possibleLogins, XWikiContext context
      ) throws XWikiException {
    String loginname = getUsernameForUserData(logincredential, possibleLogins, context);
    if ("".equals(loginname) && possibleLogins.matches("(.*,)?loginname(,.*)?")) {
        loginname = logincredential;
    }
    return context.getWiki().getAuthService().checkAuth(loginname, password, rememberme,
        context);
  }

  public void enableMappedMenuItems(XWikiContext context) {
    GetMappedMenuItemsForParentCommand cmd = new GetMappedMenuItemsForParentCommand();
    cmd.set_isActive(true);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY, cmd);
  }

  public boolean executeAction(Document actionDoc, Map<String, String[]> request, 
      XWikiDocument includingDoc, XWikiContext context) {
    mLogger.info("Executing action on doc '" + actionDoc.getFullName() + "'");
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    vcontext.put("theDoc", actionDoc);
    Boolean debug = (Boolean)vcontext.get("debug");
    vcontext.put("debug", true);
    Boolean hasedit = (Boolean)vcontext.get("hasedit");
    vcontext.put("hasedit", true);
    Object req = vcontext.get("request");
    vcontext.put("request", getApiUsableMap(request));
    XWikiDocument execAct = null;
    try {
      execAct = context.getWiki()
          .getDocument("celements2web:Macros.executeActions", context);
    } catch (XWikiException e) {
      mLogger.error("Could not get action Macro", e);
    }
    String actionContent = "";
    if(execAct != null) {
      String execContent = execAct.getContent();
      execContent = execContent.replaceAll("\\{(/?)pre\\}", "");
      actionContent = context.getWiki().getRenderingEngine().interpretText(
          execContent, includingDoc, context);
    }
    boolean successful = "true".equals(vcontext.get("successful"));
    if(!successful) {
      mLogger.error("Error executing action. Output:" + vcontext.get("actionScriptOutput"));
      mLogger.error("Rendered Action Script: " + actionContent);
    }
    vcontext.put("debug", debug);
    vcontext.put("hasedit", hasedit);
    vcontext.put("request", req);
    return successful;
  }

  //FIXME Hack to get mail execution to work. The script is not expecting arrays in the
  //      map, since it expects a request. Multiple values with the same name get lost 
  //      in this "quick and dirty" fix
  private Object getApiUsableMap(Map<String, String[]> request) {
    Map<String, String> apiConform = new HashMap<String, String>();
    for (String key : request.keySet()) {
      if((request.get(key) != null) && (request.get(key).length > 0)) {
        apiConform.put(key, request.get(key)[0]);
      } else {
        apiConform.put(key, null);
      }
    }
    return apiConform;
  }

  public List<String> getSupportedAdminLanguages() {
    if (supportedAdminLangList == null) {
      setSupportedAdminLanguages(Arrays.asList(new String[] {"de","fr","en","it"}));
    }
    return supportedAdminLangList;
  }

  public void setSupportedAdminLanguages(List<String> newSupportedAdminLangList) {
    supportedAdminLangList = newSupportedAdminLangList;
  }

  public boolean writeUTF8Response(String filename, String renderDocFullName, 
      XWikiContext context) {
    boolean success = false;
    if(context.getWiki().exists(renderDocFullName, context)) {
      XWikiDocument renderDoc;
      try {
        renderDoc = context.getWiki().getDocument(renderDocFullName, context);
        adjustResponseHeader(filename, context.getResponse(), context);
        setResponseContent(renderDoc, context.getResponse(), context);
      } catch (XWikiException e) {
        mLogger.error(e);
      }
      context.setFinished(true);
    }
    return success;
  }
  
  void adjustResponseHeader(String filename, XWikiResponse response, 
      XWikiContext context) {
    response.setContentType("text/plain");
    String ofilename = Util.encodeURI(filename, context).replaceAll("\\+", " ");
    response.addHeader("Content-disposition", "attachment; filename=\"" + ofilename + 
        "\"; charset='UTF-8'");
  }

  void setResponseContent(XWikiDocument renderDoc, XWikiResponse response,
      XWikiContext context) throws XWikiException {
    String renderedContent = new RenderCommand(context).renderDocument(renderDoc);
    byte[] data = {};
    try {
      data = renderedContent.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    response.setContentLength(data.length + 3);
    try {
      response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
      response.getOutputStream().write(data);
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
          "Exception while sending response", e);
    }
  }

  public boolean isFormFilled(Map<String, String[]> parameterMap, 
      Set<String> additionalFields) {
    boolean isFilled = false;
    if(parameterMap.size() > getIsFilledModifier(parameterMap, additionalFields)) {
      isFilled = true;
    }
    return isFilled;
  }
  
  short getIsFilledModifier(Map<String, String[]> parameterMap, 
      Set<String> additionalFields) {
    List<String> standardParams = new ArrayList<String>();
    standardParams.add(PARAM_XPAGE);
    standardParams.add(PARAM_CONF);
    standardParams.add(PARAM_AJAX_MODE);
    standardParams.add(PARAM_SKIN);
    standardParams.add(PARAM_LANGUAGE);
    standardParams.add(PARAM_XREDIRECT);
    short modifier = 0;
    if(parameterMap.containsKey(PARAM_XPAGE) && parameterMap.containsKey(PARAM_CONF) && 
        arrayContains(parameterMap.get(PARAM_XPAGE), "overlay")) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_XPAGE) && parameterMap.containsKey(PARAM_AJAX_MODE) && 
        arrayContains(parameterMap.get(PARAM_XPAGE), "celements_ajax")) {
      modifier += 1;
      if(parameterMap.containsKey(PARAM_SKIN)) {
        modifier += 1;
      }
    }
    if(parameterMap.containsKey(PARAM_XPAGE)) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_XREDIRECT)) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_LANGUAGE)) {
      modifier += 1;
    }
    if((additionalFields != null) && additionalFields.size() > 0) {
      for (String param : additionalFields) {
        if(!standardParams.contains(param) && parameterMap.containsKey(param)) {
          modifier += 1;
        }
      }
    }
    return modifier;
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
    return getWebService().getDefaultLanguage();
  }

  private IWebUtilsService getWebService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  /**
   * addTranslation
   * @param fullName
   * @param language
   * @param context
   * @return
   * 
   * @deprecated since 2.14.0 please use the AddTranslationCommand directly
   */
  @Deprecated
  public boolean addTranslation(String fullName, String language, XWikiContext context) {
    return new AddTranslationCommand().addTranslation(fullName, language, context);
  }

}