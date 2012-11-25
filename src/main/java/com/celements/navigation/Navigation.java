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
package com.celements.navigation;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.PageTypeResolverService;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class Navigation implements INavigation {

  public static final int DEFAULT_MAX_LEVEL = 100;

  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS_DOC
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_DOC =
    NavigationClasses.NAVIGATION_CONFIG_CLASS_DOC;
  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS_SPACE
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_SPACE =
    NavigationClasses.NAVIGATION_CONFIG_CLASS_SPACE;
  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS =
    NavigationClasses.NAVIGATION_CONFIG_CLASS;

  private INavigationBuilder navBuilder;

  public static final String LIST_LAYOUT_TYPE = "list";

  private static final String _PAGE_MENU_DATA_TYPE = "pageMenu";

  private static final String _CEL_CM_NAV_MI_DEFAULT_CSSCLASS =
    "cel_cm_navigation_menuitem";

  public static final String MENU_TYPE_MENUITEM = "menuitem";

  private static Log LOGGER = LogFactory.getFactory().getInstance(Navigation.class);

  private static final String _NAVIGATION_COUNTER_KEY =
    NavigationApi.class.getCanonicalName() + "_NavigationCounter";

  private static final String _LANGUAGE_MENU_DATA_TYPE = "languages";

  String uniqueName;
  IWebUtils utils;

  private boolean navigationEnabled;
  
  String configName;
  int fromHierarchyLevel;
  int toHierarchyLevel;
  String menuPart;
  String menuSpace;
  private int showInactiveToLevel;

  private String cmCssClass;

  private String mainUlCssClasses;

  private String dataType;

  private IPresentationTypeRole presentationType = null;

  private boolean _showAll;

  private boolean _hasLink;

  private INavFilter<BaseObject> navFilter;

  private MultilingualMenuNameCommand menuNameCmd;

  private String navLanguage;

  public ITreeNodeService injected_TreeNodeService;

  public IWebUtilsService injected_WebUtilsService;

  public PageTypeResolverService injected_PageTypeResolverService;

  public Navigation(String navUniqueId) {
    this.menuNameCmd = new MultilingualMenuNameCommand();
    this.uniqueName = navUniqueId;
    this.navigationEnabled = true;
    this.fromHierarchyLevel = 1;
    this.toHierarchyLevel = DEFAULT_MAX_LEVEL;
    this.menuPart = "";
    this.dataType = _PAGE_MENU_DATA_TYPE;
    this._showAll = false;
    this._hasLink = true;
    try {
      setLayoutType(LIST_LAYOUT_TYPE);
    } catch (UnknownLayoutTypeException exp) {
      LOGGER.fatal("Native List Layout Type not available!", exp);
      throw new IllegalStateException("Native List Layout Type not available!",
          exp);
    }
    this.menuSpace = null;
    this.mainUlCssClasses = "";
    this.cmCssClass = _CEL_CM_NAV_MI_DEFAULT_CSSCLASS;
    utils = WebUtils.getInstance();
  }

  /**
   * @deprecated since 2.18.0 instead use getNavigationConfigClassRef of NavigationClasses
   */
  @Deprecated
  public static DocumentReference getNavigationConfigClassReference(String wikiName) {
    return new DocumentReference(wikiName, NAVIGATION_CONFIG_CLASS_SPACE,
        NAVIGATION_CONFIG_CLASS_DOC);
  }

  public String getLayoutType() {
    return navBuilder.getLayoutTypeName();
  }

  public void setLayoutType(String layoutType
      ) throws UnknownLayoutTypeException {
    //TODO implement a component role
    if (LIST_LAYOUT_TYPE.equals(layoutType)) {
      this.navBuilder = new ListBuilder(uniqueName);
    } else {
      throw new UnknownLayoutTypeException(layoutType);
    }
  }

  public IPresentationTypeRole getPresentationType() {
    if (presentationType == null) {
      return Utils.getComponent(IPresentationTypeRole.class);
    } else {
      return presentationType;
    }
  }

  public void setPresentationType(IPresentationTypeRole presentationType) {
    this.presentationType = presentationType;
  }

  public void setPresentationType(String presentationTypeHint) {
    if (presentationTypeHint != null) {
      try {
        setPresentationType(Utils.getComponentManager().lookup(
            IPresentationTypeRole.class, presentationTypeHint));
      } catch (ComponentLookupException failedToLoadException) {
        LOGGER.error("setPresentationType failed to load IPresentationTypeRole for hint ["
            + presentationTypeHint + "].", failedToLoadException);
        this.presentationType = null;
      }
    } else {
      this.presentationType = null;
    }
  }

  /**
   * setFromHierarchyLevel
   * @param fromHierarchyLevel starting (including) at Hierarchy Level
   *          1 = mainMenu , 0 = spaceMenu (including all first mainMenuItems
   *           of all Spaces)
   */
  public void setFromHierarchyLevel(int fromHierarchyLevel) {
    this.fromHierarchyLevel = fromHierarchyLevel;
  }

  /**
   * setToHierarchyLevel
   * @param toHierarchyLevel ending (including) with Hierarchy Level
   */
  public void setToHierarchyLevel(int toHierarchyLevel) {
    this.toHierarchyLevel = toHierarchyLevel;
  }

  public void setMenuPart(String menuPart) {
    this.menuPart = menuPart;
  }

  /**
   * 
   * @param menuSpace (default: $doc.web)
   */
  public void setMenuSpace(String menuSpace) {
    if ((menuSpace != null) && (!"".equals(menuSpace))) {
      this.menuSpace = menuSpace;
    } else {
      this.menuSpace = null;
    }
  }

  public String includeNavigation(XWikiContext context) {
    LOGGER.debug("includeNavigation: navigationEnabled [" + navigationEnabled + "].");
    if(navigationEnabled){
      StringBuilder outStream = new StringBuilder();
      if (_PAGE_MENU_DATA_TYPE.equals(dataType)) {
          if (fromHierarchyLevel > 0) {
            String parent = utils.getParentForLevel(fromHierarchyLevel, context);
            try {
          	  if(parent != null) {
                  addNavigationForParent(outStream, parent, getNumLevels(), context);
          	  }
            } catch (XWikiException e) {
              LOGGER.error("addNavigationForParent failed for [" + parent + "].", e);
            }
          } else {
            throw new IllegalArgumentException("fromHierarchyLevel [" + fromHierarchyLevel
                + "] must be greater than zero");
          }
      } else if (_LANGUAGE_MENU_DATA_TYPE.equals(dataType)) {
        navBuilder.useStream(outStream);
        generateLanguageMenu(navBuilder, context);
      }
      return outStream.toString();
    } else{
      return "";
    }
  }

  public String getMenuSpace(XWikiContext context) {
    if (menuSpace == null) {
      SpaceReference currentSpaceRef = context.getDoc().getDocumentReference(
        ).getLastSpaceReference();
      if (fromHierarchyLevel == 1) {
        if (isEmptyMainMenu(currentSpaceRef) && getWebUtilsService().hasParentSpace()) {
          // is main Menu and no mainMenuItem found ; user has edit rights
          menuSpace = getWebUtilsService().getParentSpace();
        }
      }
      if (menuSpace == null) {
        menuSpace = currentSpaceRef.getName();
      }
    }
    return menuSpace;
  }

  private boolean isEmptyMainMenu(SpaceReference spaceRef) {
    getNavFilter().setMenuPart(getMenuPartForLevel(1));
    return getTreeNodeService().getSubNodesForParent(spaceRef, getNavFilter()
        ).size() == 0;
  }

  INavFilter<BaseObject> getNavFilter() {
    if (navFilter == null) {
      navFilter = new InternalRightsFilter();
    }
    return navFilter;
  }

  public void setNavFilter(INavFilter<BaseObject> navFilter) {
    this.navFilter = navFilter;
  }

  private int getNumLevels() {
    return toHierarchyLevel - fromHierarchyLevel + 1;
  }

  void addNavigationForParent(StringBuilder outStream,
      EntityReference parentRef, int numMoreLevels, XWikiContext context
  ) throws XWikiException {
    String parent = "";
    if (parentRef != null) {
      parent = getSerializer().serialize(parentRef);
    }
    addNavigationForParent(outStream, parent, numMoreLevels, context);
  }

  void addNavigationForParent(StringBuilder outStream,
      String parent, int numMoreLevels, XWikiContext context
      ) throws XWikiException {
    LOGGER.trace("addNavigationForParent: parent [" + parent + "] numMoreLevels ["
        + numMoreLevels + "].");
    if (numMoreLevels > 0) {
      getNavFilter().setMenuPart(getMenuPartForLevel(getCurrentLevel(numMoreLevels)));
      List<TreeNode> currentMenuItems =
        getTreeNodeService().getSubNodesForParent(parent, getMenuSpace(context),
            getNavFilter());
      if (currentMenuItems.size() > 0) {
        outStream.append("<ul " + addUniqueContainerId(parent) + " "
            + getMainUlCSSClasses() + ">");
        boolean isFirstItem = true;
        for (TreeNode treeNode : currentMenuItems) {
          DocumentReference nodeRef = treeNode.getDocumentReference();
          boolean isLastItem = (currentMenuItems.lastIndexOf(treeNode)
              == (currentMenuItems.size() - 1));
          writeMenuItemWithSubmenu(outStream, parent, numMoreLevels, nodeRef,
              isFirstItem, isLastItem, context);
          isFirstItem = false;
        }
        outStream.append("</ul>");
      } else if ((getCurrentLevel(numMoreLevels) == 1)
          && "".equals(parent) && hasedit(context)) {
        // is main Menu and no mainMenuItem found ; user has edit rights
        outStream.append("<ul>");
        openMenuItemOut(outStream, null, true, true, false);
        outStream.append("<span " + addUniqueElementId(null)
            + " " + addCssClasses(null, true, true, true, false)
            + ">" + getWebUtilsService().getAdminMessageTool().get("cel_nav_nomenuitems")
            + "</span>");
        closeMenuItemOut(outStream);
        outStream.append("</ul>");
      }
    }
  }

  private int getCurrentLevel(int numMoreLevels) {
    return getNumLevels() - numMoreLevels + 1;
  }

  /**
   * menuPart is only valid for the first level of a menu block.
   * 
   * @param currentLevel
   * @return
   */
  String getMenuPartForLevel(int currentLevel) {
    if (currentLevel == 1) {
      return menuPart;
    } else {
      return "";
    }
  }

  String getMainUlCSSClasses() {
    if (!"".equals(mainUlCssClasses.trim())) {
      return " class=\"" + mainUlCssClasses.trim() + "\" ";
    } else {
      return "";
    }
  }

  public void addUlCSSClass(String cssClass) {
    if (!(" " + mainUlCssClasses + " ").contains(" " + cssClass + " ")) {
      mainUlCssClasses = mainUlCssClasses.trim() + " " + cssClass;
    }
  }

  private boolean hasedit(XWikiContext context) throws XWikiException {
    return context.getWiki().getRightService().hasAccessLevel("edit",
        context.getUser(), context.getDoc().getFullName(), context);
  }

  private void writeMenuItemWithSubmenu(StringBuilder outStream, String parent,
      int numMoreLevels, DocumentReference docRef, boolean isFirstItem,
      boolean isLastItem, XWikiContext context) throws XWikiException {
    boolean showSubmenu = showSubmenuForMenuItem(docRef, getCurrentLevel(numMoreLevels
        ), context);
    String fullName = getSerializer().serialize(docRef);
    boolean isLeaf = isLeaf(fullName, context);
    openMenuItemOut(outStream, docRef, isFirstItem, isLastItem, isLeaf);
    writeMenuItemContent(outStream, isFirstItem, isLastItem, docRef, isLeaf);
    if (showSubmenu) {
      addNavigationForParent(outStream, docRef, numMoreLevels - 1, context);
    }
    closeMenuItemOut(outStream);
  }

  void writeMenuItemContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf
      ) throws XWikiException {
    getPresentationType().writeNodeContent(outStream, isFirstItem, isLastItem, docRef,
          isLeaf, this);
  }

  private boolean isLeaf(String fullName, XWikiContext context) {
    List<TreeNode> currentMenuItems = getTreeNodeService().getSubNodesForParent(fullName,
        getMenuSpace(context), getNavFilter());
    boolean isLeaf = (currentMenuItems.size() <= 0);
    return isLeaf;
  }

  public String getNavLanguage() {
    if (this.navLanguage != null) {
      return this.navLanguage;
    }
    return getContext().getLanguage();
  }

  public boolean useImagesForNavigation() {
    return getContext().getWiki().getSpacePreferenceAsInt("use_navigation_images", 0,
        getContext()) > 0;
  }

  private void closeMenuItemOut(StringBuilder outStream) {
    outStream.append("<!-- IE6 --></li>");
  }

  void openMenuItemOut(StringBuilder outStream, DocumentReference docRef,
      boolean isFirstItem, boolean isLastItem, boolean isLeaf) {
    outStream.append("<li" + addCssClasses(docRef, false, isFirstItem, isLastItem, isLeaf)
        + ">");
  }

  public String addCssClasses(DocumentReference docRef, boolean withCM,
      boolean isFirstItem, boolean isLastItem, boolean isLeaf) {
    String cssClasses = getCssClasses(docRef, withCM, isFirstItem, isLastItem, isLeaf);
    if (!"".equals(cssClasses.trim())) {
      return " class=\"" + cssClasses + "\"";
    }
    return "";
  }

  public String addUniqueElementId(DocumentReference docRef) {
    return "id=\"" + getUniqueId(docRef) + "\"";
  }

  private String addUniqueContainerId(String parent) {
    return "id=\"C" + getUniqueId(parent) + "\"";
  }

  public String getUniqueId(DocumentReference docRef) {
    String fullName = null;
    if (docRef != null) {
      fullName = getSerializer().serialize(docRef);
    }
    return getUniqueId(fullName);
  }

  public String getUniqueId(String fullName) {
    String theMenuSpace = getMenuSpace(getContext());
    if (fullName != null) {
      return uniqueName + ":" + theMenuSpace + ":" + fullName;
    } else {
      return uniqueName + ":" + theMenuSpace + ":" + menuPart + ":";
    }
  }

  boolean showSubmenuForMenuItem(DocumentReference docRef, int currentLevel,
      XWikiContext context) {
    return (isShowAll() || isBelowShowAllHierarchy(currentLevel)
        || isActiveMenuItem(docRef));
  }

  private boolean isBelowShowAllHierarchy(int currentLevel) {
    return (currentLevel <  showInactiveToLevel);
  }

  String getCssClasses(DocumentReference docRef, boolean withCM, boolean isFirstItem,
      boolean isLastItem, boolean isLeaf) {
    String cssClass = "";
    if (withCM) {
      cssClass += cmCssClass;
    }
    if (isFirstItem) {
      cssClass += " first";
    }
    if (isLastItem) {
      cssClass += " last";
    }
    if (isLeaf) {
      cssClass += " cel_nav_isLeaf";
    } else {
      cssClass += " cel_nav_hasChildren";
    }
    if (docRef != null) {
      if (docRef.equals(getContext().getDoc().getDocumentReference())) {
        cssClass += " currentPage";
      }
      cssClass += " " + getPageTypeConfigName(docRef);
      if (isActiveMenuItem(docRef)) {
        cssClass += " active";
      }
    }
    return cssClass.trim();
  }

  String getPageTypeConfigName(DocumentReference docRef) {
    PageTypeReference pageTypeRef = getPageTypeResolverService(
        ).getPageTypeRefForDocWithDefault(docRef);
    String getPageTypeConfigName = pageTypeRef.getConfigName();
    return getPageTypeConfigName;
  }

  boolean isActiveMenuItem(DocumentReference docRef) {
      DocumentReference currentDocRef = getContext().getDoc().getDocumentReference();
      List<DocumentReference> docParentList = getWebUtilsService().getDocumentParentsList(
          currentDocRef, true);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("isActiveMenuItem: for [" + docRef + "] with parents ["
            + Arrays.deepToString(docParentList.toArray(new String[0])) + "].");
      }
      return (docRef != null) && (docParentList.contains(docRef)
          || docRef.equals(currentDocRef));
  }

  public String getMenuLink(DocumentReference docRef) {
    String docURL = getContext().getWiki().getURL(docRef, "view", getContext());
    //FIX for bug in xwiki url-factory 2.7.2
    if ("".equals(docURL)) {
      docURL = "/";
    }
    return docURL;
  }

  public static INavigation createNavigation(XWikiContext context) {
    return new Navigation(Navigation.newNavIdForContext(context));
  }

  static String newNavIdForContext(XWikiContext context) {
    Long navCounter = (Long) getNavCounterFromContext(context) + 1;
    context.put(_NAVIGATION_COUNTER_KEY, navCounter);
    return "N" + navCounter;
  }

  private static Long getNavCounterFromContext(XWikiContext context) {
    if (!context.containsKey(_NAVIGATION_COUNTER_KEY)) {
      return new Long(0);
    }
    java.lang.Object navCounterObj = context.get(_NAVIGATION_COUNTER_KEY);
    if (navCounterObj instanceof Long) {
      return (Long) navCounterObj + 1;
    } else {
      throw new IllegalArgumentException("Long object in context expected but got "
          + navCounterObj.getClass());
    }
  }

  public int getMenuItemPos(String fullName, XWikiContext context) {
    return WebUtils.getInstance().getMenuItemPos(fullName, menuPart, context);
  }

  public int getActiveMenuItemPos(int menuLevel, XWikiContext context) {
    return WebUtils.getInstance().getActiveMenuItemPos(menuLevel, menuPart,
        context);
  }

  public List<com.xpn.xwiki.api.Object> getMenuItemsForHierarchyLevel(int menuLevel,
      XWikiContext context) {
    return WebUtils.getInstance().getMenuItemsForHierarchyLevel(menuLevel,
        menuPart, context);
  }

  public String getPrevMenuItemFullName(String fullName,
      XWikiContext context) {
    BaseObject prevMenuItem = null;
    try {
      prevMenuItem = WebUtils.getInstance().getPrevMenuItem(fullName, context);
    } catch (XWikiException e) {
      LOGGER.error(e, e);
    }
    if (prevMenuItem != null) {
      return prevMenuItem.getName();
    } else {
      return "";
    }
  }

  public String getNextMenuItemFullName(String fullName,
      XWikiContext context) {
    BaseObject nextMenuItem = null;
    try {
      nextMenuItem = WebUtils.getInstance().getNextMenuItem(fullName, context);
    } catch (XWikiException e) {
      LOGGER.error(e, e);
    }
    if (nextMenuItem != null) {
      return nextMenuItem.getName();
    } else {
      return "";
    }
  }

  public boolean isNavigationEnabled() {
    return navigationEnabled;
  }

  /**
   * 
   * Look for a Celements2.NavigationConfigClass object
   * on the WebPreferences, XWiki.XWikiPreferences or skin_doc in this
   * order an take the first place where any Celements2.NavigationConfigClass
   * object was found. If NO object for the given menu_element_name (configName)
   * at the selected place is found. This navigation should be set to disabled
   * and includeNavigation must return an empty string.
   */
  public void loadConfigByName(String configName, XWikiContext context) {
    XWikiDocument doc = context.getDoc();
    try{
      BaseObject prefObj = utils.getConfigDocByInheritance(doc,
          NAVIGATION_CONFIG_CLASS, context).getObject(NAVIGATION_CONFIG_CLASS,
              "menu_element_name", configName, false);
      loadConfigFromObject(prefObj);
    } catch(XWikiException e){
      LOGGER.error(e, e);
    }
  }

  public void loadConfigFromObject(BaseObject prefObj) {
    if (prefObj != null) {
      configName = prefObj.getStringValue("menu_element_name");
      LOGGER.debug("loadConfigFromObject: configName [" + configName + "] from doc ["
          + prefObj.getName() + "].");
      fromHierarchyLevel = prefObj.getIntValue("from_hierarchy_level", 0);
      toHierarchyLevel = prefObj.getIntValue("to_hierarchy_level", DEFAULT_MAX_LEVEL);
      showInactiveToLevel = prefObj.getIntValue("show_inactive_to_level", 0);
      menuPart = prefObj.getStringValue("menu_part");
      setMenuSpace(prefObj.getStringValue("menu_space"));
      if (!"".equals(prefObj.getStringValue("data_type"))
          && (prefObj.getStringValue("data_type") != null)) {
        dataType = prefObj.getStringValue("data_type");
      }
      if (!"".equals(prefObj.getStringValue("layout_type"))
          && (prefObj.getStringValue("layout_type") != null)) {
        try {
          setLayoutType(prefObj.getStringValue("layout_type"));
        } catch (UnknownLayoutTypeException exp) {
          LOGGER.error(exp, exp);
        }
      }
      String presentationTypeStr = prefObj.getStringValue(
          NavigationClasses.NAVIGATION_CONFIG_PRESENTATION_TYPE);
      if (!"".equals(presentationTypeStr)) {
        setPresentationType(presentationTypeStr);
      }
      setCMcssClass(prefObj.getStringValue("cm_css_class"));
//        setMenuTypeByTypeName(prefObj.getStringValue("menu_type"));
    } else{
      navigationEnabled = false;
    }
  }

  private void generateLanguageMenu(INavigationBuilder navBuilder,
      XWikiContext context) {
    List<String> langs = WebUtils.getInstance().getAllowedLanguages(context);
    mainUlCssClasses += " language";
    navBuilder.openLevel(mainUlCssClasses);
    for (String language : langs) {
      navBuilder.openMenuItemOut();
      boolean isLastItem = (langs.lastIndexOf(language) == (langs.size() - 1));
      navBuilder.appendMenuItemLink(language, "?language=" + language, getLanguageName(
          language, context), language.equals(getNavLanguage()), isLastItem, cmCssClass);
      navBuilder.closeMenuItemOut();
    }
    navBuilder.closeLevel();
  }

  private String getLanguageName(String lang, XWikiContext context) {
    XWikiMessageTool msg = context.getMessageTool();
    String space = context.getDoc().getDocumentReference().getLastSpaceReference().getName();    
    if(!msg.get("nav_cel_" + space + "_" + lang + "_" + lang).equals(
          "nav_cel_" + space + "_" + lang + "_" + lang)) {
      return msg.get("nav_cel_" + space + "_" + lang + "_" + lang);
    } else if(!msg.get("nav_cel_" + lang + "_" + lang).equals(
    	  "nav_cel_" + lang + "_" + lang)) {
      return msg.get("nav_cel_" + lang + "_" + lang);
    } else {
      return msg.get("cel_" + lang + "_" + lang);
    }
  }

  public void setCMcssClass(String cmCssClass) {
    if ((cmCssClass == null) || "".equals(cmCssClass)) {
      this.cmCssClass = _CEL_CM_NAV_MI_DEFAULT_CSSCLASS;
    } else {
      this.cmCssClass = cmCssClass;
    }
  }

  /**
   *  for Tests only !!!
   **/
  public void testInjectUtils(IWebUtils utils) {
    this.utils = utils;
  }

  /**
   *  for Tests only !!!
   **/
  public INavigationBuilder getNavBuilder() {
    return navBuilder;
  }

  public void setShowAll(boolean showAll) {
    this._showAll = showAll;
  }

  boolean isShowAll() {
    return this._showAll;
  }

  public void setShowInactiveToLevel(int showInactiveToLevel) {
    this.showInactiveToLevel = showInactiveToLevel;
  }

  public void setHasLink(boolean hasLink) {
    this._hasLink = hasLink;
  }

  public boolean hasLink() {
    return this._hasLink;
  }

  public MultilingualMenuNameCommand getMenuNameCmd() {
    return menuNameCmd;
  }

  public void setLanguage(String language) {
    this.navLanguage = language;
  }

  public void inject_menuNameCmd(MultilingualMenuNameCommand menuNameCmd) {
    this.menuNameCmd = menuNameCmd;
  }

  private IWebUtilsService getWebUtilsService() {
    if (injected_WebUtilsService != null) {
      return injected_WebUtilsService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private ITreeNodeService getTreeNodeService() {
    if (injected_TreeNodeService != null) {
      return injected_TreeNodeService;
    }
    return Utils.getComponent(ITreeNodeService.class);
  }

  @SuppressWarnings("unchecked")
  private EntityReferenceSerializer<String> getSerializer() {
    return Utils.getComponent(EntityReferenceSerializer.class, "local");
  }

  IPageTypeResolverRole getPageTypeResolverService() {
    if (injected_PageTypeResolverService != null) {
      return injected_PageTypeResolverService;
    }
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

}