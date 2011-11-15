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

import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class ExternalUsageFilter implements INavFilter<com.xpn.xwiki.api.Object> {

  private InternalRightsFilter rightsFilter;

  public com.xpn.xwiki.api.Object convertObject(BaseObject baseObj, XWikiContext context) {
    return baseObj.newObjectApi(baseObj, context);
  }

  public String getMenuPart() {
    return getRightsFilter().getMenuPart();
  }

  InternalRightsFilter getRightsFilter() {
    if (rightsFilter == null) {
      rightsFilter = new InternalRightsFilter();
    }
    return rightsFilter;
  }

  public void setRightsFilter(InternalRightsFilter rightsFilter) {
    this.rightsFilter = rightsFilter;
  }

  public boolean includeMenuItem(BaseObject baseObj, XWikiContext context) {
    return getRightsFilter().includeMenuItem(baseObj, context);
  }

  public void setMenuPart(String menuPart) {
    getRightsFilter().setMenuPart(menuPart);
  }

  public boolean includeTreeNode(TreeNode node, XWikiContext context) {
    return getRightsFilter().includeTreeNode(node, context);
  }

}
