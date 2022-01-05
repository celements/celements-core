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
package com.celements.pagelayout;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.IPageTypeClassConfig;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class DefaultLayoutServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiStoreInterface storeMock;
  private QueryManager queryManagerMock;

  private DefaultLayoutService layoutService;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    context = getContext();
    context.setAction("view");
    xwiki = getWikiMock();
    storeMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    queryManagerMock = registerComponentMock(QueryManager.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    layoutService = (DefaultLayoutService) Utils.getComponent(LayoutServiceRole.class);
  }

  @Test
  public void test_getPageLayoutHQL() {
    assertEquals("select doc.space, pl.prettyname" + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl" + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'" + " and pl.id.id=obj.id"
        + " order by pl.prettyname asc", layoutService.getPageLayoutHQL(false));
  }

  @Test
  public void test_getPageLayoutHQL_onlyActive() {
    assertEquals("select doc.space, pl.prettyname" + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl" + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'" + " and pl.id.id=obj.id"
        + " and pl.isActive = 1" + " order by pl.prettyname asc",
        layoutService.getPageLayoutHQL(true));
  }

  @Test
  public void test_getCelLayoutEditorSpaceRef() {
    SpaceReference celLayoutEditorSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, new WikiReference(context.getDatabase()));
    assertEquals(celLayoutEditorSpaceRef, layoutService.getCelLayoutEditorSpaceRef());
  }

  @Test
  public void test_getAllPageLayouts() throws Exception {
    List<Object> resultList = new ArrayList<>();
    resultList.add(new String[] { "layout1Space", "Layout 1 pretty name" });
    resultList.add(new String[] { "layout2Space", "Layout 2 pretty name" });
    Query queryMock = createMockAndAddToDefault(Query.class);
    Capture<String> capturedHQL = newCapture();
    expect(queryManagerMock.createQuery(capture(capturedHQL), eq(Query.HQL))).andReturn(queryMock);
    expect(queryMock.execute()).andReturn(resultList);
    replayDefault();
    Map<SpaceReference, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout1Space")
        .build(SpaceReference.class), "Layout 1 pretty name");
    expectedPLmap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout2Space")
        .build(SpaceReference.class), "Layout 2 pretty name");
    assertEquals(expectedPLmap, layoutService.getAllPageLayouts());
    assertFalse("hql must not contain isActiv constrains.", capturedHQL.getValue().contains(
        "pl.isActive"));
    verifyDefault();
  }

  @Test
  public void test_getActivePageLyouts() throws Exception {
    List<Object> resultList = new ArrayList<>();
    resultList.add(new String[] { "layout1Space", "Layout 1 pretty name" });
    resultList.add(new String[] { "layout2Space", "Layout 2 pretty name" });
    Query queryMock = createMockAndAddToDefault(Query.class);
    Capture<String> capturedHQL = newCapture();
    expect(queryManagerMock.createQuery(capture(capturedHQL), eq(Query.HQL))).andReturn(queryMock);
    expect(queryMock.execute()).andReturn(resultList);
    replayDefault();
    Map<SpaceReference, String> expectedPLmap = new HashMap<>();
    expectedPLmap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout1Space")
        .build(SpaceReference.class), "Layout 1 pretty name");
    expectedPLmap.put(RefBuilder.create().wiki(context.getDatabase()).space("layout2Space")
        .build(SpaceReference.class), "Layout 2 pretty name");
    assertEquals(expectedPLmap, layoutService.getActivePageLayouts());
    assertTrue("hql must contain isActiv constrains.", capturedHQL.getValue().contains(
        "pl.isActive"));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_localLayout_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference(
        context.getDatabase()));
    replayDefault();
    assertTrue("local db always", layoutService.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_celements2web_always() {
    SpaceReference layoutSpaceRef = new SpaceReference("celements2web", new WikiReference(
        context.getDatabase()));
    replayDefault();
    assertTrue("celements2web always", layoutService.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_checkLayoutAccess_noLocalLayout_never() {
    SpaceReference layoutSpaceRef = new SpaceReference("myLayout", new WikiReference("someDB"));
    replayDefault();
    assertFalse("someDB never", layoutService.checkLayoutAccess(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_null() throws Exception {
    replayDefault();
    assertNull(layoutService.getPageLayoutForDoc(null));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_centralPageLayout() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDocName");
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    String layoutName = "MyPageLayout";
    expect(inheritor.getStringValue(eq(IPageTypeClassConfig.PAGE_TYPE_LAYOUT_FIELD),
        (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andReturn(new XWikiDocument(
            webHomeDocRef));
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName,
        layoutService.getCentralWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault(injectedInheritorFactory, inheritor);
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef);
    assertNotNull(pageLayoutForDoc);
    assertEquals(centralLayoutSpaceRef, pageLayoutForDoc);
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defined() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase()).space("mySpace")
        .doc("MyDocName").build(DocumentReference.class);
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(false);
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, layoutService.getCentralWikiRef()), false);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull(layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defaultAvailable() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(false);
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName,
        layoutService.getCentralWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertEquals(centralLayoutSpaceRef, layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    String layoutName = "MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", layoutService.getCentralWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, layoutService.getCentralWikiRef()), false);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull(layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_central() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    String layoutName = "celements2web:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(false);
    expectLayoutDoc(new SpaceReference("MyPageLayout", layoutService.getCentralWikiRef()), true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertEquals(layoutName, layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_noAccess_someDB() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "someDB:MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(false);
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", layoutService.getCentralWikiRef()), false);
    expectLayoutDoc(new SpaceReference("MyPageLayout", new WikiReference("someDB")), true);

    replayDefault(injectedInheritorFactory, inheritor);
    assertNull("no access to someDB:MyPageLayout",
        layoutService.getPageLayoutForDoc(new DocumentReference(
            context.getDatabase(), "mySpace", "MyDocName")));
    verifyDefault(injectedInheritorFactory, inheritor);
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase())
        .space("MyPageLayout").doc("MyDocName").build(DocumentReference.class);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    expect(modelAccessMock.exists(eq(layoutEditorPropDocRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(layoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    layoutEditorPropObj.setXClassReference(pagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(modelAccessMock.getDocument(eq(layoutEditorPropDocRef))).andReturn(
        layoutEditorPropDoc);
    replayDefault(injectedInheritorFactory);
    assertEquals(RefBuilder.create().wiki(context.getDatabase()).space("CelLayoutEditor")
        .build(SpaceReference.class), layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory);
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace_centralLayoutEditor() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    layoutService.inject_TEST_InheritorFactory(injectedInheritorFactory);
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase())
        .space("MyPageLayout").doc("MyDocName").build(DocumentReference.class);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    expect(modelAccessMock.exists(eq(layoutEditorPropDocRef))).andReturn(false);
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference("celements2web",
        "CelLayoutEditor", "WebHome");
    expect(modelAccessMock.exists(eq(centralLayoutEditorPropDocRef))).andReturn(
        true).atLeastOnce();
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(centralLayoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = new DocumentReference("celements2web",
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutEditorPropObj.setXClassReference(centralPagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(modelAccessMock.getDocument(eq(centralLayoutEditorPropDocRef))).andReturn(
        layoutEditorPropDoc);
    replayDefault(injectedInheritorFactory);
    assertEquals(RefBuilder.create().wiki("celements2web").space("CelLayoutEditor")
        .build(SpaceReference.class), layoutService.getPageLayoutForDoc(myDocRef));
    verifyDefault(injectedInheritorFactory);
  }

  @Test
  public void test_layoutExists_noLayout() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    replayDefault();
    assertFalse("Current space is missing the WebHome document. Without LayoutProperties it is no "
        + "valid Layout-Space", layoutService.layoutExists(currDocRef.getLastSpaceReference()));
    verifyDefault();
  }

  @Test
  public void test_layoutExists_LayoutSpace() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MyTestLayout",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    SpaceReference layoutSpaceRef = currDocRef.getLastSpaceReference();
    expectLayoutDoc(layoutSpaceRef, true, true);
    replayDefault();
    assertTrue("Current space is a valid Layout-Space with an LayoutProperty object",
        layoutService.layoutExists(layoutSpaceRef));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForCurrentDoc_noLayout() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc).atLeastOnce();
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(mySpacePrefDoc)
        .atLeastOnce();
    DocumentReference xWikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xWikiPrefDoc = new XWikiDocument(xWikiPrefDocRef);
    expect(xwiki.getDocument(eq(xWikiPrefDocRef), same(context))).andReturn(xWikiPrefDoc)
        .atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout")))
        .andReturn("SimpleLayout").once();
    DocumentReference simpleLayoutLocalDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    expect(modelAccessMock.getDocument(eq(simpleLayoutLocalDocRef)))
        .andThrow(new DocumentNotExistsException(simpleLayoutLocalDocRef)).atLeastOnce();
    DocumentReference simpleLayoutCentralDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "WebHome");
    expect(modelAccessMock.getDocument(eq(simpleLayoutCentralDocRef)))
        .andThrow(new DocumentNotExistsException(simpleLayoutCentralDocRef)).atLeastOnce();
    replayDefault();
    assertNull(layoutService.getPageLayoutForCurrentDoc());
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForCurrentDoc_in_Layout_Space() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MyTestLayout",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    SpaceReference layoutSpaceRef = currDocRef.getLastSpaceReference();
    expectLayoutDoc(layoutSpaceRef, true, true);
    SpaceReference layoutEditorSpaceRef = RefBuilder.create().wiki(context.getDatabase())
        .space("CelLayoutEditor").build(SpaceReference.class);
    expectLayoutDoc(layoutEditorSpaceRef, true, true);
    replayDefault();
    assertEquals(layoutEditorSpaceRef, layoutService.getPageLayoutForCurrentDoc());
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForCurrentDoc_withLayout() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc).atLeastOnce();
    String layoutSpaceName = "MyTestLayout";
    SpaceReference layoutSpaceRef = RefBuilder.create().wiki(context.getDatabase())
        .space(layoutSpaceName).build(SpaceReference.class);
    expectLayoutDoc(layoutSpaceRef, true, true);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    BaseObject webPrefPageTypeObj = new BaseObject();
    DocumentReference pageTypeClassRef = Utils.getComponent(IPageTypeClassConfig.class)
        .getPageTypeClassRef();
    webPrefPageTypeObj.setStringValue(IPageTypeClassConfig.PAGE_TYPE_LAYOUT_FIELD, layoutSpaceName);
    webPrefPageTypeObj.setXClassReference(pageTypeClassRef);
    mySpacePrefDoc.addXObject(webPrefPageTypeObj);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(mySpacePrefDoc)
        .atLeastOnce();
    replayDefault();
    assertEquals(layoutSpaceRef, layoutService.getPageLayoutForCurrentDoc());
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDocRefForCurrentDoc_noLayout() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc).atLeastOnce();
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(mySpacePrefDoc)
        .atLeastOnce();
    DocumentReference xWikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xWikiPrefDoc = new XWikiDocument(xWikiPrefDocRef);
    expect(xwiki.getDocument(eq(xWikiPrefDocRef), same(context))).andReturn(xWikiPrefDoc)
        .atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout")))
        .andReturn("SimpleLayout").once();
    DocumentReference simpleLayoutLocalDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    expect(modelAccessMock.getDocument(eq(simpleLayoutLocalDocRef)))
        .andThrow(new DocumentNotExistsException(simpleLayoutLocalDocRef)).atLeastOnce();
    DocumentReference simpleLayoutCentralDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "WebHome");
    expect(modelAccessMock.getDocument(eq(simpleLayoutCentralDocRef)))
        .andThrow(new DocumentNotExistsException(simpleLayoutCentralDocRef)).atLeastOnce();
    replayDefault();
    assertFalse("no Layout defined for current doc",
        layoutService.getLayoutPropDocRefForCurrentDoc().isPresent());
    verifyDefault();
  }

  @Test
  public void test_getLayoutPropDocRefForCurrentDoc_withLayout() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    expect(xwiki.getDocument(eq(currDocRef), same(context))).andReturn(currDoc).atLeastOnce();
    String layoutSpaceName = "MyTestLayout";
    SpaceReference layoutSpaceRef = RefBuilder.create().wiki(context.getDatabase())
        .space(layoutSpaceName).build(SpaceReference.class);
    XWikiDocument layoutWebHomeDoc = expectLayoutDoc(layoutSpaceRef, true, true);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    DocumentReference mySpacePrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument mySpacePrefDoc = new XWikiDocument(mySpacePrefDocRef);
    BaseObject webPrefPageTypeObj = new BaseObject();
    DocumentReference pageTypeClassRef = Utils.getComponent(IPageTypeClassConfig.class)
        .getPageTypeClassRef();
    webPrefPageTypeObj.setStringValue(IPageTypeClassConfig.PAGE_TYPE_LAYOUT_FIELD, layoutSpaceName);
    webPrefPageTypeObj.setXClassReference(pageTypeClassRef);
    mySpacePrefDoc.addXObject(webPrefPageTypeObj);
    expect(xwiki.getDocument(eq(mySpacePrefDocRef), same(context))).andReturn(mySpacePrefDoc)
        .atLeastOnce();
    replayDefault();
    Optional<DocumentReference> layoutPropDocRefOptional = layoutService
        .getLayoutPropDocRefForCurrentDoc();
    assertTrue(layoutPropDocRefOptional.isPresent());
    assertEquals(layoutWebHomeDoc.getDocumentReference(), layoutPropDocRefOptional.get());
    verifyDefault();
  }

  @Test
  public void test_deleteLayout() throws Exception {
    QueryManager queryManagerMock = createMock(QueryManager.class);
    layoutService.queryManager = queryManagerMock;
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
    modelAccessMock.deleteDocument(eq(myCellDocRef), eq(true));
    expectLastCall().once();
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), layoutName,
        "WebHome");
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    webHomeDoc.setNew(false);
    webHomeDoc.setStore(storeMock);
    modelAccessMock.deleteDocument(eq(webHomeDocRef), eq(true));
    expectLastCall().once();
    replayDefault(queryManagerMock, queryMock);
    assertTrue(layoutService.deleteLayout(layoutSpaceRef));
    verifyDefault(queryManagerMock, queryMock);
  }

  @Test
  public void test_getDefaultLayout_default() {
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        DefaultLayoutService.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayDefault();
    assertEquals("TestLayout", layoutService.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_null() {
    context.setAction(null);
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        DefaultLayoutService.SIMPLE_LAYOUT))).andReturn("TestLayout");
    replayDefault();
    assertEquals("TestLayout", layoutService.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_login() {
    context.setAction("login");
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        DefaultLayoutService.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn("LoginLayout");
    replayDefault();
    assertEquals("LoginLayout", layoutService.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLayout_edit() {
    context.setAction("edit");
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT), eq(
        DefaultLayoutService.SIMPLE_LAYOUT))).andReturn("TestLayout");
    expect(xwiki.Param(eq(DefaultLayoutService.XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "."
        + context.getAction()), eq("TestLayout"))).andReturn("EditLayout");
    replayDefault();
    assertEquals("EditLayout", layoutService.getDefaultLayout());
    verifyDefault();
  }

  @Test
  public void test_isLayoutEditorAvailable_local() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, true);
    replayDefault();
    assertTrue(layoutService.isLayoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_isLayoutEditorAvailable_central() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, layoutService.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, true);
    replayDefault();
    assertTrue(layoutService.isLayoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_isLayoutEditorAvailable_notavailable() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, layoutService.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, false);
    replayDefault();
    assertFalse(layoutService.isLayoutEditorAvailable());
    verifyDefault();
  }

  @Test
  public void test_resolveValidLayoutSpace_exists() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = layoutService.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertEquals(ret, layoutSpaceRef);
  }

  @Test
  public void test_resolveValidLayoutSpace_exists_central() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expectLayoutDoc(layoutSpaceRef, false);
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, layoutService.getCentralWikiRef());
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = layoutService.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertEquals(ret, centralLayoutSpaceRef);
  }

  @Test
  public void test_resolveValidLayoutSpace_otherDB() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, new WikiReference("external"));
    expectLayoutDoc(layoutSpaceRef, true);

    replayDefault();
    SpaceReference ret = layoutService.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void test_resolveValidLayoutSpace_absent() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    expect(modelAccessMock.exists(eq(layoutService.getLayoutPropDocRef(layoutSpaceRef).get())))
        .andReturn(false).atLeastOnce();
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, layoutService.getCentralWikiRef());
    expect(
        modelAccessMock.exists(eq(layoutService.getLayoutPropDocRef(centralLayoutSpaceRef).get())))
            .andReturn(false).atLeastOnce();
    replayDefault();
    SpaceReference ret = layoutService.resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    verifyDefault();

    assertNull(ret);
  }

  @Test
  public void test__getLayoutPropDocRef_standardPropDocRef() throws Exception {
    SpaceReference layoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    replayDefault();
    Optional<DocumentReference> ret = layoutService.getLayoutPropDocRef(layoutSpaceRef);
    assertTrue(ret.isPresent());
    assertEquals(layoutSpaceRef, ret.get().getParent());
    assertEquals("WebHome", ret.get().getName());
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML_default() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML_default_empty() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, "");
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML_default_null() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, null);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_HTML5() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD,
        HtmlDoctype.HTML5.getValue());
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.HTML5, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  @Test
  public void test_getHTMLType_XHTML() throws Exception {
    SpaceReference layoutRef = new SpaceReference("MyPageLayout", new WikiReference(
        context.getDatabase()));
    DocumentReference webHomeDocRef = new DocumentReference("WebHome", layoutRef);
    expect(modelAccessMock.exists(eq(webHomeDocRef))).andReturn(true).atLeastOnce();
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = new DocumentReference(context.getDatabase(),
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_SPACE,
        LayoutServiceRole.PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD,
        HtmlDoctype.XHTML.getValue());
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  private XWikiDocument expectLayoutDoc(SpaceReference layoutSpaceRef, boolean exists)
      throws Exception {
    return expectLayoutDoc(layoutSpaceRef, exists, true);
  }

  private XWikiDocument expectLayoutDoc(SpaceReference layoutSpaceRef, boolean exists,
      boolean withObject) throws Exception {
    DocumentReference layoutDocRef = layoutService.getLayoutPropDocRef(layoutSpaceRef).get();
    expect(modelAccessMock.exists(eq(layoutDocRef))).andReturn(exists).anyTimes();
    XWikiDocument layoutDoc = new XWikiDocument(layoutDocRef);
    layoutDoc.setNew(!exists);
    if (exists) {
      expect(modelAccessMock.getDocument(eq(layoutDocRef))).andReturn(layoutDoc)
          .once();
      if (withObject) {
        BaseObject layoutPropObj = new BaseObject();
        layoutPropObj.setXClassReference(layoutService.getPageLayoutPropertiesClassRef(
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
