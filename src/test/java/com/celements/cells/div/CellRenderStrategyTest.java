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

import static com.celements.cells.CellRenderStrategy.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.classes.CellAttributeClass;
import com.celements.cells.classes.CellClass;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.navigation.TreeNode;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.velocity.VelocityService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CellRenderStrategyTest extends AbstractComponentTest {

  private CellRenderStrategy renderer;
  private ICellWriter outWriterMock;
  private XWikiContext context;
  private RenderCommand mockctRendererCmd;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class,
        IPageTypeRole.class, LayoutServiceRole.class, VelocityService.class);
    outWriterMock = createDefaultMock(ICellWriter.class);
    context = getContext();
    mockctRendererCmd = createDefaultMock(RenderCommand.class);
    renderer = new CellRenderStrategy(outWriterMock, mockctRendererCmd);
  }

  @Test
  public void test_getCellTypeConfig_noTypeConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    assertFalse(renderer.getCellTypeConfig(cellRef).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getCellTypeConfig_withTypeConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    IPageTypeConfig typeConfig = createDefaultMock(IPageTypeConfig.class);
    expectCellTypeConfig(cellRef, typeConfig);
    replayDefault();
    assertSame(typeConfig, renderer.getCellTypeConfig(cellRef).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getAsString() {
    String expectedOutput = "blabla";
    expect(outWriterMock.getAsString()).andReturn(expectedOutput);
    replay(outWriterMock);
    assertEquals("asString must return the current state of the StringBuilder (out).",
        expectedOutput, renderer.getAsString());
    verify(outWriterMock);
  }

  @Test
  public void test_startRendering() {
    outWriterMock.clear();
    expectLastCall().once();
    replay(outWriterMock);
    renderer.startRendering();
    verify(outWriterMock);
  }

  @Test
  public void test_endRenderCell() {
    outWriterMock.closeLevel();
    expectLastCall().once();
    replay(outWriterMock);
    renderer.endRenderCell(null, true, false);
    verify(outWriterMock);
  }

  @Test
  public void test_getMenuPart() {
    assertEquals("expecting empty menuPart for cells.", "", renderer.getMenuPart(null));
  }

  @Test
  public void test_isRenderCell() {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    assertFalse(renderer.isRenderCell(null));
    assertTrue(renderer.isRenderCell(new TreeNode(cellRef, null, 0)));
  }

  @Test
  public void test_isRenderSubCells() {
    assertFalse(renderer.isRenderSubCells(null));
    SpaceReference layoutSpaceRef = new SpaceReference("TestLayout", new WikiReference(
        context.getDatabase()));
    assertTrue(renderer.isRenderSubCells(layoutSpaceRef));
  }

  @Test
  public void test_startRenderCell() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cellFN = "Skin.MasterCell";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_noCssClasses() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String idname = "myDivId";
    String cellFN = "Skin.MasterCell";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes("", idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_auto_id_for_null() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("css_styles", cssStyles);
    String idname = "cell:Skin.MasterCell";
    String cellFN = "Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_auto_id_forEmpty() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", "");
    cellObj.setStringValue("css_styles", cssStyles);
    String idname = "cell:Skin.MasterCell";
    String cellFN = "Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_auto_id_forEmpty_diffdb() throws Exception {
    DocumentReference cellRef = new DocumentReference("layoutdb", "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", "");
    cellObj.setStringValue("css_styles", cssStyles);
    String idname = "cell:layoutdb..Skin.MasterCell";
    String cellFN = "layoutdb:Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_id_repetitiveCell() throws Exception {
    getBeanFactory().getBean(Execution.class).getContext()
        .setProperty(EXEC_CTX_KEY_REPETITIVE, true);
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    TreeNode node = new TreeNode(cellRef, null, 0);
    expectNewDoc(cellRef);
    String cellFN = "Skin.MasterCell";
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, false, true);
    assertDefaultAttributes("", "", cellFN, "", capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_otherDb() throws Exception {
    String masterCellDb = "master";
    DocumentReference cellRef = new DocumentReference(masterCellDb, "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cellFN = "master:Skin.MasterCell";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    expectNoCellTypeConfig(cellRef);
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    assertDefaultAttributes(cssClasses, idname, cellFN, cssStyles, capturedAttrList);
    verifyDefault();
  }

  @Test
  public void test_startRenderCell_additionalAttributes() throws Exception {
    String masterCellDb = context.getDatabase();
    DocumentReference cellRef = new DocumentReference(masterCellDb, "Skin", "MasterCell");
    boolean isLastItem = true;
    boolean isFirstItem = false;
    TreeNode node = new TreeNode(cellRef, null, 0);
    XWikiDocument doc = expectNewDoc(cellRef);
    BaseObject cellObj = addCellObj(doc);
    String cssClasses = "classes two";
    String idname = "myDivId";
    String cellFN = "Skin.MasterCell";
    String cssStyles = "width:100px;\nheight:10px;\n";
    cellObj.setStringValue("css_classes", cssClasses);
    cellObj.setStringValue("idname", idname);
    cellObj.setStringValue("css_styles", cssStyles);
    BaseObject attrObj1 = addObj(doc, CellAttributeClass.CLASS_REF);
    attrObj1.setStringValue("name", "custom-x");
    attrObj1.setStringValue("value", "custom velocity x");
    BaseObject attrObj2 = addObj(doc, CellAttributeClass.CLASS_REF);
    attrObj2.setStringValue("name", "custom-y");
    attrObj2.setStringValue("value", "custom velocity y");
    Capture<List<CellAttribute>> capturedAttrList = newCapture();
    outWriterMock.openLevel(isNull(String.class), capture(capturedAttrList));
    IPageTypeConfig typeConfig = expectCellTypeConfig(cellRef, (String) null);
    typeConfig.collectAttributes(isA(AttributeBuilder.class), eq(cellRef));
    expect(getMock(VelocityService.class).evaluateVelocityText("custom velocity x"))
        .andReturn("custom_evaluated_x");
    expect(getMock(VelocityService.class).evaluateVelocityText("custom velocity y"))
        .andReturn("");
    replayDefault();
    renderer.startRenderCell(node, isFirstItem, isLastItem);
    verifyDefault();
    Map<String, CellAttribute> attrMap = assertDefaultAttributes(cssClasses, idname, cellFN,
        cssStyles, capturedAttrList);
    assertTrue(attrMap.containsKey("custom-x"));
    assertTrue(attrMap.get("custom-x").getValue().isPresent());
    assertEquals("custom_evaluated_x", attrMap.get("custom-x").getValue().get());
    assertTrue(attrMap.containsKey("custom-y"));
    assertFalse(attrMap.get("custom-y").getValue().isPresent());
  }

  @Test
  public void test_renderEmptyChildren() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    TreeNode cellNode = new TreeNode(cellRef, null, 0);
    String cellContentExpected = "Cell test content Skin.MasterCell";
    expect(mockctRendererCmd.renderCelementsCell(eq(cellRef))).andReturn(
        cellContentExpected).once();
    // ASSERT
    expect(outWriterMock.appendContent(eq(cellContentExpected))).andReturn(outWriterMock);
    replayDefault();
    renderer.renderEmptyChildren(cellNode);
    verifyDefault();
  }

  @Test
  public void test_getTagName_noCellConfig_fallback_CellType() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    expectNewDoc(cellRef);
    String configName = "TestType";
    expectCellTypeConfig(cellRef, configName);
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(configName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_emptyTagName_fallback_CellType() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    addCellObj(expectNewDoc(cellRef));
    String configName = "TestType";
    expectCellTypeConfig(cellRef, configName);
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(configName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_fromCellConfig() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    BaseObject cellObj = addCellObj(expectNewDoc(cellRef));
    String expectedTagName = "form";
    cellObj.setStringValue(CellClass.FIELD_TAG_NAME.getName(), expectedTagName);
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef);
    assertNotNull(tagName);
    assertTrue(tagName.isPresent());
    assertEquals(expectedTagName, tagName.get());
    verifyDefault();
  }

  @Test
  public void test_getTagName_emptyTagName_fallback_CellType_noTagName() throws Exception {
    DocumentReference cellRef = new DocumentReference(context.getDatabase(), "Skin", "MasterCell");
    expectNewDoc(cellRef);
    expectCellTypeConfig(cellRef, (String) null);
    replayDefault();
    Optional<String> tagName = renderer.getTagName(cellRef);
    assertNotNull(tagName);
    assertFalse(tagName.isPresent());
    verifyDefault();
  }

  private void expectNoCellTypeConfig(DocumentReference cellRef) {
    expectCellTypeConfig(cellRef, (IPageTypeConfig) null);
  }

  private IPageTypeConfig expectCellTypeConfig(DocumentReference cellRef, String configName) {
    IPageTypeConfig typeConfig = createDefaultMock(IPageTypeConfig.class);
    expectCellTypeConfig(cellRef, typeConfig);
    expect(typeConfig.defaultTagName())
        .andReturn(com.google.common.base.Optional.fromNullable(configName));
    return typeConfig;
  }

  private XWikiDocument expectNewDoc(DocumentReference docRef) throws DocumentNotExistsException {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setNew(false);
    expect(getMock(IModelAccessFacade.class).getDocument(docRef)).andReturn(doc).anyTimes();
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(docRef)).andReturn(doc).anyTimes();
    return doc;
  }

  private BaseObject addCellObj(XWikiDocument doc) {
    return addObj(doc, CellClass.CLASS_REF);
  }

  private BaseObject addObj(XWikiDocument doc, ClassReference classRef) {
    BaseObject cellObj = new BaseObject();
    cellObj.setDocumentReference(doc.getDocumentReference());
    cellObj.setXClassReference(classRef.getDocRef(
        doc.getDocumentReference().getWikiReference()));
    doc.addXObject(cellObj);
    return cellObj;
  }

  private void expectCellTypeConfig(DocumentReference cellRef, IPageTypeConfig typeConfig) {
    PageTypeReference cellTypeRef = new PageTypeReference("TestType", "TestTypeComponent",
        Arrays.asList(CellTypeCategory.CELLTYPE_NAME));
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeReferenceWithDefault(eq(cellRef)))
        .andReturn(cellTypeRef).atLeastOnce();
    expect(getMock(IPageTypeRole.class).getPageTypeConfigForPageTypeRef(eq(cellTypeRef)))
        .andReturn(typeConfig).atLeastOnce();
  }

  private Map<String, CellAttribute> assertDefaultAttributes(String cssClasses, String idname,
      String cellFN, String cssStyles, Capture<List<CellAttribute>> capturedAttrList) {
    List<CellAttribute> attrList = capturedAttrList.getValue();
    assertNotNull(attrList);
    assertFalse(attrList.isEmpty());
    Map<String, CellAttribute> attrMap = new HashMap<>();
    for (CellAttribute attr : attrList) {
      attrMap.put(attr.getName(), attr);
    }
    assertEquals(!idname.isEmpty(), attrMap.containsKey("id"));
    if (attrMap.containsKey("id")) {
      assertEquals("wrong id attribute", idname, attrMap.get("id").getValue().orElse(""));
    }
    assertTrue("cell-ref attribute not found", attrMap.containsKey("data-cell-ref"));
    assertEquals("wrong cell-ref attribute", cellFN,
        attrMap.get("data-cell-ref").getValue().get());
    assertTrue("cssClass attribute not found", attrMap.containsKey("class"));
    assertEquals("wrong cssClass attribute", ("cel_cell " + cssClasses).trim(),
        attrMap.get("class").getValue().get());
    assertEquals(!cssStyles.isEmpty(), attrMap.containsKey("style"));
    if (attrMap.containsKey("style")) {
      assertEquals("wrong styles attribute", cssStyles.replaceAll("[\n\r]", ""),
          attrMap.get("style").getValue().orElse(""));
    }
    return attrMap;
  }

}
