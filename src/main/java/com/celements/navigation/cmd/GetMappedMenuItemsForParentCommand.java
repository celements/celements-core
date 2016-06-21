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

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.navigation.TreeNode;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.MoreObjects;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class GetMappedMenuItemsForParentCommand {

  public final static String CELEMENTS_MAPPED_MENU_ITEMS_KEY = "com.celements.web.utils.GetMappedMenuItemsForParendCmd";

  private final static Logger LOGGER = LoggerFactory.getLogger(
      GetMappedMenuItemsForParentCommand.class);

  private boolean isActive;

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  public boolean isActive() {
    return isActive;
  }

  /**
   * @deprecated since 1.140 instead use getTreeNodesForParentKey(String)
   */
  @Deprecated
  public List<TreeNode> getTreeNodesForParentKey(String parentKey, XWikiContext context) {
    return getTreeNodesForParentKey(parentKey);
  }

  public List<TreeNode> getTreeNodesForParentKey(String parentKey) {
    if (isActive() && !"".equals(parentKey)) {
      if (parentKey.trim().endsWith(".")) {
        parentKey = " ";
      }
      if (parentKey.indexOf(':') < 0) {
        parentKey = getContext().getDatabase() + ":" + parentKey;
      }
      List<Object> parameterList = new Vector<Object>();
      parameterList.add(parentKey.split(":")[1]);
      String saveDatabase = getContext().getDatabase();
      getContext().setDatabase(parentKey.split(":")[0]);
      List<TreeNode> menu = new ArrayList<TreeNode>();
      try {
        List<Object[]> results = getContext().getWiki().getStore().search(getHQL(), 0, 0,
            getContext());
        LOGGER.info("getMenuItems: found " + results.size() + " menus with parentKey " + parentKey);
        for (Object[] nodeData : results) {
          DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
              nodeData[1].toString(), nodeData[0].toString().split("\\.")[1]);
          String partName = MoreObjects.firstNonNull(nodeData[4], "").toString();
          String parentFN = MoreObjects.firstNonNull(nodeData[2], "").toString();
          TreeNode treeNode = new TreeNode(docRef, resolveParentRef(parentFN),
              (Integer) nodeData[3], partName);
          menu.add(treeNode);
        }
      } catch (XWikiException e) {
        LOGGER.error("getMenuItems ", e);
      }
      getContext().setDatabase(saveDatabase);
      return menu;
    } else {
      if (isActive()) {
        LOGGER.warn("getMenuItems: parentKey is emtpy");
      }
      return Collections.emptyList();
    }
  }

  private EntityReference resolveParentRef(@NotNull String parentFN) {
    return (parentFN.isEmpty()) ? null
        : getWebUtils().resolveEntityReference(parentFN, getEntityType(parentFN));
  }

  private EntityType getEntityType(String parentFN) {
    return (parentFN.contains(".")) ? EntityType.DOCUMENT : EntityType.SPACE;
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
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
