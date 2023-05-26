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
package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.HtmlDoctype;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.CelConstant;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

@Deprecated
public class PageLayoutCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiStoreInterface storeMock;
  private WikiReference centralWikiRef;
  private IModelAccessFacade modelAccessMock;
  private LayoutServiceRole layoutServiceMock;

  private PageLayoutCommand plCmd;

  @Before
  public void prepareTest() throws Exception {
    centralWikiRef = RefBuilder.create().wiki(CelConstant.CENTRAL_WIKI_NAME)
        .build(WikiReference.class);
    context = getContext();
    context.setAction("view");
    xwiki = getWikiMock();
    layoutServiceMock = registerComponentMock(LayoutServiceRole.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    storeMock = createDefaultMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    plCmd = new PageLayoutCommand();
  }

  @Test
  public void test_getAllPageLayouts() throws Exception {
    Map<SpaceReference, String> resultMap = new HashMap<>();
    resultMap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout1Space")
        .build(SpaceReference.class), "Layout 1 pretty name");
    resultMap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout2Space")
        .build(SpaceReference.class), "Layout 2 pretty name");
    expect(layoutServiceMock.getAllPageLayouts()).andReturn(resultMap);
    replayDefault();
    Map<String, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put("layout1Space", "Layout 1 pretty name");
    expectedPLmap.put("layout2Space", "Layout 2 pretty name");
    assertEquals(expectedPLmap, plCmd.getAllPageLayouts());
    verifyDefault();
  }

  @Test
  public void test_getActivePageLyouts() throws Exception {
    Map<SpaceReference, String> resultMap = new HashMap<>();
    resultMap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout1Space")
        .build(SpaceReference.class), "Layout 1 pretty name");
    resultMap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout2Space")
        .build(SpaceReference.class), "Layout 2 pretty name");
    expect(layoutServiceMock.getActivePageLayouts()).andReturn(resultMap);
    replayDefault();
    Map<String, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put("layout1Space", "Layout 1 pretty name");
    expectedPLmap.put("layout2Space", "Layout 2 pretty name");
    assertEquals(expectedPLmap, plCmd.getActivePageLyouts());
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_proxyCheck() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    expect(layoutServiceMock.checkLayoutAccess(eq(layoutSpaceRef))).andReturn(false);
    replayDefault();
    assertFalse("someDB never", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_null() throws Exception {
    expect(layoutServiceMock.getPageLayoutForDoc(isNull(DocumentReference.class))).andReturn(null);
    replayDefault();
    assertNull(plCmd.getPageLayoutForDoc(null));
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_absent() throws Exception {
    expect(layoutServiceMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.empty());
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc());
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_proxyCheck() throws Exception {
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(layoutServiceMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        webHomeDocRef));
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(webHomeDoc);
    replayDefault();
    assertSame(webHomeDoc, plCmd.getLayoutPropDoc());
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_proxyCheck_DocNotExist() throws Exception {
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(layoutServiceMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        webHomeDocRef));
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).anyTimes();
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc());
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_SpaceReference_absent() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    expect(layoutServiceMock.getLayoutPropDocRef(layoutSpaceRef)).andReturn(Optional.empty());
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_SpaceReference_proxyCheck() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(layoutServiceMock.getLayoutPropDocRef(layoutSpaceRef))
        .andReturn(Optional.of(webHomeDocRef));
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(webHomeDoc);
    expect(layoutServiceMock.getLayoutPropertyObj(layoutSpaceRef)).andReturn(
        Optional.of(new BaseObject()));
    replayDefault();
    assertSame(webHomeDoc, plCmd.getLayoutPropDoc(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_SpaceReference_noPropObject() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(layoutServiceMock.getLayoutPropDocRef(layoutSpaceRef))
        .andReturn(Optional.of(webHomeDocRef));
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(webHomeDoc);
    expect(layoutServiceMock.getLayoutPropertyObj(layoutSpaceRef)).andReturn(null);
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDoc_SpaceReference_proxyCheck_DocNotExist() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(layoutServiceMock.getLayoutPropDocRef(layoutSpaceRef))
        .andReturn(Optional.of(webHomeDocRef));
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).anyTimes();
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_deleteLayout() throws Exception {
    String layoutName = "delLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.deleteLayout(eq(layoutSpaceRef))).andReturn(true);
    replayDefault();
    assertTrue(plCmd.deleteLayout(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_layoutEditorAvailable_local() throws Exception {
    expect(layoutServiceMock.isLayoutEditorAvailable()).andReturn(true);
    replayDefault();
    assertTrue(plCmd.layoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_layoutEditorAvailable_notavailable() throws Exception {
    expect(layoutServiceMock.isLayoutEditorAvailable()).andReturn(false);
    replayDefault();
    assertFalse(plCmd.layoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_canRenderLayout_true() throws Exception {
    String layoutName = "TestLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.canRenderLayout(layoutSpaceRef)).andReturn(true);
    replayDefault();
    assertTrue(plCmd.canRenderLayout(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_canRenderLayout_false() throws Exception {
    String layoutName = "TestLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.canRenderLayout(layoutSpaceRef)).andReturn(false);
    replayDefault();
    assertFalse(plCmd.canRenderLayout(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_layoutExists_true() throws Exception {
    String layoutName = "TestLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.existsLayout(layoutSpaceRef)).andReturn(true);
    replayDefault();
    assertTrue(plCmd.layoutExists(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_layoutExists_false() throws Exception {
    String layoutName = "TestLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.existsLayout(layoutSpaceRef)).andReturn(false);
    replayDefault();
    assertFalse(plCmd.layoutExists(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_createNew_proxyCheck() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    @NotNull
    boolean response = true;
    expect(layoutServiceMock.createLayout(eq(layoutSpaceRef))).andReturn(response);
    replayDefault();
    String ret = plCmd.createNew(layoutSpaceRef);
    verifyDefault();

    assertEquals(ret, "cel_layout_create_successful");
  }

  @Test
  public void test_getLayoutPropertyObj_proxyCheck() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    BaseObject propObj = new BaseObject();
    expect(layoutServiceMock.getLayoutPropertyObj(eq(layoutSpaceRef))).andReturn(Optional.of(
        propObj));
    replayDefault();
    BaseObject ret = plCmd.getLayoutPropertyObj(layoutSpaceRef);
    verifyDefault();

    assertSame(ret, propObj);
  }

  @Test
  public void test_resolveValidLayoutSpace_proxyCheck() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, centralWikiRef);
    expect(layoutServiceMock.resolveValidLayoutSpace(eq(layoutSpaceRef)))
        .andReturn(Optional.of(centralLayoutSpaceRef));
    replayDefault();
    SpaceReference ret = plCmd.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertEquals(ret, centralLayoutSpaceRef);
  }

  @Test
  public void test_resolveValidLayoutSpace_absent() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expect(layoutServiceMock.resolveValidLayoutSpace(eq(layoutSpaceRef)))
        .andReturn(Optional.empty());
    replayDefault();
    SpaceReference ret = plCmd.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void test_standardPropDocRef() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    DocumentReference ret = plCmd.standardPropDocRef(layoutSpaceRef);
    assertNotNull(ret);
    assertEquals(layoutSpaceRef, ret.getParent());
    assertEquals("WebHome", ret.getName());
  }

  @Test
  public void test_getHTMLType_XHTML_proxyCheck() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.getHTMLType(eq(layoutRef))).andReturn(HtmlDoctype.XHTML);
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_HTML5_proxyCheck() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    expect(layoutServiceMock.getHTMLType(eq(layoutRef))).andReturn(HtmlDoctype.HTML5);
    replayDefault();
    assertEquals(HtmlDoctype.HTML5, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  private WikiReference getWikiRef() {
    return new WikiReference(getContext().getDatabase());
  }

}
