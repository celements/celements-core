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

import static com.celements.model.util.ReferenceSerializationMode.*;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.TreeNode;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CellRenderStrategy implements IRenderStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellRenderStrategy.class);

  private ICellWriter cellWriter;
  private XWikiContext context;

  private SpaceReference spaceReference;

  RenderCommand rendererCmd;
  PageLayoutCommand pageLayoutCmd = new PageLayoutCommand();
  private final ICellsClassConfig cellClassConfig = Utils.getComponent(ICellsClassConfig.class);
  private final IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
  private final ModelUtils modelUtils = Utils.getComponent(ModelUtils.class);
  private final Execution execution = Utils.getComponent(Execution.class);

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
    Optional<String> tagName = Optional.absent();
    AttributeBuilder attributes = new DefaultAttributeBuilder().addCssClasses("cel_cell");
    DocumentReference cellDocRef = node.getDocumentReference();
    try {
      LOGGER.debug("startRenderCell: cellDocRef [{}] context db [{}].", cellDocRef,
          context.getDatabase());
      BaseObject cellObj = modelAccess.getXObject(cellDocRef, cellClassConfig.getCellClassRef(
          cellDocRef.getWikiReference().getName()));
      if (cellObj != null) {
        String cellObjCssClasses = cellObj.getStringValue("css_classes");
        if (!Strings.isNullOrEmpty(cellObjCssClasses)) {
          attributes.addCssClasses(cellObjCssClasses);
        }
        attributes.addStyles(cellObj.getStringValue("css_styles"));
      }
      attributes.addId(collectId(cellDocRef, cellObj));
      tagName = getTagName(cellDocRef, cellObj);
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("failed to get cell [{}] document.", cellDocRef, exp);
    }
    IPageTypeConfig cellTypeConfig = getCellTypeConfig(cellDocRef);
    if (cellTypeConfig != null) {
      cellTypeConfig.collectAttributes(attributes, cellDocRef);
    }
    cellWriter.openLevel(tagName.orNull(), attributes.build());
  }

  private String collectId(DocumentReference cellDocRef, BaseObject cellObj) {
    String id = "";
    if (cellObj != null) {
      id = cellObj.getStringValue(ICellsClassConfig.CELLCLASS_IDNAME_FIELD).trim();
    }
    if (id.isEmpty()) {
      id = modelUtils.serializeRef(cellDocRef, COMPACT).replace(":", "..");
      return "cell:" + id;
    }
    Integer idNb = Ints.tryParse(Objects.toString(execution.getContext().getProperty(
        "celements.globalvalues.cell.number")));
    if (idNb != null) {
      id += "_" + idNb;
    }
    return id;
  }

  Optional<String> getTagName(DocumentReference cellDocRef, BaseObject cellObj) {
    Optional<String> tagName = Optional.absent();
    if (cellObj != null) {
      tagName = Optional.fromNullable(Strings.emptyToNull(cellObj.getStringValue(
          ICellsClassConfig.CELLCLASS_TAGNAME_FIELD)));
    }
    if (!tagName.isPresent()) {
      IPageTypeConfig cellTypeConfig = getCellTypeConfig(cellDocRef);
      if (cellTypeConfig != null) {
        tagName = cellTypeConfig.defaultTagName();
      }
    }
    return tagName;
  }

  IPageTypeConfig getCellTypeConfig(DocumentReference cellDocRef) {
    PageTypeReference cellTypeRef = getPageTypeResolver().getPageTypeRefForDocWithDefault(
        cellDocRef);
    IPageTypeConfig cellTypeConfig = null;
    if (cellTypeRef != null) {
      cellTypeConfig = getPageTypeService().getPageTypeConfigForPageTypeRef(cellTypeRef);
    }
    return cellTypeConfig;
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

  IPageTypeResolverRole getPageTypeResolver() {
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

  IPageTypeRole getPageTypeService() {
    return Utils.getComponent(IPageTypeRole.class);
  }

}
