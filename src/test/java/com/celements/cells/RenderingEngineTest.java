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

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.context.Contextualiser;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWikiContext;

public class RenderingEngineTest extends AbstractComponentTest {

  private RenderingEngine renderingEngine;
  private XWikiContext context;
  private IRenderStrategy renderStrategyMock;
  private ITreeNodeService mockTreeNodeService;

  @Before
  public void prepare() throws Exception {
    renderStrategyMock = createDefaultMock(IRenderStrategy.class);
    mockTreeNodeService = registerComponentMock(ITreeNodeService.class);
    renderingEngine = new RenderingEngine().setRenderStrategy(renderStrategyMock);
    context = getXContext();
  }

  @Test
  public void test_renderCell_notRender() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    TreeNode node = new TreeNode(docRef, null, 0);
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderCell(node);
    verifyDefault();
  }

  @Test
  public void test_renderCell_isRendering() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    TreeNode node = new TreeNode(docRef, null, 0);
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(true).once();
    // isFirst AND isLast because only this item and its children is
    // rendered (NO SIBLINGS!)
    boolean isLastItem = true;
    boolean isFirstItem = true;
    expect(renderStrategyMock.getContextualiser(node)).andReturn(new Contextualiser());
    renderStrategyMock.startRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    renderStrategyMock.endRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    expect(renderStrategyMock.isRenderSubCells(eq(docRef))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderCell(node);
    verifyDefault();
  }

  @Test
  public void test_renderLayout_notRender() {
    SpaceReference spaceReference = new SpaceReference("MySkin", new WikiReference(
        context.getDatabase()));
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock.isRenderSubCells(eq(spaceReference))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderLayout(spaceReference);
    verifyDefault();
  }

  @Test
  public void test_renderLayout_isRendering() {
    SpaceReference masterPageLayoutRef = new SpaceReference("MasterPageLayout", new WikiReference(
        context.getDatabase()));
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock.isRenderSubCells(eq(masterPageLayoutRef))).andReturn(true).once();
    String menuPart = "mainPart";
    expect(renderStrategyMock.getMenuPart((TreeNode) isNull())).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(masterPageLayoutRef), eq(
        menuPart))).andReturn(subCellList);
    expect(renderStrategyMock.isRenderCell((TreeNode) isNull())).andReturn(false).once();
    replayDefault();
    renderingEngine.renderLayout(masterPageLayoutRef);
    verifyDefault();
  }

  @Test
  public void test_renderLayout_otherDB() {
    SpaceReference layoutRef = new SpaceReference("MasterPageLayout", new WikiReference(
        "celements2web"));
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock.isRenderSubCells(eq(layoutRef))).andReturn(true).once();
    expect(renderStrategyMock.getMenuPart((TreeNode) isNull())).andReturn("");
    List<TreeNode> subCellList = new ArrayList<>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(layoutRef), eq(""))).andReturn(subCellList);
    expect(renderStrategyMock.isRenderCell((TreeNode) isNull())).andReturn(false);
    replayDefault();
    renderingEngine.renderLayout(layoutRef);
    verifyDefault();
  }

  @Test
  public void test_renderCell_internal_notRender() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "Cell2");
    TreeNode node = new TreeNode(docRef, null, 0);
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderCell(node, true, false);
    verifyDefault();
  }

  @Test
  public void test_renderPageLayoutPartial() {
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "TestLayout",
        "ParentCell");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "TestLayout", "Cell2");
    TreeNode node = new TreeNode(docRef, parentDocRef, 0);
    renderStrategyMock.startRendering();
    renderStrategyMock.endRendering();
    expect(renderStrategyMock
        .isRenderSubCells(eq(node.getDocumentReference().getLastSpaceReference()))).andReturn(false)
            .once();
    replayDefault();
    renderingEngine.renderLayoutPartial(node);
    verifyDefault();
  }

  @Test
  public void test_renderCell_internal_isRendering() {
    boolean isLastItem = false;
    boolean isFirstItem = true;
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "Cell2");
    TreeNode node = new TreeNode(docRef, null, 0);
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(true).once();
    expect(renderStrategyMock.getContextualiser(node)).andReturn(new Contextualiser());
    renderStrategyMock.startRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    renderStrategyMock.endRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    expect(renderStrategyMock.isRenderSubCells(eq(docRef))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderCell(node, isFirstItem, isLastItem);
    verifyDefault();
  }

  @Test
  public void test_renderSubCells_notRenderSubCells() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "Cell2");
    TreeNode node = new TreeNode(docRef, null, 1);
    expect(renderStrategyMock.isRenderSubCells(eq(docRef))).andReturn(false).once();
    replayDefault();
    renderingEngine.renderSubCells(node, docRef);
    verifyDefault();
  }

  @Test
  public void test_renderSubCells_isRendering_noSubcells() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySkin", "Cell2");
    TreeNode node = new TreeNode(docRef, null, 1);
    expect(renderStrategyMock.isRenderSubCells(eq(docRef))).andReturn(true).once();
    String menuPart = "mainPart";
    expect(renderStrategyMock.getMenuPart(eq(node))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(docRef), eq(menuPart))).andReturn(
        subCellList);
    expect(renderStrategyMock.isRenderCell(eq(node))).andReturn(true);
    renderStrategyMock.renderEmptyChildren(eq(node));
    replayDefault();
    renderingEngine.renderSubCells(node, docRef);
    verifyDefault();
  }

  @Test
  public void test_renderSubCells_isRendering_withSubcells() {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    TreeNode cellNode = new TreeNode(cellRef, null, 1);
    expect(renderStrategyMock.isRenderSubCells(eq(cellRef))).andReturn(true).once();
    String menuSpace = "MyCellSpace";
    String menuPart = "mainPart";
    expect(renderStrategyMock.getMenuPart(eq(cellNode))).andReturn(menuPart).once();
    List<TreeNode> subCellList = new ArrayList<>();
    TreeNode subCell1 = new TreeNode(new DocumentReference(context.getDatabase(), menuSpace,
        "subCell1"), null, 1);
    subCellList.add(subCell1);
    TreeNode subCell2 = new TreeNode(new DocumentReference(context.getDatabase(), menuSpace,
        "subCell2"), null, 2);
    subCellList.add(subCell2);
    TreeNode subCell3 = new TreeNode(new DocumentReference(context.getDatabase(), menuSpace,
        "subCell3"), null, 3);
    subCellList.add(subCell3);
    TreeNode subCell4 = new TreeNode(new DocumentReference(context.getDatabase(), menuSpace,
        "subCell4"), null, 4);
    subCellList.add(subCell4);
    expect(mockTreeNodeService.getSubNodesForParent(eq(cellRef), eq(menuPart))).andReturn(
        subCellList);
    renderStrategyMock.startRenderChildren(eq(cellRef));
    renderStrategyMock.endRenderChildren(eq(cellRef));
    expect(renderStrategyMock.isRenderCell(same(subCell1))).andReturn(true).once();
    expect(renderStrategyMock.getContextualiser(subCell1)).andReturn(new Contextualiser());
    renderStrategyMock.startRenderCell(eq(subCell1), eq(true), eq(false));
    renderStrategyMock.endRenderCell(eq(subCell1), eq(true), eq(false));
    expect(renderStrategyMock.isRenderSubCells(eq(subCell1.getDocumentReference())))
        .andReturn(false);
    expect(renderStrategyMock.isRenderCell(same(subCell2))).andReturn(true).once();
    expect(renderStrategyMock.getContextualiser(subCell2)).andReturn(new Contextualiser());
    renderStrategyMock.startRenderCell(eq(subCell2), eq(false), eq(false));
    renderStrategyMock.endRenderCell(eq(subCell2), eq(false), eq(false));
    expect(renderStrategyMock.isRenderSubCells(eq(subCell2.getDocumentReference())))
        .andReturn(false);
    expect(renderStrategyMock.isRenderCell(same(subCell3))).andReturn(false).once();
    expect(renderStrategyMock.isRenderCell(same(subCell4))).andReturn(true).once();
    expect(renderStrategyMock.getContextualiser(subCell4)).andReturn(new Contextualiser());
    renderStrategyMock.startRenderCell(eq(subCell4), eq(false), eq(true));
    renderStrategyMock.endRenderCell(eq(subCell4), eq(false), eq(true));
    expect(renderStrategyMock.isRenderSubCells(eq(subCell4.getDocumentReference())))
        .andReturn(false);
    replayDefault();
    renderingEngine.renderSubCells(cellNode, cellRef);
    verifyDefault();
  }

}
