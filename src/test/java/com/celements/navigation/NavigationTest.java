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
package com.celements.navigation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.presentation.DefaultPresentationType;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.PageTypeClasses;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.pagetype.service.PageTypeResolverService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;


public class NavigationTest extends AbstractBridgedComponentTestCase {

  private Navigation nav;
  private XWiki xwiki;
  private XWikiContext context;
  private IWebUtils utils;
  private XWikiDocument currentDoc;
  private INavFilter<BaseObject> navFilterMock;
  private ITreeNodeService tNServiceMock;
  private DocumentReference currentDocRef;
  private IWebUtilsService wUServiceMock;
  private PageTypeResolverService ptResolverServiceMock;
  private PageLayoutCommand mockLayoutCmd;

  @Before
  public void setUp_NavigationTest() throws Exception {
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = new Navigation("N1");
    navFilterMock = createMock(InternalRightsFilter.class);
    nav.setNavFilter(navFilterMock);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    utils = createMock(IWebUtils.class);
    nav.testInjectUtils(utils);
    tNServiceMock = createMock(ITreeNodeService.class);
    nav.injected_TreeNodeService = tNServiceMock;
    wUServiceMock = createMock(IWebUtilsService.class);
    nav.injected_WebUtilsService = wUServiceMock;
    ptResolverServiceMock = createMock(PageTypeResolverService.class);
    nav.injected_PageTypeResolverService = ptResolverServiceMock;
    mockLayoutCmd = createMock(PageLayoutCommand.class);
    nav.pageLayoutCmd = mockLayoutCmd;
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
  }

  @Test
  public void testGetPageTypeResolverService() {
    nav.injected_PageTypeResolverService = null;
    assertNotNull(nav.getPageTypeResolverService());
  }

  @Test
  public void testGetRightsFilter() {
    nav.setNavFilter(null);
    INavFilter<BaseObject> filterNew = nav.getNavFilter();
    assertNotNull(filterNew);
    assertSame("expecting singleton", filterNew, nav.getNavFilter());
  }

  @Test
  public void testSetRightsFilter() {
    InternalRightsFilter filterNew = createMock(InternalRightsFilter.class);
    nav.setNavFilter(filterNew);
    assertNotNull(nav.getNavFilter());
    assertSame("expecting injected filter object", filterNew, nav.getNavFilter());
  }

