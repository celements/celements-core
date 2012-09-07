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

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CellRendererTest extends AbstractBridgedComponentTestCase {

  private CellRenderer renderer;
  private ICellWriter outWriterMock;
  private XWikiContext context;
  private XWiki xwiki;
  private RenderCommand mockctRendererCmd;

  @Before
  public void setUp_CellRendererTest() throws Exception {
    outWriterMock = createMock(ICellWriter.class);
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    renderer = new CellRenderer(context).setOutputWriter(outWriterMock);
    mockctRendererCmd = createMock(RenderCommand.class);
    renderer.inject_ctRenderCmd(mockctRendererCmd);
  }

  @Test
  public void testPageTypeCmd() {
    renderer.inject_ctRenderCmd(null);
    assertNotNull(renderer.ctRendererCmd());
    assertSame("Expecting singleton.", renderer.ctRendererCmd(), 
        renderer.ctRendererCmd());
  }

  @Test
  public void testInject_ctRendererCmd() {
    renderer.inject_ctRenderCmd(mockctRendererCmd);
    assertNotNull(renderer.ctRendererCmd());
    assertSame("Expecting injected mock object.", mockctRendererCmd,
        renderer.ctRendererCmd());
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
  public void testGetMenuSpace() {
    assertEquals("expecting 'Skin' menuSpace for cells.", "Skin",
        renderer.getMenuSpace(null));
  }

  @Test
  public void testIsRenderCell() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin",
        "MasterCell");
    assertFalse(renderer.isRenderCell(null));
    assertTrue(renderer.isRenderCell(new TreeNode(docRef, "", 0)));
  }

  @Test
  public void testSetSpaceName() {
    String spaceName = "TestLayout";
    renderer.setSpaceName(spaceName);
    assertEquals(spaceName, renderer.getMenuSpace(""));
  }

  @Test
  public void testSetSpaceName_null_default() {
    renderer.setSpaceName(null);
    assertEquals("Skin", renderer.getMenuSpace(""));
  }

  @Test
  public void testSetSpaceName_emptyString_default() {
    renderer.setSpaceName("");
    assertEquals("Skin", renderer.getMenuSpace(""));
  }

  @Test
  public void testSetSpaceName_null_setBack() {
    renderer.setSpaceName("initSpace");
    renderer.setSpaceName(null);
    assertEquals("Skin", renderer.getMenuSpace(""));
  }

  @Test
  public void testIsRenderSubCells() {
    assertFalse(renderer.isRenderSubCells(null));
    assertTrue(renderer.isRenderSubCells("notNullValue"));
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
        CellRenderer.CELEMENTS_CELL_CLASS_SPACE, CellRenderer.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc);
    outWriterMock.openLevel(eq(idname), eq(cssClasses), eq(cssStyles));

    replay(xwiki, outWriterMock);
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    verify(xwiki, outWriterMock);
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
        CellRenderer.CELEMENTS_CELL_CLASS_SPACE, CellRenderer.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc);
    outWriterMock.openLevel(eq(idname), eq(cssClasses), eq(cssStyles));

    replay(xwiki, outWriterMock);
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    verify(xwiki, outWriterMock);
  }

  @Test
  public void testRenderEmptyChildren() throws XWikiException {
    String fullname = "Skin.MasterCell";
    String cellContentExpected = "Cell test content Skin.MasterCell";
    expect(mockctRendererCmd.renderCelementsCell(eq(fullname))
        ).andReturn(cellContentExpected).once();
    //ASSERT
    outWriterMock.appendContent(eq(cellContentExpected));
    replay(xwiki, outWriterMock, mockctRendererCmd);
    renderer.renderEmptyChildren(fullname);
    verify(xwiki, outWriterMock, mockctRendererCmd);
  }

}
