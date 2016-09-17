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

import org.python.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.TreeNode;
import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CellRenderStrategy implements IRenderStrategy {

  private static Logger LOGGER = LoggerFactory.getLogger(CellRenderStrategy.class);

  private ICellWriter cellWriter;
  private XWikiContext context;

  private SpaceReference spaceReference;

  RenderCommand rendererCmd;
  PageLayoutCommand pageLayoutCmd = new PageLayoutCommand();
  private ICellsClassConfig cellClassConfig = Utils.getComponent(ICellsClassConfig.class);
  private IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
  private ModelUtils modelUtils = Utils.getComponent(ModelUtils.class);

  public CellRenderStrategy(XWikiContext context) {
    this.context = context;
  }

  @Override
  public void endRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    cellWriter.closeLevel();
  }

  @Override
  public void endRenderChildren(EntityReference parentRef) {
  }

  @Override
  public void endRendering() {
  }

  @Override
  public String getMenuPart(TreeNode node) {
    return "";
  }

  @Override
  public SpaceReference getSpaceReference() {
    if (spaceReference == null) {
      return pageLayoutCmd.getDefaultLayoutSpaceReference();
    } else {
      return spaceReference;
    }
  }

  @Override
  public boolean isRenderCell(TreeNode node) {
    return node != null;
  }

  @Override
  public boolean isRenderSubCells(EntityReference parentRef) {
    return parentRef != null;
  }

  @Override
  public void startRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    String tagName = "";
    String cssClasses = "cel_cell";
    String cssStyles = "";
    String idname = "";
    try {
      DocumentReference cellDocRef = node.getDocumentReference();
      LOGGER.debug("startRenderCell: cellDocRef [{}] context db [{}].", cellDocRef,
          context.getDatabase());
      BaseObject cellObj = modelAccess.getXObject(cellDocRef, cellClassConfig.getCellClassRef(
          cellDocRef.getWikiReference().getName()));
      if (cellObj != null) {
        String cellObjCssClasses = cellObj.getStringValue("css_classes");
        if (!Strings.isNullOrEmpty(cellObjCssClasses)) {
          cssClasses += " " + cellObjCssClasses;
        }
        tagName = cellObj.getStringValue(ICellsClassConfig.CELLCLASS_TAGNAME_FIELD);
        cssStyles = cellObj.getStringValue("css_styles");
        idname = cellObj.getStringValue(ICellsClassConfig.CELLCLASS_IDNAME_FIELD);
      }
      if (Strings.isNullOrEmpty(idname)) {
        String nodeFN = modelUtils.serializeRef(node.getDocumentReference());
        nodeFN = nodeFN.replaceAll(context.getDatabase() + ":", "");
        idname = "cell:" + nodeFN.replaceAll(":", "..");
      }
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("failed to get cell [{}] document.", node.getDocumentReference(), exp);
    }
    cellWriter.openLevel(tagName, idname, cssClasses, cssStyles);
  }

  @Override
  public void startRenderChildren(EntityReference parentRef) {
  }

  @Override
  public void startRendering() {
    cellWriter.clear();
  }

  public CellRenderStrategy setOutputWriter(ICellWriter newWriter) {
    this.cellWriter = newWriter;
    return this;
  }

  @Override
  public String getAsString() {
    return cellWriter.getAsString();
  }

  @Override
  public void renderEmptyChildren(TreeNode node) {
    String cellContent = "";
    try {
      LOGGER.debug("renderEmptyChildren: parent [{}].", node);
      long millisec = System.currentTimeMillis();
      cellContent = getRendererCmd().renderCelementsCell(node.getDocumentReference());
      LOGGER.info("renderEmptyChildren: rendered parent [{}]. Time used in millisec: {}", node,
          (System.currentTimeMillis() - millisec));
    } catch (XWikiException exp) {
      LOGGER.error("failed to get cell [{}] document to render cell content.", node, exp);
    }
    cellWriter.appendContent(cellContent);
  }

  RenderCommand getRendererCmd() {
    if (rendererCmd == null) {
      rendererCmd = new RenderCommand();
    }
    return rendererCmd;
  }

  @Override
  public void setSpaceReference(SpaceReference spaceReference) {
    this.spaceReference = spaceReference;
  }
}
