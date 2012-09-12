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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.navigation.TreeNode;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CellRenderer implements IRenderStrategy {
  
  public static final String CELEMENTS_CELL_CLASS_SPACE = "Celements";
  public static final String CELEMENTS_CELL_CLASS_NAME = "CellClass";
  public static final String CELEMENTS_CELL_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
    + CELEMENTS_CELL_CLASS_NAME;

  private static Log LOGGER = LogFactory.getFactory().getInstance(CellRenderer.class);

  private ICellWriter cellWriter;
  private XWikiContext context;

  private SpaceReference spaceReference;

  private RenderCommand ctRendererCmd;

  public CellRenderer(XWikiContext context) {
    this.context = context;
  }

  public void endRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    cellWriter.closeLevel();
  }

  public void endRenderChildren(String parent) {}

  public void endRendering() {}

  public String getMenuPart(String parent) {
    return "";
  }

  @Deprecated
  public String getMenuSpace(String fullName) {
      return getSpaceReference().getName();
  }

  public SpaceReference getSpaceReference() {
    if (spaceReference == null) {
      return new SpaceReference("Skin", new WikiReference(context.getDatabase()));
    } else {
      return spaceReference;
    }
  }

  public boolean isRenderCell(TreeNode node) {
    return node != null;
  }

  public boolean isRenderSubCells(String parent) {
    return parent != null;
  }

  public void startRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    String cssClasses = "";
    String cssStyles = "";
    String idname = "";
    try {
      DocumentReference cellDocRef = node.getDocumentReference();
      XWikiDocument cellDoc = context.getWiki().getDocument(cellDocRef, context);
      BaseObject cellObj = cellDoc.getXObject(new DocumentReference(
          cellDocRef.getWikiReference().getName(),
          CELEMENTS_CELL_CLASS_SPACE, CELEMENTS_CELL_CLASS_NAME));
      if(cellObj != null) {
        cssClasses = cellObj.getStringValue("css_classes");
        cssStyles = cellObj.getStringValue("css_styles");
        idname  = cellObj.getStringValue("idname");
      }
    } catch (XWikiException e) {
      LOGGER.error("failed to get cell [" + node.getDocumentReference()
          + "] document.", e);
    }
    cellWriter.openLevel(idname, cssClasses, cssStyles);
  }

  public void startRenderChildren(String parent) {}

  public void startRendering() {
    cellWriter.clear();
  }

  public CellRenderer setOutputWriter(ICellWriter newWriter) {
    this.cellWriter = newWriter;
    return this;
  }

  public String getAsString() {
    return cellWriter.getAsString();
  }

  public void renderEmptyChildren(String parent) {
    String cellContent = "";
    try {
      cellContent = ctRendererCmd().renderCelementsCell(parent);
    } catch (XWikiException exp) {
      LOGGER.error("failed to get cell [" + parent + "] document to render cell"
          + " content.", exp);
    }
    cellWriter.appendContent(cellContent);
  }

  RenderCommand ctRendererCmd() {
    if (ctRendererCmd == null) {
      ctRendererCmd = new RenderCommand();
    }
    return ctRendererCmd;
  }

  void inject_ctRenderCmd(RenderCommand mockPtRenderCmd) {
    ctRendererCmd = mockPtRenderCmd;
  }

  @Deprecated
  public void setSpaceName(String spaceName) {
    if ((spaceName != null) && (!"".equals(spaceName))) {
      setSpaceReference(new SpaceReference(spaceName, new WikiReference(
        context.getDatabase())));
    } else {
      setSpaceReference(null);
    }
  }

  public void setSpaceReference(SpaceReference spaceReference) {
    this.spaceReference = spaceReference;
  }

}