  @Test
  public void testSetMenuSpace() {
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    nav.setMenuSpace("");
    assertEquals("MySpace", nav.getMenuSpace(context));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_null() {
    String menuItemName = null;
    String menuPart = "menuPartTest";
    nav.setMenuPart(menuPart);
    navFilterMock.setMenuPart(eq(menuPart));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":menuPartTest:"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_emptyString() {
    String menuItemName = "";
    String menuPart = "menuPartTest";
    nav.setMenuPart(menuPart);
    navFilterMock.setMenuPart(eq(menuPart));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":menuPartTest:"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_null_menuSpace() {
    String menuItemName = null;
    String menuSpace = "testMenuSpace";
    nav.setMenuPart("menuPartTest");
    SpaceReference menuSpaceRef = new SpaceReference(menuSpace, new WikiReference(
        context.getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(menuSpace))).andReturn(menuSpaceRef
        ).anyTimes();
    replayAll();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":testMenuSpace:menuPartTest:"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId() {
    BaseObject menuItem = new BaseObject();
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "Space",
        "TestName");
    menuItem.setDocumentReference(myDocRef);
    String menuPart = "menuPartTest";
    nav.setMenuPart(menuPart);
    navFilterMock.setMenuPart(eq(menuPart));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(":Space.TestName"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_menuSpace() {
    String menuSpace = "testMenuSpace";
    BaseObject menuItem = new BaseObject();
    menuItem.setName("Space.TestName");
    nav.setMenuPart("menuPartTest");
    SpaceReference menuSpaceRef = new SpaceReference(menuSpace, new WikiReference(
        context.getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(menuSpace))).andReturn(menuSpaceRef
        ).anyTimes();
    replayAll();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(
        ":testMenuSpace:Space.TestName"));
    verifyAll();
  }

  @Test
  public void testGetPageTypeConfigName_integrationTest() throws Exception {
    nav.injected_PageTypeResolverService = null;
    ComponentManager componentManager = Utils.getComponentManager();
    ComponentDescriptor<IPageTypeRole> ptServiceDesc =
      componentManager.getComponentDescriptor(IPageTypeRole.class, "default");
    IPageTypeRole ptServiceMock = createMock(IPageTypeRole.class);
    componentManager.registerComponent(ptServiceDesc, ptServiceMock);
    BaseObject ptObj = new BaseObject();
    ptObj.setXClassReference(new PageTypeClasses().getPageTypeClassRef(
        context.getDatabase()));
    ptObj.setStringValue(PageTypeClasses.PAGE_TYPE_FIELD, "TestPageType");
    currentDoc.addXObject(ptObj);
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentDoc);
    String testPageType = "TestPageType";
    expect(ptServiceMock.getPageTypeRefByConfigName(testPageType)).andReturn(
        new PageTypeReference(testPageType, "myTestProvider",
            Collections.<String>emptyList()));
    replayAll(ptServiceMock);
    assertEquals(testPageType, nav.getPageTypeConfigName(currentDocRef));
    verifyAll(ptServiceMock);
    componentManager.release(ptServiceMock);
  }

  @Test
  public void testOpenMenuItemOut_notActive() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    replayAll(pageTypeRef);
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false);
    assertEquals("<li class=\"cel_nav_hasChildren myUltimativePageType\">",
        outStream.toString());
    verifyAll(pageTypeRef);
  }

  @Test
  public void testOpenMenuItemOut_active() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), docRef));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    replayAll(pageTypeRef);
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false);
    assertEquals("<li class=\"cel_nav_hasChildren myUltimativePageType active\">",
        outStream.toString());
    verifyAll(pageTypeRef);
  }

  @Test
  public void testGetMenuPartForLevel_firstLevel() {
    nav.setMenuPart("menuPartTest");
    assertEquals("menuPart must be concidered on first level.",
        "menuPartTest", nav.getMenuPartForLevel(1));
  }
  
  @Test
  public void testGetMenuPartForLevel_secondLevel() {
    nav.setMenuPart("menuPartTest");
    assertEquals("menuPart must only be concidered on first level.",
        "", nav.getMenuPartForLevel(2));
  }

  @Test
  public void testSetLayoutType_unknown() {
    try {
      nav.setLayoutType("blabliType");
      fail("Expected UnknownLayoutTypeException for blabliType layoutType.");
    } catch (UnknownLayoutTypeException e) {
      //expected
    }
  }

  @Test
  public void testSetLayoutType_LIST_LAYOUT_TYPE(
      ) throws UnknownLayoutTypeException {
    nav.setLayoutType(Navigation.LIST_LAYOUT_TYPE);
    assertTrue("Expecting navBuilder of type ListBuilder",
        nav.getNavBuilder() instanceof ListBuilder);
  }

  @Test
  public void testHasLink_default() {
    assertTrue(new Navigation("").hasLink());
  }

  @Test
  public void testSetHasLink() {
    nav.setHasLink(false);
    assertFalse(nav.hasLink());
    nav.setHasLink(true);
    assertTrue(nav.hasLink());
  }

  @Test
  public void testIsShowAll_default() {
    assertFalse(new Navigation("").isShowAll());
  }

  @Test
  public void testSetShowAll() {
    nav.setShowAll(true);
    assertTrue(nav.isShowAll());
    nav.setShowAll(false);
    assertFalse(nav.isShowAll());
  }

  @Test
  public void testGetCssClasses_withOut_CM() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    replayAll(pageTypeRef);
    String cssClasses = nav.getCssClasses(menuItem.getDocumentReference(), false, false,
        false, false);
    verifyAll(pageTypeRef);
    assertFalse("Expected to NOT find the cmCSSclass. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" cel_cm_navigation_menuitem "));
  }

  @Test
  public void testGetCssClasses_pageType() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    replayAll(pageTypeRef);
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false);
    verifyAll(pageTypeRef);
    assertTrue("Expected to found pageType in css classes. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_pageLayout() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    replayAll(pageTypeRef);
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false);
    verifyAll(pageTypeRef);
    assertTrue("Expected to found pageLayout in css classes. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" layout_MyLayout "));
  }

  @Test
  public void testGetCssClasses_NullName() throws XWikiException {
    String pageType = "myUltimativePageType";
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
//        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayAll();
    String cssClasses = nav.getCssClasses(null, true, false, false, false);
    verifyAll();
    assertFalse("Expected to not find pageType (because fullName is null) in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_hasChildren() throws XWikiException {
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
//        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayAll();
    String cssClasses = nav.getCssClasses(null, true, false, false, false);
    verifyAll();
    assertTrue("Expected to find 'cel_nav_hasChildren' (because not a leaf) in css"
        + " classes. [" + cssClasses + "]", (" " + cssClasses + " ").contains(
            " cel_nav_hasChildren "));
  }

  @Test
  public void testGetCssClasses_isLeaf() throws XWikiException {
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
//        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayAll();
    String cssClasses = nav.getCssClasses(null, true, false, false, true);
    verifyAll();
    assertTrue("Expected to find 'cel_nav_isLeaf' (because no children) in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_isLeaf "));
  }

  @Test
  public void testGetMenuSpace() {
    List<TreeNode> emptyMenuItemList = Collections.emptyList();
    String parentSpaceName = "MyParentSpace";
    String spaceName = "MySpace";
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(true);
    expect(wUServiceMock.getParentSpace(eq(spaceName))).andReturn(parentSpaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(emptyMenuItemList);
    nav.setMenuPart("");
    nav.testInjectUtils(utils);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    SpaceReference parentSpaceRef = new SpaceReference(parentSpaceName, new WikiReference(
        context.getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(parentSpaceName))).andReturn(
        parentSpaceRef).anyTimes();
    replayAll();
    String menuSpace = nav.getMenuSpace(context);
    verifyAll();
    assertEquals("Expected to receive parentSpace ["
        + parentSpaceName + "]", parentSpaceName, menuSpace);
  }

  @Test
  public void testIsActiveMenuItem_isActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), currentDocRef));
    replayAll();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyAll();
  }

  @Test
  public void testIsActiveMenuItem_isActive_currentDoc() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayAll();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyAll();
  }

  @Test
  public void testIsActiveMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayAll();
    assertFalse(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyAll();
  }

  @Test
  public void testIsActiveMenuItem_menuItemNULL() {
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
      ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
          getDocRefForDocName("blu"), null));
    replayAll();
    assertFalse(nav.isActiveMenuItem(null));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), currentDocRef));
    replayAll();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayAll();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_ShowAll() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
