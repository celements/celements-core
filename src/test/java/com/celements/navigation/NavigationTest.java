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
import com.xpn.xwiki.user.api.XWikiRightService;
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
  private XWikiRightService mockRightService;

  @Before
  public void setUp_NavigationTest() throws Exception {
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = new Navigation("N1");
    navFilterMock = createMockAndAddToDefault(InternalRightsFilter.class);
    nav.setNavFilter(navFilterMock);
    xwiki = getWikiMock();
    utils = createMockAndAddToDefault(IWebUtils.class);
    nav.testInjectUtils(utils);
    tNServiceMock = createMockAndAddToDefault(ITreeNodeService.class);
    nav.injected_TreeNodeService = tNServiceMock;
    wUServiceMock = createMockAndAddToDefault(IWebUtilsService.class);
    expect(wUServiceMock.getRefLocalSerializer()).andReturn(
        Utils.getComponent(IWebUtilsService.class).getRefLocalSerializer()).anyTimes();
    nav.injected_WebUtilsService = wUServiceMock;
    ptResolverServiceMock = createMockAndAddToDefault(PageTypeResolverService.class);
    nav.injected_PageTypeResolverService = ptResolverServiceMock;
    mockLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    nav.pageLayoutCmd = mockLayoutCmd;
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    mockRightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
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
    InternalRightsFilter filterNew = createMockAndAddToDefault(
        InternalRightsFilter.class);
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
    replayDefault();
    nav.setMenuSpace("");
    assertEquals("MySpace", nav.getMenuSpace(context));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":menuPartTest:"));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":menuPartTest:"));
    verifyDefault();
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
    replayDefault();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":testMenuSpace:menuPartTest:"));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(":Space.TestName"));
    verifyDefault();
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
    replayDefault();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(
        ":testMenuSpace:Space.TestName"));
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_ignore_invalid_value() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(0);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 3,
        nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_smaller() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(2);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 2,
        nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_bigger() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(10);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 10,
        nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testGetPageTypeConfigName_integrationTest() throws Exception {
    nav.injected_PageTypeResolverService = null;
    ComponentManager componentManager = Utils.getComponentManager();
    ComponentDescriptor<IPageTypeRole> ptServiceDesc =
      componentManager.getComponentDescriptor(IPageTypeRole.class, "default");
    IPageTypeRole ptServiceMock = createMockAndAddToDefault(IPageTypeRole.class);
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
    replayDefault();
    assertEquals(testPageType, nav.getPageTypeConfigName(currentDocRef));
    verifyDefault();
    componentManager.release(ptServiceMock);
  }

  @Test
  public void testOpenMenuItemOut_notActive() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false,
        2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " myUltimativePageType\">", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testIsRestrictedRights() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andThrow(new XWikiException());
    replayDefault();
    assertFalse(nav.isRestrictedRights(docRef));
    verifyDefault();
  }

  @Test
  public void testOpenMenuItemOut_restrictedAccessRights() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(false).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false,
        2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " myUltimativePageType cel_nav_restricted_rights\">", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testOpenMenuItemOut_active() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), docRef));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false,
        2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " myUltimativePageType active\">",
        outStream.toString());
    verifyDefault();
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
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(menuItem.getDocumentReference(), false, false,
        false, false, 2);
    verifyDefault();
    assertFalse("Expected to NOT find the cmCSSclass. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" cel_cm_navigation_menuitem "));
  }

  @Test
  public void testGetCssClasses_pageType() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 3);
    verifyDefault();
    assertTrue("Expected to found pageType in css classes. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_pageLayout() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
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
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyMenuItemDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 2);
    verifyDefault();
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
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, false, 3);
    verifyDefault();
    assertFalse("Expected to not find pageType (because fullName is null) in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_hasChildren() throws XWikiException {
    //FIXME getDocumentParentsList not needed anymore?
//    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
//        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
//            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, false, 2);
    verifyDefault();
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
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 3);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_isLeaf' (because no children) in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_isLeaf "));
  }

  @Test
  public void testGetCssClasses_numItem_odd() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 3);
    verifyDefault();
    assertFalse("Expected NOT to find 'cel_nav_even' in css classes."
        + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_even "));
    assertTrue("Expected to find 'cel_nav_odd' in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_odd "));
  }

  @Test
  public void testGetCssClasses_numItem_even() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 4);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_even' in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_even "));
    assertFalse("Expected NOT to find 'cel_nav_odd' in css classes."
        + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_odd "));
  }

  @Test
  public void testGetCssClasses_numItem() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 4321);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_item4321' in css classes."
      + " [" + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_item4321 "));
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
    replayDefault();
    String menuSpace = nav.getMenuSpace(context);
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_isActive_currentDoc() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayDefault();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayDefault();
    assertFalse(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_menuItemNULL() {
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
      ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
          getDocRefForDocName("blu"), null));
    replayDefault();
    assertFalse(nav.isActiveMenuItem(null));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), currentDocRef));
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(context.getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    replayDefault();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, context));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 2, context));
    verifyDefault();
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
    replayDefault();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 3, context));
    verifyDefault();
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
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 5, context));
    verifyDefault();
  }

  @Test
  public void testAddUlCSSClass() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    replayDefault();
    assertEquals("class=\"mainCss firstCss secondCss\"",
        nav.getMainUlCSSClasses().trim());
    verifyDefault();
  }

  @Test
  public void testAddUlCSSClass_double_add() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    nav.addUlCSSClass("mainCss"); //double add existing class
    replayDefault();
    assertEquals("class=\"mainCss firstCss secondCss\"",
        nav.getMainUlCSSClasses().trim());
    verifyDefault();
  }

  @Test
  public void testGetNavLanguage_contextLanguage() {
    context.setLanguage("de");
    replayDefault();
    assertEquals("de", nav.getNavLanguage());
    verifyDefault();
  }

  @Test
  public void testGetNavLanguage_navLanguage() {
    context.setLanguage("de");
    nav.setLanguage("fr");
    replayDefault();
    assertEquals("fr", nav.getNavLanguage());
    verifyDefault();
  }

  @Test
  public void testGetMenuLink_Content_WebHome() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Content",
        "WebHome");
    expect(xwiki.getURL(eq(docRef), eq("view"), same(context))
        ).andReturn(""); // BUG IN XWIKI !!!
    replayDefault();
    assertEquals("/", nav.getMenuLink(docRef));
    verifyDefault();
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
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(context));
    assertEquals("default for fromHierarchyLevel must be greater than zero.", 1,
        nav.fromHierarchyLevel);
    verifyDefault();
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
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("theMenuSpace", nav.getMenuSpace(context));
    verifyDefault();
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
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(context));
    verifyDefault();
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
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    ComponentManager mockComponentManager = createMockAndAddToDefault(
        ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IWebUtilsService.class), eq("default"))
        ).andReturn(wUServiceMock);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class),
        eq("testPresentationType"))).andReturn(componentInstance);
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    verifyDefault();
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType() throws Exception {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    ComponentManager mockComponentManager = createMockAndAddToDefault(
        ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IWebUtilsService.class), eq("default"))
        ).andReturn(wUServiceMock);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class),
        eq("testPresentationType"))).andReturn(componentInstance);
    replayDefault();
    nav.setPresentationType("testPresentationType");
    verifyDefault();
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType_null() throws Exception {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayDefault();
    nav.setPresentationType((String)null);
    assertNotNull(nav.getPresentationType());
    assertEquals(DefaultPresentationType.class, nav.getPresentationType().getClass());
    verifyDefault();
  }

  @Test
  public void testSetPresentationType_NotFoundException() throws Exception {
    ComponentManager mockComponentManager = createMockAndAddToDefault(
        ComponentManager.class);
    Utils.setComponentManager(mockComponentManager);
    expect(mockComponentManager.lookup(eq(IWebUtilsService.class), eq("default"))
        ).andReturn(wUServiceMock);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class),
        eq("testNotFoundPresentationType"))).andThrow(new ComponentLookupException(
            "not found"));
    replayDefault();
    nav.setPresentationType("testNotFoundPresentationType");
    verifyDefault();
  }

  @Test
  public void testWriteMenuItemContent_PresentationType() throws Exception {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    StringBuilder outStream = new StringBuilder();
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    componentInstance.writeNodeContent(same(outStream), eq(true), eq(false), eq(docRef),
        eq(true), eq(1), same(nav));
    expectLastCall().once();
    replayDefault();
    nav.writeMenuItemContent(outStream, true, false, docRef, true, 1);
    verifyDefault();
  }

  @Test
  public void testGetCMcssClass_default() {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    expect(componentInstance.getDefaultCssClass()).andReturn("cel_cm_menu").atLeastOnce();
    replayDefault();
    assertEquals("cel_cm_menu", nav.getCMcssClass());
    verifyDefault();
  }

  @Test
  public void testGetCMcssClass() {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayDefault();
    nav.setCMcssClass("cm_test_class");
    assertEquals("cm_test_class", nav.getCMcssClass());
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName_null() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null);
    replayDefault();
    assertEquals("", nav.getPageLayoutName(docRef));
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName_overwritePresentationType() {
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    SpaceReference overwriteLayoutRef = new SpaceReference("MyOverwriteLayout",
        new WikiReference(context.getDatabase()));
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    expect(componentInstance.getPageLayoutForDoc(eq(docRef))).andReturn(
        overwriteLayoutRef);
    replayDefault();
    assertEquals("layout_MyOverwriteLayout", nav.getPageLayoutName(docRef));
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef);
    replayDefault();
    assertEquals("layout_MyLayout", nav.getPageLayoutName(docRef));
    verifyDefault();
  }

  @Test
  public void testIncludeNavigation_noItemLevel1_hasEdit() throws Exception {
    String myUserName = "XWiki.MyUserName";
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    expect(wUServiceMock.getParentForLevel(1)).andReturn(null).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName),same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockRightService.hasAccessLevel(eq("edit"), eq(myUserName),
        eq("MySpace.MyCurrentDoc"), same(context))).andReturn(true);
    expect(wUServiceMock.getAdminMessageTool()).andReturn(context.getMessageTool()
        ).anyTimes();
    ((TestMessageTool)context.getMessageTool()).injectMessage("cel_nav_nomenuitems",
        "No Navitems found.");
    replayDefault();
    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
    //set after calling replay
    context.setUser(myUserName);
    assertEquals("no menuitem for level 1. Yet with hasEdit, thus no empty string"
        + " expected.", "<ul class=\"cel_nav_empty\">"
        + "<li class=\"first last cel_nav_odd cel_nav_item1"
        + " cel_nav_hasChildren\">"
        + "<span id=\"N1:MySpace::\" "
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_odd cel_nav_item1"
        + " cel_nav_hasChildren\">"
        + "No Navitems found.</span><!-- IE6 --></li></ul>", nav.includeNavigation());
    verifyDefault();
  }

  @Test
  public void testIncludeNavigation_noItemLevel3() {
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 3;
    expect(wUServiceMock.getParentForLevel(3)).andReturn(null).atLeastOnce();
    replayDefault();
    assertEquals("no menuitem for level 3. Thus empty string expected.", "",
        nav.includeNavigation());
    verifyDefault();
  }

  @Test
  public void testIncludeNavigation_hasItemsLevel1() throws Exception {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 99;
    expect(wUServiceMock.getParentForLevel(eq(1))).andReturn(null).atLeastOnce();
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
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
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
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.Home"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals("one tree node for level 1. Thus output expected.", "<ul"
        + " id=\"CN1:MySpace::\" ><li class=\"first last cel_nav_odd cel_nav_item1"
        + " cel_nav_isLeaf RichText\">"
        + "<a href=\"/Home\" class=\"cel_cm_navigation_menuitem first last cel_nav_odd"
        + " cel_nav_item1 cel_nav_isLeaf"
        + " RichText\" id=\"N1:MySpace:MySpace.Home\">Home</a><!-- IE6 --></li></ul>",
        nav.includeNavigation());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_true() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    DocumentReference parentRef = new DocumentReference(context.getDatabase(),spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    replayDefault();
    assertTrue(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_false() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    DocumentReference parentRef = new DocumentReference(context.getDatabase(),spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    DocumentReference subNodeRef = new DocumentReference(context.getDatabase(), spaceName,
        "SubNodeDoc");
    List<TreeNode> nodeList = Arrays.asList(new TreeNode(subNodeRef, "", 1));
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))
        ).andReturn(nodeList);
    replayDefault();
    assertFalse(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_menuPart_sublevels() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    nav.setMenuPart("myPart");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(),spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    replayDefault();
    assertTrue(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_menuPart_mainLevel() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 4;
    nav.setMenuPart("myPart");
    DocumentReference parentRef = new DocumentReference(context.getDatabase(),spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(1))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq("myPart"));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    replayDefault();
    assertTrue(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_noParent_on_from_level() {
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    nav.setMenuPart("");
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(null).atLeastOnce();
    replayDefault();
    assertTrue(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetOffset_default() {
    replayDefault();
    assertEquals(0, nav.getOffset());
    verifyDefault();
  }

  @Test
  public void testGetOffset_resetZero() {
    nav.setOffset(2);
    nav.setOffset(0);
    replayDefault();
    assertEquals(0, nav.getOffset());
    verifyDefault();
  }

  @Test
  public void testSetOffset_positiv() {
    nav.setOffset(2);
    replayDefault();
    assertEquals(2, nav.getOffset());
    verifyDefault();
  }

  @Test
  public void testSetOffset_negativ() {
    nav.setOffset(2);
    nav.setOffset(-2);
    replayDefault();
    assertEquals(0, nav.getOffset());
    verifyDefault();
  }

  @Test
  public void testGetNumberOfItem_default() {
    replayDefault();
    assertEquals(-1, nav.getNumberOfItem());
    verifyDefault();
  }

  @Test
  public void testSetNumberOfItem_positiv() {
    nav.setNumberOfItem(5);
    replayDefault();
    assertEquals(5, nav.getNumberOfItem());
    verifyDefault();
  }

  @Test
  public void testSetNumberOfItem_negativ() {
    nav.setNumberOfItem(5);
    nav.setNumberOfItem(-5);
    replayDefault();
    assertEquals(-1, nav.getNumberOfItem());
    verifyDefault();
  }

  @Test
  public void testSetNumberOfItem_zero() {
    nav.setNumberOfItem(5);
    nav.setNumberOfItem(0);
    replayDefault();
    assertEquals(-1, nav.getNumberOfItem());
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_noPaging_mainMenu() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = context.getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, "", 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, "", 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, "", 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, "", 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, "", 5);
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3,
        treeNode4, treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
        ).andReturn(expectedMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(expectedMenuItemsList).once();
    replayDefault();
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_withPaging_mainMenu_zeroOffset() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(3);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = context.getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, "", 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, "", 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, "", 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, "", 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, "", 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3,
        treeNode4, treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_withPaging_mainMenu_NONzeroOffset() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(3);
    nav.setOffset(1);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = context.getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, "", 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, "", 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, "", 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, "", 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, "", 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3,
        treeNode4, treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode2, treeNode3, treeNode4);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_withPaging_mainMenu_OverflowEnd() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(3);
    nav.setOffset(3);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = context.getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, "", 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, "", 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, "", 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, "", 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, "", 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3,
        treeNode4, treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode4, treeNode5);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_withPaging_mainMenu_negativOffset() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(3);
    nav.setOffset(-3);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = context.getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, "", 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, "", 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, "", 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, "", 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, "", 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3,
        treeNode4, treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
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

}
