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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.cells.HtmlDoctype;
import com.celements.cells.ICellsClassConfig;
import com.celements.common.test.AbstractComponentTest;
import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class PageLayoutCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private PageLayoutCommand plCmd;
  private XWikiStoreInterface storeMock;

  @Before
  public void prepareTest() throws Exception {
    context = getContext();
    context.setAction("view");
    xwiki = getWikiMock();
    storeMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    plCmd = new PageLayoutCommand();
  }

  @Test
  public void test_noInitQueryManagerOnCreation() {
    assertNull("do not use Utils.getComponentManager on object creation."
        + " It may fail application startup!", new PageLayoutCommand().queryManager);
  }

  @Test
  public void test_getPageLayoutHQL() {
    assertEquals("select doc.space, pl.prettyname" + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl" + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'" + " and pl.id.id=obj.id"
        + " order by pl.prettyname asc", plCmd.getPageLayoutHQL(false));
  }

  @Test
  public void test_getPageLayoutHQL_onlyActive() {
    assertEquals("select doc.space, pl.prettyname" + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl" + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'" + " and pl.id.id=obj.id"
        + " and pl.isActive = 1" + " order by pl.prettyname asc", plCmd.getPageLayoutHQL(true));
  }

  @Test
  public void testExtendToFullName() {
    assertEquals("Test.name", plCmd.extendToFullName("Test", "name"));
    assertEquals("Test.name", plCmd.extendToFullName("Test2", "Test.name"));
  }

  @Test
  public void test_getCelLayoutEditorSpaceRef() {
    SpaceReference celLayoutEditorSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, new WikiReference(context.getDatabase()));
    assertEquals(celLayoutEditorSpaceRef, plCmd.getCelLayoutEditorSpaceRef());
  }

  @Test
  public void test_getAllPageLayouts() throws Exception {
    List<Object> resultList = new ArrayList<>();
    resultList.add(new Object[] { "layout1Space", "Layout 1 pretty name" });
    resultList.add(new Object[] { "layout2Space", "Layout 2 pretty name" });
    Capture<String> capturedHQL = newCapture();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replayDefault();
    Map<String, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put("layout1Space", "Layout 1 pretty name");
    expectedPLmap.put("layout2Space", "Layout 2 pretty name");
    assertEquals(expectedPLmap, plCmd.getAllPageLayouts());
    assertFalse("hql must not contain isActiv constrains.", capturedHQL.getValue().contains(
        "pl.isActive"));
    verifyDefault();
  }

  @Test
  public void test_getActivePageLyouts() throws Exception {
    List<Object> resultList = new ArrayList<>();
    resultList.add(new Object[] { "layout1Space", "Layout 1 pretty name" });
    resultList.add(new Object[] { "layout2Space", "Layout 2 pretty name" });
    Capture<String> capturedHQL = newCapture();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replayDefault();
    Map<String, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put("layout1Space", "Layout 1 pretty name");
    expectedPLmap.put("layout2Space", "Layout 2 pretty name");
    assertEquals(expectedPLmap, plCmd.getActivePageLyouts());
    assertTrue("hql must contain isActiv constrains.", capturedHQL.getValue().contains(
        "pl.isActive"));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_localLayout_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference(
        context.getDatabase()));
    replayDefault();
    assertTrue("local db always", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_celements2web_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("celements2web", new WikiReference(
        context.getDatabase()));
    replayDefault();
    assertTrue("celements2web always", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_noLocalLayout_never() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    replayDefault();
    assertFalse("someDB never", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_null() throws Exception {
    replayDefault();
    assertNull(plCmd.getPageLayoutForDoc(null));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_centralPageLayout() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDocName");
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    String layoutName = "MyPageLayout";
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(new XWikiDocument(
        webHomeDocRef));
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName,
        plCmd.getCentralWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault(injectedInheritorFactory, inheritor);
    SpaceReference pageLayoutForDoc = plCmd.getPageLayoutForDoc(docRef);
    assertNotNull(pageLayoutForDoc);
    assertEquals(centralLayoutSpaceRef, pageLayoutForDoc);
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defined() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, plCmd.getCentralWikiRef()), false);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull(plCmd.getPageLayoutForDoc(fullName, context));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defaultAvailable() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName,
        plCmd.getCentralWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertEquals(centralLayoutSpaceRef, plCmd.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", plCmd.getCentralWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, plCmd.getCentralWikiRef()), false);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull(plCmd.getPageLayoutForDoc(fullName, context));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_central() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "celements2web:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expectLayoutDoc(new SpaceReference("MyPageLayout", plCmd.getCentralWikiRef()), true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertEquals(layoutName, plCmd.getPageLayoutForDoc(fullName, context));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_noAccess_someDB() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "someDB:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", plCmd.getCentralWikiRef()), false);
    expectLayoutDoc(new SpaceReference("MyPageLayout", new WikiReference("someDB")), true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull("no access to someDB:MyPageLayout", plCmd.getPageLayoutForDoc(new DocumentReference(
        context.getDatabase(), "mySpace", "MyDocName")));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "MyPageLayout.MyDocName";
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(layoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    layoutEditorPropObj.setXClassReference(pagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(xwiki.getDocument(eq(layoutEditorPropDocRef), same(context))).andReturn(
        layoutEditorPropDoc);
    replayDefault(injectedInheritorFactory);
    assertEquals("CelLayoutEditor", plCmd.getPageLayoutForDoc(fullName, context));
    verifyDefault(injectedInheritorFactory);
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace_centralLayoutEditor() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "MyPageLayout.MyDocName";
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(false);
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference("celements2web",
        "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(
        true).atLeastOnce();
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(centralLayoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutEditorPropObj.setXClassReference(centralPagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(xwiki.getDocument(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(
        layoutEditorPropDoc);
    replayDefault(injectedInheritorFactory);
    assertEquals("celements2web:CelLayoutEditor", plCmd.getPageLayoutForDoc(fullName, context));
    verifyDefault(injectedInheritorFactory);
  }

  @Test
  public void test_getLayoutPropDoc() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(
        mySpacePrefDoc).atLeastOnce();
    DocumentReference xWikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xWikiPrefDoc = new XWikiDocument(xWikiPrefDocRef);
    expect(xwiki.getDocument(eq(xWikiPrefDocRef), same(context))).andReturn(
        xWikiPrefDoc).atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout", "WebHome")),
        same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", "SimpleLayout", "WebHome")), same(
        context))).andReturn(false);
    replayDefault();
    assertNull(plCmd.getLayoutPropDoc());
    verifyDefault();
  }

  @Test
  public void test_deleteLayout() throws Exception {
    QueryManager queryManagerMock = createMock(QueryManager.class);
    plCmd.queryManager = queryManagerMock;
    String layoutName = "delLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    Query queryMock = createMock(Query.class);
    expect(queryManagerMock.createQuery(eq("where doc.space = :space"), eq(Query.XWQL))).andReturn(
        queryMock).once();
    expect(queryMock.bindValue(eq("space"), eq(layoutSpaceRef.getName()))).andReturn(
        queryMock).once();
    List<String> resultList = Arrays.asList(layoutName + ".myCell", layoutName + ".WebHome");
    expect(queryMock.<String>execute()).andReturn(resultList).once();
    DocumentReference myCellDocRef = new DocumentReference(context.getDatabase(), layoutName,
        "myCell");
    XWikiDocument myCellDoc = new XWikiDocument(myCellDocRef);
    myCellDoc.setNew(false);
    myCellDoc.setStore(storeMock);
    expect(xwiki.exists(eq(myCellDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(myCellDocRef), same(context))).andReturn(myCellDoc).atLeastOnce();
    xwiki.deleteDocument(same(myCellDoc), eq(true), same(context));
    expectLastCall().once();
    expect(storeMock.getTranslationList(same(myCellDoc), same(context))).andReturn(
        Collections.<String>emptyList()).once();
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), layoutName,
        "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    webHomeDoc.setNew(false);
    webHomeDoc.setStore(storeMock);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(webHomeDoc).atLeastOnce();
    xwiki.deleteDocument(same(webHomeDoc), eq(true), same(context));
    expectLastCall().once();
    expect(storeMock.getTranslationList(same(webHomeDoc), same(context))).andReturn(
        Collections.<String>emptyList()).once();
    replayDefault(queryManagerMock, queryMock);
    assertTrue(plCmd.deleteLayout(layoutSpaceRef));
    verifyDefault(queryManagerMock, queryMock);
  }

  @Test
  public void test_getDefaultLayout_default() {
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayDefault();
    assertEquals("TestLayout", plCmd.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_null() {
    context.setAction(null);
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayDefault();
    assertEquals("TestLayout", plCmd.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_login() {
    context.setAction("login");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn("LoginLayout");
    replayDefault();
    assertEquals("LoginLayout", plCmd.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_edit() {
    context.setAction("edit");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn("EditLayout");
    replayDefault();
    assertEquals("EditLayout", plCmd.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_layoutEditorAvailable_local() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, true);
    replayDefault();
    assertTrue(plCmd.layoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_layoutEditorAvailable_central() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, plCmd.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, true);
    replayDefault();
    assertTrue(plCmd.layoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_layoutEditorAvailable_notavailable() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, plCmd.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, false);
    replayDefault();
    assertFalse(plCmd.layoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_resolveValidLayoutSpace_exists() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = plCmd.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertEquals(ret, layoutSpaceRef);
  }

  @Test
  public void test_resolveValidLayoutSpace_exists_central() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, plCmd.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = plCmd.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertEquals(ret, centralLayoutSpaceRef);
  }

  @Test
  public void test_resolveValidLayoutSpace_otherDB() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, new WikiReference("external"));
    expectLayoutDoc(layoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = plCmd.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void test_resolveValidLayoutSpace_absent() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expect(xwiki.exists(eq(plCmd.standardPropDocRef(layoutSpaceRef)), same(context)))
        .andReturn(false).atLeastOnce();
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, plCmd.getCentralWikiRef());
    expect(xwiki.exists(eq(plCmd.standardPropDocRef(centralLayoutSpaceRef)), same(context)))
        .andReturn(false).atLeastOnce();

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
  public void test_getHTMLType_XHTML_default() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML_default_empty() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, "");
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML_default_null() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, null);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_HTML5() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD,
        HtmlDoctype.HTML5.getValue());
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.HTML5, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD,
        HtmlDoctype.XHTML.getValue());
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, plCmd.getHTMLType(layoutRef));
    verifyDefault();
  }

  private XWikiDocument expectLayoutDoc(SpaceReference layoutSpaceRef,
      boolean exists) throws XWikiException {
    return expectLayoutDoc(layoutSpaceRef, exists, true);
  }

  private XWikiDocument expectLayoutDoc(SpaceReference layoutSpaceRef,
      boolean exists, boolean withObject) throws XWikiException {
    DocumentReference layoutDocRef = plCmd.standardPropDocRef(layoutSpaceRef);
    expect(xwiki.exists(eq(layoutDocRef), same(context))).andReturn(exists).anyTimes();
    XWikiDocument layoutDoc = new XWikiDocument(layoutDocRef);
    layoutDoc.setNew(!exists);
    if (exists) {
      expect(xwiki.getDocument(eq(layoutDocRef), same(context))).andReturn(layoutDoc).once();
      if (withObject) {
        BaseObject layoutPropObj = new BaseObject();
        layoutPropObj.setXClassReference(plCmd.getPageLayoutPropertiesClassRef(
            (WikiReference) layoutSpaceRef.getParent()));
        layoutDoc.addXObject(layoutPropObj);
      }
    }
    return layoutDoc;
  }

  private WikiReference getWikiRef() {
    return new WikiReference(getContext().getDatabase());
  }

}
