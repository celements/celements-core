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

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.web.Utils;

public class RenderingEngine implements IRenderingEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderingEngine.class);

  private IRenderStrategy renderStrategy;

  ITreeNodeService treeNodeService;
  IWebUtilsService webUtilsService;

  public RenderingEngine() {}

  /*
   * (non-Javadoc)
   *
   * @see com.celements.web.cells.IRenderingEngine#renderCell(
   * com.xpn.xwiki.objects.BaseObject)
   */
  @Override
  public void renderCell(TreeNode node) {
    renderStrategy.startRendering();
    // isFirst AND isLast because only this item and its children is
    // rendered (NO SIBLINGS!)
    renderCell(node, true, true);
    renderStrategy.endRendering();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.web.cells.IRenderingEngine#renderSubCells(java.lang.String)
   */
  @Override
  @Deprecated
  public void renderPageLayout(String spaceName) {
    SpaceReference spaceRef = getWebUtilsService().resolveSpaceReference(spaceName);
    renderPageLayout(spaceRef);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.web.cells.IRenderingEngine#renderSubCells(
   * org.xwiki.model.reference.SpaceReference)
   */
  @Override
  public void renderPageLayout(SpaceReference spaceRef) {
    checkNotNull(spaceRef);
    LOGGER.debug("renderPageLayout: start rendering [{}].", spaceRef);
    renderStrategy.startRendering();
    renderSubCells(null, spaceRef);
    renderStrategy.endRendering();
  }

  void renderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    if (renderStrategy.isRenderCell(node)) {
      renderStrategy.getContextualiser(node).execute(() -> {
        renderStrategy.startRenderCell(node, isFirstItem, isLastItem);
        renderSubCells(node, Optional.ofNullable(node)
            .map(TreeNode::getDocumentReference)
            .orElse(null));
        renderStrategy.endRenderCell(node, isFirstItem, isLastItem);
      });
    }
  }

  void renderSubCells(TreeNode parentNode, EntityReference parentRef) {
    if (renderStrategy.isRenderSubCells(parentRef)) {
      List<TreeNode> children = getTreeNodeService().getSubNodesForParent(parentRef,
          renderStrategy.getMenuPart(parentNode));
      LOGGER.debug("internal_renderSubCells: for parent [{}] render [{}] children [{}].",
          parentRef, children.size(), children);
      if (!children.isEmpty()) {
        renderStrategy.startRenderChildren(parentRef);
        boolean isFirstItem = true;
        for (TreeNode node : children) {
          boolean isLastItem = (children.lastIndexOf(node) == (children.size() - 1));
          renderCell(node, isFirstItem, isLastItem);
          isFirstItem = false;
        }
        renderStrategy.endRenderChildren(parentRef);
      } else if (renderStrategy.isRenderCell(parentNode)) {
        renderStrategy.renderEmptyChildren(parentNode);
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

  IWebUtilsService getWebUtilsService() {
    if (webUtilsService != null) {
      return webUtilsService;
    }
    return Utils.getComponent(IWebUtilsService.class);
  }

}
