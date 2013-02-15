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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.INavFilter;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public interface INavigation {

  /**
   * setFromHierarchyLevel
   * @param fromHierarchyLevel starting (including) at Hierarchy Level
   *          1 = mainMenu , 0 = spaceMenu (including all first mainMenuItems
   *           of all Spaces)
   */
  public void setFromHierarchyLevel(int fromHierarchyLevel);

  public MultilingualMenuNameCommand getMenuNameCmd();

  /**
   * setToHierarchyLevel
   * @param toHierarchyLevel ending (including) with Hierarchy Level
   */
  public void setToHierarchyLevel(int toHierarchyLevel);

  public void setMenuPart(String menuPart);

  /**
   * 
   * @param menuSpace (default: $doc.web)
   * 
   * @deprecated since 2.24.0 instead use setNodeSpace
   */
  @Deprecated
  public void setMenuSpace(String menuSpace);

  public void setNodeSpace(SpaceReference newNodeSpaceRef);

  /**
   * @deprecated since 2.24.0 use includeNavigation() instead.
   */
  @Deprecated
  public String includeNavigation(XWikiContext context);

  public String includeNavigation();

  public String includeNavigation(DocumentReference parentRef);

  public int getMenuItemPos(String fullName, XWikiContext context);

  public int getActiveMenuItemPos(int menuLevel, XWikiContext context);

  public List<com.xpn.xwiki.api.Object> getMenuItemsForHierarchyLevel(int menuLevel,
      XWikiContext context);

  public String getPrevMenuItemFullName(String fullName, XWikiContext context);

  /**
   * @deprecated since 2.24.0 instead use getNodeSpaceRef()
   */
  @Deprecated
  public String getMenuSpace(XWikiContext context);

  public SpaceReference getNodeSpaceRef();

  public boolean isEmptyMainMenu();

  public boolean isNavigationEnabled();

  public void loadConfigByName(String configName, XWikiContext context);

  public void setCMcssClass(String cmCssClass);

  public String getCMcssClass();

  public String getNextMenuItemFullName(String name, XWikiContext context);

  public String getUniqueId(DocumentReference docRef);

  public String getUniqueId(String fullName);

  public void setShowAll(boolean showAll);

  public void setHasLink(boolean hasLink);

  public void addUlCSSClass(String cssClass);

  public void setNavFilter(INavFilter<BaseObject> navFilter);

  public void loadConfigFromObject(BaseObject prefObj);

  public void setLanguage(String language);

  public void setShowInactiveToLevel(int showInactiveToLevel);

  public boolean hasLink();

  public boolean useImagesForNavigation();

  public String getMenuLink(DocumentReference docRef);

  public String getNavLanguage();

  public String addUniqueElementId(DocumentReference docRef);

  public String addCssClasses(DocumentReference docRef, boolean withCM,
      boolean isFirstItem, boolean isLastItem, boolean isLeaf, int numItem);

  public void setPresentationType(String presentationTypeHint);

}
