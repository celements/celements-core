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
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.cells.classes.CellAttributeClass;
import com.celements.cells.classes.CellClass;
import com.celements.common.MoreOptional;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.TreeNode;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.velocity.VelocityService;
import com.celements.web.service.CelementsWebScriptService;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CellRenderStrategy implements IRenderStrategy {

  public static final String EXEC_CTX_KEY = "celements.cell";
  public static final String EXEC_CTX_KEY_DOC_SUFFIX = ".document";
  public static final String EXEC_CTX_KEY_DOC = EXEC_CTX_KEY + EXEC_CTX_KEY_DOC_SUFFIX;
  public static final String EXEC_CTX_KEY_OBJ_NB_SUFFIX = ".number";
  public static final String EXEC_CTX_KEY_OBJ_NB = EXEC_CTX_KEY + EXEC_CTX_KEY_OBJ_NB_SUFFIX;
  public static final String EXEC_CTX_KEY_GLOBAL = CelementsWebScriptService.CEL_GLOBALVAL_PREFIX
      + "cell";
  public static final String EXEC_CTX_KEY_GLOBAL_OBJ_NB = EXEC_CTX_KEY_GLOBAL
      + EXEC_CTX_KEY_OBJ_NB_SUFFIX;

  private static final Logger LOGGER = LoggerFactory.getLogger(CellRenderStrategy.class);

  private ICellWriter cellWriter;
  private XWikiContext context;

  private SpaceReference spaceReference;

  RenderCommand rendererCmd;
  private final IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
  private final ModelUtils modelUtils = Utils.getComponent(ModelUtils.class);
  private final Execution execution = Utils.getComponent(Execution.class);
  private final VelocityService velocityService = Utils.getComponent(VelocityService.class);

  public CellRenderStrategy(XWikiContext context) {
    this.context = context;
  }

  @Override
  public void endRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem) {
    cellWriter.closeLevel();
  }

  @Override
  public void endRenderChildren(EntityReference parentRef) {}

  @Override
  public void endRendering() {}

  @Override
  public String getMenuPart(TreeNode node) {
    return "";
  }

  @Override
  public SpaceReference getSpaceReference() {
    if (spaceReference == null) {
      return getLayoutService().getDefaultLayoutSpaceReference();
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
    AttributeBuilder attrBuilder = new DefaultAttributeBuilder().addCssClasses("cel_cell");
    DocumentReference cellDocRef = node.getDocumentReference();
    LOGGER.debug("startRenderCell: cellDocRef [{}] db [{}].", cellDocRef, context.getDatabase());
    collectCellAttributes(cellDocRef, attrBuilder);
    getCellTypeConfig(cellDocRef).ifPresent(cellTypeConfig -> cellTypeConfig
        .collectAttributes(attrBuilder, cellDocRef));
    cellWriter.openLevel(getTagName(cellDocRef).orElse(null), attrBuilder.build());
  }

  private void collectCellAttributes(DocumentReference cellDocRef,
      AttributeBuilder attrBuilder) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc).filter(CellClass.CLASS_REF);
      attrBuilder.addId(collectId(cellDocRef, fetcher));
      attrBuilder.addAttribute("data-cell-ref", modelUtils.serializeRef(cellDocRef, COMPACT));
      fetcher.fetchField(CellClass.FIELD_CSS_CLASSES).stream().findFirst()
          .ifPresent(attrBuilder::addCssClasses);
      fetcher.fetchField(CellClass.FIELD_CSS_STYLES).stream().findFirst()
          .ifPresent(attrBuilder::addStyles);
      fetcher.fetchField(CellClass.FIELD_EVENT_DATA_ATTR).stream().findFirst()
          .ifPresent(value -> collectEventDataAttr(cellDocRef, attrBuilder, value));
      XWikiObjectFetcher.on(cellDoc).filter(CellAttributeClass.CLASS_REF).stream()
          .forEach(attrObj -> collectCustomAttributes(attrObj, attrBuilder));
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("failed to get cell doc [{}]", cellDocRef, exc);
    }
  }

  Optional<String> getTagName(DocumentReference cellDocRef) {
    Optional<String> tagName = XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(cellDocRef))
        .fetchField(CellClass.FIELD_TAG_NAME).stream().findFirst();
    if (!tagName.isPresent()) {
      tagName = getCellTypeConfig(cellDocRef)
          .flatMap(cellTypeConfig -> cellTypeConfig.defaultTagName().toJavaUtil());
    }
    return tagName;
  }

  private String collectId(DocumentReference cellDocRef, XWikiObjectFetcher fetcher) {
    String id = fetcher.fetchField(CellClass.FIELD_ID_NAME).stream().findFirst()
        .orElseGet(() -> "cell:" + modelUtils.serializeRef(cellDocRef, COMPACT).replace(":", ".."));
    return id + Stream.of(EXEC_CTX_KEY_OBJ_NB, EXEC_CTX_KEY_GLOBAL_OBJ_NB)
        .map(execution.getContext()::getProperty)
        .map(val -> Ints.tryParse(Objects.toString(val)))
        .filter(Objects::nonNull)
        .map(nb -> "_" + nb)
        .findFirst().orElse("");
  }

  private void collectEventDataAttr(DocumentReference cellDocRef, AttributeBuilder attributes,
      String value) {
    evaluateVelocity(value, cellDocRef).ifPresent(text -> {
      attributes.addCssClasses("celOnEvent");
      attributes.addAttribute("data-cel-event", value);
    });
  }

  private void collectCustomAttributes(BaseObject attrObj, AttributeBuilder attrBuilder) {
    DocumentReference cellDocRef = attrObj.getDocumentReference();
    String name = attrObj.getStringValue(CellAttributeClass.FIELD_NAME.getName());
    String value = attrObj.getStringValue(CellAttributeClass.FIELD_VALUE.getName());
    Optional<String> text = evaluateVelocity(value, cellDocRef);
    if (text.isPresent()) {
      attrBuilder.addAttribute(name, text.get());
    } else {
      attrBuilder.addEmptyAttribute(name);
    }
  }

  private Optional<String> evaluateVelocity(String text, DocumentReference cellDocRef) {
    try {
      return MoreOptional.asNonBlank(velocityService.evaluateVelocityText(text));
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("unable to velo-evaluate text on [{}]", cellDocRef, exc);
      return Optional.empty();
    }
  }

  Optional<IPageTypeConfig> getCellTypeConfig(DocumentReference cellDocRef) {
    PageTypeReference cellTypeRef = getPageTypeResolver()
        .resolvePageTypeReferenceWithDefault(cellDocRef);
    IPageTypeConfig cellTypeConfig = getPageTypeService()
        .getPageTypeConfigForPageTypeRef(cellTypeRef);
    return Optional.ofNullable(cellTypeConfig);
  }

  @Override
  public void startRenderChildren(EntityReference parentRef) {}

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

  LayoutServiceRole getLayoutService() {
    return Utils.getComponent(LayoutServiceRole.class);
  }

}