//        ).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    nav.setShowAll(true);
    replayAll();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_showHierarchyLevel() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
//        ).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayAll();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 2, context));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_showHierarchyLevel_Over() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayAll();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 3, context));
    verifyAll();
  }

  @Test
  public void testShowSubmenuForMenuItem_isActive_showHierarchyLevel_Over() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), currentDocRef));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayAll();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 5, context));
    verifyAll();
  }

  @Test
  public void testAddUlCSSClass() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    replayAll();
    assertEquals("class=\"mainCss firstCss secondCss\"",
        nav.getMainUlCSSClasses().trim());
    verifyAll();
  }

  @Test
  public void testAddUlCSSClass_double_add() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    nav.addUlCSSClass("mainCss"); //double add existing class
    replayAll();
    assertEquals("class=\"mainCss firstCss secondCss\"",
        nav.getMainUlCSSClasses().trim());
    verifyAll();
  }

  @Test
  public void testGetNavLanguage_contextLanguage() {
    context.setLanguage("de");
    replayAll();
    assertEquals("de", nav.getNavLanguage());
    verifyAll();
  }

  @Test
  public void testGetNavLanguage_navLanguage() {
    context.setLanguage("de");
    nav.setLanguage("fr");
    replayAll();
    assertEquals("fr", nav.getNavLanguage());
    verifyAll();
  }

  @Test
  public void testGetMenuLink_Content_WebHome() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Content",
        "WebHome");
    expect(xwiki.getURL(eq(docRef), eq("view"), same(context))
        ).andReturn(""); // BUG IN XWIKI !!!
    replayAll();
    assertEquals("/", nav.getMenuLink(docRef));
    verifyAll();
  }

  @Test
  public void testLoadConfigFromObject_defaults() {
    DocumentReference cellConfigDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(context));
    assertEquals("default for fromHierarchyLevel must be greater than zero.", 1,
        nav.fromHierarchyLevel);
    verifyAll();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace() {
    DocumentReference cellConfigDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String nodeSpaceName = "theMenuSpace";
    navConfigObj.setStringValue("menu_space", nodeSpaceName);
    SpaceReference parentSpaceRef = new SpaceReference(nodeSpaceName, new WikiReference(
        context.getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(nodeSpaceName))).andReturn(
        parentSpaceRef).anyTimes();
    replayAll();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("theMenuSpace", nav.getMenuSpace(context));
    verifyAll();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace_empty() {
    DocumentReference cellConfigDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayAll();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(context));
    verifyAll();
  }

  @Test
  public void testLoadConfigFromObject_presentationType_notEmpty() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    navConfigObj.setStringValue(NavigationClasses.PRESENTATION_TYPE_FIELD,
        "testPresentationType");
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    ComponentManager mockComponentManager = createMock(ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IPresentationTypeRole.class),
        eq("testPresentationType"))).andReturn(componentInstance);
    replayAll(mockComponentManager, componentInstance);
    nav.loadConfigFromObject(navConfigObj);
    verifyAll(mockComponentManager, componentInstance);
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType() throws Exception {
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    ComponentManager mockComponentManager = createMock(ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IPresentationTypeRole.class),
        eq("testPresentationType"))).andReturn(componentInstance);
    replayAll(mockComponentManager, componentInstance);
    nav.setPresentationType("testPresentationType");
    verifyAll(mockComponentManager, componentInstance);
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType_null() throws Exception {
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayAll(componentInstance);
    nav.setPresentationType((String)null);
    assertNotNull(nav.getPresentationType());
    assertEquals(DefaultPresentationType.class, nav.getPresentationType().getClass());
    verifyAll(componentInstance);
  }

  @Test
  public void testSetPresentationType_NotFoundException() throws Exception {
    ComponentManager mockComponentManager = createMock(ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IPresentationTypeRole.class),
        eq("testNotFoundPresentationType"))).andThrow(new ComponentLookupException(
            "not found"));
    replayAll(mockComponentManager);
    nav.setPresentationType("testNotFoundPresentationType");
    verifyAll(mockComponentManager);
  }

  @Test
  public void testWriteMenuItemContent_PresentationType() throws Exception {
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    StringBuilder outStream = new StringBuilder();
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    componentInstance.writeNodeContent(same(outStream), eq(true), eq(false), eq(docRef),
        eq(true), same(nav));
    expectLastCall().once();
    replayAll(componentInstance);
    nav.writeMenuItemContent(outStream, true, false, docRef, true);
    verifyAll(componentInstance);
  }

  @Test
  public void testGetCMcssClass_default() {
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    expect(componentInstance.getDefaultCssClass()).andReturn("cel_cm_menu").atLeastOnce();
    replayAll(componentInstance);
    assertEquals("cel_cm_menu", nav.getCMcssClass());
    verifyAll(componentInstance);
  }

  @Test
  public void testGetCMcssClass() {
    IPresentationTypeRole componentInstance = createMock(IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayAll(componentInstance);
    nav.setCMcssClass("cm_test_class");
    assertEquals("cm_test_class", nav.getCMcssClass());
    verifyAll(componentInstance);
  }

  @Test
  public void testGetPageLayoutName_null() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null);
    replayAll();
    assertEquals("", nav.getPageLayoutName(docRef));
    verifyAll();
  }

  @Test
  public void testGetPageLayoutName() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef);
    replayAll();
    assertEquals("layout_MyLayout", nav.getPageLayoutName(docRef));
    verifyAll();
  }

  @Test
  public void testIncludeNavigation_noItemLevel3() {
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 3;
    expect(wUServiceMock.getParentForLevel(3)).andReturn(null).atLeastOnce();
    replayAll();
    assertEquals("no menuitem for level 3. Thus empty string expected.", "",
        nav.includeNavigation());
    verifyAll();
  }

  @Test
  public void testIncludeNavigation_hasItemsLevel1() throws Exception {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 99;
    expect(wUServiceMock.getParentForLevel(1)).andReturn(null).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    DocumentReference homeDocRef = new DocumentReference(context.getDatabase(), spaceName,
        "Home");
    List<TreeNode> mainNodeList = Arrays.asList(new TreeNode(homeDocRef, "", 1));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(mainNodeList);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName),same(navFilterMock))
        ).andReturn(mainNodeList);
    expect(tNServiceMock.getSubNodesForParent(eq("MySpace.Home"), eq(spaceName),
        same(navFilterMock))).andReturn(Collections.<TreeNode>emptyList());
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(homeDocRef))
        ).andReturn(new PageTypeReference("RichText", "test",
            Collections.<String>emptyList())).atLeastOnce();
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(homeDocRef))).andReturn(null
        ).atLeastOnce();
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Collections.<DocumentReference>emptyList()).atLeastOnce();
    expect(xwiki.getURL(eq(homeDocRef), eq("view"), same(context))).andReturn("/Home");
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    expect(xwiki.getDocument(eq("MySpace.Home"), same(context))).andReturn(
        new XWikiDocument(homeDocRef)).atLeastOnce();
    replayAll();
    assertEquals("one tree node for level 1. Thus output expected.", "<ul"
        + " id=\"CN1:MySpace::\" ><li class=\"first last cel_nav_isLeaf RichText\">"
        + "<a href=\"/Home\" class=\"cel_cm_navigation_menuitem first last cel_nav_isLeaf"
        + " RichText\" id=\"N1:MySpace:MySpace.Home\">Home</a><!-- IE6 --></li></ul>",
        nav.includeNavigation());
    verifyAll();
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private DocumentReference getDocRefForDocName(String docName) {
    return new DocumentReference(context.getDatabase(), "MySpace", docName);
  }

  private NavigationClasses getNavClasses() {
    return (NavigationClasses) Utils.getComponent(
        IClassCollectionRole.class, "celements.celNavigationClasses");
  }
  
  private void replayAll(Object ... mocks) {
    replay(xwiki, navFilterMock, utils, tNServiceMock, wUServiceMock,
        ptResolverServiceMock, mockLayoutCmd);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, navFilterMock, utils, tNServiceMock, wUServiceMock,
        ptResolverServiceMock, mockLayoutCmd);
    verify(mocks);
  }

}
