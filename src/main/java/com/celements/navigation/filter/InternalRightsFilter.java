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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

public class InternalRightsFilter implements INavFilter<BaseObject> {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      InternalRightsFilter.class);
  private String menuPart;

  public InternalRightsFilter() {
    menuPart = "";
  }

  public String getMenuPart() {
    return menuPart;
  }

  public boolean includeMenuItem(BaseObject baseObj, XWikiContext context) {
    try {
      return context.getWiki().getRightService().hasAccessLevel("view", context.getUser(),
          baseObj.getName(), context) && ("".equals(getMenuPart())
              || getMenuPart().equals(baseObj.getStringValue("part_name")));
    } catch (XWikiException e) {
      mLogger.error(e);
      return false;
    }
  }

  public void setMenuPart(String menuPart) {
    this.menuPart = menuPart;
  }

  public BaseObject convertObject(BaseObject baseObj, XWikiContext context) {
    return baseObj;
  }

  /**
   * includeTreeNode
   * @param node MUST NOT be null
   * @param context
   */
  public boolean includeTreeNode(TreeNode node, XWikiContext context) {
    mLogger.debug("includeTreeNode: for [" + node.getFullName() + "]");
    try {
      return context.getWiki().getRightService().hasAccessLevel("view", context.getUser(),
          node.getFullName(), context) && checkMenuPart(node, context);
    } catch (XWikiException exp) {
      mLogger.error(exp);
      return false;
    }
  }

  private boolean checkMenuPart(TreeNode node, XWikiContext context) {
    return ("".equals(getMenuPart()) || ("".equals(node.getParent())
        && getMenuPart().equals(node.getPartName(context))));
  }

}
