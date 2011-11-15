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
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;

public class RenderingEngine implements IRenderingEngine {

  private IWebUtils utils;
  private IRenderStrategy renderStrategy;

  public RenderingEngine() {
  }

  /* (non-Javadoc)
   * @see com.celements.web.cells.IRenderingEngine#renderCell(
   *   com.xpn.xwiki.objects.BaseObject, com.xpn.xwiki.XWikiContext)
   */
  public void renderCell(TreeNode node, XWikiContext context) {
    renderStrategy.startRendering();
    //isFirst AND isLast because only this item and its children is
    //rendered (NO SIBLINGS!)
    internal_renderCell(node, true, true, context);
    renderStrategy.endRendering();
  }

  /* (non-Javadoc)
   * @see com.celements.web.cells.IRenderingEngine#renderSubCells(
   *   java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public void renderPageLayout(String spaceName, XWikiContext context) {
    renderStrategy.startRendering();
    renderStrategy.setSpaceName(spaceName);
    internal_renderSubCells("", context);
    renderStrategy.endRendering();
  }

  void internal_renderCell(TreeNode node, boolean isFirstItem, boolean isLastItem,
      XWikiContext context) {
    if(renderStrategy.isRenderCell(node)) {
      renderStrategy.startRenderCell(node, isFirstItem, isLastItem);
      internal_renderSubCells(node.getFullName(), context);
      renderStrategy.endRenderCell(node, isFirstItem, isLastItem);
    }
  }

  void internal_renderSubCells(String parent, XWikiContext context) {
    if(renderStrategy.isRenderSubCells(parent)) {
      List<TreeNode> children = getWebUtils().getSubNodesForParent(parent,
          renderStrategy.getMenuSpace(parent), renderStrategy.getMenuPart(parent), context);
      if (children.size() > 0) {
        renderStrategy.startRenderChildren(parent);
        boolean isFirstItem = true;
        for (TreeNode node : children) {
          boolean isLastItem = (children.lastIndexOf(node)
              == (children.size() - 1));
            internal_renderCell(node, isFirstItem, isLastItem, context);
          isFirstItem = false;
        }
        renderStrategy.endRenderChildren(parent);
      } else {
        renderStrategy.renderEmptyChildren(parent);
      }
    }
  }

  IWebUtils getWebUtils() {
    if(utils == null) {
      utils = WebUtils.getInstance();
    }
    return utils;
  }

  public RenderingEngine setRenderStrategy(IRenderStrategy newStrategy) {
    this.renderStrategy = newStrategy;
    return this;
  }

  /**
   * FOR TEST USE ONLY!!!
   * @param webUtils
   */
  void inject_WebUtils(IWebUtils webUtils) {
    this.utils= webUtils;
  }

}
