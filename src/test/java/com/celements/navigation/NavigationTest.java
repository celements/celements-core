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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.TestMessageTool;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.NavigationItemContext.ChildState;
import com.celements.navigation.NavigationItemContext.ContainerCssClasses;
import com.celements.navigation.NavigationItemContext.ContextualState;
import com.celements.navigation.NavigationItemContext.Position;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.presentation.DefaultPresentationType;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.PageTypeClasses;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.pagetype.service.PageTypeResolverService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class NavigationTest extends AbstractComponentTest {

  private Navigation nav;
  private XWiki wikMock;
  private XWikiDocument currentDoc;
  private INavFilter<BaseObject> navFilterMock;
  private ITreeNodeService tNServiceMock;
  private DocumentReference currentDocRef;
  private IWebUtilsService wUServiceMock;
  private PageTypeResolverService ptResolverServiceMock;
  private PageLayoutCommand mockLayoutCmd;
  private XWikiRightService mockRightService;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, LayoutServiceRole.class);
    wikMock = getMock(XWiki.class);
    currentDocRef = new DocumentReference(getXContext().getDatabase(), "MySpace", "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    currentDoc.setNew(false);
    getXContext().setDoc(currentDoc);
    nav = new Navigation("N1");
    navFilterMock = createDefaultMock(InternalRightsFilter.class);
    nav.setNavFilter(navFilterMock);
    tNServiceMock = createDefaultMock(ITreeNodeService.class);
    nav.injected_TreeNodeService = tNServiceMock;
    wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.getRefLocalSerializer()).andReturn(Utils.getComponent(
        EntityReferenceSerializer.class, "local")).anyTimes();
    ptResolverServiceMock = createDefaultMock(PageTypeResolverService.class);
    nav.injected_PageTypeResolverService = ptResolverServiceMock;
    mockLayoutCmd = createDefaultMock(PageLayoutCommand.class);
    nav.pageLayoutCmd = mockLayoutCmd;
    expect(wikMock.isMultiLingual(same(getXContext()))).andReturn(true).anyTimes();
    mockRightService = createDefaultMock(XWikiRightService.class);
    expect(wikMock.getRightService()).andReturn(mockRightService).anyTimes();
    expect(wikMock.isVirtualMode()).andReturn(true).anyTimes();
  }

  @After
  public void tearDownTest() {
    reset(wUServiceMock);
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
    InternalRightsFilter filterNew = createDefaultMock(InternalRightsFilter.class);
    nav.setNavFilter(filterNew);
    assertNotNull(nav.getNavFilter());
    assertSame("expecting injected filter object", filterNew, nav.getNavFilter());
  }

  @Test
  public void testSetMenuSpace() {
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayDefault();
    nav.setMenuSpace("");
    assertEquals("MySpace", nav.getMenuSpace(getXContext()));
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
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
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
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
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
        getXContext().getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(menuSpace))).andReturn(menuSpaceRef).anyTimes();
    replayDefault();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":testMenuSpace:menuPartTest:"));
    verifyDefault();
  }

  @Test
  public void testGetUniqueId() {
    BaseObject menuItem = new BaseObject();
    DocumentReference myDocRef = new DocumentReference(getXContext().getDatabase(), "Space",
        "TestName");
    menuItem.setDocumentReference(myDocRef);
    String menuPart = "menuPartTest";
    nav.setMenuPart(menuPart);
    navFilterMock.setMenuPart(eq(menuPart));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
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
        getXContext().getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(menuSpace))).andReturn(menuSpaceRef).anyTimes();
    replayDefault();
    nav.setMenuSpace(menuSpace);
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(":testMenuSpace:Space.TestName"));
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_ignore_invalid_value() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(0);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 3, nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_smaller() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(2);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 2, nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testSetFromHierarchyLevel_bigger() {
    nav.fromHierarchyLevel = 3;
    replayDefault();
    nav.setFromHierarchyLevel(10);
    assertEquals("ignore invalid value in setFromHierarchyLevel", 10, nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testGetPageTypeConfigName_integrationTest() throws Exception {
    nav.injected_PageTypeResolverService = null;
    ComponentManager componentManager = Utils.getComponentManager();
    ComponentDescriptor<IPageTypeRole> ptServiceDesc = componentManager.getComponentDescriptor(
        IPageTypeRole.class, "default");
    IPageTypeRole ptServiceMock = createDefaultMock(IPageTypeRole.class);
    componentManager.registerComponent(ptServiceDesc, ptServiceMock);
    BaseObject ptObj = new BaseObject();
    ptObj
        .setXClassReference(new PageTypeClasses().getPageTypeClassRef(getXContext().getDatabase()));
    ptObj.setStringValue(PageTypeClasses.PAGE_TYPE_FIELD, "TestPageType");
    currentDoc.addXObject(ptObj);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(currentDocRef))).andReturn(currentDoc);
    String testPageType = "TestPageType";
    expect(ptServiceMock.getPageTypeReference(testPageType)).andReturn(Optional.of(
        new PageTypeReference(testPageType, "myTestProvider", Collections.<String>emptyList())));
    replayDefault();
    assertEquals(testPageType, nav.getPageTypeConfigName(currentDocRef));
    verifyDefault();
    componentManager.release(ptServiceMock);
  }

  @Test
  public void testOpenMenuItemOut_notActive() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false, 2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " cel_nav_nodeSpace_MySpace cel_nav_nodeName_MyMenuItemDoc" + " myUltimativePageType\">",
        outStream.toString());
    verifyDefault();
  }

  @Test
  public void testIsRestrictedRights() throws Exception {
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andThrow(new XWikiException());
    replayDefault();
    assertFalse(nav.isRestrictedRights(docRef));
    verifyDefault();
  }

  @Test
  public void testOpenMenuItemOut_restrictedAccessRights() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(false).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false, 2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " cel_nav_nodeSpace_MySpace cel_nav_nodeName_MyMenuItemDoc myUltimativePageType"
        + " cel_nav_restricted_rights\">", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testOpenMenuItemOut_active() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu"), docRef));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false, 2);
    assertEquals("<li class=\"cel_nav_even cel_nav_item2 cel_nav_hasChildren"
        + " cel_nav_nodeSpace_MySpace cel_nav_nodeName_MyMenuItemDoc myUltimativePageType"
        + " active\">", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testGetMenuPartForLevel_firstLevel() {
    nav.setMenuPart("menuPartTest");
    assertEquals("menuPart must be concidered on first level.", "menuPartTest",
        nav.getMenuPartForLevel(1));
  }

  @Test
  public void testGetMenuPartForLevel_secondLevel() {
    nav.setMenuPart("menuPartTest");
    assertEquals("menuPart must only be concidered on first level.", "", nav.getMenuPartForLevel(
        2));
  }

  @Test
  public void testSetLayoutType_unknown() {
    try {
      nav.setLayoutType("blabliType");
      fail("Expected UnknownLayoutTypeException for blabliType layoutType.");
    } catch (UnknownLayoutTypeException e) {
      // expected
    }
  }

  @Test
  public void testSetLayoutType_LIST_LAYOUT_TYPE() throws UnknownLayoutTypeException {
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
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(menuItem.getDocumentReference(), false, false, false,
        false, 2);
    verifyDefault();
    assertFalse("Expected to NOT find the cmCSSclass. [" + cssClasses + "]", (" " + cssClasses
        + " ").contains(" cel_cm_navigation_menuitem "));
  }

  @Test
  public void testGetCssClasses_pageType() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 3);
    verifyDefault();
    assertTrue("Expected to found pageType in css classes. [" + cssClasses + "]", (" " + cssClasses
        + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_pageLayout() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        getXContext().getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 2);
    verifyDefault();
    assertTrue("Expected to found pageLayout in css classes. [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" layout_MyLayout "));
  }

  @Test
  public void testGetCssClasses_pageSpace() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        getXContext().getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 2);
    verifyDefault();
    assertTrue("Expected to found page-space in css classes. [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_nodeSpace_MySpace"));
  }

  @Test
  public void testGetCssClasses_pageName() throws XWikiException {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
        anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
            getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        getXContext().getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(
        "MySpace.MyMenuItemDoc"), same(getXContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false, 2);
    verifyDefault();
    assertTrue("Expected to found page-space in css classes. [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_nodeName_MyMenuItemDoc"));
  }

  @Test
  public void testGetCssClasses_NullName() throws XWikiException {
    String pageType = "myUltimativePageType";
    // FIXME getDocumentParentsList not needed anymore?
    // expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
    // anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
    // getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, false, 3);
    verifyDefault();
    assertFalse("Expected to not find pageType (because fullName is null) in css classes." + " ["
        + cssClasses + "]", (" " + cssClasses + " ").contains(" " + pageType + " "));
  }

  @Test
  public void testGetCssClasses_hasChildren() throws XWikiException {
    // FIXME getDocumentParentsList not needed anymore?
    // expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
    // anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
    // getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, false, 2);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_hasChildren' (because not a leaf) in css" + " classes. ["
        + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_hasChildren "));
  }

  @Test
  public void testGetCssClasses_isLeaf() throws XWikiException {
    // FIXME getDocumentParentsList not needed anymore?
    // expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class),
    // anyBoolean())).andReturn(Arrays.asList(getDocRefForDocName("bla"),
    // getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 3);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_isLeaf' (because no children) in css classes." + " ["
        + cssClasses + "]", (" " + cssClasses + " ").contains(" cel_nav_isLeaf "));
  }

  @Test
  public void testGetCssClasses_numItem_odd() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 3);
    verifyDefault();
    assertFalse("Expected NOT to find 'cel_nav_even' in css classes." + " [" + cssClasses + "]",
        (" " + cssClasses + " ").contains(" cel_nav_even "));
    assertTrue("Expected to find 'cel_nav_odd' in css classes." + " [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_odd "));
  }

  @Test
  public void testGetCssClasses_numItem_even() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 4);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_even' in css classes." + " [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_even "));
    assertFalse("Expected NOT to find 'cel_nav_odd' in css classes." + " [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_odd "));
  }

  @Test
  public void testGetCssClasses_numItem() throws XWikiException {
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, false, false, true, 4321);
    verifyDefault();
    assertTrue("Expected to find 'cel_nav_item4321' in css classes." + " [" + cssClasses + "]", (" "
        + cssClasses + " ").contains(" cel_nav_item4321 "));
  }

  @Test
  public void testGetCssClassTokens_withoutContextualState() throws XWikiException {
    String pageType = "myPageType";
    DocumentReference docRef = currentDocRef;
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(docRef)))
        .andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(getXContext()))).andReturn(true);
    replayDefault();
    List<String> tokens = nav.getCssClassTokens(new NavigationItemContext(docRef,
        ContainerCssClasses.INCLUDE, Position.ONLY, ChildState.LEAF, 1, ContextualState.OMIT));
    verifyDefault();
    assertEquals(Arrays.asList("cel_cm_navigation_menuitem", "first", "last", "cel_nav_odd",
        "cel_nav_item1", "cel_nav_isLeaf", "cel_nav_nodeSpace_MySpace",
        "cel_nav_nodeName_MyCurrentDoc", pageType), tokens);
    assertFalse(tokens.contains("currentPage"));
    assertFalse(tokens.contains("active"));
  }

  @Test
  public void testGetCssClassTokens_exactPresentationMetadataAndContextSuppression()
      throws XWikiException {
    nav.setCMcssClass("cel_cm_presentation_treenode");
    PageTypeReference pageTypeRef = createDefaultMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(currentDocRef)))
        .andReturn(pageTypeRef).times(2);
    expect(pageTypeRef.getConfigName()).andReturn("SomePageType").times(2);
    SpaceReference layoutRef = new SpaceReference("About-Layout",
        new SpaceReference("Layouts", currentDocRef.getWikiReference()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(currentDocRef))).andReturn(layoutRef).times(2);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), eq(true)))
        .andReturn(Collections.emptyList());
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(getXContext()))).andReturn(false).times(2);
    replayDefault();
    List<String> restTokens = nav.getCssClassTokens(new NavigationItemContext(currentDocRef,
        ContainerCssClasses.INCLUDE, Position.ONLY, ChildState.LEAF, 1,
        ContextualState.OMIT));
    List<String> legacyTokens = nav.getCssClassTokens(new NavigationItemContext(currentDocRef,
        ContainerCssClasses.INCLUDE, Position.ONLY, ChildState.LEAF, 1,
        ContextualState.INCLUDE));
    verifyDefault();
    assertEquals(Arrays.asList("cel_cm_presentation_treenode", "first", "last", "cel_nav_odd",
        "cel_nav_item1", "cel_nav_isLeaf", "cel_nav_nodeSpace_MySpace",
        "cel_nav_nodeName_MyCurrentDoc", "SomePageType", "layout_About-Layout",
        "cel_nav_restricted_rights"), restTokens);
    assertEquals(Arrays.asList("cel_cm_presentation_treenode", "first", "last", "cel_nav_odd",
        "cel_nav_item1", "cel_nav_isLeaf", "cel_nav_nodeSpace_MySpace",
        "cel_nav_nodeName_MyCurrentDoc", "currentPage", "SomePageType",
        "layout_About-Layout", "active", "cel_nav_restricted_rights"), legacyTokens);
  }

  @Test
  public void testGetUniqueId_exactPresentationNavigationNumberVariants() {
    SpaceReference menuSpace = new SpaceReference("Content", currentDocRef.getWikiReference());
    DocumentReference slideRef = new DocumentReference(getXContext().getDatabase(), "Content",
        "First");
    nav.setNodeSpace(menuSpace);
    Navigation secondNavigation = new Navigation("N2");
    secondNavigation.setNodeSpace(menuSpace);
    replayDefault();
    assertEquals("N1:Content:Content.First", nav.getUniqueId(slideRef));
    assertEquals("N2:Content:Content.First", secondNavigation.getUniqueId(slideRef));
    verifyDefault();
  }

  @Test
  public void testGetCssClasses_preservesConfiguredWhitespace() throws XWikiException {
    nav.setCMcssClass("  alpha   beta  ");
    replayDefault();
    String cssClasses = nav.getCssClasses(null, true, true, false, true, 1);
    List<String> tokens = nav.getCssClassTokens(new NavigationItemContext(null,
        ContainerCssClasses.INCLUDE, Position.FIRST, ChildState.LEAF, 1,
        ContextualState.INCLUDE));
    verifyDefault();
    assertEquals("alpha   beta   first cel_nav_odd cel_nav_item1 cel_nav_isLeaf", cssClasses);
    assertEquals(Arrays.asList("alpha", "beta", "first", "cel_nav_odd", "cel_nav_item1",
        "cel_nav_isLeaf"), tokens);
  }

  @Test
  public void testGetMenuSpace() {
    List<TreeNode> emptyMenuItemList = Collections.emptyList();
    String parentSpaceName = "MyParentSpace";
    String spaceName = "MySpace";
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(true);
    expect(wUServiceMock.getParentSpace(eq(spaceName))).andReturn(parentSpaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        emptyMenuItemList);
    nav.setMenuPart("");
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    SpaceReference parentSpaceRef = new SpaceReference(parentSpaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(parentSpaceName))).andReturn(
        parentSpaceRef).anyTimes();
    replayDefault();
    String menuSpace = nav.getMenuSpace(getXContext());
    verifyDefault();
    assertEquals("Expected to receive parentSpace [" + parentSpaceName + "]", parentSpaceName,
        menuSpace);
  }

  @Test
  public void testIsActiveMenuItem_isActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu"), currentDocRef));
    replayDefault();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_isActive_currentDoc() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu")));
    replayDefault();
    assertTrue(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(getXContext().getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu")));
    replayDefault();
    assertFalse(nav.isActiveMenuItem(menuItem.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void testIsActiveMenuItem_menuItemNULL() {
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu"), null));
    replayDefault();
    assertFalse(nav.isActiveMenuItem(null));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu"), currentDocRef));
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, getXContext()));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(getXContext().getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu")));
    replayDefault();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, getXContext()));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_ShowAll() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(getXContext().getDatabase(), "MySpace",
        "isNotActiveDoc"));
    // FIXME getDocumentParentsList not needed anymore?
    // expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
    // ).andReturn(Arrays.asList(getDocRefForDocName("bla"),
    // getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    nav.setShowAll(true);
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 1, getXContext()));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_showHierarchyLevel() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(getXContext().getDatabase(), "MySpace",
        "isNotActiveDoc"));
    // FIXME getDocumentParentsList not needed anymore?
    // expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
    // ).andReturn(Arrays.asList(getDocRefForDocName("bla"),
    // getDocRefForDocName("bli"), getDocRefForDocName("blu")));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 2, getXContext()));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isNOTActive_showHierarchyLevel_Over() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(new DocumentReference(getXContext().getDatabase(), "MySpace",
        "isNotActiveDoc"));
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu")));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayDefault();
    assertFalse(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 3, getXContext()));
    verifyDefault();
  }

  @Test
  public void testShowSubmenuForMenuItem_isActive_showHierarchyLevel_Over() {
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"), getDocRefForDocName(
            "blu"), currentDocRef));
    nav.setShowAll(false);
    nav.setShowInactiveToLevel(3);
    replayDefault();
    assertTrue(nav.showSubmenuForMenuItem(menuItem.getDocumentReference(), 5, getXContext()));
    verifyDefault();
  }

  @Test
  public void testAddUlCSSClass() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    replayDefault();
    assertEquals("class=\"mainCss firstCss secondCss\"", nav.getMainUlCSSClasses().trim());
    verifyDefault();
  }

  @Test
  public void testAddUlCSSClass_double_add() throws Exception {
    nav.addUlCSSClass("mainCss");
    nav.addUlCSSClass("firstCss");
    nav.addUlCSSClass("secondCss");
    nav.addUlCSSClass("mainCss"); // double add existing class
    replayDefault();
    assertEquals("class=\"mainCss firstCss secondCss\"", nav.getMainUlCSSClasses().trim());
    verifyDefault();
  }

  @Test
  public void testGetNavLanguage_context_Language() {
    getXContext().setLanguage("de");
    replayDefault();
    assertEquals("de", nav.getNavLanguage());
    verifyDefault();
  }

  @Test
  public void testGetNavLanguage_navLanguage() {
    getXContext().setLanguage("de");
    nav.setLanguage("fr");
    replayDefault();
    assertEquals("fr", nav.getNavLanguage());
    verifyDefault();
  }

  @Test
  public void testGetMenuLink_Content_WebHome() throws Exception {
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "Content",
        "WebHome");
    expect(wikMock.getURL(eq(docRef), eq("view"), same(getXContext()))).andReturn(""); // BUG
                                                                                       // IN
                                                                                       // XWIKI
                                                                                       // !!!
    replayDefault();
    assertEquals("/", nav.getMenuLink(docRef));
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_defaults() {
    DocumentReference cellConfigDocRef = new DocumentReference(getXContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    String spaceName = "MySpace";
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(wUServiceMock.resolveSpaceReference(eq(spaceName))).andReturn(mySpaceRef).anyTimes();
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(getXContext()));
    assertEquals("default for fromHierarchyLevel must be greater than zero.", 1,
        nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace() {
    DocumentReference cellConfigDocRef = new DocumentReference(getXContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    String nodeSpaceName = "theMenuSpace";
    navConfigObj.setStringValue("menu_space", nodeSpaceName);
    SpaceReference parentSpaceRef = new SpaceReference(nodeSpaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(wUServiceMock.resolveSpaceReference(eq(nodeSpaceName))).andReturn(
        parentSpaceRef).anyTimes();
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("theMenuSpace", nav.getMenuSpace(getXContext()));
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace_empty() {
    DocumentReference cellConfigDocRef = new DocumentReference(getXContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(getXContext()));
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_presentationType_notEmpty() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getXContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    navConfigObj.setStringValue(INavigationClassConfig.PRESENTATION_TYPE_FIELD,
        "testPresentationType");
    IPresentationTypeRole componentInstance = registerComponentMock(
        IPresentationTypeRole.class);
    replayDefault();
    nav.loadConfigFromObject(navConfigObj);
    verifyDefault();
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testLoadConfig_defaults() {
    String spaceName = "MySpace";
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    NavigationConfig navConfig = new NavigationConfig.Builder().nodeSpaceRef(mySpaceRef).build();
    replayDefault();
    nav.loadConfig(navConfig);
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    assertEquals("MySpace", nav.getNodeSpaceRef().getName());
    assertEquals("default for fromHierarchyLevel must be greater than zero.", 1,
        nav.fromHierarchyLevel);
    verifyDefault();
  }

  @Test
  public void testLoadConfig_menuSpace() {
    String nodeSpaceName = "theMenuSpace";
    SpaceReference parentSpaceRef = new SpaceReference(nodeSpaceName, new WikiReference(
        getXContext().getDatabase()));
    NavigationConfig navConfig = new NavigationConfig.Builder().nodeSpaceRef(
        parentSpaceRef).build();
    replayDefault();
    nav.loadConfig(navConfig);
    assertEquals(parentSpaceRef, nav.getNodeSpaceRef());
    assertEquals("theMenuSpace", nav.getNodeSpaceRef().getName());
    verifyDefault();
  }

  @Test
  public void testLoadConfig_menuSpace_empty() {
    String spaceName = "MySpace";
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    NavigationConfig navConfig = new NavigationConfig.Builder().nodeSpaceRef(mySpaceRef).build();
    replayDefault();
    nav.loadConfig(navConfig);
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    assertEquals("MySpace", nav.getNodeSpaceRef().getName());
    verifyDefault();
  }

  @Test
  public void testLoadConfig_presentationType_notEmpty() throws Exception {
    IPresentationTypeRole componentInstance = registerComponentMock(
        IPresentationTypeRole.class);
    String presentationTypeHint = "testPresentationType";
    NavigationConfig navConfig = new NavigationConfig.Builder().presentationTypeHint(
        presentationTypeHint).build();
    replayDefault();
    nav.loadConfig(navConfig);
    assertEquals(componentInstance, nav.getPresentationType());
    verifyDefault();
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType() throws Exception {
    IPresentationTypeRole componentInstance = registerComponentMock(
        IPresentationTypeRole.class);
    replayDefault();
    nav.setPresentationType("testPresentationType");
    verifyDefault();
    assertSame(componentInstance, nav.getPresentationType());
  }

  @Test
  public void testSetPresentationType_null() throws Exception {
    IPresentationTypeRole componentInstance = createDefaultMock(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayDefault();
    nav.setPresentationType((String) null);
    assertNotNull(nav.getPresentationType());
    assertEquals(DefaultPresentationType.class, nav.getPresentationType().getClass());
    verifyDefault();
  }

  @Test
  public void testSetPresentationType_NotFoundException() throws Exception {
    replayDefault();
    nav.setPresentationType("testNotFoundPresentationType");
    verifyDefault();
  }

  @Test
  public void testWriteMenuItemContent_PresentationType() throws Exception {
    IPresentationTypeRole<INavigation> componentInstance = registerComponentMock(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    StringBuilder outStream = new StringBuilder();
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    componentInstance.writeNodeContent(same(outStream), eq(true), eq(false), eq(docRef), eq(true),
        eq(1), same(nav));
    expectLastCall().once();
    replayDefault();
    nav.writeMenuItemContent(outStream, true, false, docRef, true, 1);
    verifyDefault();
  }

  @Test
  public void testGetCMcssClass_default() {
    IPresentationTypeRole componentInstance = createDefaultMock(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    expect(componentInstance.getDefaultCssClass()).andReturn("cel_cm_menu").atLeastOnce();
    replayDefault();
    assertEquals("cel_cm_menu", nav.getCMcssClass());
    verifyDefault();
  }

  @Test
  public void testGetCMcssClass() {
    IPresentationTypeRole componentInstance = createDefaultMock(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    replayDefault();
    nav.setCMcssClass("cm_test_class");
    assertEquals("cm_test_class", nav.getCMcssClass());
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName_null() {
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(null);
    replayDefault();
    assertEquals("", nav.getPageLayoutName(docRef));
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName_overwritePresentationType() {
    IPresentationTypeRole componentInstance = createDefaultMock(
        IPresentationTypeRole.class);
    nav.setPresentationType(componentInstance);
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    SpaceReference overwriteLayoutRef = new SpaceReference("MyOverwriteLayout", new WikiReference(
        getXContext().getDatabase()));
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        getXContext().getDatabase()));
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(docRef))).andReturn(layoutRef).anyTimes();
    expect(componentInstance.getPageLayoutForDoc(eq(docRef))).andReturn(overwriteLayoutRef);
    replayDefault();
    assertEquals("layout_MyOverwriteLayout", nav.getPageLayoutName(docRef));
    verifyDefault();
  }

  @Test
  public void testGetPageLayoutName() {
    DocumentReference docRef = new DocumentReference(getXContext().getDatabase(), "MySpace",
        "MyMenuItemDoc");
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        getXContext().getDatabase()));
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
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockRightService.hasAccessLevel(eq("edit"), eq(myUserName), eq("MySpace.MyCurrentDoc"),
        same(getXContext()))).andReturn(true);
    expect(wUServiceMock.getAdminMessageTool()).andReturn(getXContext().getMessageTool())
        .anyTimes();
    ((TestMessageTool) getXContext().getMessageTool()).injectMessage("cel_nav_nomenuitems",
        "No Navitems found.");
    replayDefault();
    // getXContext().setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it
    // must be
    // set after calling replay
    getXContext().setUser(myUserName);
    assertEquals("no menuitem for level 1. Yet with hasEdit, thus no empty string" + " expected.",
        "<ul class=\"cel_nav_empty\">" + "<li class=\"first last cel_nav_odd cel_nav_item1"
            + " cel_nav_hasChildren\">" + "<span id=\"N1:MySpace::\" "
            + " class=\"cel_cm_navigation_menuitem first last cel_nav_odd cel_nav_item1"
            + " cel_nav_hasChildren\">" + "No Navitems found.</span></li></ul>",
        nav.includeNavigation());
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
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    DocumentReference homeDocRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "Home");
    List<TreeNode> mainNodeList = Arrays.asList(new TreeNode(homeDocRef, null, 1));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        mainNodeList);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(mainNodeList);
    expect(tNServiceMock.getSubNodesForParent(eq("MySpace.Home"), eq(spaceName), same(
        navFilterMock))).andReturn(Collections.<TreeNode>emptyList());
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(homeDocRef))).andReturn(
        new PageTypeReference("RichText", "test", Collections.<String>emptyList())).atLeastOnce();
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(homeDocRef))).andReturn(null).atLeastOnce();
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())).andReturn(
        Collections.<DocumentReference>emptyList()).atLeastOnce();
    expect(wikMock.getURL(eq(homeDocRef), eq("view"), same(getXContext())))
        .andReturn("/Home");
    expect(wikMock.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(
        getXContext()))).andReturn(0);
    XWikiDocument homeDoc = new XWikiDocument(homeDocRef);
    homeDoc.setTitle("HomeTitle");
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(homeDocRef))
        .andReturn(homeDoc).anyTimes();
    expect(getMock(IModelAccessFacade.class).getDocumentOpt(homeDocRef))
        .andReturn(java.util.Optional.of(homeDoc)).anyTimes();
    expect(getMock(IModelAccessFacade.class).getDocumentOpt(homeDocRef, "de"))
        .andReturn(java.util.Optional.empty()).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq("MySpace.Home"),
        same(getXContext()))).andReturn(true).atLeastOnce();
    String dictMenuNameKey = "menuname_MySpace.Home";
    getMessageToolStub().injectMessage(dictMenuNameKey, dictMenuNameKey);
    expect(wUServiceMock.getAdminMessageTool()).andReturn(getMessageToolStub()).anyTimes();
    replayDefault();
    assertEquals("one tree node for level 1. Thus output expected.", "<ul"
        + " id=\"CN1:MySpace::\" ><li class=\"first last cel_nav_odd cel_nav_item1"
        + " cel_nav_isLeaf cel_nav_nodeSpace_MySpace cel_nav_nodeName_Home"
        + " RichText\"><a href=\"/Home\" class=\"cel_cm_navigation_menuitem first last"
        + " cel_nav_odd cel_nav_item1 cel_nav_isLeaf"
        + " cel_nav_nodeSpace_MySpace cel_nav_nodeName_Home RichText\""
        + " id=\"N1:MySpace:MySpace.Home\">HomeTitle</a></li></ul>",
        nav.includeNavigation());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_true() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    DocumentReference parentRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
    replayDefault();
    assertTrue(nav.isEmpty());
    verifyDefault();
  }

  @Test
  public void testIsEmpty_false() {
    String spaceName = "MySpace";
    nav.fromHierarchyLevel = 3;
    nav.toHierarchyLevel = 4;
    DocumentReference parentRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    DocumentReference subNodeRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "SubNodeDoc");
    List<TreeNode> nodeList = Arrays.asList(new TreeNode(subNodeRef, null, 1));
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))).andReturn(
        nodeList);
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
    DocumentReference parentRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(3))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
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
    DocumentReference parentRef = new DocumentReference(getXContext().getDatabase(), spaceName,
        "myDocument");
    expect(wUServiceMock.getParentForLevel(eq(1))).andReturn(parentRef).atLeastOnce();
    navFilterMock.setMenuPart(eq("myPart"));
    expectLastCall().anyTimes();
    expect(tNServiceMock.getSubNodesForParent(eq(parentRef), same(navFilterMock))).andReturn(
        Collections.<TreeNode>emptyList());
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
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(expectedMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        expectedMenuItemsList).once();
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
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
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
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
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
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode4, treeNode5);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_withPaging_offsetOutOfBounds() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(3);
    nav.setOffset(8);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
    replayDefault();
    assertEquals(0, nav.getCurrentMenuItems(1, "").size());
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
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetCurrentMenuItems_NoPaging_mainMenu_NONzeroOffset() {
    nav.fromHierarchyLevel = 1;
    nav.toHierarchyLevel = 1;
    nav.setMenuPart("");
    nav.setNumberOfItem(-1);
    nav.setOffset(3);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().anyTimes();
    String spaceName = "MySpace";
    String wikiName = getXContext().getDatabase();
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    DocumentReference docRef1 = new DocumentReference(wikiName, spaceName, "myPage1");
    DocumentReference docRef2 = new DocumentReference(wikiName, spaceName, "myPage2");
    DocumentReference docRef3 = new DocumentReference(wikiName, spaceName, "myPage3");
    DocumentReference docRef4 = new DocumentReference(wikiName, spaceName, "myPage4");
    DocumentReference docRef5 = new DocumentReference(wikiName, spaceName, "myPage5");
    TreeNode treeNode1 = new TreeNode(docRef1, mySpaceRef, 1);
    TreeNode treeNode2 = new TreeNode(docRef2, mySpaceRef, 2);
    TreeNode treeNode3 = new TreeNode(docRef3, mySpaceRef, 3);
    TreeNode treeNode4 = new TreeNode(docRef4, mySpaceRef, 4);
    TreeNode treeNode5 = new TreeNode(docRef5, mySpaceRef, 5);
    List<TreeNode> allMenuItemsList = Arrays.asList(treeNode1, treeNode2, treeNode3, treeNode4,
        treeNode5);
    expect(tNServiceMock.getSubNodesForParent(eq(""), eq(spaceName), same(
        navFilterMock))).andReturn(allMenuItemsList).once();
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))).andReturn(
        allMenuItemsList).once();
    replayDefault();
    List<TreeNode> expectedMenuItemsList = Arrays.asList(treeNode4, treeNode5);
    assertEquals(expectedMenuItemsList, nav.getCurrentMenuItems(1, ""));
    verifyDefault();
  }

  @Test
  public void testGetEffectiveNumberOfItems_empty() {
    nav.inject_navInclude("");
    assertEquals(0, nav.getEffectiveNumberOfItems());
  }

  @Test
  public void testGetEffectiveNumberOfItems_noItems() {
    nav.inject_navInclude("<div>xyz</div>");
    assertEquals(0, nav.getEffectiveNumberOfItems());
  }

  @Test
  public void testGetEffectiveNumberOfItems_hasElems() {
    nav.inject_navInclude("<ul><li><ul><li>x</li></ul></li><li><div>xyz</div></li></ul>");
    assertEquals(2, nav.getEffectiveNumberOfItems());
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private DocumentReference getDocRefForDocName(String docName) {
    return new DocumentReference(getXContext().getDatabase(), "MySpace", docName);
  }

  private INavigationClassConfig getNavClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
