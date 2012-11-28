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

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
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
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DefaultPresentationTypeTest extends AbstractBridgedComponentTestCase {

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
  private DefaultPresentationType defPresType;

  @Before
  public void setUp_DefaultPresentationTypeTest() throws Exception {
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
    defPresType = (DefaultPresentationType) Utils.getComponent(
        IPresentationTypeRole.class);
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
    PageTypeReference pageTypeRef = createMock(PageTypeReference.class);
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
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    defPresType.menuNameCmd = menuNameCmdMock;
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
    defPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false, nav);
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
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    defPresType.menuNameCmd = menuNameCmdMock;
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
    defPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), false, nav);
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
    MultilingualMenuNameCommand menuNameCmdMock = createMock(
        MultilingualMenuNameCommand.class);
    nav.inject_menuNameCmd(menuNameCmdMock);
    defPresType.menuNameCmd = menuNameCmdMock;
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
    defPresType.appendMenuItemLink(outStream, isFirstItem, isLastItem,
        menuItem.getDocumentReference(), true, nav);
    assertEquals("<span class=\"cel_cm_navigation_menuitem first last cel_nav_isLeaf"
        + " currentPage myUltimativePageType active\" id=\"N1:MySpace:MySpace."
        + "MyCurrentDoc\">My Current Doc</span>", outStream.toString());
    verifyAll(pageTypeRef, menuNameCmdMock);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private DocumentReference getDocRefForDocName(String docName) {
    return new DocumentReference(context.getDatabase(), "MySpace", docName);
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
