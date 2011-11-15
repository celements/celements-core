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
package com.celements.navigation.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.TreeNode;
import com.celements.web.plugin.CelementsWebPlugin;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class GetMappedMenuItemsForParentCommand {
  
  public static final String CELEMENTS_MAPPED_MENU_ITEMS_KEY =
    "com.celements.web.utils.GetMappedMenuItemsForParendCmd";
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      CelementsWebPlugin.class);
  
  private boolean _isActive;
  
  public void set_isActive(boolean _isActive) {
    this._isActive = _isActive;
  }

  public boolean is_isActive() {
    return _isActive;
  }

  public List<TreeNode> getTreeNodesForParentKey(String parentKey, XWikiContext context) {
    if (is_isActive() && !"".equals(parentKey)) {
      if(parentKey.trim().endsWith(".")){
        parentKey = " ";
      }
      if(parentKey.indexOf(':') < 0) {
        parentKey = context.getDatabase() + ":" + parentKey;
      }
      List<Object> parameterList = new Vector<Object>();
      parameterList.add(parentKey.split(":")[1]);
      String saveDatabase = context.getDatabase();
      context.setDatabase(parentKey.split(":")[0]);
      List<TreeNode> menu = new ArrayList<TreeNode>();
      try {
        List<Object[]> results = context.getWiki().getStore().search(getHQL(), 0, 0,
            context);
        mLogger.info("getMenuItems: found " + results.size() + " menus with parentKey " + 
            parentKey);
        for (Object[] nodeData : results) {
          TreeNode treeNode = new TreeNode(new DocumentReference(context.getDatabase(),
              nodeData[1].toString(), nodeData[0].toString().split("\\.")[1]),
              nodeData[2].toString(), (Integer) nodeData[3]);
          treeNode.setPartName(nodeData[4].toString());
          menu.add(treeNode);
        }
      } catch (XWikiException e) {
        mLogger.error("getMenuItems ", e);
      }
      context.setDatabase(saveDatabase);
      return menu;
    } else {
      if (is_isActive()) {
        mLogger.warn("getMenuItems: parentKey is emtpy");
      }
      return Collections.emptyList();
    }
  }

  String getHQL() {
    String hql = "select doc.fullName, doc.space, doc.parent,";
    hql += " menuitem.menu_position, menuitem.part_name";
    hql += " from XWikiDocument doc, BaseObject as obj,";
    hql += " Classes.MenuItemClass as menuitem";
    hql += " where doc.parent=?";
    hql += " and doc.translation='0'";
    hql += " and obj.name=doc.fullName";
    hql += " and obj.id=menuitem.id";
    hql += " order by menuitem.menu_position";
    return hql;
  }


}
