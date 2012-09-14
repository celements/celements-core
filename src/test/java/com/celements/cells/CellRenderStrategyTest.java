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

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CellRenderStrategyTest extends AbstractBridgedComponentTestCase {

  private CellRenderStrategy renderer;
  private ICellWriter outWriterMock;
  private XWikiContext context;
  private XWiki xwiki;
  private RenderCommand mockctRendererCmd;
  private PageLayoutCommand pageLayoutCmdMock;

  @Before
  public void setUp_CellRendererTest() throws Exception {
    outWriterMock = createMock(ICellWriter.class);
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    renderer = new CellRenderStrategy(context).setOutputWriter(outWriterMock);
    mockctRendererCmd = createMock(RenderCommand.class);
    renderer.rendererCmd = mockctRendererCmd;
    pageLayoutCmdMock = createMock(PageLayoutCommand.class);
    renderer.pageLayoutCmd = pageLayoutCmdMock;
  }

  @Test
  public void testPageTypeCmd() {
    renderer.rendererCmd = null;
    assertNotNull(renderer.getRendererCmd());
    assertSame("Expecting singleton.", renderer.getRendererCmd(), 
        renderer.getRendererCmd());
  }

  @Test
  public void testInject_ctRendererCmd() {
    renderer.rendererCmd = mockctRendererCmd;
    assertNotNull(renderer.getRendererCmd());
    assertSame("Expecting injected mock object.", mockctRendererCmd,
        renderer.getRendererCmd());
  }

  @Test
  public void testGetAsString() {
    String expectedOutput = "blabla";
    expect(outWriterMock.getAsString()).andReturn(expectedOutput);
    replay(outWriterMock);
    assertEquals("asString must return the current state of the StringBuilder (out).",
        expectedOutput, renderer.getAsString());
    verify(outWriterMock);
  }

  @Test
  public void testStartRendering() {
    outWriterMock.clear();
    expectLastCall().once();
    replay(outWriterMock);
    renderer.startRendering();
    verify(outWriterMock);
  }

  @Test
  public void testEndRenderCell() {
    outWriterMock.closeLevel();
    expectLastCall().once();
    replay(outWriterMock);
    renderer.endRenderCell(null, true, false);
    verify(outWriterMock);
  }

  @Test
  public void testGetMenuPart() {
    assertEquals("expecting empty menuPart for cells.", "", renderer.getMenuPart(null));
  }

  @Test
  public void testIsRenderCell() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    assertFalse(renderer.isRenderCell(null));
    assertTrue(renderer.isRenderCell(new TreeNode(docRef, "", 0)));
  }

  @Test
  public void testGetSpaceReference_default() {
    renderer.setSpaceReference(null);
    SpaceReference defaultLayout = new SpaceReference("SimpleLayout", new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getDefaultLayoutSpaceReference()).andReturn(defaultLayout);
    replayAll();
    assertEquals("expecting default layout space", defaultLayout,
        renderer.getSpaceReference());
    verifyAll();
  }

  @Test
  public void testGetSpaceReference() {
    SpaceReference layoutSpaceRef = new SpaceReference("TestLayout", new WikiReference(
        context.getDatabase()));
    renderer.setSpaceReference(layoutSpaceRef);
    replayAll();
    assertEquals(layoutSpaceRef, renderer.getSpaceReference());
    verifyAll();
  }

  @Test
  public void testGetSpaceReference_null_setBack() {
    SpaceReference layoutSpaceRef = new SpaceReference("TestLayout", new WikiReference(
        context.getDatabase()));
    renderer.setSpaceReference(layoutSpaceRef);
    renderer.setSpaceReference(null);
    SpaceReference defaultLayout = new SpaceReference("SimpleLayout", new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getDefaultLayoutSpaceReference()).andReturn(defaultLayout);
    replayAll();
    assertEquals(defaultLayout, renderer.getSpaceReference());
    verifyAll();
  }

  @Test
  public void testIsRenderSubCells() {
    assertFalse(renderer.isRenderSubCells(null));
    SpaceReference layoutSpaceRef = new SpaceReference("TestLayout", new WikiReference(
        context.getDatabase()));
    assertTrue(renderer.isRenderSubCells(layoutSpaceRef));
  }

  @Test
  public void testStartRenderCell() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, "", 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<BaseObject>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(context.getDatabase(),
        CellRenderStrategy.CELEMENTS_CELL_CLASS_SPACE,
        CellRenderStrategy.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc);
    outWriterMock.openLevel(eq(idname), eq(cssClasses), eq(cssStyles));

    replayAll();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    verifyAll();
  }

  @Test
  public void testStartRenderCell_otherDb() throws XWikiException {
    String masterCellDb = "theMasterCellDB";
    DocumentReference docRef = new DocumentReference(masterCellDb, "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, "", 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<BaseObject>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(masterCellDb,
        CellRenderStrategy.CELEMENTS_CELL_CLASS_SPACE,
        CellRenderStrategy.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc);
    outWriterMock.openLevel(eq(idname), eq(cssClasses), eq(cssStyles));

    replayAll();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    verifyAll();
  }

  @Test
  public void testRenderEmptyChildren() throws XWikiException {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    TreeNode cellNode = new TreeNode(cellRef, "", 0);
    String cellContentExpected = "Cell test content Skin.MasterCell";
    expect(mockctRendererCmd.renderCelementsCell(eq(cellRef))).andReturn(
        cellContentExpected).once();
    //ASSERT
    outWriterMock.appendContent(eq(cellContentExpected));
    replayAll();
    renderer.renderEmptyChildren(cellNode);
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, outWriterMock, mockctRendererCmd, pageLayoutCmdMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, outWriterMock, mockctRendererCmd, pageLayoutCmdMock);
    verify(mocks);
  }

}
