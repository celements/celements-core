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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.PageTypeResolverService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class Navigation implements INavigation {

  @Deprecated
  public static final int DEFAULT_MAX_LEVEL = NavigationConfig.DEFAULT_MAX_LEVEL;

  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS_DOC
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_DOC = NavigationClasses.NAVIGATION_CONFIG_CLASS_DOC;
  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS_SPACE
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_SPACE = NavigationClasses.NAVIGATION_CONFIG_CLASS_SPACE;
  /**
   * @deprecated since 2.18.0 instead use NavigationClasses.NAVIGATION_CONFIG_CLASS
   */
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS = NavigationClasses.NAVIGATION_CONFIG_CLASS;

  private INavigationBuilder navBuilder;

  @Deprecated
  public static final String LIST_LAYOUT_TYPE = NavigationConfig.LIST_LAYOUT_TYPE;

  public static final String MENU_TYPE_MENUITEM = "menuitem";

  private final static Logger LOGGER = LoggerFactory.getLogger(Navigation.class);

  private static final String NAVIGATION_COUNTER_KEY = NavigationApi.class.getCanonicalName()
      + "_NavigationCounter";

  private static final String LANGUAGE_MENU_DATA_TYPE = "languages";

  public PageLayoutCommand pageLayoutCmd = new PageLayoutCommand();

  String uniqueName;
  IWebUtils utils;

  private boolean navigationEnabled;

  String configName;
  int fromHierarchyLevel;
  int toHierarchyLevel;
  String menuPart;
  SpaceReference nodeSpaceRef;
  private int showInactiveToLevel;
  private int offset = 0;
  private int nrOfItemsPerPage = -1;

  private String cmCssClass;

  private String mainUlCssClasses;

  private String emptyDictKeySuffix;

  private String dataType;

  private String navInclude = null;

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
    this.fromHierarchyLevel = NavigationConfig.DEFAULT_MIN_LEVEL;
    this.toHierarchyLevel = DEFAULT_MAX_LEVEL;
    this.menuPart = "";
    this.dataType = NavigationConfig.PAGE_MENU_DATA_TYPE;
    this._showAll = false;
    this._hasLink = true;
    try {
      setLayoutType(LIST_LAYOUT_TYPE);
    } catch (UnknownLayoutTypeException exp) {
      LOGGER.error("Native List Layout Type not available!", exp);
      throw new IllegalStateException("Native List Layout Type not available!", exp);
    }
    this.nodeSpaceRef = null;
    this.mainUlCssClasses = "";
    this.cmCssClass = "";
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

  public void setLayoutType(String layoutType) throws UnknownLayoutTypeException {
    // TODO implement a component role
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

  @Override
  public void setPresentationType(String presentationTypeHint) {
    if (presentationTypeHint != null) {
      try {
        LOGGER.info("setPresentationType to [" + presentationTypeHint + "].");
        setPresentationType(Utils.getComponent(IWebUtilsService.class).lookup(
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
   *
   * @param fromHierarchyLevel
   *          starting (including) at Hierarchy Level 1 = mainMenu , 0 = spaceMenu
   *          (including all first mainMenuItems of all Spaces)
   */
  @Override
  public void setFromHierarchyLevel(int fromHierarchyLevel) {
    if (fromHierarchyLevel > 0) {
      this.fromHierarchyLevel = fromHierarchyLevel;
    }
  }

  /**
   * setToHierarchyLevel
   *
   * @param toHierarchyLevel
   *          ending (including) with Hierarchy Level
   */
  @Override
  public void setToHierarchyLevel(int toHierarchyLevel) {
    this.toHierarchyLevel = toHierarchyLevel;
  }

  @Override
  public void setMenuPart(String menuPart) {
    this.menuPart = menuPart;
  }

  /**
   * @param menuSpace
   *          (default: $doc.web)
   * @deprecated since 2.24.0 instead use setNodeSpace
   */
  @Override
  @Deprecated
  public void setMenuSpace(String menuSpace) {
    if ((menuSpace != null) && (!"".equals(menuSpace))) {
      setNodeSpace(getWebUtilsService().resolveSpaceReference(menuSpace));
    } else {
      setNodeSpace(null);
    }
  }

  @Override
  public void setNodeSpace(SpaceReference newNodeSpaceRef) {
    this.nodeSpaceRef = newNodeSpaceRef;
  }

  /**
   * @deprecated since 2.24.0 use includeNavigation() instead.
   */
  @Override
  @Deprecated
  public String includeNavigation(XWikiContext context) {
    return includeNavigation();
  }

  @Override
  public String includeNavigation() {
    if (fromHierarchyLevel > 0) {
      DocumentReference parentRef = getWebUtilsService().getParentForLevel(fromHierarchyLevel);
      if ((fromHierarchyLevel == 1) || (parentRef != null)) {
        return includeNavigation(parentRef);
      }
      return "";
    } else {
      throw new IllegalArgumentException("fromHierarchyLevel [" + fromHierarchyLevel
          + "] must be greater than zero");
    }
  }

  @Override
  public String includeNavigation(DocumentReference parentRef) {
    LOGGER.debug("includeNavigation: navigationEnabled [" + navigationEnabled + "].");
    if (navInclude == null) {
      if (navigationEnabled) {
        StringBuilder outStream = new StringBuilder();
        if (NavigationConfig.PAGE_MENU_DATA_TYPE.equals(dataType)) {
          try {
            addNavigationForParent(outStream, parentRef, getNumLevels());
          } catch (XWikiException e) {
            LOGGER.error("addNavigationForParent failed for [" + parentRef + "].", e);
          }
        } else if (LANGUAGE_MENU_DATA_TYPE.equals(dataType)) {
          navBuilder.useStream(outStream);
          generateLanguageMenu(navBuilder, getContext());
        }
        navInclude = outStream.toString();
      } else {
        navInclude = "";
      }
    }
    return navInclude;
  }

  /**
   * @deprecated since 2.24.0 instead use getNodeSpaceRef()
   */
  @Override
  @Deprecated
  public String getMenuSpace(XWikiContext context) {
    return getWebUtilsService().getRefLocalSerializer().serialize(getNodeSpaceRef());
  }

  @Override
  public SpaceReference getNodeSpaceRef() {
    if (nodeSpaceRef == null) {
      SpaceReference currentSpaceRef = getContext().getDoc().getDocumentReference().getLastSpaceReference();
      if (fromHierarchyLevel == 1) {
        if (isEmptyMainMenu(currentSpaceRef) && getWebUtilsService().hasParentSpace(
            currentSpaceRef.getName())) {
          // is main Menu and no mainMenuItem found ; user has edit rights
          nodeSpaceRef = getWebUtilsService().resolveSpaceReference(
              getWebUtilsService().getParentSpace(currentSpaceRef.getName()));
        }
      }
      if (nodeSpaceRef == null) {
        nodeSpaceRef = currentSpaceRef;
      }
    }
    SpaceReference theNodeSpaceRef = nodeSpaceRef;
    return theNodeSpaceRef;
  }

  @Override
  public boolean isEmptyMainMenu() {
    return isEmptyMainMenu(getNodeSpaceRef());
  }

  public boolean isEmptyMainMenu(SpaceReference spaceRef) {
    getNavFilter().setMenuPart(getMenuPartForLevel(1));
    return getTreeNodeService().getSubNodesForParent(spaceRef, getNavFilter()).size() == 0;
  }

  @Override
  public boolean isEmpty() {
    getNavFilter().setMenuPart(getMenuPartForLevel(fromHierarchyLevel));
    EntityReference parentRef = getWebUtilsService().getParentForLevel(fromHierarchyLevel);
    LOGGER.debug("isEmpty: parentRef [" + parentRef + "] for level [" + fromHierarchyLevel + "]");
    if (parentRef == null) {
      LOGGER.info("isEmpty: no subnode for level [" + fromHierarchyLevel + "] found.");
      return true;
    }
    List<TreeNode> subNodeList = getTreeNodeService().getSubNodesForParent(parentRef,
        getNavFilter());
    LOGGER.info("isEmpty: subNodeList size [" + subNodeList.size() + "] for parentRef [" + parentRef
        + "] -> [" + subNodeList.isEmpty() + "].");
    return subNodeList.isEmpty();
  }

  INavFilter<BaseObject> getNavFilter() {
    if (navFilter == null) {
      navFilter = new InternalRightsFilter();
    }
    return navFilter;
  }

  @Override
  public void setNavFilter(INavFilter<BaseObject> navFilter) {
    this.navFilter = navFilter;
  }

  private int getNumLevels() {
    return (toHierarchyLevel - fromHierarchyLevel) + 1;
  }

  void addNavigationForParent(StringBuilder outStream, EntityReference parentRef, int numMoreLevels)
      throws XWikiException {
    LOGGER.debug("addNavigationForParent: parent [" + parentRef + "] numMoreLevels ["
        + numMoreLevels + "].");
    if (numMoreLevels > 0) {
      String parent = "";
      if (parentRef != null) {
        parent = getWebUtilsService().getRefLocalSerializer().serialize(parentRef);
      }
      List<TreeNode> currentMenuItems = getCurrentMenuItems(numMoreLevels, parent);
      if (currentMenuItems.size() > 0) {
        outStream.append("<ul " + addUniqueContainerId(parent) + " " + getMainUlCSSClasses() + ">");
        boolean isFirstItem = true;
        int numItem = 0;
        if (this.getOffset() > 0) {
          numItem = this.getOffset();
        }
        for (TreeNode treeNode : currentMenuItems) {
          numItem = numItem + 1;
          DocumentReference nodeRef = treeNode.getDocumentReference();
          boolean isLastItem = (currentMenuItems.lastIndexOf(treeNode) == (currentMenuItems.size()
              - 1));
          writeMenuItemWithSubmenu(outStream, parent, numMoreLevels, nodeRef, isFirstItem,
              isLastItem, numItem);
          isFirstItem = false;
        }
        outStream.append("</ul>");
      } else if ((getCurrentLevel(numMoreLevels) == 1) && hasedit()) {
        LOGGER.trace("addNavigationForParent: empty navigation hint for parent [" + parentRef
            + "] numMoreLevels [" + numMoreLevels + "], currentLevel [" + getCurrentLevel(
                numMoreLevels) + "].");
        // is main Menu and no mainMenuItem found ; user has edit rights
        outStream.append("<ul class=\"cel_nav_empty\">");
        openMenuItemOut(outStream, null, true, true, false, 1);
        outStream.append("<span " + addUniqueElementId(null) + " " + addCssClasses(null, true, true,
            true, false, 1) + ">" + getWebUtilsService().getAdminMessageTool().get(
                getEmptyDictKey()) + "</span>");
        closeMenuItemOut(outStream);
        outStream.append("</ul>");
      } else {
        LOGGER.debug("addNavigationForParent: empty output for parent [" + parentRef
            + "] numMoreLevels [" + numMoreLevels + "], currentLevel [" + getCurrentLevel(
                numMoreLevels) + "], hasEdit [" + hasedit() + "].");
      }
    }
  }

  List<TreeNode> getCurrentMenuItems(int numMoreLevels, String parent) {
    getNavFilter().setMenuPart(getMenuPartForLevel(getCurrentLevel(numMoreLevels)));
    List<TreeNode> currentMenuItems = getTreeNodeService().getSubNodesForParent(parent,
        getMenuSpace(getContext()), getNavFilter());
    int endIdx = currentMenuItems.size();
    if (((offset > 0) || (nrOfItemsPerPage > 0)) && (offset < endIdx)) {
      if (nrOfItemsPerPage > 0) {
        endIdx = Math.min(endIdx, offset + nrOfItemsPerPage);
      }
      currentMenuItems = currentMenuItems.subList(offset, endIdx);
    } else if (offset >= endIdx) {
      currentMenuItems = Collections.emptyList();
    }
    return currentMenuItems;
  }

  @Override
  public String getEmptyDictKey() {
    return getPresentationType().getEmptyDictionaryKey() + getEmptyDictKeySuffix();
  }

  @Override
  public void setEmptyDictKeySuffix(String emptyDictKeySuffix) {
    this.emptyDictKeySuffix = emptyDictKeySuffix;
  }

  private String getEmptyDictKeySuffix() {
    if (emptyDictKeySuffix != null) {
      return emptyDictKeySuffix;
    }
    return "";
  }

  private int getCurrentLevel(int numMoreLevels) {
    return (getNumLevels() - numMoreLevels) + 1;
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

  @Override
  public void addUlCSSClass(String cssClass) {
    if (!(" " + mainUlCssClasses + " ").contains(" " + cssClass + " ")) {
      mainUlCssClasses = mainUlCssClasses.trim() + " " + cssClass;
    }
  }

  private boolean hasedit() throws XWikiException {
    return getContext().getWiki().getRightService().hasAccessLevel("edit", getContext().getUser(),
        getContext().getDoc().getFullName(), getContext());
  }

  private void writeMenuItemWithSubmenu(StringBuilder outStream, String parent, int numMoreLevels,
      DocumentReference docRef, boolean isFirstItem, boolean isLastItem, int numItem)
      throws XWikiException {
    boolean showSubmenu = showSubmenuForMenuItem(docRef, getCurrentLevel(numMoreLevels),
        getContext());
    String fullName = getWebUtilsService().getRefLocalSerializer().serialize(docRef);
    boolean isLeaf = isLeaf(fullName, getContext());
    openMenuItemOut(outStream, docRef, isFirstItem, isLastItem, isLeaf, numItem);
    writeMenuItemContent(outStream, isFirstItem, isLastItem, docRef, isLeaf, numItem);
    if (showSubmenu) {
      addNavigationForParent(outStream, docRef, numMoreLevels - 1);
    }
    closeMenuItemOut(outStream);
  }

  void writeMenuItemContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem) throws XWikiException {
    getPresentationType().writeNodeContent(outStream, isFirstItem, isLastItem, docRef, isLeaf,
        numItem, this);
  }

  private boolean isLeaf(String fullName, XWikiContext context) {
    List<TreeNode> currentMenuItems = getTreeNodeService().getSubNodesForParent(fullName,
        getMenuSpace(context), getNavFilter());
    boolean isLeaf = (currentMenuItems.size() <= 0);
    return isLeaf;
  }

  @Override
  public String getNavLanguage() {
    if (this.navLanguage != null) {
      return this.navLanguage;
    }
    return getContext().getLanguage();
  }

  @Override
  public boolean useImagesForNavigation() {
    return getContext().getWiki().getSpacePreferenceAsInt("use_navigation_images", 0,
        getContext()) > 0;
  }

  private void closeMenuItemOut(StringBuilder outStream) {
    outStream.append("<!-- IE6 --></li>");
  }

  void openMenuItemOut(StringBuilder outStream, DocumentReference docRef, boolean isFirstItem,
      boolean isLastItem, boolean isLeaf, int numItem) {
    outStream.append("<li" + addCssClasses(docRef, false, isFirstItem, isLastItem, isLeaf, numItem)
        + ">");
  }

  @Override
  public String addCssClasses(DocumentReference docRef, boolean withCM, boolean isFirstItem,
      boolean isLastItem, boolean isLeaf, int numItem) {
    String cssClasses = getCssClasses(docRef, withCM, isFirstItem, isLastItem, isLeaf, numItem);
    if (!"".equals(cssClasses.trim())) {
      return " class=\"" + cssClasses + "\"";
    }
    return "";
  }

  @Override
  public String addUniqueElementId(DocumentReference docRef) {
    return "id=\"" + getUniqueId(docRef) + "\"";
  }

  private String addUniqueContainerId(String parent) {
    return "id=\"C" + getUniqueId(parent) + "\"";
  }

  @Override
  public String getUniqueId(DocumentReference docRef) {
    String fullName = null;
    if (docRef != null) {
      fullName = getWebUtilsService().getRefLocalSerializer().serialize(docRef);
    }
    return getUniqueId(fullName);
  }

  @Override
  public String getUniqueId(String fullName) {
    String theMenuSpace = getMenuSpace(getContext());
    if ((fullName != null) && !"".equals(fullName)) {
      return uniqueName + ":" + theMenuSpace + ":" + fullName;
    } else {
      return uniqueName + ":" + theMenuSpace + ":" + menuPart + ":";
    }
  }

  boolean showSubmenuForMenuItem(DocumentReference docRef, int currentLevel, XWikiContext context) {
    return (isShowAll() || isBelowShowAllHierarchy(currentLevel) || isActiveMenuItem(docRef));
  }

  private boolean isBelowShowAllHierarchy(int currentLevel) {
    return (currentLevel < showInactiveToLevel);
  }

  String getCssClasses(DocumentReference docRef, boolean withCM, boolean isFirstItem,
      boolean isLastItem, boolean isLeaf, int numItem) {
    String cssClass = "";
    if (withCM) {
      cssClass += getCMcssClass();
    }
    if (isFirstItem) {
      cssClass += " first";
    }
    if (isLastItem) {
      cssClass += " last";
    }
    if ((numItem & 1) == 0) {
      cssClass += " cel_nav_even";
    } else {
      cssClass += " cel_nav_odd";
    }
    cssClass += " cel_nav_item" + numItem;
    if (isLeaf) {
      cssClass += " cel_nav_isLeaf";
    } else {
      cssClass += " cel_nav_hasChildren";
    }
    if (docRef != null) {
      cssClass += " cel_nav_nodeSpace_" + docRef.getLastSpaceReference().getName();
      cssClass += " cel_nav_nodeName_" + docRef.getName();
      if (docRef.equals(getContext().getDoc().getDocumentReference())) {
        cssClass += " currentPage";
      }
      cssClass += " " + getPageTypeConfigName(docRef);
      String pageLayoutName = getPageLayoutName(docRef);
      if (!"".equals(pageLayoutName)) {
        cssClass += " " + pageLayoutName;
      }
      if (isActiveMenuItem(docRef)) {
        cssClass += " active";
      }
      if (isRestrictedRights(docRef)) {
        cssClass += " cel_nav_restricted_rights";
      }
    }
    return cssClass.trim();
  }

  boolean isRestrictedRights(DocumentReference docRef) {
    try {
      String docFN = getWebUtilsService().getRefLocalSerializer().serialize(docRef);
      XWikiRightService rightService = getContext().getWiki().getRightService();
      boolean isRestricted = !rightService.hasAccessLevel("view", "XWiki.XWikiGuest", docFN,
          getContext());
      LOGGER.debug("isRestrictedRights for [" + docFN + "] returns [" + isRestricted
          + "] using rights service [" + rightService.getClass().getName() + "].");
      return isRestricted;
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check isRestrictedRights for [" + docRef + "].", exp);
    }
    return false;
  }

  String getPageLayoutName(DocumentReference docRef) {
    SpaceReference pageLayoutRef = getPresentationType().getPageLayoutForDoc(docRef);
    if (pageLayoutRef == null) {
      pageLayoutRef = pageLayoutCmd.getPageLayoutForDoc(docRef);
    }
    if (pageLayoutRef != null) {
      return "layout_" + pageLayoutRef.getName();
    }
    return "";
  }

  String getPageTypeConfigName(DocumentReference docRef) {
    PageTypeReference pageTypeRef = getPageTypeResolverService().getPageTypeRefForDocWithDefault(
        docRef);
    String getPageTypeConfigName = pageTypeRef.getConfigName();
    return getPageTypeConfigName;
  }

  boolean isActiveMenuItem(DocumentReference docRef) {
    DocumentReference currentDocRef = getContext().getDoc().getDocumentReference();
    List<DocumentReference> docParentList = getWebUtilsService().getDocumentParentsList(
        currentDocRef, true);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("isActiveMenuItem: for [" + docRef + "] with [" + docParentList.size()
          + "] parents [" + Arrays.deepToString(docParentList.toArray(new DocumentReference[0]))
          + "].");
    }
    return (docRef != null) && (docParentList.contains(docRef) || docRef.equals(currentDocRef));
  }

  @Override
  public String getMenuLink(DocumentReference docRef) {
    String docURL = getContext().getWiki().getURL(docRef, "view", getContext());
    // FIX for bug in xwiki url-factory 2.7.2
    if ("".equals(docURL)) {
      docURL = "/";
    }
    return docURL;
  }

  /**
   * @deprecated since 1.142 instead use createNavigation()
   */
  @Deprecated
  public static INavigation createNavigation(XWikiContext context) {
    return new Navigation(Navigation.newNavIdForContext());
  }

  public static INavigation createNavigation() {
    return new Navigation(Navigation.newNavIdForContext());
  }

  static String newNavIdForContext() {
    ExecutionContext executionContext = Utils.getComponent(Execution.class).getContext();
    Long navCounter = getNavCounterFromContext(executionContext) + 1;
    executionContext.setProperty(NAVIGATION_COUNTER_KEY, navCounter);
    return "N" + navCounter;
  }

  private static Long getNavCounterFromContext(ExecutionContext executionContext) {
    if (executionContext.getProperty(NAVIGATION_COUNTER_KEY) == null) {
      return new Long(0);
    }
    java.lang.Object navCounterObj = executionContext.getProperty(NAVIGATION_COUNTER_KEY);
    if (navCounterObj instanceof Long) {
      return (Long) navCounterObj + 1;
    } else {
      throw new IllegalArgumentException("Long object in context expected but got "
          + navCounterObj.getClass());
    }
  }

  @Override
  public int getMenuItemPos(String fullName, XWikiContext context) {
    return WebUtils.getInstance().getMenuItemPos(fullName, menuPart, context);
  }

  @Override
  public int getActiveMenuItemPos(int menuLevel, XWikiContext context) {
    return WebUtils.getInstance().getActiveMenuItemPos(menuLevel, menuPart, context);
  }

  @Override
  public List<com.xpn.xwiki.api.Object> getMenuItemsForHierarchyLevel(int menuLevel,
      XWikiContext context) {
    return WebUtils.getInstance().getMenuItemsForHierarchyLevel(menuLevel, menuPart, context);
  }

  @Override
  public String getPrevMenuItemFullName(String fullName, XWikiContext context) {
    BaseObject prevMenuItem = null;
    try {
      prevMenuItem = WebUtils.getInstance().getPrevMenuItem(fullName, context);
    } catch (XWikiException exp) {
      LOGGER.error("getPrevMenuItemFullName failed.", exp);
    }
    if (prevMenuItem != null) {
      return prevMenuItem.getName();
    } else {
      return "";
    }
  }

  @Override
  public String getNextMenuItemFullName(String fullName, XWikiContext context) {
    BaseObject nextMenuItem = null;
    try {
      nextMenuItem = WebUtils.getInstance().getNextMenuItem(fullName, context);
    } catch (XWikiException exp) {
      LOGGER.error("getNextMenuItemFullName failed.", exp);
    }
    if (nextMenuItem != null) {
      return nextMenuItem.getName();
    } else {
      return "";
    }
  }

  @Override
  public boolean isNavigationEnabled() {
    return navigationEnabled;
  }

  /**
   * Look for a Celements2.NavigationConfigClass object on the WebPreferences,
   * XWiki.XWikiPreferences or skin_doc in this order an take the first place where any
   * Celements2.NavigationConfigClass object was found. If NO object for the given
   * menu_element_name (configName) at the selected place is found. This navigation should
   * be set to disabled and includeNavigation must return an empty string.
   */
  @Override
  public void loadConfigByName(String configName, XWikiContext context) {
    XWikiDocument doc = context.getDoc();
    try {
      BaseObject prefObj = utils.getConfigDocByInheritance(doc, NAVIGATION_CONFIG_CLASS,
          context).getObject(NAVIGATION_CONFIG_CLASS, "menu_element_name", configName, false);
      loadConfigFromObject(prefObj);
    } catch (XWikiException exp) {
      LOGGER.error("loadConfigByName failed.", exp);
    }
  }

  /**
   * @deprecated since 1.142 instead use XObjectNavigationFactory
   *             createNavigation(DocumentReference)
   */
  @Override
  @Deprecated
  public void loadConfigFromObject(BaseObject prefObj) {
    if (prefObj != null) {
      configName = prefObj.getStringValue("menu_element_name");
      LOGGER.debug("loadConfigFromObject: configName [" + configName + "] from doc ["
          + prefObj.getName() + "].");
      fromHierarchyLevel = prefObj.getIntValue("from_hierarchy_level", 1);
      toHierarchyLevel = prefObj.getIntValue("to_hierarchy_level", DEFAULT_MAX_LEVEL);
      showInactiveToLevel = prefObj.getIntValue("show_inactive_to_level", 0);
      menuPart = prefObj.getStringValue("menu_part");
      setMenuSpace(prefObj.getStringValue("menu_space"));
      if (!"".equals(prefObj.getStringValue("data_type")) && (prefObj.getStringValue(
          "data_type") != null)) {
        dataType = prefObj.getStringValue("data_type");
      }
      if (!"".equals(prefObj.getStringValue("layout_type")) && (prefObj.getStringValue(
          "layout_type") != null)) {
        try {
          setLayoutType(prefObj.getStringValue("layout_type"));
        } catch (UnknownLayoutTypeException exp) {
          LOGGER.error("loadConfigFromObject failed on setLayoutType.", exp);
        }
      }
      int itemsPerPage = prefObj.getIntValue(INavigationClassConfig.ITEMS_PER_PAGE);
      if (itemsPerPage > 0) {
        nrOfItemsPerPage = itemsPerPage;
      }
      String presentationTypeStr = prefObj.getStringValue(
          NavigationClasses.PRESENTATION_TYPE_FIELD);
      if (!"".equals(presentationTypeStr)) {
        setPresentationType(presentationTypeStr);
      }
      setCMcssClass(prefObj.getStringValue("cm_css_class"));
      // setMenuTypeByTypeName(prefObj.getStringValue("menu_type"));
    } else {
      navigationEnabled = false;
    }
  }

  @Override
  public void loadConfig(@NotNull NavigationConfig config) {
    navigationEnabled = config.isEnabled();
    if (navigationEnabled) {
      configName = config.getConfigName();
      fromHierarchyLevel = config.getFromHierarchyLevel();
      toHierarchyLevel = config.getToHierarchyLevel();
      showInactiveToLevel = config.getShowInactiveToLevel();
      menuPart = config.getMenuPart();
      nodeSpaceRef = config.getNodeSpaceRef().orNull();
      LOGGER.trace("loadConfig: nodeSpaceRef '{}'", nodeSpaceRef);
      dataType = config.getDataType();
      try {
        setLayoutType(config.getLayoutType());
      } catch (UnknownLayoutTypeException exp) {
        LOGGER.error("loadConfig failed on setLayoutType.", exp);
      }
      nrOfItemsPerPage = config.getNrOfItemsPerPage();
      presentationType = config.getPresentationType().orNull();
      cmCssClass = config.getCssClass();
    } else {
      LOGGER.info("loadConfig: nafigation '{}' disabled!", config.getConfigName());
    }
  }

  private void generateLanguageMenu(INavigationBuilder navBuilder, XWikiContext context) {
    List<String> langs = getWebUtilsService().getAllowedLanguages();
    mainUlCssClasses += " language";
    navBuilder.openLevel(mainUlCssClasses);
    for (String language : langs) {
      navBuilder.openMenuItemOut();
      boolean isLastItem = (langs.lastIndexOf(language) == (langs.size() - 1));
      navBuilder.appendMenuItemLink(language, "?language=" + language, getLanguageName(language,
          context), language.equals(getNavLanguage()), isLastItem, cmCssClass);
      navBuilder.closeMenuItemOut();
    }
    navBuilder.closeLevel();
  }

  private String getLanguageName(String lang, XWikiContext context) {
    XWikiMessageTool msg = context.getMessageTool();
    String space = context.getDoc().getDocumentReference().getLastSpaceReference().getName();
    if (!msg.get("nav_cel_" + space + "_" + lang + "_" + lang).equals("nav_cel_" + space + "_"
        + lang + "_" + lang)) {
      return msg.get("nav_cel_" + space + "_" + lang + "_" + lang);
    } else if (!msg.get("nav_cel_" + lang + "_" + lang).equals("nav_cel_" + lang + "_" + lang)) {
      return msg.get("nav_cel_" + lang + "_" + lang);
    } else {
      return msg.get("cel_" + lang + "_" + lang);
    }
  }

  @Override
  public void setCMcssClass(String cmCssClass) {
    this.cmCssClass = cmCssClass;
  }

  @Override
  public String getCMcssClass() {
    if ((cmCssClass == null) || "".equals(cmCssClass)) {
      return getPresentationType().getDefaultCssClass();
    } else {
      return cmCssClass;
    }
  }

  /**
   * for Tests only !!!
   **/
  public void testInjectUtils(IWebUtils utils) {
    this.utils = utils;
  }

  /**
   * for Tests only !!!
   **/
  public INavigationBuilder getNavBuilder() {
    return navBuilder;
  }

  @Override
  public void setShowAll(boolean showAll) {
    this._showAll = showAll;
  }

  boolean isShowAll() {
    return this._showAll;
  }

  @Override
  public void setShowInactiveToLevel(int showInactiveToLevel) {
    this.showInactiveToLevel = showInactiveToLevel;
  }

  @Override
  public void setHasLink(boolean hasLink) {
    this._hasLink = hasLink;
  }

  @Override
  public boolean hasLink() {
    return this._hasLink;
  }

  @Override
  public MultilingualMenuNameCommand getMenuNameCmd() {
    return menuNameCmd;
  }

  @Override
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
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
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

  IPageTypeResolverRole getPageTypeResolverService() {
    if (injected_PageTypeResolverService != null) {
      return injected_PageTypeResolverService;
    }
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  @Override
  public void setOffset(int offset) {
    if (offset >= 0) {
      this.offset = offset;
    } else {
      this.offset = 0;
    }
  }

  @Override
  public int getOffset() {
    return this.offset;
  }

  @Override
  public void setNumberOfItem(int nrOfItem) {
    if (nrOfItem > 0) {
      this.nrOfItemsPerPage = nrOfItem;
    } else {
      this.nrOfItemsPerPage = -1;
    }
  }

  @Override
  public int getNumberOfItem() {
    return this.nrOfItemsPerPage;
  }

  @Deprecated
  @Override
  public int getEffectiveNumberOfItems() {
    Tidy tidy = new Tidy();
    tidy.setXHTML(true);
    ByteArrayInputStream includeIn = new ByteArrayInputStream(includeNavigation().getBytes());
    Document dom = tidy.parseDOM(includeIn, null);
    NodeList uls = dom.getElementsByTagName("ul");
    int elems = 0;
    if (uls.getLength() > 0) {
      elems = uls.item(0).getChildNodes().getLength();
    }
    return elems;
  }

  void inject_navInclude(String navInclude) {
    this.navInclude = navInclude;
  }

  @Override
  public boolean hasMore() {
    DocumentReference parentRef = getWebUtilsService().getParentForLevel(fromHierarchyLevel);
    String parent = "";
    if (parentRef != null) {
      parent = getWebUtilsService().getRefLocalSerializer().serialize(parentRef);
    }
    List<TreeNode> currentMenuItems = getCurrentMenuItems(fromHierarchyLevel, parent);
    LOGGER.debug("hasMore: parentRef [" + parentRef + "] currentMenuItems.size() ["
        + currentMenuItems.size() + "] offset [" + offset + "]" + " nrOfItemsPerPage ["
        + nrOfItemsPerPage + "] fromHierarchyLevel [" + fromHierarchyLevel + "] parent [" + parent
        + "]");
    return currentMenuItems.size() > 0;
  }

}
