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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class PageLayoutCommandTest extends AbstractBridgedComponentTestCase{

  private XWikiContext context;
  private XWiki xwiki;
  private PageLayoutCommand plCmd;
  private XWikiStoreInterface storeMock;

  @Before
  public void setUp_PageLayoutCommandTest() throws Exception {
    context = getContext();
    context.setAction("view");
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    storeMock = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    plCmd = new PageLayoutCommand();
  }

  @Test
  public void testNoInitQueryManagerOnCreation() {
    assertNull("do not use Utils.getComponentManager on object creation."
        + " It may fail application startup!", new PageLayoutCommand().queryManager);
  }

  @Test
  public void testGetPageLayoutHQL() {
    assertEquals("select doc.space, pl.prettyname"
        + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl"
        + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'"
        + " and pl.id.id=obj.id"
        + " order by pl.prettyname asc",
        plCmd.getPageLayoutHQL(false));
  }

  @Test
  public void testGetPageLayoutHQL_onlyActive() {
    assertEquals("select doc.space, pl.prettyname"
        + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl"
        + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'"
        + " and pl.id.id=obj.id"
        + " and pl.isActive = 1"
        + " order by pl.prettyname asc",
        plCmd.getPageLayoutHQL(true));
  }

  @Test
  public void testExtendToFullName() {
    assertEquals("Test.name", plCmd.extendToFullName("Test", "name"));
    assertEquals("Test.name", plCmd.extendToFullName("Test2", "Test.name"));
  }

  @Test
  public void testGetCelLayoutEditorSpaceRef() {
    SpaceReference celLayoutEditorSpaceRef = new SpaceReference(
        PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, new WikiReference(
            context.getDatabase()));
    assertEquals(celLayoutEditorSpaceRef , plCmd.getCelLayoutEditorSpaceRef());
  }

  @Test
  public void testGetAllPageLayouts() throws Exception {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[] {"layout1Space","Layout 1 pretty name"});
    resultList.add(new Object[]  {"layout2Space","Layout 2 pretty name"});
    Capture<String> capturedHQL = new Capture<String>();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replayAll();
    Map<String, String> expectedPLmap = new HashMap<String, String>();
    expectedPLmap.put("layout1Space","Layout 1 pretty name");
    expectedPLmap.put("layout2Space","Layout 2 pretty name");
    assertEquals(expectedPLmap , plCmd.getAllPageLayouts());
    assertFalse("hql must not contain isActiv constrains.", capturedHQL.getValue(
        ).contains("pl.isActive"));
    verifyAll();
  }

  @Test
  public void testGetActivePageLyouts() throws Exception {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[] {"layout1Space","Layout 1 pretty name"});
    resultList.add(new Object[]  {"layout2Space","Layout 2 pretty name"});
    Capture<String> capturedHQL = new Capture<String>();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replayAll();
    Map<String, String> expectedPLmap = new HashMap<String, String>();
    expectedPLmap.put("layout1Space","Layout 1 pretty name");
    expectedPLmap.put("layout2Space","Layout 2 pretty name");
    assertEquals(expectedPLmap , plCmd.getActivePageLyouts());
    assertTrue("hql must contain isActiv constrains.", capturedHQL.getValue(
      ).contains("pl.isActive"));
    verifyAll();
  }

  @Test
  public void testCheckLayoutAccess_localLayout_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference(
        context.getDatabase()));
    replayAll();
    assertTrue("local db always", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyAll();
  }

  @Test
  public void testCheckLayoutAccess_celements2web_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("celements2web", new WikiReference(
        context.getDatabase()));
    replayAll();
    assertTrue("celements2web always", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyAll();
  }

  @Test
  public void testCheckLayoutAccess_noLocalLayout_never() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference(
        "someDB"));
    replayAll();
    assertFalse("someDB never", plCmd.checkLayoutAccess(layoutSpaceRef));
    verifyAll();
  }

  @Test
  public void testGetPageLayoutForDoc_centralPageLayout() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    String layoutName = "MyPageLayout";
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())).andReturn(
        layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(
        new XWikiDocument(webHomeDocRef));
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), layoutName,
        "WebHome")), same(context))).andReturn(false);
    DocumentReference centralLayoutPropDocRef = new DocumentReference("celements2web",
        layoutName, "WebHome");
    expect(xwiki.exists(eq(centralLayoutPropDocRef), same(context))).andReturn(true);
    XWikiDocument centralLayoutPropDoc = new XWikiDocument(centralLayoutPropDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(centralPagePropClassRef);
    centralLayoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(centralLayoutPropDocRef), same(context))).andReturn(
        centralLayoutPropDoc);
    replayAll(injectedInheritorFactory, inheritor);
    SpaceReference pageLayoutForDoc = plCmd.getPageLayoutForDoc(docRef);
    assertNotNull(pageLayoutForDoc);
    SpaceReference centralLayoutRef = new SpaceReference(layoutName, new WikiReference(
        "celements2web"));
    assertEquals(centralLayoutRef, pageLayoutForDoc);
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_noPageLayout_defined() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    replayAll(injectedInheritorFactory, inheritor);
    assertNull(plCmd.getPageLayoutForDoc(fullName, context));
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_noPageLayout_defaultAvailable() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    DocumentReference centralSimpleLayoutDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "WebHome");
    expect(xwiki.exists(eq(centralSimpleLayoutDocRef), same(context))).andReturn(true);
    XWikiDocument layoutPropDoc = new XWikiDocument(centralSimpleLayoutDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(centralSimpleLayoutDocRef), same(context))).andReturn(
        layoutPropDoc).once();
    replayAll(injectedInheritorFactory, inheritor);
    SpaceReference simpleLayoutSpaceRef = new SpaceReference("SimpleLayout",
        new WikiReference("celements2web"));
    assertEquals(simpleLayoutSpaceRef, plCmd.getPageLayoutForDoc(myDocRef));
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_noLayoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())).andReturn(
        layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), layoutName,
        "WebHome")), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", layoutName, "WebHome")),
        same(context))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    replayAll(injectedInheritorFactory, inheritor);
    assertNull(plCmd.getPageLayoutForDoc(fullName, context));
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_noLayoutSpace_central() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "celements2web:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())
      ).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    DocumentReference layoutPropDocRef = new DocumentReference("celements2web",
        "MyPageLayout", "WebHome");
    expect(xwiki.exists(eq(layoutPropDocRef), same(context))).andReturn(true);
    XWikiDocument layoutPropDoc = new XWikiDocument(layoutPropDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(layoutPropDocRef), same(context))).andReturn(
        layoutPropDoc).once();
    replayAll(injectedInheritorFactory, inheritor);
    assertEquals(layoutName, plCmd.getPageLayoutForDoc(fullName, context));
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_noLayoutSpace_noAccess_someDB() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "someDB:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())
      ).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    DocumentReference layoutPropDocRef = new DocumentReference("someDB",
        "MyPageLayout", "WebHome");
    expect(xwiki.exists(eq(layoutPropDocRef), same(context))).andReturn(true);
    XWikiDocument layoutPropDoc = new XWikiDocument(layoutPropDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference("someDB",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(layoutPropDocRef), same(context))).andReturn(
        layoutPropDoc).once();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    replayAll(injectedInheritorFactory, inheritor);
    assertNull("no access to someDB:MyPageLayout", plCmd.getPageLayoutForDoc(
        new DocumentReference(context.getDatabase(), "mySpace", "MyDocName")));
    verifyAll(injectedInheritorFactory, inheritor);
  }

  @Test
  public void testGetPageLayoutForDoc_layoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "MyPageLayout.MyDocName";
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "MyPageLayout", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true);
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(
        context.getDatabase(), "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(true);
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(layoutEditorPropDocRef);
    BaseObject layoutEditorPropObj = new BaseObject();
    layoutEditorPropObj.setXClassReference(pagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(xwiki.getDocument(eq(layoutEditorPropDocRef), same(context))).andReturn(
        layoutEditorPropDoc);
    replayAll(injectedInheritorFactory);
    assertEquals("CelLayoutEditor", plCmd.getPageLayoutForDoc(fullName, context));
    verifyAll(injectedInheritorFactory);
  }

  @Test
  public void testGetPageLayoutForDoc_layoutSpace_centralLayoutEditor() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "MyPageLayout.MyDocName";
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "MyPageLayout", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true);
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(
        context.getDatabase(), "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(false);
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference(
        "celements2web", "CelLayoutEditor", "WebHome");
    expect(xwiki.exists(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(true
        );
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(centralLayoutEditorPropDocRef);
    BaseObject layoutEditorPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutEditorPropObj.setXClassReference(centralPagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(xwiki.getDocument(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(
        layoutEditorPropDoc);
    replayAll(injectedInheritorFactory);
    assertEquals("celements2web:CelLayoutEditor", plCmd.getPageLayoutForDoc(fullName,
        context));
    verifyAll(injectedInheritorFactory);
  }

  @Test
  public void testGetLayoutPropDoc() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "WebHome");
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(false);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc
      ).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(
        mySpacePrefDoc).atLeastOnce();
    DocumentReference xWikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument xWikiPrefDoc = new XWikiDocument(xWikiPrefDocRef);
    expect(xwiki.getDocument(eq(xWikiPrefDocRef), same(context))).andReturn(
        xWikiPrefDoc).atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expect(xwiki.exists(eq(new DocumentReference(context.getDatabase(), "SimpleLayout",
        "WebHome")), same(context))).andReturn(false);
    expect(xwiki.exists(eq(new DocumentReference("celements2web", "SimpleLayout",
      "WebHome")), same(context))).andReturn(false);
    replayAll();
    assertNull(plCmd.getLayoutPropDoc());
    verifyAll();
  }

  @Test
  public void testDeleteLayout() throws Exception {
    QueryManager queryManagerMock = createMock(QueryManager.class);
    plCmd.queryManager = queryManagerMock;
    String layoutName = "delLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    Query queryMock = createMock(Query.class);
    expect(queryManagerMock.createQuery(eq("where doc.space = :space"), eq(Query.XWQL))
        ).andReturn(queryMock).once();
    expect(queryMock.bindValue(eq("space"), eq(layoutSpaceRef.getName()))).andReturn(
        queryMock).once();
    List<String> resultList = Arrays.asList(layoutName + ".myCell", layoutName
        + ".WebHome");
    expect(queryMock.<String>execute()).andReturn(resultList).once();
    DocumentReference myCellDocRef = new DocumentReference(context.getDatabase(),
        layoutName, "myCell");
    XWikiDocument myCellDoc = new XWikiDocument(myCellDocRef);
    expect(xwiki.getDocument(eq(myCellDocRef), same(context))).andReturn(myCellDoc
        ).once();
    xwiki.deleteAllDocuments(same(myCellDoc), same(context));
    expectLastCall().once();
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(),
        layoutName, "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(webHomeDoc
        ).once();
    xwiki.deleteAllDocuments(same(webHomeDoc), same(context));
    expectLastCall().once();
    replayAll(queryManagerMock, queryMock);
    assertTrue(plCmd.deleteLayout(layoutSpaceRef));
    verifyAll(queryManagerMock, queryMock);
  }

  @Test
  public void testGetDefaultLayout_default() {
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayAll();
    assertEquals("TestLayout", plCmd.getDefaultLayout());
    verifyAll();
  }

  @Test
  public void testGetDefaultLayout_null() {
    context.setAction(null);
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayAll();
    assertEquals("TestLayout", plCmd.getDefaultLayout());
    verifyAll();
  }

  @Test
  public void testGetDefaultLayout_login() {
    context.setAction("login");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn(
            "LoginLayout");
    replayAll();
    assertEquals("LoginLayout", plCmd.getDefaultLayout());
    verifyAll();
  }

  @Test
  public void testGetDefaultLayout_edit() {
    context.setAction("edit");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        PageLayoutCommand.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(PageLayoutCommand.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn(
            "EditLayout");
    replayAll();
    assertEquals("EditLayout", plCmd.getDefaultLayout());
    verifyAll();
  }

  @Test
  public void testLayoutEditorAvailable_local() throws Exception {
    DocumentReference layoutEditorPropDocRef = new DocumentReference(
        context.getDatabase(), PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(true
        ).once();
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(layoutEditorPropDocRef);
    expect(xwiki.getDocument(eq(layoutEditorPropDocRef), same(context))).andReturn(
        layoutEditorPropDoc).once();
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference localPagePropClassRef = new DocumentReference(
        context.getDatabase(), PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(localPagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutPropObj);
    replayAll();
    assertTrue(plCmd.layoutEditorAvailable());
    verifyAll();
  }

  @Test
  public void testLayoutEditorAvailable_central() throws Exception {
    DocumentReference layoutEditorPropDocRef = new DocumentReference(
        context.getDatabase(), PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(false
        ).once();
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference(
        "celements2web", PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, "WebHome");
    expect(xwiki.exists(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(true
        ).once();
    XWikiDocument centralLayoutEditorPropDoc = new XWikiDocument(
        centralLayoutEditorPropDocRef);
    expect(xwiki.getDocument(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(
        centralLayoutEditorPropDoc).once();
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = new DocumentReference("celements2web",
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        PageLayoutCommand.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(centralPagePropClassRef);
    centralLayoutEditorPropDoc.addXObject(layoutPropObj);
    replayAll();
    assertTrue(plCmd.layoutEditorAvailable());
    verifyAll();
  }

  @Test
  public void testLayoutEditorAvailable_notavailable() throws Exception {
    DocumentReference layoutEditorPropDocRef = new DocumentReference(
        context.getDatabase(), PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, "WebHome");
    expect(xwiki.exists(eq(layoutEditorPropDocRef), same(context))).andReturn(false
        ).once();
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference(
        "celements2web", PageLayoutCommand.CEL_LAYOUT_EDITOR_PL_NAME, "WebHome");
    expect(xwiki.exists(eq(centralLayoutEditorPropDocRef), same(context))).andReturn(false
        ).once();
    replayAll();
    assertFalse(plCmd.layoutEditorAvailable());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, storeMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, storeMock);
    verify(mocks);
  }

}
