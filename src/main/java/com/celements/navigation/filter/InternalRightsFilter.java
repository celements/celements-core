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
package com.celements.navigation.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.navigation.TreeNode;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class InternalRightsFilter implements INavFilter<BaseObject> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalRightsFilter.class);

  private String menuPart = "";

  @Override
  public String getMenuPart() {
    return menuPart;
  }

  /**
   * @Deprecated instead use includeTreeNode(TreeNode, context)
   */
  @Override
  @Deprecated
  public boolean includeMenuItem(BaseObject baseObj, XWikiContext context) {
    return getRightsAccess().hasAccessLevel(baseObj.getDocumentReference(), EAccessLevel.VIEW,
        context.getXWikiUser()) && (getMenuPart().isEmpty() || getMenuPart().equals(
            baseObj.getStringValue("part_name")));
  }

  @Override
  public void setMenuPart(String menuPart) {
    this.menuPart = Strings.nullToEmpty(menuPart);
  }

  @Override
  public BaseObject convertObject(BaseObject baseObj, XWikiContext context) {
    return baseObj;
  }

  /**
   * includeTreeNode
   *
   * @param node
   *          MUST NOT be null
   * @param context
   */
  @Override
  public boolean includeTreeNode(TreeNode node, XWikiContext context) {
    LOGGER.debug("includeTreeNode: for [" + node.getDocumentReference() + "]");
    return getRightsAccess().hasAccessLevel(node.getDocumentReference(), EAccessLevel.VIEW,
        context.getXWikiUser()) && checkMenuPart(node, context);
  }

  private boolean checkMenuPart(TreeNode node, XWikiContext context) {
    return (getMenuPart().isEmpty() || (node.isEmptyParentRef() && getMenuPart().equals(
        node.getPartName())));
  }

  private IRightsAccessFacadeRole getRightsAccess() {
    return Utils.getComponent(IRightsAccessFacadeRole.class);
  }

  @Override
  public String toString() {
    return "InternalRightsFilter [menuPart=" + menuPart + "]";
  }

}
