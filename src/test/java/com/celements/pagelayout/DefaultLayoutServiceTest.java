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
import static com.celements.pagelayout.DefaultLayoutService.*;
import static java.util.stream.Collectors.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.cells.HtmlDoctype;
import com.celements.cells.classes.PageLayoutPropertiesClass;
import com.celements.common.test.AbstractComponentTest;
import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.web.CelConstant;
import com.google.common.collect.ImmutableList;
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
  private IModelAccessFacade modelAccessMock;

  private DefaultLayoutService layoutService;

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
  public void test_getCelLayoutEditorSpaceRef() {
    SpaceReference celLayoutEditorSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, getWikiRef());
    assertEquals(celLayoutEditorSpaceRef, layoutService.getCelLayoutEditorSpaceRef());
  }

  @Test
  public void test_streamLayoutsSpaces() throws Exception {
    List<WikiReference> wikis = ImmutableList.of(
        new WikiReference("wiki1"),
        new WikiReference("wiki2"),
        new WikiReference("wiki3"));
    List<SpaceReference> allLayouts = new ArrayList<>();
    for (WikiReference wiki : wikis) {
      List<SpaceReference> layouts = ImmutableList.of(
          new SpaceReference("layout1Space", wiki),
          new SpaceReference("layout2Space", wiki));
      expectLayoutQuery(wiki, layouts);
      allLayouts.addAll(layouts);
    }
    replayDefault();
    assertEquals(allLayouts, layoutService.streamLayoutsSpaces(wikis.toArray(new WikiReference[0]))
        .collect(toList()));
    verifyDefault();
  }

  @Test
  public void test_streamAllLayoutsSpaces() throws Exception {
    List<WikiReference> wikis = ImmutableList.of(getWikiRef(), CelConstant.CENTRAL_WIKI);
    List<SpaceReference> allLayouts = new ArrayList<>();
    for (WikiReference wiki : wikis) {
      List<SpaceReference> layouts = ImmutableList.of(
          new SpaceReference("layout1Space", wiki),
          new SpaceReference("layout2Space", wiki));
      expectLayoutQuery(wiki, layouts);
      allLayouts.addAll(layouts);
    }
    replayDefault();
    assertEquals(allLayouts, layoutService.streamAllLayoutsSpaces().collect(toList()));
    verifyDefault();
  }

  @Test
  public void test_streamAllLayoutsSpaces_override() throws Exception {
    SpaceReference localLayout = new SpaceReference("layoutSpace", getWikiRef());
    expectLayoutQuery(getWikiRef(), ImmutableList.of(localLayout));
    SpaceReference centralLayout = new SpaceReference("layoutSpace", CelConstant.CENTRAL_WIKI);
    expectLayoutQuery(CelConstant.CENTRAL_WIKI, ImmutableList.of(centralLayout));

    replayDefault();
    assertEquals("only the local should be returned since it overrides the central",
        ImmutableList.of(localLayout),
        layoutService.streamAllLayoutsSpaces().collect(toList()));
    verifyDefault();
  }

  @Test
  public void test_getAllPageLayouts() throws Exception {
    List<SpaceReference> allLayouts = ImmutableList.of(
        new SpaceReference("layout1Space", getWikiRef()),
        new SpaceReference("layout2Space", getWikiRef()));
    expectLayoutQuery(getWikiRef(), allLayouts);
    expectLayoutQuery(CelConstant.CENTRAL_WIKI, ImmutableList.of());
    for (int i = 0; i < allLayouts.size(); i++) {
      SpaceReference layout = allLayouts.get(i);
      BaseObject layoutPropObj = expectLayoutDoc(layout, true);
      layoutPropObj.setStringValue(PageLayoutPropertiesClass.FIELD_PRETTYNAME.getName(),
          "Layout PrettyName " + i);
    }
    replayDefault();
    Map<SpaceReference, String> actual = layoutService.getAllPageLayouts();
    verifyDefault();
    assertEquals(allLayouts.size(), actual.size());
    for (int i = 0; i < allLayouts.size(); i++) {
      SpaceReference layout = allLayouts.get(i);
      assertTrue(actual.containsKey(layout));
      assertEquals("Layout PrettyName " + i, actual.get(layout));
    }
  }

  @Test
  public void test_getActivePageLayouts() throws Exception {
    List<SpaceReference> allLayouts = ImmutableList.of(
        new SpaceReference("layout1Space", getWikiRef()),
        new SpaceReference("layout2Space", getWikiRef()),
        new SpaceReference("layout3Space", getWikiRef()));
    expectLayoutQuery(getWikiRef(), allLayouts);
    expectLayoutQuery(CelConstant.CENTRAL_WIKI, ImmutableList.of());
    expectLayoutDoc(allLayouts.get(0), true);
    BaseObject layoutPropObj2 = expectLayoutDoc(allLayouts.get(1), true);
    layoutPropObj2.setIntValue(PageLayoutPropertiesClass.FIELD_IS_ACTIVE.getName(), 1);
    BaseObject layoutPropObj3 = expectLayoutDoc(allLayouts.get(2), true);
    layoutPropObj3.setIntValue(PageLayoutPropertiesClass.FIELD_IS_ACTIVE.getName(), 0);
    replayDefault();
    Map<SpaceReference, String> actual = layoutService.getActivePageLayouts();
    verifyDefault();
    assertEquals(1, actual.size());
    assertTrue(actual.containsKey(allLayouts.get(1)));
  }

  private void expectLayoutQuery(WikiReference wiki, List<SpaceReference> layouts)
      throws QueryException {
    Query queryMock = createMockAndAddToDefault(Query.class);
    expect(queryManagerMock.createQuery(eq(HQL_PAGE_LAYOUT), eq(Query.HQL))).andReturn(queryMock);
    expect(queryMock.setWiki(wiki.getName())).andReturn(queryMock);
    expect(queryMock.execute()).andReturn(layouts.stream()
        .map(s -> new Object[] { s.getName() })
        .collect(toList()));
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
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String fullName = "mySpace.MyDocName";
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    String layoutName = "MyPageLayout";
    expect(inheritor.getStringValue(eq(IPageTypeClassConfig.PAGE_TYPE_LAYOUT_FIELD),
        (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andReturn(new XWikiDocument(webHomeDocRef));
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName, CelConstant.CENTRAL_WIKI);
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault();
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef,
        injectedInheritorFactory);
    assertNotNull(pageLayoutForDoc);
    assertEquals(centralLayoutSpaceRef, pageLayoutForDoc);
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defined() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase()).space("mySpace")
        .doc("MyDocName").build(DocumentReference.class);
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, CelConstant.CENTRAL_WIKI), false);

    replayDefault();
    assertNull(layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_noPageLayout_defaultAvailable() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String fullName = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(null);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    String layoutName = "SimpleLayout";
    expect(xwiki.Param(eq("celements.layout.default"), eq(layoutName)))
        .andReturn(layoutName).once();
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, getWikiRef());
    SpaceReference centralLayoutSpaceRef = new SpaceReference(layoutName, CelConstant.CENTRAL_WIKI);
    expectLayoutDoc(layoutSpaceRef, false);
    expectLayoutDoc(centralLayoutSpaceRef, true);

    replayDefault();
    assertEquals(centralLayoutSpaceRef,
        layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    String layoutName = "MyPageLayout";
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", CelConstant.CENTRAL_WIKI),
        false);
    expectLayoutDoc(new SpaceReference(layoutName, getWikiRef()), false);
    expectLayoutDoc(new SpaceReference(layoutName, CelConstant.CENTRAL_WIKI), false);

    replayDefault();
    assertNull(layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_central() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String myDocFN = "mySpace.MyDocName";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDocName");
    String layoutName = "celements2web:MyPageLayout";
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(myDocFN), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    SpaceReference expectedLayoutSpaceRef = new SpaceReference("MyPageLayout",
        CelConstant.CENTRAL_WIKI);
    expectLayoutDoc(expectedLayoutSpaceRef, true);

    replayDefault();
    assertEquals(expectedLayoutSpaceRef,
        layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
  }

  @Test
  public void test_getPageLayoutForDoc_noLayoutSpace_noAccess_someDB() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    String fullName = "mySpace.MyDocName";
    String layoutName = "someDB:MyPageLayout";
    FieldInheritor inheritor = createMockAndAddToDefault(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))).andReturn(
        inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String) isNull())).andReturn(layoutName);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebHome");
    expect(modelAccessMock.getDocument(eq(webHomeDocRef)))
        .andThrow(new DocumentNotExistsException(webHomeDocRef)).atLeastOnce();
    expect(xwiki.Param(eq("celements.layout.default"), eq("SimpleLayout"))).andReturn(
        "SimpleLayout").once();
    expectLayoutDoc(new SpaceReference("SimpleLayout", getWikiRef()), false);
    expectLayoutDoc(new SpaceReference("SimpleLayout", CelConstant.CENTRAL_WIKI),
        false);
    expectLayoutDoc(new SpaceReference("MyPageLayout", new WikiReference("someDB")), true);

    replayDefault();
    assertNull("no access to someDB:MyPageLayout",
        layoutService.getPageLayoutForDoc(new DocumentReference(
            context.getDatabase(), "mySpace", "MyDocName"), injectedInheritorFactory));
    verifyDefault();
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase())
        .space("MyPageLayout").doc("MyDocName").build(DocumentReference.class);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(layoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    layoutEditorPropObj.setXClassReference(pagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(modelAccessMock.getDocument(eq(layoutEditorPropDocRef))).andReturn(
        layoutEditorPropDoc);
    replayDefault();
    assertEquals(RefBuilder.create().wiki(context.getDatabase()).space("CelLayoutEditor")
        .build(SpaceReference.class),
        layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
  }

  @Deprecated
  @Test
  public void test_getPageLayoutForDoc_layoutSpace_centralLayoutEditor() throws Exception {
    InheritorFactory injectedInheritorFactory = createMockAndAddToDefault(InheritorFactory.class);
    DocumentReference myDocRef = RefBuilder.create().wiki(context.getDatabase())
        .space("MyPageLayout").doc("MyDocName").build(DocumentReference.class);
    DocumentReference webHomeDocRef = new DocumentReference(context.getDatabase(), "MyPageLayout",
        "WebHome");
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(layoutPropDoc);
    DocumentReference layoutEditorPropDocRef = new DocumentReference(context.getDatabase(),
        "CelLayoutEditor", "WebHome");
    expect(modelAccessMock.getDocument(eq(layoutEditorPropDocRef)))
        .andThrow(new DocumentNotExistsException(layoutEditorPropDocRef)).atLeastOnce();
    DocumentReference centralLayoutEditorPropDocRef = new DocumentReference("celements2web",
        "CelLayoutEditor", "WebHome");
    XWikiDocument layoutEditorPropDoc = new XWikiDocument(centralLayoutEditorPropDocRef);
    layoutEditorPropDoc.setNew(false);
    BaseObject layoutEditorPropObj = new BaseObject();
    DocumentReference centralPagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(CelConstant.CENTRAL_WIKI);
    layoutEditorPropObj.setXClassReference(centralPagePropClassRef);
    layoutEditorPropDoc.addXObject(layoutEditorPropObj);
    expect(modelAccessMock.getDocument(eq(centralLayoutEditorPropDocRef))).andReturn(
        layoutEditorPropDoc);
    replayDefault();
    assertEquals(RefBuilder.create().wiki("celements2web").space("CelLayoutEditor")
        .build(SpaceReference.class),
        layoutService.getPageLayoutForDoc(myDocRef, injectedInheritorFactory));
    verifyDefault();
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
        + "valid Layout-Space", layoutService.existsLayout(currDocRef.getLastSpaceReference()));
    verifyDefault();
  }

  @Test
  public void test_layoutExists_LayoutSpace() throws Exception {
    DocumentReference currDocRef = new DocumentReference(context.getDatabase(), "MyTestLayout",
        "MyPage");
    XWikiDocument currDoc = new XWikiDocument(currDocRef);
    context.setDoc(currDoc);
    SpaceReference layoutSpaceRef = currDocRef.getLastSpaceReference();
    expectLayoutDoc(layoutSpaceRef, true);
    replayDefault();
    assertTrue("Current space is a valid Layout-Space with an LayoutProperty object",
        layoutService.existsLayout(layoutSpaceRef));
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
    expectLayoutDoc(layoutSpaceRef, true);
    SpaceReference layoutEditorSpaceRef = RefBuilder.create().wiki(context.getDatabase())
        .space("CelLayoutEditor").build(SpaceReference.class);
    expectLayoutDoc(layoutEditorSpaceRef, true);
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
    expectLayoutDoc(layoutSpaceRef, true);
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
  public void test_getLayoutPropertyObj_withLayout() throws Exception {
    String layoutSpaceName = "MyTestLayout";
    SpaceReference layoutSpaceRef = RefBuilder.create().wiki(context.getDatabase())
        .space(layoutSpaceName).build(SpaceReference.class);
    BaseObject layoutPropObj = expectLayoutDoc(layoutSpaceRef, true);
    replayDefault();
    Optional<BaseObject> layoutPropertyObjOptional = layoutService
        .getLayoutPropertyObj(layoutSpaceRef);
    assertTrue(layoutPropertyObjOptional.isPresent());
    assertEquals(layoutPropObj, layoutPropertyObjOptional.get());
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
    DocumentReference layoutWebHomeDocRef = expectLayoutDoc(layoutSpaceRef, true)
        .getDocumentReference();
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
    assertEquals(layoutWebHomeDocRef, layoutPropDocRefOptional.get());
    verifyDefault();
  }

  @Test
  public void test_deleteLayout() throws Exception {
    String layoutName = "delLayout";
    SpaceReference layoutSpaceRef = new SpaceReference(layoutName, new WikiReference(
        context.getDatabase()));
    Query queryMock = createMockAndAddToDefault(Query.class);
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
    replayDefault();
    assertTrue(layoutService.deleteLayout(layoutSpaceRef));
    verifyDefault();
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
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, CelConstant.CENTRAL_WIKI);
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
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, CelConstant.CENTRAL_WIKI);
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
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, CelConstant.CENTRAL_WIKI);
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
    DocumentReference layoutWebHomeDocRef = layoutService.getLayoutPropDocRef(layoutSpaceRef).get();
    expect(modelAccessMock.getDocument(eq(layoutWebHomeDocRef)))
        .andThrow(new DocumentNotExistsException(layoutWebHomeDocRef)).atLeastOnce();
    SpaceReference centralLayoutSpaceRef = new SpaceReference(
        LayoutServiceRole.CEL_LAYOUT_EDITOR_PL_NAME, CelConstant.CENTRAL_WIKI);
    DocumentReference centralLayoutWebHomeDocRef = layoutService
        .getLayoutPropDocRef(centralLayoutSpaceRef).get();
    expect(modelAccessMock.getDocument(eq(centralLayoutWebHomeDocRef)))
        .andThrow(new DocumentNotExistsException(centralLayoutWebHomeDocRef)).atLeastOnce();
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
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
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
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(PageLayoutPropertiesClass.FIELD_LAYOUT_DOCTYPE.getName(), "");
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
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(PageLayoutPropertiesClass.FIELD_LAYOUT_DOCTYPE.getName(), null);
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
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(PageLayoutPropertiesClass.FIELD_LAYOUT_DOCTYPE.getName(),
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
    XWikiDocument layoutPropDoc = new XWikiDocument(webHomeDocRef);
    layoutPropDoc.setNew(false);
    BaseObject layoutPropObj = new BaseObject();
    DocumentReference pagePropClassRef = PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(webHomeDocRef.getWikiReference());
    layoutPropObj.setXClassReference(pagePropClassRef);
    layoutPropObj.setStringValue(PageLayoutPropertiesClass.FIELD_LAYOUT_DOCTYPE.getName(),
        HtmlDoctype.XHTML.getValue());
    layoutPropDoc.addXObject(layoutPropObj);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(
        layoutPropDoc).atLeastOnce();
    replayDefault();
    assertEquals(HtmlDoctype.XHTML, layoutService.getHTMLType(layoutRef));
    verifyDefault();
  }

  private BaseObject expectLayoutDoc(SpaceReference layoutSpaceRef, boolean exists)
      throws Exception {
    DocumentReference layoutDocRef = layoutService.getLayoutPropDocRef(layoutSpaceRef).get();
    expect(modelAccessMock.exists(eq(layoutDocRef))).andReturn(exists).anyTimes();
    XWikiDocument layoutDoc = new XWikiDocument(layoutDocRef);
    layoutDoc.setNew(!exists);
    BaseObject layoutPropObj = new BaseObject();
    layoutPropObj.setXClassReference(PageLayoutPropertiesClass.CLASS_REF
        .getDocRef(layoutDoc.getDocumentReference().getWikiReference()));
    layoutDoc.addXObject(layoutPropObj);
    if (exists) {
      expect(modelAccessMock.getDocument(eq(layoutDocRef))).andReturn(layoutDoc).atLeastOnce();
    } else {
      expect(modelAccessMock.getDocument(eq(layoutDocRef)))
          .andThrow(new DocumentNotExistsException(layoutDocRef))
          .anyTimes();
    }
    return layoutPropObj;
  }

  private WikiReference getWikiRef() {
    return new WikiReference(getContext().getDatabase());
  }

}
