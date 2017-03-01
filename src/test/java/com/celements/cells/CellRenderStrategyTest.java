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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.CellAttribute;
import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.TreeNode;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CellRenderStrategyTest extends AbstractComponentTest {

  private CellRenderStrategy renderer;
  private ICellWriter outWriterMock;
  private XWikiContext context;
  private XWiki xwiki;
  private RenderCommand mockctRendererCmd;
  private PageLayoutCommand pageLayoutCmdMock;
  private IPageTypeResolverRole pageTypeResolverMock;
  private IPageTypeRole pageTypeServiceMock;

  @Before
  public void setUp_CellRendererTest() throws Exception {
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    pageTypeServiceMock = registerComponentMock(IPageTypeRole.class);
    outWriterMock = createMockAndAddToDefault(ICellWriter.class);
    context = getContext();
    xwiki = getWikiMock();
    renderer = new CellRenderStrategy(context).setOutputWriter(outWriterMock);
    mockctRendererCmd = createMockAndAddToDefault(RenderCommand.class);
    renderer.rendererCmd = mockctRendererCmd;
    pageLayoutCmdMock = createMockAndAddToDefault(PageLayoutCommand.class);
    renderer.pageLayoutCmd = pageLayoutCmdMock;
  }

  @Test
  public void testPageTypeCmd() {
    renderer.rendererCmd = null;
    assertNotNull(renderer.getRendererCmd());
    assertSame("Expecting singleton.", renderer.getRendererCmd(), renderer.getRendererCmd());
  }

  @Test
  public void testInject_ctRendererCmd() {
    renderer.rendererCmd = mockctRendererCmd;
    assertNotNull(renderer.getRendererCmd());
    assertSame("Expecting injected mock object.", mockctRendererCmd, renderer.getRendererCmd());
  }

  @Test
  public void test_getCellTypeConfig_noTypeRef() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        null).once();
    replayDefault();
    assertNull(renderer.getCellTypeConfig(cellRef));
    verifyDefault();
  }

  @Test
  public void test_getCellTypeConfig_noTypeConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    PageTypeReference cellTypeRef = new PageTypeReference("TestType", "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).once();
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        null).once();
    replayDefault();
    assertNull(renderer.getCellTypeConfig(cellRef));
    verifyDefault();
  }

  @Test
  public void test_getCellTypeConfig_withTypeConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    PageTypeReference cellTypeRef = new PageTypeReference("TestType", "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).once();
    IPageTypeConfig typeConfig = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        typeConfig).once();
    replayDefault();
    assertSame(typeConfig, renderer.getCellTypeConfig(cellRef));
    verifyDefault();
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
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    assertFalse(renderer.isRenderCell(null));
    assertTrue(renderer.isRenderCell(new TreeNode(docRef, null, 0)));
  }

  @Test
  public void testGetSpaceReference_default() {
    renderer.setSpaceReference(null);
    SpaceReference defaultLayout = new SpaceReference("SimpleLayout", new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getDefaultLayoutSpaceReference()).andReturn(defaultLayout);
    replayDefault();
    assertEquals("expecting default layout space", defaultLayout, renderer.getSpaceReference());
    verifyDefault();
  }

  @Test
  public void testGetSpaceReference() {
    SpaceReference layoutSpaceRef = new SpaceReference("TestLayout", new WikiReference(
        context.getDatabase()));
    renderer.setSpaceReference(layoutSpaceRef);
    replayDefault();
    assertEquals(layoutSpaceRef, renderer.getSpaceReference());
    verifyDefault();
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
    replayDefault();
    assertEquals(defaultLayout, renderer.getSpaceReference());
    verifyDefault();
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
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(context.getDatabase(),
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_noCssClasses() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(context.getDatabase(),
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes("", idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_auto_id_for_null() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(context.getDatabase(),
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    String idname = "cell:Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_auto_id_forEmpty() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", "");
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(context.getDatabase(),
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    String idname = "cell:Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_auto_id_forEmpty_diffdb() throws XWikiException {
    DocumentReference docRef = new DocumentReference("layoutDb", "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    DocumentReference cellClassRef = new DocumentReference("layoutDb",
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", "");
    cellObj.setStringValue("css_styles", cssStyles);
    cellObj.setXClassReference(cellClassRef);
    doc.addXObject(cellObj);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    String idname = "cell:layoutDb..Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_otherDb() throws XWikiException {
    String masterCellDb = "theMasterCellDB";
    DocumentReference docRef = new DocumentReference(masterCellDb, "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(docRef, null, 0);
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(masterCellDb,
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).atLeastOnce();
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        null).atLeastOnce();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testStartRenderCell_additionalAttributes() throws Exception {
    String masterCellDb = context.getDatabase();
    DocumentReference cellRef = new DocumentReference(masterCellDb, "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = new XWikiDocument(cellRef);
    BaseObject cellObj = new BaseObject();
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Vector<BaseObject> cellObjList = new Vector<>();
    cellObjList.add(cellObj);
    DocumentReference cellClassRef = new DocumentReference(masterCellDb,
        ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE, ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME);
    doc.setXObjects(cellClassRef, cellObjList);
    expect(xwiki.exists(eq(cellRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(cellRef), same(context))).andReturn(doc).atLeastOnce();
    Capture<List<CellAttribute>> capturedAttrList = new Capture<>();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectLastCall().once();
    String configName = "TestType";
    PageTypeReference cellTypeRef = new PageTypeReference(configName, "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).atLeastOnce();
    IPageTypeConfig typeConfig = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        typeConfig).atLeastOnce();
    expect(typeConfig.defaultTagName()).andReturn(Optional.<String>absent()).atLeastOnce();
    typeConfig.collectAttributes(isA(AttributeBuilder.class), eq(cellRef));
    expectLastCall().once();
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void testRenderEmptyChildren() throws XWikiException {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    TreeNode cellNode = new TreeNode(cellRef, null, 0);
    String cellContentExpected = "Cell test content Skin.MasterCell";
    expect(mockctRendererCmd.renderCelementsCell(eq(cellRef))).andReturn(
        cellContentExpected).once();
    // ASSERT
    outWriterMock.appendContent(eq(cellContentExpected));
    replayDefault();
    renderer.renderEmptyChildren(cellNode);
    verifyDefault();
  }

  @Test
  public void test_getTagName_noCellConfig_fallback_CellType() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    String configName = "TestType";
    PageTypeReference cellTypeRef = new PageTypeReference(configName, "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).once();
    IPageTypeConfig typeConfig = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        typeConfig).once();
    expect(typeConfig.defaultTagName()).andReturn(Optional.of(configName)).once();
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef, null);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(configName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_emptyTagName_fallback_CellType() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    String configName = "TestType";
    PageTypeReference cellTypeRef = new PageTypeReference(configName, "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).once();
    IPageTypeConfig typeConfig = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        typeConfig).once();
    BaseObject cellObj = new BaseObject();
    cellObj.setStringValue(ICellsClassConfig.CELLCLASS_TAGNAME_FIELD, "");
    expect(typeConfig.defaultTagName()).andReturn(Optional.of(configName)).once();
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef, cellObj);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(configName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_fromCellConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    BaseObject cellObj = new BaseObject();
    String expectedTagName = "form";
    cellObj.setStringValue(ICellsClassConfig.CELLCLASS_TAGNAME_FIELD, expectedTagName);
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef, cellObj);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(expectedTagName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_emptyTagName_fallback_CellType_noTagName() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    String configName = "TestType";
    PageTypeReference cellTypeRef = new PageTypeReference(configName, "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(pageTypeResolverMock.getPageTypeRefForDocWithDefault(eq(cellRef))).andReturn(
        cellTypeRef).once();
    IPageTypeConfig typeConfig = createMockAndAddToDefault(IPageTypeConfig.class);
    expect(pageTypeServiceMock.getPageTypeConfigForPageTypeRef(eq(cellTypeRef))).andReturn(
        typeConfig).once();
    BaseObject cellObj = new BaseObject();
    cellObj.setStringValue(ICellsClassConfig.CELLCLASS_TAGNAME_FIELD, "");
    expect(typeConfig.defaultTagName()).andReturn(Optional.<String>absent()).once();
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef, cellObj);
    assertNotNull(tagName);
    assertFalse(tagName.isPresent());
    verifyDefault();
  }

  private void assertDefaultAttributes(String cssClasses, String idname, String cssStyles,
      Capture<List<CellAttribute>> capturedAttrList) {
    List<CellAttribute> attrList = capturedAttrList.getValue();
    assertNotNull(attrList);
    assertFalse(attrList.isEmpty());
    Map<String, CellAttribute> attrMap = new HashMap<>();
    for (CellAttribute attr : attrList) {
      attrMap.put(attr.getName(), attr);
    }
    assertTrue("id attribute not found", attrMap.containsKey("id"));
    assertEquals("wrong id attribute", idname, attrMap.get("id").getValue().get());
    assertTrue("cssClass attribute not found", attrMap.containsKey("class"));
    assertEquals("wrong cssClass attribute", ("cel_cell " + cssClasses).trim(), attrMap.get(
        "class").getValue().get());
    assertTrue("styles attribute not found", attrMap.containsKey("style"));
    assertEquals("wrong styles attribute", cssStyles.replaceAll("[\n\r]", ""), attrMap.get(
        "style").getValue().get());
  }

}
