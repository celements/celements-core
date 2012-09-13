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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class RenderingEngineTest extends AbstractBridgedComponentTestCase {

  private RenderingEngine renderingEngine;
  private XWikiContext context;
  private IRenderStrategy renderStrategyMock;
  private ITreeNodeService mockTreeNodeService;

  @Before
  public void setUp_RenderingEngineTest() throws Exception {
    renderStrategyMock = createMock(IRenderStrategy.class);
    mockTreeNodeService = createMock(ITreeNodeService.class);
    renderingEngine = new RenderingEngine().setRenderStrategy(renderStrategyMock);
    renderingEngine.treeNodeService = mockTreeNodeService;
    context = getContext();
  }

  @Test
  public void testRenderCell_notRender() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    TreeNode node = new TreeNode(docRef, "", 0);
    renderStrategyMock.startRendering();
    expectLastCall().once();
    renderStrategyMock.endRendering();
    expectLastCall().once();
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(false).once();

    replay(renderStrategyMock);
    renderingEngine.renderCell(node);
    verify(renderStrategyMock);
  }

  @Test
  public void testRenderCell_isRendering() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    TreeNode node = new TreeNode(docRef, "", 0);
    renderStrategyMock.startRendering();
    expectLastCall().once();
    renderStrategyMock.endRendering();
    expectLastCall().once();
    expect(renderStrategyMock.isRenderCell(same(node))).andReturn(true).once();
    //isFirst AND isLast because only this item and its children is
    //rendered (NO SIBLINGS!)
    boolean isLastItem = true;
    boolean isFirstItem = true;
    renderStrategyMock.startRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    expectLastCall().once();
    renderStrategyMock.endRenderCell(eq(node), eq(isFirstItem), eq(isLastItem));
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq("Skin.MasterCell"))).andReturn(false
        ).once();

    replay(renderStrategyMock);
    renderingEngine.renderCell(node);
    verify(renderStrategyMock);
  }

  @Test
  public void testRenderPageLayout_notRender() {
    String spaceName = "Skin";
    renderStrategyMock.startRendering();
    expectLastCall().once();
    SpaceReference spaceReference = new SpaceReference(spaceName, new WikiReference(
        context.getDatabase()));
    renderStrategyMock.setSpaceReference(eq(spaceReference));
    expectLastCall().once();
    renderStrategyMock.endRendering();
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq(""))).andReturn(false).once();

    replay(renderStrategyMock);
    renderingEngine.renderPageLayout(spaceName);
    verify(renderStrategyMock);
  }

  @Test
  public void testRenderPageLayout_isRendering() {
    String pageLayName = "MasterPageLayout";
    renderStrategyMock.startRendering();
    expectLastCall().once();
    renderStrategyMock.endRendering();
    expectLastCall().once();
    SpaceReference spaceReference = new SpaceReference(pageLayName, new WikiReference(
        context.getDatabase()));
    renderStrategyMock.setSpaceReference(eq(spaceReference));
    expectLastCall().once();

    expect(renderStrategyMock.isRenderSubCells(eq(""))).andReturn(true).once();
    String menuSpace = "MyCellSpace";
    String menuPart = "mainPart";
    EntityReference menuSpaceRef = new SpaceReference(menuSpace, new WikiReference(
        context.getDatabase()));
    expect(renderStrategyMock.getParentReference(eq(""))).andReturn(menuSpaceRef);
    expect(renderStrategyMock.getMenuPart(eq(""))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(menuSpaceRef), eq(menuPart))
        ).andReturn(subCellList);
    renderStrategyMock.renderEmptyChildren(eq(""));
    expectLastCall().once();

    replay(renderStrategyMock, mockTreeNodeService);
    renderingEngine.renderPageLayout(pageLayName);
    verify(renderStrategyMock, mockTreeNodeService);
  }

  @Test
  public void testRenderPageLayout_otherDB() {
    String pageLaySpaceName = "MasterPageLayout";
    String pageLayName = "celements2web:" + pageLaySpaceName;
    renderStrategyMock.startRendering();
    expectLastCall().once();
    renderStrategyMock.endRendering();
    expectLastCall().once();
    SpaceReference spaceReference = new SpaceReference(pageLaySpaceName,
        new WikiReference("celements2web"));
    renderStrategyMock.setSpaceReference(eq(spaceReference));
    expectLastCall().once();

    expect(renderStrategyMock.isRenderSubCells(eq(""))).andReturn(true).once();
    String menuSpace = "celements2web:MyCellSpace";
    String menuPart = "mainPart";
    EntityReference menuSpaceRef = new SpaceReference("MyCellSpace", new WikiReference(
        "celements2web"));
    expect(renderStrategyMock.getParentReference(eq(""))).andReturn(menuSpaceRef);
    expect(renderStrategyMock.getMenuPart(eq(""))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(menuSpaceRef), eq(menuPart))
        ).andReturn(subCellList);
    renderStrategyMock.renderEmptyChildren(eq(""));
    expectLastCall().once();

    replay(renderStrategyMock, mockTreeNodeService);
    renderingEngine.renderPageLayout(pageLayName);
    verify(renderStrategyMock, mockTreeNodeService);
  }

  @Test
  public void testInternal_RenderCell_notRender() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "Cell2");
    TreeNode menuItem = new TreeNode(docRef, "", 0);
    expect(renderStrategyMock.isRenderCell(same(menuItem))).andReturn(false).once();

    replay(renderStrategyMock);
    renderingEngine.internal_renderCell(menuItem, true, false);
    verify(renderStrategyMock);
  }

  @Test
  public void testInternal_RenderCell_isRendering() {
    String fullName = "Skin.Cell2";
    boolean isLastItem = false;
    boolean isFirstItem = true;
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "Cell2");
    TreeNode menuItem = new TreeNode(docRef, "", 0);
    expect(renderStrategyMock.isRenderCell(same(menuItem))).andReturn(true).once();
    renderStrategyMock.startRenderCell(eq(menuItem), eq(isFirstItem), eq(isLastItem));
    expectLastCall().once();
    renderStrategyMock.endRenderCell(eq(menuItem), eq(isFirstItem), eq(isLastItem));
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq(fullName))).andReturn(false).once();

    replay(renderStrategyMock);
    renderingEngine.internal_renderCell(menuItem, isFirstItem, isLastItem);
    verify(renderStrategyMock);
  }

  @Test
  public void testInternal_renderSubCells_notRenderSubCells() {
    String fullName = "Skin.Cell2";
    expect(renderStrategyMock.isRenderSubCells(eq(fullName))).andReturn(false).once();

    replay(renderStrategyMock);
    renderingEngine.internal_renderSubCells(fullName);
    verify(renderStrategyMock);
  }

  @Test
  public void testInternal_renderSubCells_isRendering_noSubcells() {
    String fullName = "Skin.Cell2";
    expect(renderStrategyMock.isRenderSubCells(eq(fullName))).andReturn(true).once();
    String menuSpace = "MyCellSpace";
    String menuPart = "mainPart";
    EntityReference parentDocRef = new DocumentReference(context.getDatabase(),
        menuSpace, "Cell2");
    expect(renderStrategyMock.getParentReference(eq(fullName))).andReturn(parentDocRef);
    expect(renderStrategyMock.getMenuPart(eq(fullName))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(parentDocRef), eq(menuPart))
        ).andReturn(subCellList);
    renderStrategyMock.renderEmptyChildren(eq(fullName));
    expectLastCall().once();

    replay(renderStrategyMock, mockTreeNodeService);
    renderingEngine.internal_renderSubCells(fullName);
    verify(renderStrategyMock, mockTreeNodeService);
  }

  @Test
  public void testInternal_renderSubCells_isRendering_withSubcells() {
    String fullName = "Skin.Cell2";
    expect(renderStrategyMock.isRenderSubCells(eq(fullName))).andReturn(true).once();
    String menuSpace = "MyCellSpace";
    String menuPart = "mainPart";
    EntityReference parentDocRef = new DocumentReference(context.getDatabase(), menuSpace,
        "Cell2");
    expect(renderStrategyMock.getParentReference(eq(fullName))).andReturn(parentDocRef);
    expect(renderStrategyMock.getMenuPart(eq(fullName))).andReturn(menuPart).once();
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    TreeNode subCell1 = new TreeNode(new DocumentReference(context.getDatabase(),
        menuSpace, "subCell1"), "", 1);
    subCellList.add(subCell1);
    TreeNode subCell2 = new TreeNode(new DocumentReference(context.getDatabase(),
        menuSpace, "subCell2"), "", 2);
    subCellList.add(subCell2);
    TreeNode subCell3 = new TreeNode(new DocumentReference(context.getDatabase(),
        menuSpace, "subCell3"), "", 3);
    subCellList.add(subCell3);
    TreeNode subCell4 = new TreeNode(new DocumentReference(context.getDatabase(),
        menuSpace, "subCell4"), "", 4);
    subCellList.add(subCell4);
    expect(mockTreeNodeService.getSubNodesForParent(eq(parentDocRef), eq(menuPart))
        ).andReturn(subCellList);
    renderStrategyMock.startRenderChildren(eq(fullName));
    expectLastCall().once();
    renderStrategyMock.endRenderChildren(eq(fullName));
    expectLastCall().once();

    expect(renderStrategyMock.isRenderCell(same(subCell1))).andReturn(true).once();
    renderStrategyMock.startRenderCell(eq(subCell1), eq(true), eq(false));
    expectLastCall().once();
    renderStrategyMock.endRenderCell(eq(subCell1), eq(true), eq(false));
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq(subCell1.getFullName()))).andReturn(
        false);
    
    expect(renderStrategyMock.isRenderCell(same(subCell2))).andReturn(true).once();
    renderStrategyMock.startRenderCell(eq(subCell2), eq(false), eq(false));
    expectLastCall().once();
    renderStrategyMock.endRenderCell(eq(subCell2), eq(false), eq(false));
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq(subCell2.getFullName()))).andReturn(
        false);

    expect(renderStrategyMock.isRenderCell(same(subCell3))).andReturn(false).once();

    expect(renderStrategyMock.isRenderCell(same(subCell4))).andReturn(true).once();
    renderStrategyMock.startRenderCell(eq(subCell4), eq(false), eq(true));
    expectLastCall().once();
    renderStrategyMock.endRenderCell(eq(subCell4), eq(false), eq(true));
    expectLastCall().once();
    expect(renderStrategyMock.isRenderSubCells(eq(subCell4.getFullName()))).andReturn(
        false);

    replay(renderStrategyMock, mockTreeNodeService);
    renderingEngine.internal_renderSubCells(fullName);
    verify(renderStrategyMock, mockTreeNodeService);
  }

  @Test
  public void testGetTreeNodeService_inject() {
    assertSame(mockTreeNodeService, renderingEngine.getTreeNodeService());
  }

  @Test
  public void testGetTreeNodeService_defaultInstancing() {
    renderingEngine.treeNodeService = null;
    ITreeNodeService treeNodeService = renderingEngine.getTreeNodeService();
    assertNotNull(treeNodeService);
    assertSame(Utils.getComponent(ITreeNodeService.class), treeNodeService);
    assertNotSame(mockTreeNodeService, treeNodeService);
  }

}
