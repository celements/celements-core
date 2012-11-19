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
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.PageTypeResolverService;
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
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
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
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
    replayAll();
    assertTrue(nav.getUniqueId(menuItemName).endsWith(":menuPartTest:"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_null_menuSpace() {
    String menuItemName = null;
    nav.setMenuPart("menuPartTest");
    nav.setMenuSpace("testMenuSpace");
    replayAll();
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
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
    replayAll();
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(":Space.TestName"));
    verifyAll();
  }

  @Test
  public void testGetUniqueId_menuSpace() {
    BaseObject menuItem = new BaseObject();
    menuItem.setName("Space.TestName");
    nav.setMenuPart("menuPartTest");
    nav.setMenuSpace("testMenuSpace");
    replayAll();
    assertTrue(nav.getUniqueId(menuItem.getName()).endsWith(
        ":testMenuSpace:Space.TestName"));
    verifyAll();
  }

  @Test
  public void testOpenMenuItemOut_notActive() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    replayAll(pageTypeRef);
    StringBuilder outStream = new StringBuilder();
    nav.openMenuItemOut(outStream, menuItem.getDocumentReference(), false, false, false);
    assertEquals("<li class=\"cel_nav_hasChildren myUltimativePageType\">",
        outStream.toString());
    verifyAll(pageTypeRef);
  }

  private DocumentReference getDocRefForDocName(String docName) {
    return new DocumentReference(context.getDatabase(), "MySpace", docName);
  }

  @Test
  public void testOpenMenuItemOut_active() throws Exception {
    String pageType = "myUltimativePageType";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyMenuItemDoc");
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu"), docRef));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
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
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(docRef))).andReturn(
        pageTypeRef);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
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
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(docRef))).andReturn(
        pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(isA(DocumentReference.class), anyBoolean()
        )).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(docRef);
    replayAll(pageTypeRef);
    String cssClasses = nav.getCssClasses(docRef, true, false, false, false);
    verifyAll(pageTypeRef);
    assertTrue("Expected to found pageType in css classes. ["
        + cssClasses + "]",
        (" " + cssClasses + " ").contains(" " + pageType + " "));
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
    expect(wUServiceMock.hasParentSpace()).andReturn(true);
    expect(wUServiceMock.getParentSpace()).andReturn(parentSpaceName);
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(emptyMenuItemList);
    nav.setMenuPart("");
    nav.testInjectUtils(utils);
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().once();
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
  public void testAppendMenuItemLink() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.getURL(eq(currentDocRef), eq("view"), same(context))
        ).andReturn("/MySpace/MyCurrentDoc");
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    String menuName = "My Current Doc";
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn(menuName).atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
    replayAll(pageTypeRef, menuNameCmdMock);
    nav.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false);
    assertEquals("<a href=\"/MySpace/MyCurrentDoc\""
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_hasChildren currentPage"
        + " myUltimativePageType active\" id=\"N1:MySpace:MySpace.MyCurrentDoc\""
        + ">My Current Doc</a>", outStream.toString());
    verifyAll(pageTypeRef, menuNameCmdMock);
  }

  @Test
  public void testAppendMenuItemLink_use_navImages() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.getURL(eq(currentDocRef), eq("view"), same(context))
        ).andReturn("/MySpace/MyCurrentDoc");
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(1);
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    String menuName = "My Current Doc";
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn(menuName).atLeastOnce();
    expect(menuNameCmdMock.addNavImageStyle(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn("style=\"background-image:url(abc);\"").atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
    replayAll(pageTypeRef, menuNameCmdMock);
    nav.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false);
    assertEquals("<a href=\"/MySpace/MyCurrentDoc\""
        + " style=\"background-image:url(abc);\""
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_hasChildren currentPage"
        + " myUltimativePageType active\" id=\"N1:MySpace:MySpace.MyCurrentDoc\""
        + ">My Current Doc</a>", outStream.toString());
    verifyAll(pageTypeRef, menuNameCmdMock);
  }

  @Test
  public void testAppendMenuItemLink_noLink() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
    expect(ptResolverServiceMock.getDefaultPageTypeRefForDoc(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    nav.setHasLink(false);
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn("My Current Doc").atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    EntityReference mySpaceRef = new SpaceReference("MySpace",
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace()).andReturn(false);
    replayAll(pageTypeRef, menuNameCmdMock);
    nav.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), true);
    assertEquals("<span class=\"cel_cm_navigation_menuitem first last cel_nav_isLeaf"
        + " currentPage myUltimativePageType active\" id=\"N1:MySpace:MySpace."
        + "MyCurrentDoc\">My Current Doc</span>", outStream.toString());
    verifyAll(pageTypeRef, menuNameCmdMock);
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
    assertEquals("de", nav.getNavLanguage(context));
    verifyAll();
  }

  @Test
  public void testGetNavLanguage_navLanguage() {
    context.setLanguage("de");
    nav.setLanguage("fr");
    replayAll();
    assertEquals("fr", nav.getNavLanguage(context));
    verifyAll();
  }

  @Test
  public void testGetMenuLink_Content_WebHome() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Content",
        "WebHome");
    expect(xwiki.getURL(eq(docRef), eq("view"), same(context))
        ).andReturn(""); // BUG IN XWIKI !!!
    replayAll();
    assertEquals("/", nav.getMenuLink(docRef, context));
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
    navConfigObj.setStringValue("menu_space", "theMenuSpace");
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
    replayAll();
    nav.loadConfigFromObject(navConfigObj);
    assertEquals("MySpace", nav.getMenuSpace(context));
    verifyAll();
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private NavigationClasses getNavClasses() {
    return (NavigationClasses) Utils.getComponent(
        IClassCollectionRole.class, "celements.celNavigationClasses");
  }
  
  private void replayAll(Object ... mocks) {
    replay(xwiki, navFilterMock, utils, tNServiceMock, wUServiceMock,
        ptResolverServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, navFilterMock, utils, tNServiceMock, wUServiceMock,
        ptResolverServiceMock);
    verify(mocks);
  }

}
