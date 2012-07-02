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
package com.celements.cells;

import java.util.List;

import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.web.Utils;

public class RenderingEngine implements IRenderingEngine {

  private IRenderStrategy renderStrategy;

  ITreeNodeService treeNodeService;

  public RenderingEngine() {
  }

  /* (non-Javadoc)
   * @see com.celements.web.cells.IRenderingEngine#renderCell(
   *   com.xpn.xwiki.objects.BaseObject)
   */
  public void renderCell(TreeNode node) {
    renderStrategy.startRendering();
    //isFirst AND isLast because only this item and its children is
    //rendered (NO SIBLINGS!)
    internal_renderCell(node, true, true);
    renderStrategy.endRendering();
  }

  /* (non-Javadoc)
   * @see com.celements.web.cells.IRenderingEngine#renderSubCells(java.lang.String)
   */
  public void renderPageLayout(String spaceName) {
    renderStrategy.startRendering();
    renderStrategy.setSpaceName(spaceName);
    internal_renderSubCells("");
    renderStrategy.endRendering();
  }

  void internal_renderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    if(renderStrategy.isRenderCell(node)) {
      renderStrategy.startRenderCell(node, isFirstItem, isLastItem);
      internal_renderSubCells(node.getFullName());
      renderStrategy.endRenderCell(node, isFirstItem, isLastItem);
    }
  }

  void internal_renderSubCells(String parent) {
    if(renderStrategy.isRenderSubCells(parent)) {
      List<TreeNode> children = getTreeNodeService().getSubNodesForParent(parent,
          renderStrategy.getMenuSpace(parent), renderStrategy.getMenuPart(parent));
      if (children.size() > 0) {
        renderStrategy.startRenderChildren(parent);
        boolean isFirstItem = true;
        for (TreeNode node : children) {
          boolean isLastItem = (children.lastIndexOf(node)
              == (children.size() - 1));
            internal_renderCell(node, isFirstItem, isLastItem);
          isFirstItem = false;
        }
        renderStrategy.endRenderChildren(parent);
      } else {
        renderStrategy.renderEmptyChildren(parent);
      }
    }
  }

  public RenderingEngine setRenderStrategy(IRenderStrategy newStrategy) {
    this.renderStrategy = newStrategy;
    return this;
  }

  ITreeNodeService getTreeNodeService() {
    if (treeNodeService != null) {
      return treeNodeService;
    }
    return Utils.getComponent(ITreeNodeService.class);
  }

}
