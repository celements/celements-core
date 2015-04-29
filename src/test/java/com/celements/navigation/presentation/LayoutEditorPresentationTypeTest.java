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
package com.celements.navigation.presentation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.CellsClasses;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.PageTypeResolverService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class LayoutEditorPresentationTypeTest extends AbstractBridgedComponentTestCase {

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
  private LayoutEditorPresentationType layoutEditorPresType;
  private PageLayoutCommand mockLayoutCmd;
  private XWikiRightService mockRightService;

  @Before
  public void setUp_DefaultPresentationTypeTest() throws Exception {
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = new Navigation("N1");
    navFilterMock = createMockAndAddToDefault(InternalRightsFilter.class);
    nav.setNavFilter(navFilterMock);
    mockLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    nav.pageLayoutCmd = mockLayoutCmd;
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
    layoutEditorPresType = (LayoutEditorPresentationType) Utils.getComponent(
        IPresentationTypeRole.class, "layoutEditor");
    mockRightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentDoc
        ).anyTimes();
    expect(xwiki.getDocument(eq("MySpace.MyCurrentDoc"), same(context))).andReturn(
        currentDoc).anyTimes();
  }

  @Test
  public void testAppendMenuItemLink() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.getURL(eq(currentDocRef), eq("view"), same(context))
        ).andReturn("/MySpace/MyCurrentDoc");
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    MultilingualMenuNameCommand menuNameCmdMock = createMockAndAddToDefault(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    layoutEditorPresType.menuNameCmd = menuNameCmdMock;
    String menuName = "My Current Doc";
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn(menuName).atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(currentDocRef))).andReturn(null);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    layoutEditorPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false, 1, nav);
    assertEquals("<a href=\"/MySpace/MyCurrentDoc\""
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_odd cel_nav_item1"
        + " cel_nav_hasChildren currentPage"
        + " myUltimativePageType active\" id=\"N1:MySpace:MySpace.MyCurrentDoc\""
        + ">My Current Doc</a>", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testAppendMenuItemLink_withCellId() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.getURL(eq(currentDocRef), eq("view"), same(context))
        ).andReturn("/MySpace/MyCurrentDoc");
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    MultilingualMenuNameCommand menuNameCmdMock = createMockAndAddToDefault(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    layoutEditorPresType.menuNameCmd = menuNameCmdMock;
    String menuName = "My Current Doc";
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn(menuName).atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(currentDocRef))).andReturn(null);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(context))).andReturn(true).atLeastOnce();
    BaseObject cellPropertiesObj = new BaseObject();
    cellPropertiesObj.setXClassReference(new CellsClasses().getCellClassRef(
        context.getDatabase()));
    cellPropertiesObj.setStringValue(CellsClasses.CELLCLASS_IDNAME_FIELD, "cellTestId");
    currentDoc.addXObject(cellPropertiesObj);
    replayDefault();
    layoutEditorPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false, 1, nav);
    assertEquals("<a href=\"/MySpace/MyCurrentDoc\""
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_odd cel_nav_item1"
        + " cel_nav_hasChildren currentPage"
        + " myUltimativePageType active\" id=\"N1:MySpace:MySpace.MyCurrentDoc\""
        + ">My Current Doc (#cellTestId)</a>", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testAppendMenuItemLink_use_navImages() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(currentDocRef))
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
    MultilingualMenuNameCommand menuNameCmdMock = createMockAndAddToDefault(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    layoutEditorPresType.menuNameCmd = menuNameCmdMock;
    String menuName = "My Current Doc";
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn(menuName).atLeastOnce();
    expect(menuNameCmdMock.addNavImageStyle(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn("style=\"background-image:url(abc);\"").atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(currentDocRef))).andReturn(null);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    layoutEditorPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false, 1, nav);
    assertEquals("<a href=\"/MySpace/MyCurrentDoc\""
        + " style=\"background-image:url(abc);\""
        + " class=\"cel_cm_navigation_menuitem first last cel_nav_odd cel_nav_item1"
        + " cel_nav_hasChildren currentPage"
        + " myUltimativePageType active\" id=\"N1:MySpace:MySpace.MyCurrentDoc\""
        + ">My Current Doc</a>", outStream.toString());
    verifyDefault();
  }

  @Test
  public void testAppendMenuItemLink_noLink() throws Exception {
    String pageType = "myUltimativePageType";
    BaseObject menuItem = new BaseObject();
    menuItem.setDocumentReference(currentDocRef);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = true;
    PageTypeReference pageTypeRef = createMockAndAddToDefault(PageTypeReference.class);
    expect(ptResolverServiceMock.getPageTypeRefForDocWithDefault(eq(currentDocRef))
        ).andReturn(pageTypeRef);
    expect(pageTypeRef.getConfigName()).andReturn(pageType);
    expect(wUServiceMock.getDocumentParentsList(eq(currentDocRef), anyBoolean())
        ).andReturn(Arrays.asList(getDocRefForDocName("bla"), getDocRefForDocName("bli"),
            getDocRefForDocName("blu")));
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    nav.setHasLink(false);
    expect(xwiki.getSpacePreferenceAsInt(eq("use_navigation_images"), eq(0), same(context)
        )).andReturn(0);
    MultilingualMenuNameCommand menuNameCmdMock = createMockAndAddToDefault(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    layoutEditorPresType.menuNameCmd = menuNameCmdMock;
    expect(menuNameCmdMock.getMultilingualMenuName(eq(currentDoc.getFullName()), eq("de"),
        same(context))).andReturn("My Current Doc").atLeastOnce();
    expect(menuNameCmdMock.addToolTip(eq(currentDoc.getFullName()), eq("de"), same(context
        ))).andReturn("").atLeastOnce();
    navFilterMock.setMenuPart(eq(""));
    expectLastCall().atLeastOnce();
    String spaceName = "MySpace";
    EntityReference mySpaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    expect(tNServiceMock.getSubNodesForParent(eq(mySpaceRef), same(navFilterMock))
        ).andReturn(Collections.<TreeNode>emptyList());
    expect(wUServiceMock.hasParentSpace(eq(spaceName))).andReturn(false);
    expect(mockLayoutCmd.getPageLayoutForDoc(eq(currentDocRef))).andReturn(null);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("MySpace.MyCurrentDoc"), same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    layoutEditorPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), true, 1, nav);
    assertEquals("<span class=\"cel_cm_navigation_menuitem first last cel_nav_odd"
        + " cel_nav_item1 cel_nav_isLeaf"
        + " currentPage myUltimativePageType active\" id=\"N1:MySpace:MySpace."
        + "MyCurrentDoc\">My Current Doc</span>", outStream.toString());
    verifyDefault();
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private DocumentReference getDocRefForDocName(String docName) {
    return new DocumentReference(context.getDatabase(), "MySpace", docName);
  }

}
