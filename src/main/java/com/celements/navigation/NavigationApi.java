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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class NavigationApi extends Api {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationApi.class);

  private INavigation navigation;

  private NavigationApi(INavigation navigation, XWikiContext context) {
    super(context);
    this.navigation = navigation;
  }

  public static NavigationApi getAPIObject(INavigation navigation, XWikiContext context) {
    return new NavigationApi(navigation, context);
  }

  public static NavigationApi createNavigation(XWikiContext context) {
    return getAPIObject(Navigation.createNavigation(), context);
  }

  /**
   * getMultilingualMenuName DOES NOT allow empty menu names as return value in contrast
   * to getMultilingualMenuNameOnly
   *
   * @param fullName
   * @param language
   * @return
   */
  public String getMultilingualMenuName(String fullName, String language) {
    return navigation.getMenuNameCmd().getMultilingualMenuName(fullName, language, context);
  }

  public String getMultilingualMenuName(com.xpn.xwiki.api.Object menuItem, String language) {
    return navigation.getMenuNameCmd().getMultilingualMenuName(menuItem, language, context);
  }

  public String getMultilingualMenuName(com.xpn.xwiki.api.Object menuItem, String language,
      boolean allowEmptyMenuNames) {
    return navigation.getMenuNameCmd().getMultilingualMenuName(menuItem.getXWikiObject(), language,
        allowEmptyMenuNames, context);
  }

  /**
   * getMultilingualMenuNameOnly DOES allow empty menu names as return value in contrast
   * to getMultilingualMenuName
   *
   * @param fullName
   * @param language
   * @return
   */
  public String getMultilingualMenuNameOnly(String fullName, String language) {
    return navigation.getMenuNameCmd().getMultilingualMenuNameOnly(fullName, language, true,
        context);
  }

  public String getMultilingualMenuNameOnly(String fullName, String language,
      boolean allowEmptyMenuNames) {
    return navigation.getMenuNameCmd().getMultilingualMenuNameOnly(fullName, language,
        allowEmptyMenuNames, context);
  }

  public String includeNavigation() {
    return navigation.includeNavigation();
  }

  public String includeNavigation(DocumentReference parentRef) {
    return navigation.includeNavigation(parentRef);
  }

  public int getMenuItemPos(String fullName) {
    return navigation.getMenuItemPos(fullName, context);
  }

  public int getActiveMenuItemPos(int menuLevel) {
    return navigation.getActiveMenuItemPos(menuLevel, context);
  }

  /**
   * @deprecated since 6.0 instead use TreeNodeService
   */
  @Deprecated
  public List<com.xpn.xwiki.api.Object> getMenuItemsForHierarchyLevel(int menuLevel) {
    return navigation.getMenuItemsForHierarchyLevel(menuLevel, context);
  }

  public void setFromHierarchyLevel(int fromHierarchyLevel) {
    navigation.setFromHierarchyLevel(fromHierarchyLevel);
  }

  public void setToHierarchyLevel(int toHierarchyLevel) {
    navigation.setToHierarchyLevel(toHierarchyLevel);
  }

  public void setMenuPart(String menuPart) {
    navigation.setMenuPart(menuPart);
  }

  /**
   * @param menuSpace
   *          (default: $doc.web)
   * @deprecated since 2.57.0 instead use setNodeSpace
   */
  @Deprecated
  public void setMenuSpace(String menuSpace) {
    navigation.setMenuSpace(menuSpace);
  }

  public void setNodeSpace(SpaceReference newNodeSpaceRef) {
    navigation.setNodeSpace(newNodeSpaceRef);
  }

  public String getPrevMenuItemFullName(com.xpn.xwiki.api.Object menuItem) {
    if (menuItem != null) {
      return navigation.getPrevMenuItemFullName(menuItem.getName(), context);
    }
    return null;
  }

  public String getNextMenuItemFullName(com.xpn.xwiki.api.Object menuItem) {
    return navigation.getNextMenuItemFullName(menuItem.getName(), context);
  }

  public boolean isNavigationEnabled() {
    return navigation.isNavigationEnabled();
  }

  private DocumentReference getNavigationConfigClassRef(XWikiDocument doc) {
    return getNavigationClasses().getNavigationConfigClassRef(getWebUtilsService().getWikiRef(
        doc.getDocumentReference()).getName());
  }

  public void loadConfigByName(String configName) {
    navigation.loadConfigByName(configName, context);
  }

  /**
   * @deprecated since 2.18.0 instead use loadConfigFromDoc(DocumentReference)
   */
  @Deprecated
  public void loadConfigFromDoc(String fullName) {
    DocumentReference configDocRef = getWebUtilsService().resolveDocumentReference(fullName);
    loadConfig_internal(configDocRef, null);
  }

  /**
   * @deprecated since 2.18.0 instead use loadConfigFromDoc(DocumentReference, int)
   */
  @Deprecated
  public void loadConfigFromDoc(String fullName, int objNum) {
    DocumentReference configDocRef = getWebUtilsService().resolveDocumentReference(fullName);
    loadConfig_internal(configDocRef, objNum);
  }

  public void loadConfigFromDoc(DocumentReference configDocRef) {
    loadConfig_internal(configDocRef, null);
  }

  public void loadConfigFromDoc(DocumentReference configDocRef, int objNum) {
    loadConfig_internal(configDocRef, objNum);
  }

  private void loadConfig_internal(DocumentReference configDocRef, Integer objNum) {
    try {
      XWikiDocument doc = context.getWiki().getDocument(configDocRef, context);
      BaseObject navConfigXobj = getNavigationConfigObject(doc, objNum);
      if (navConfigXobj != null) {
        LOGGER.debug("loadConfig_internal: configName [" + navConfigXobj.getStringValue(
            "menu_element_name") + "] , " + navConfigXobj);
        navigation.loadConfigFromObject(navConfigXobj);
      } else {
        LOGGER.warn("cannot load navigation config from doc [" + configDocRef + "],"
            + " because no config object found.");
      }
    } catch (XWikiException exp) {
      LOGGER.warn("failed to get document [" + configDocRef + "].");
    }
  }

  private BaseObject getNavigationConfigObject(XWikiDocument doc, Integer objNum) {
    BaseObject navConfigXobj;
    if (objNum == null) {
      LOGGER.debug("get xobject for [" + getNavigationConfigClassRef(doc) + "] on doc ["
          + doc.getDocumentReference() + "].");
      navConfigXobj = doc.getXObject(getNavigationConfigClassRef(doc));
    } else {
      LOGGER.debug("get xobject objNum [" + objNum + "] for [" + getNavigationConfigClassRef(doc)
          + "] on doc [" + doc.getDocumentReference() + "].");
      navConfigXobj = doc.getXObject(getNavigationConfigClassRef(doc), objNum);
    }
    return navConfigXobj;
  }

  public void setCMcssClass(String cmCssClass) {
    navigation.setCMcssClass(cmCssClass);
  }

  public String getEmptyDictKey() {
    return navigation.getEmptyDictKey();
  }

  public void setEmptyDictKeySuffix(String emptyDictKeySuffix) {
    navigation.setEmptyDictKeySuffix(emptyDictKeySuffix);
  }

  public SpaceReference getNodeSpaceRef() {
    return navigation.getNodeSpaceRef();
  }

  /**
   * @deprecated since 2.30.0 instead use getNodeSpaceRef()
   */
  @Deprecated
  public String getMenuSpace() {
    return navigation.getMenuSpace(context);
  }

  public String getUniqueId(String menuItemName) {
    return navigation.getUniqueId(menuItemName);
  }

  public void setShowAll(boolean showAll) {
    navigation.setShowAll(showAll);
  }

  public void setHasLink(boolean hasLink) {
    navigation.setHasLink(hasLink);
  }

  public void addUlCSSClass(String cssClass) {
    navigation.addUlCSSClass(cssClass);
  }

  public void setLanguage(String language) {
    navigation.setLanguage(language);
  }

  public void setShowInactiveToLevel(int showInactiveToLevel) {
    navigation.setShowInactiveToLevel(showInactiveToLevel);
  }

  public void setPresentationType(String presentationTypeHint) {
    navigation.setPresentationType(presentationTypeHint);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private INavigationClassConfig getNavigationClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

  public boolean isEmptyMainMenu() {
    return navigation.isEmptyMainMenu();
  }

  public boolean isEmpty() {
    boolean isEmpty = navigation.isEmpty();
    LOGGER.debug("isEmpty api: returning [" + isEmpty + "].");
    return isEmpty;
  }

  public void setOffset(int offset) {
    navigation.setOffset(offset);
  }

  public int getOffset() {
    return navigation.getOffset();
  }

  public void setNumberOfItem(int nrOfItem) {
    navigation.setNumberOfItem(nrOfItem);
  }

  public int getNumberOfItem() {
    return navigation.getNumberOfItem();
  }

  /**
   * use hasMore() instead
   */
  @Deprecated
  public int getEffectiveNumberOfItems() {
    return navigation.getEffectiveNumberOfItems();
  }

  public boolean hasMore() {
    return navigation.hasMore();
  }
}
