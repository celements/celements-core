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
    TreeNode node = new TreeNode("Skin.MasterCell", "", 0, context.getDatabase());
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
    TreeNode node = new TreeNode("Skin.MasterCell", "", 0, context.getDatabase());
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
    renderStrategyMock.setSpaceName(eq(spaceName));
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
    renderStrategyMock.setSpaceName(eq(pageLayName));
    expectLastCall().once();

    expect(renderStrategyMock.isRenderSubCells(eq(""))).andReturn(true).once();
    String menuSpace = "MyCellSpace";
    String menuPart = "mainPart";
    expect(renderStrategyMock.getMenuSpace(eq(""))).andReturn(menuSpace);
    expect(renderStrategyMock.getMenuPart(eq(""))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(""), eq(menuSpace), eq(menuPart))
        ).andReturn(subCellList);
    renderStrategyMock.renderEmptyChildren(eq(""));
    expectLastCall().once();

    replay(renderStrategyMock, mockTreeNodeService);
    renderingEngine.renderPageLayout(pageLayName);
    verify(renderStrategyMock, mockTreeNodeService);
  }

  @Test
  public void testInternal_RenderCell_notRender() {
    TreeNode menuItem = new TreeNode("Skin.Cell2", "", 0, context.getDatabase());
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
    TreeNode menuItem = new TreeNode(fullName, "", 0, context.getDatabase());
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
    expect(renderStrategyMock.getMenuSpace(eq(fullName))).andReturn(menuSpace);
    expect(renderStrategyMock.getMenuPart(eq(fullName))).andReturn(menuPart);
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    expect(mockTreeNodeService.getSubNodesForParent(eq(fullName), eq(menuSpace),
        eq(menuPart))).andReturn(subCellList);
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
    expect(renderStrategyMock.getMenuSpace(eq(fullName))).andReturn(menuSpace).once();
    expect(renderStrategyMock.getMenuPart(eq(fullName))).andReturn(menuPart).once();
    List<TreeNode> subCellList = new ArrayList<TreeNode>();
    TreeNode subCell1 = new TreeNode(menuSpace + ".subCell1", "", 1,
        context.getDatabase());
    subCellList.add(subCell1);
    TreeNode subCell2 = new TreeNode(menuSpace + ".subCell2", "", 2,
        context.getDatabase());
    subCellList.add(subCell2);
    TreeNode subCell3 = new TreeNode(menuSpace + ".subCell3", "", 3,
        context.getDatabase());
    subCellList.add(subCell3);
    TreeNode subCell4 = new TreeNode(menuSpace + ".subCell4", "", 4,
        context.getDatabase());
    subCellList.add(subCell4);
    expect(mockTreeNodeService.getSubNodesForParent(eq(fullName), eq(menuSpace),
        eq(menuPart))).andReturn(subCellList);
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
