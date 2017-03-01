package com.celements.navigation.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.CellsClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class TreeNodeServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki wiki;
  private XWikiStoreInterface mockStore;
  private TreeNodeService treeNodeService;
  private ITreeNodeCache mockTreeNodeCache;
  private GetNotMappedMenuItemsForParentCommand mockGetNotMenuItemCommand;
  private GetMappedMenuItemsForParentCommand mockGetMenuItemCommand;
  private XWikiRightService mockRightService;
  private Map<String, ITreeNodeProvider> backupNodeProviders;

  @Before
  public void setUp_TreeNodeServiceTest() throws Exception {
    context = getContext();
    wiki = getWikiMock();
    mockStore = createMockAndAddToDefault(XWikiStoreInterface.class);
    treeNodeService = (TreeNodeService) Utils.getComponent(ITreeNodeService.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    mockRightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightService).anyTimes();
    mockTreeNodeCache = createMockAndAddToDefault(ITreeNodeCache.class);
    treeNodeService.treeNodeCache = mockTreeNodeCache;
    treeNodeService.execution = Utils.getComponent(Execution.class);
    mockGetNotMenuItemCommand = createMockAndAddToDefault(
        GetNotMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getNotMappedMenuItemsForParentCmd()).andReturn(
        mockGetNotMenuItemCommand).anyTimes();
    mockGetMenuItemCommand = createMockAndAddToDefault(GetMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getMappedMenuItemsForParentCmd()).andReturn(
        mockGetMenuItemCommand).anyTimes();
    backupNodeProviders = treeNodeService.nodeProviders;
    treeNodeService.nodeProviders = new HashMap<>(backupNodeProviders);
  }

  @After
  public void tearDown_TreeNodeServiceTest() throws Exception {
    treeNodeService.nodeProviders = backupNodeProviders;
  }

  @Test
  public void testGetSubNodesForParent() throws Exception {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + ".";
    context.setDatabase(wikiName);
    EntityReference spaceRef = new SpaceReference(spaceName, new WikiReference(
        context.getDatabase()));
    TreeNode treeNode = createTreeNode(spaceName, docName, spaceName, "", 1);
    List<TreeNode> mockTreeNodeList = Arrays.asList(treeNode, null);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        mockTreeNodeList);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(wikiName + ":"
        + spaceName + "." + docName), same(context))).andReturn(true);
    replayDefault();
    List<TreeNode> resultList = treeNodeService.getSubNodesForParent(spaceRef, "");
    assertEquals(1, resultList.size());
    assertTrue(resultList.contains(treeNode));
    verifyDefault();
  }

  @Test
  public void testGetSubNodesForParent_deprecated() throws Exception {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + ".";
    context.setDatabase(wikiName);
    TreeNode treeNode = createTreeNode(spaceName, docName, spaceName, "", 1);
    List<TreeNode> mockTreeNodeList = Arrays.asList(treeNode, null);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        mockTreeNodeList);
    List<TreeNode> emptyList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        emptyList);
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), eq(wikiName + ":"
        + spaceName + "." + docName), same(context))).andReturn(true);
    replayDefault();
    List<TreeNode> resultList = treeNodeService.getSubNodesForParent("", spaceName, "");
    assertEquals(1, resultList.size());
    assertTrue(resultList.contains(treeNode));
    verifyDefault();
  }

  @Test
  public void testFetchNodesForParentKey_mergeCombinedResult() {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + "." + docName;
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);

    TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
    TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
    TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
    TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 5);
    List<TreeNode> mappedList = Arrays.asList(menuItem1, menuItem5), notMappedList = Arrays.asList(
        menuItem2, menuItem3), expectedList = Arrays.asList(menuItem1, menuItem2, menuItem3,
            menuItem5);

    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        notMappedList).once();

    replayDefault();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertEquals("result array does not match expected size.", expectedList.size(),
        menuItemsMerged.size());
    int pos = 0;
    for (TreeNode menuItem : menuItemsMerged) {
      TreeNode expectedMenuitem = expectedList.get(pos++);
      assertEquals("Array compare failed on item " + pos, expectedMenuitem.getPosition(),
          menuItem.getPosition());
      assertSame("Array compare failed on item " + pos, expectedMenuitem, menuItem);
    }
    verifyDefault();
  }

  @Test
  public void testFetchNodesForParentKey_merge_TreeNodeProviders() {
    ITreeNodeProvider nodeProviderMock = createMockAndAddToDefault(ITreeNodeProvider.class);
    treeNodeService.nodeProviders.put("testNodeProvider", nodeProviderMock);

    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + "." + docName;
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);

    TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
    TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
    TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
    TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 5);
    List<TreeNode> mappedList = Collections.emptyList();
    List<TreeNode> nodeProviderList = Arrays.asList(menuItem1, menuItem5);
    List<TreeNode> notMappedList = Arrays.asList(menuItem2, menuItem3);
    List<TreeNode> expectedList = Arrays.asList(menuItem1, menuItem2, menuItem3, menuItem5);

    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        notMappedList).once();
    expect(nodeProviderMock.getTreeNodesForParent(eq(parentKey))).andReturn(
        nodeProviderList).once();

    replayDefault();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertEquals("result array does not match expected size.", expectedList.size(),
        menuItemsMerged.size());
    int pos = 0;
    for (TreeNode menuItem : menuItemsMerged) {
      TreeNode expectedMenuitem = expectedList.get(pos++);
      assertEquals("Array compare failed on item " + pos, expectedMenuitem.getPosition(),
          menuItem.getPosition());
      assertSame("Array compare failed on item " + pos, expectedMenuitem, menuItem);
    }
    verifyDefault();
  }

  // FIXME
  // @Test
  // public void testFetchNodesForParentKey_merge_TreeNodeProviders_duplicate_pos() {
  // ITreeNodeProvider nodeProviderMock = createMockAndAddToDefault(
  // ITreeNodeProvider.class);
  // treeNodeService.nodeProviders.put("testNodeProvider", nodeProviderMock);
  //
  // String wikiName = "myWiki";
  // String spaceName = "mySpace";
  // String docName = "myDoc";
  // String parentKey = wikiName + ":" + spaceName + "." + docName;
  // context.setDatabase(wikiName);
  // DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName,
  // docName);
  //
  // TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
  // TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
  // TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
  // TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 2);
  // List<TreeNode> mappedList = Collections.emptyList();
  // List<TreeNode> nodeProviderList = Arrays.asList(menuItem1, menuItem5);
  // List<TreeNode> notMappedList = Arrays.asList(menuItem2, menuItem3);
  // List<TreeNode> expectedList = Arrays.asList(menuItem1, menuItem2, menuItem3,
  // menuItem5);
  //
  // expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
  // ).andReturn(mappedList).once();
  // expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
  // same(context))).andReturn(notMappedList).once();
  // expect(nodeProviderMock.getTreeNodesForParent(eq(parentKey))).andReturn(
  // nodeProviderList).once();
  //
  // replayDefault();
  // List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
  // assertEquals("result array does not match expected size.", expectedList.size(),
  // menuItemsMerged.size());
  // int pos = 0;
  // for (TreeNode menuItem : menuItemsMerged) {
  // TreeNode expectedMenuitem = expectedList.get(pos++);
  // assertEquals("Array compare failed on item " + pos, expectedMenuitem.getPosition(),
  // menuItem.getPosition());
  // assertSame("Array compare failed on item " + pos, expectedMenuitem, menuItem);
  // }
  // verifyDefault();
  // }
  //
  @Test
  public void testFetchNodesForParentKey_onlyOldArray() throws Exception {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + "." + docName;
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);

    TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
    TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
    List<TreeNode> oldNotMappedList = Arrays.asList(menuItem2, menuItem3);
    List<TreeNode> mappedList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        oldNotMappedList).once();
    replayDefault();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertSame("expecting old notMapped list.", oldNotMappedList, menuItemsMerged);
    verifyDefault();
  }

  @Test
  public void testFetchNodesForParentKey_onlyNewMappedList() {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + "." + docName;
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);

    List<TreeNode> oldMenuItems = Collections.emptyList();
    TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
    TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 5);
    List<TreeNode> mappedList = Arrays.asList(menuItem1, menuItem5);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        oldMenuItems).once();
    replayDefault();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertSame("expecting old notMapped list.", mappedList, menuItemsMerged);
    verifyDefault();
  }

  @Test
  public void testFetchNodesForParentKey_noMenuItems_NPE() {
    String wikiName = "myWiki";
    String spaceName = "mySpace";
    String docName = "myDoc";
    String parentKey = wikiName + ":" + spaceName + "." + docName;
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, docName);
    List<TreeNode> mappedList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        null).once();
    replayDefault();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertNotNull("expecting not null.", menuItemsMerged);
    assertEquals("expecting empty list.", 0, menuItemsMerged.size());
    verifyDefault();
  }

  private TreeNode createTreeNode(String docSpace, String docName, String parentDocSpace,
      String parentDocName, int pos) {
    EntityReference parentRef;
    if (Strings.isNullOrEmpty(parentDocName)) {
      parentRef = new SpaceReference(parentDocSpace, new WikiReference(context.getDatabase()));
    } else {
      parentRef = new DocumentReference(context.getDatabase(), parentDocSpace, parentDocName);
    }
    DocumentReference docRef = new DocumentReference(context.getDatabase(), docSpace, docName);
    return new TreeNode(docRef, parentRef, pos);
  }

  @Test
  public void testResolveEntityReference() {
    String wikiName = getContext().getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    EntityReference wikiEntRef = new EntityReference(wikiName, EntityType.WIKI),
        spaceEntRef = new EntityReference(spaceName, EntityType.SPACE, wikiEntRef),
        docEntRef = new EntityReference(docName, EntityType.DOCUMENT, spaceEntRef);

    assertEquals(docEntRef, treeNodeService.resolveEntityReference(wikiName + ":" + spaceName + "."
        + docName));
    assertEquals(spaceEntRef, treeNodeService.resolveEntityReference(wikiName + ":" + spaceName
        + "."));
    assertEquals(spaceEntRef, treeNodeService.resolveEntityReference(wikiName + ":" + spaceName));
    assertEquals(wikiEntRef, treeNodeService.resolveEntityReference(wikiName + ":"));
    assertEquals(spaceEntRef, treeNodeService.resolveEntityReference(spaceName));
  }

  @Test
  public void testGetNavObjectsFromLayout() throws Exception {
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    SpaceReference layoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(layoutRef);
    DocumentReference mainCellRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "MainCell");
    List<TreeNode> myLayoutMenuItems = Arrays.asList(new TreeNode(mainCellRef, null, 1));
    DocumentReference navConfigDocRef1 = new DocumentReference(context.getDatabase(), "MyLayout",
        "NavigationCell1");
    DocumentReference navConfigDocRef2 = new DocumentReference(context.getDatabase(), "MyLayout",
        "NavigationCell2");
    SpaceReference layoutSpaceRef = new SpaceReference("MyLayout", new WikiReference("xwikidb"));
    List<TreeNode> myLayoutSubMenuItems = Arrays.asList(new TreeNode(navConfigDocRef1,
        layoutSpaceRef, 1), new TreeNode(navConfigDocRef2, layoutSpaceRef, 2));
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq("xwikidb:MyLayout."))).andReturn(
        myLayoutMenuItems);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(
        "xwikidb:MyLayout.MainCell"))).andReturn(myLayoutSubMenuItems);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(
        "xwikidb:MyLayout.NavigationCell1"))).andReturn(Collections.<TreeNode>emptyList());
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(
        "xwikidb:MyLayout.NavigationCell2"))).andReturn(Collections.<TreeNode>emptyList());
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(isA(String.class), same(
        context))).andReturn(Collections.<TreeNode>emptyList()).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    BaseObject navConfigObj1 = new BaseObject();
    DocumentReference navigationConfigClassRef = getNavClassConfig().getNavigationConfigClassRef(
        context.getDatabase());
    XWikiDocument navConfigDoc1 = new XWikiDocument(navConfigDocRef1);
    navConfigObj1.setXClassReference(navigationConfigClassRef);
    navConfigDoc1.addXObject(navConfigObj1);
    expect(wiki.getDocument(eq(navConfigDocRef1), same(context))).andReturn(navConfigDoc1);
    XWikiDocument navConfigDoc2 = new XWikiDocument(navConfigDocRef1);
    BaseObject navConfigObj2 = new BaseObject();
    navConfigObj2.setXClassReference(navigationConfigClassRef);
    navConfigDoc2.addXObject(navConfigObj2);
    expect(wiki.getDocument(eq(navConfigDocRef2), same(context))).andReturn(navConfigDoc2);
    List<BaseObject> expectedConfigObjs = Arrays.asList(navConfigObj1, navConfigObj2);
    replayDefault();
    List<BaseObject> navConfigObjs = treeNodeService.getNavObjectsFromLayout();
    verifyDefault();
    assertNotNull(navConfigObjs);
    assertEquals(expectedConfigObjs, navConfigObjs);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_twoParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    treeNodeService.injectInheritorFactory(inheritorFact);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    context.setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))).andReturn(
        null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(wiki.getDocument(eq(webPrefDocRef), eq(context))).andReturn(webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setXObjects(getNavClassConfig().getNavigationConfigClassRef(context.getDatabase()),
        navObjects);
    expect(wiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault();
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyDefault();
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_deletedObject_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    treeNodeService.injectInheritorFactory(inheritorFact);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    context.setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))).andReturn(
        null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(wiki.getDocument(eq(webPrefDocRef), eq(context))).andReturn(webPrefDoc).atLeastOnce();
    webPrefDoc.setXObject(0, createNavObj(5, webPrefDoc));
    // skipping 1 --> webPrefDoc.setXObject(1, null); // deleting an object can lead to
    // a null pointer in the object list
    webPrefDoc.setXObject(2, createNavObj(8, webPrefDoc));
    webPrefDoc.setXObject(3, createNavObj(3, webPrefDoc));
    expect(wiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault();
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyDefault();
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_noObjectFound_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    treeNodeService.injectInheritorFactory(inheritorFact);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    context.setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))).andReturn(
        null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(wiki.getDocument(eq(webPrefDocRef), eq(context))).andReturn(webPrefDoc).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(wiki.getDocument(eq(xwikiPrefDocRef), eq(context))).andReturn(
        xwikiPrefDoc).atLeastOnce();
    DocumentReference skinDocRef = new DocumentReference(context.getDatabase(), "Skins", "MySkin");
    XWikiDocument skinDoc = new XWikiDocument(skinDocRef);
    expect(wiki.getDocument(eq(skinDocRef), eq(context))).andReturn(skinDoc).atLeastOnce();
    expect(wiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault();
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyDefault();
    assertEquals("Expecting default max level.", Navigation.DEFAULT_MAX_LEVEL, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_threeParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    treeNodeService.injectInheritorFactory(inheritorFact);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    context.setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))).andReturn(
        null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(wiki.getDocument(eq(webPrefDocRef), eq(context))).andReturn(webPrefDoc).atLeastOnce();
    expect(wiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.MySkin").atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setXObjects(getNavClassConfig().getNavigationConfigClassRef(context.getDatabase()),
        navObjects);
    replayDefault();
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyDefault();
    assertEquals("Parents are a.b, b.c and c.d therefor maxlevel must be 5.", 5, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_LayoutConfig() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMockAndAddToDefault(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.pageLayoutCmd = mockPageLayoutCmd;
    treeNodeService.injectInheritorFactory(inheritorFact);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    String layoutSpaceName = "MyLayout";
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))).andReturn(
        layoutSpaceName).atLeastOnce();
    SpaceReference layoutRef = new SpaceReference(layoutSpaceName, new WikiReference(
        context.getDatabase()));
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(layoutRef);
    DocumentReference layoutWebHomeRef = new DocumentReference(context.getDatabase(),
        layoutSpaceName, "WebHome");
    XWikiDocument layoutWebHomeDoc = new XWikiDocument(layoutWebHomeRef);
    BaseObject layoutConfigObj = new BaseObject();
    layoutConfigObj.setXClassReference(getCellsClasses().getPageLayoutPropertiesClassRef(
        context.getDatabase()));
    layoutWebHomeDoc.addXObject(layoutConfigObj);
    expect(wiki.getDocument(eq(layoutWebHomeRef), same(context))).andReturn(
        layoutWebHomeDoc).atLeastOnce();
    context.setDoc(doc);

    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(wiki.getDocument(eq(webPrefDocRef), eq(context))).andReturn(webPrefDoc).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(wiki.getDocument(eq(xwikiPrefDocRef), eq(context))).andReturn(
        xwikiPrefDoc).atLeastOnce();
    DocumentReference skinDocRef = new DocumentReference(context.getDatabase(), "Skins", "MySkin");
    XWikiDocument skinDoc = new XWikiDocument(skinDocRef);
    expect(wiki.getDocument(eq(skinDocRef), eq(context))).andReturn(skinDoc).atLeastOnce();
    expect(wiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.MySkin").atLeastOnce();

    DocumentReference navConfigDocRef = new DocumentReference(context.getDatabase(), "MyLayout",
        "NavigationCell");
    SpaceReference layoutSpaceRef = new SpaceReference("MyLayout", new WikiReference("xwikidb"));
    List<TreeNode> myLayoutMenuItems = Arrays.asList(new TreeNode(navConfigDocRef, layoutSpaceRef,
        1));
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq("xwikidb:MyLayout."))).andReturn(
        myLayoutMenuItems);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(
        "xwikidb:MyLayout.NavigationCell"))).andReturn(Collections.<TreeNode>emptyList());
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(isA(String.class), same(
        context))).andReturn(Collections.<TreeNode>emptyList()).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    BaseObject navConfigObj = new BaseObject();
    DocumentReference navigationConfigClassRef = getNavClassConfig().getNavigationConfigClassRef(
        context.getDatabase());
    navConfigObj.setXClassReference(navigationConfigClassRef);
    navConfigObj.setIntValue(INavigationClassConfig.TO_HIERARCHY_LEVEL_FIELD, 3);
    XWikiDocument navConfigDoc = new XWikiDocument(navConfigDocRef);
    navConfigDoc.addXObject(navConfigObj);
    expect(wiki.getDocument(eq(navConfigDocRef), same(context))).andReturn(navConfigDoc);

    replayDefault();
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyDefault();
    assertEquals("Expecting max level defined in layout (3).", 3, maxLevel);
  }

  @Test
  public void testGetMenuItemPos() throws Exception {
    String space = "MySpaceName";
    String parentKey = context.getDatabase() + ":" + space + ".";
    String menuPart = "";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), space, "MyDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).anyTimes();

    TreeNode tnItem = new TreeNode(docRef, null, 1);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnItem);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        nodes).once();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        new ArrayList<TreeNode>()).once();

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    assertEquals(0, treeNodeService.getMenuItemPos(docRef, menuPart));
    verifyDefault();
  }

  @Test
  public void getSiblingMenuItem_previous() throws XWikiException {
    String db = "siblingPrevious";
    String space = "Celements2";
    String celDocName = "MenuItem";
    String fullName = db + ":" + space + "." + celDocName;

    context.setDatabase(db);

    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT,
        new EntityReference(space, EntityType.SPACE, new EntityReference(db, EntityType.WIKI)));
    DocumentReference mItemDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myMenuItemDoc"), docRefPrev = new DocumentReference(context.getDatabase(), "mySpace",
            "DocPrev"), docRefNext = new DocumentReference(context.getDatabase(), "mySpace",
                "DocNext");
    XWikiDocument doc = new XWikiDocument(mItemDocRef), docPrev = new XWikiDocument(docRefPrev),
        docNext = new XWikiDocument(docRefNext);
    BaseObject menuItemItemDoc = new BaseObject(), menuItemPrev = new BaseObject(),
        menuItemNext = new BaseObject();

    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);

    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    DocumentReference menuItemClassRef = getNavClassConfig().getMenuItemClassRef(
        context.getDatabase());
    menuItemItemDoc.setXClassReference(menuItemClassRef);
    menuItemPrev.setXClassReference(menuItemClassRef);
    menuItemNext.setXClassReference(menuItemClassRef);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);

    context.setDoc(doc);

    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();

    DocumentReference parentRef = new DocumentReference(context.getDatabase(), space, celDocName);
    TreeNode tnPrev = new TreeNode(docRefPrev, parentRef, 0);
    TreeNode tnItem = new TreeNode(mItemDocRef, parentRef, 1);
    TreeNode tnNext = new TreeNode(docRefNext, parentRef, 2);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnPrev);
    nodes.add(tnItem);
    nodes.add(tnNext);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(fullName))).andReturn(nodes).times(
        2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(fullName), same(context))).andReturn(
        new ArrayList<TreeNode>()).times(2);

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, true);
    assertEquals("MySpace.DocPrev TreeNode expected.", tnPrev, prevMenuItem);
    verifyDefault();
  }

  @Test
  public void getSiblingMenuItem_next() throws XWikiException {
    String db = "siblingPrevious";
    String space = "Celements2";
    String celDocName = "MenuItem";
    String fullName = db + ":" + space + "." + celDocName;

    context.setDatabase(db);

    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT,
        new EntityReference(space, EntityType.SPACE, new EntityReference(db, EntityType.WIKI)));
    DocumentReference mItemDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myMenuItemDoc");
    DocumentReference docRefPrev = new DocumentReference(context.getDatabase(), "mySpace",
        "DocPrev");
    DocumentReference docRefNext = new DocumentReference(context.getDatabase(), "mySpace",
        "DocNext");
    XWikiDocument doc = new XWikiDocument(mItemDocRef);
    XWikiDocument docPrev = new XWikiDocument(docRefPrev);
    XWikiDocument docNext = new XWikiDocument(docRefNext);
    BaseObject menuItemItemDoc = new BaseObject();
    BaseObject menuItemPrev = new BaseObject();
    BaseObject menuItemNext = new BaseObject();

    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);

    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    DocumentReference menuItemClassRef = getNavClassConfig().getMenuItemClassRef(
        context.getDatabase());
    menuItemItemDoc.setXClassReference(menuItemClassRef);
    menuItemPrev.setXClassReference(menuItemClassRef);
    menuItemNext.setXClassReference(menuItemClassRef);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);

    context.setDoc(doc);

    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();

    DocumentReference parentRef = new DocumentReference(context.getDatabase(), space, celDocName);
    TreeNode tnPrev = new TreeNode(docRefPrev, parentRef, 0);
    TreeNode tnItem = new TreeNode(mItemDocRef, parentRef, 1);
    TreeNode tnNext = new TreeNode(docRefNext, parentRef, 2);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnPrev);
    nodes.add(tnItem);
    nodes.add(tnNext);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(fullName))).andReturn(nodes).times(
        2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(fullName), same(context))).andReturn(
        new ArrayList<TreeNode>()).times(2);

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, false);
    assertEquals("MySpace.DocNext TreeNode expected.", tnNext, prevMenuItem);
    verifyDefault();
  }

  @Test
  public void getSiblingMenuItem_previous_mainMenu() throws XWikiException {
    String db = "myWiki";
    String space = "mySpace";
    String fullName = db + ":" + space + ".";

    context.setDatabase(db);

    DocumentReference mItemDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myMenuItemDoc"), docRefPrev = new DocumentReference(context.getDatabase(), "mySpace",
            "DocPrev"), docRefNext = new DocumentReference(context.getDatabase(), "mySpace",
                "DocNext");
    XWikiDocument doc = new XWikiDocument(mItemDocRef), docPrev = new XWikiDocument(docRefPrev),
        docNext = new XWikiDocument(docRefNext);
    BaseObject menuItemItemDoc = new BaseObject(), menuItemPrev = new BaseObject(),
        menuItemNext = new BaseObject();

    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    DocumentReference menuItemClassRef = getNavClassConfig().getMenuItemClassRef(
        context.getDatabase());
    menuItemItemDoc.setXClassReference(menuItemClassRef);
    menuItemPrev.setXClassReference(menuItemClassRef);
    menuItemNext.setXClassReference(menuItemClassRef);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);

    context.setDoc(doc);

    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();

    TreeNode tnPrev = new TreeNode(docRefPrev, null, 0);
    TreeNode tnItem = new TreeNode(mItemDocRef, null, 1);
    TreeNode tnNext = new TreeNode(docRefNext, null, 2);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnPrev);
    nodes.add(tnItem);
    nodes.add(tnNext);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(fullName))).andReturn(nodes).times(
        2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(fullName), same(context))).andReturn(
        new ArrayList<TreeNode>()).times(2);

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, true);
    assertEquals("MySpace.DocPrev TreeNode expected.", tnPrev, prevMenuItem);
    verifyDefault();
  }

  @Test
  public void getSiblingMenuItem_next_mainMenu() throws XWikiException {
    String db = "myWiki";
    String space = "mySpace";
    String fullName = db + ":" + space + ".";

    context.setDatabase(db);

    DocumentReference mItemDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myMenuItemDoc"), docRefPrev = new DocumentReference(context.getDatabase(), "mySpace",
            "DocPrev"), docRefNext = new DocumentReference(context.getDatabase(), "mySpace",
                "DocNext");
    XWikiDocument doc = new XWikiDocument(mItemDocRef), docPrev = new XWikiDocument(docRefPrev),
        docNext = new XWikiDocument(docRefNext);
    BaseObject menuItemItemDoc = new BaseObject(), menuItemPrev = new BaseObject(),
        menuItemNext = new BaseObject();

    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    DocumentReference menuItemClassRef = getNavClassConfig().getMenuItemClassRef(
        context.getDatabase());
    menuItemItemDoc.setXClassReference(menuItemClassRef);
    menuItemPrev.setXClassReference(menuItemClassRef);
    menuItemNext.setXClassReference(menuItemClassRef);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);

    context.setDoc(doc);

    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();

    TreeNode tnPrev = new TreeNode(docRefPrev, null, 0);
    TreeNode tnItem = new TreeNode(mItemDocRef, null, 1);
    TreeNode tnNext = new TreeNode(docRefNext, null, 2);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnPrev);
    nodes.add(tnItem);
    nodes.add(tnNext);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(fullName))).andReturn(nodes).times(
        2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(fullName), same(context))).andReturn(
        new ArrayList<TreeNode>()).times(2);

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, false);
    assertEquals("MySpace.DocNext TreeNode expected.", tnNext, prevMenuItem);
    verifyDefault();
  }

  @Test
  public void getSiblingMenuItem_next_docNotInContextSpace() throws XWikiException {
    String db = "siblingPrevious";
    String space = "Celements2";
    String celDocName = "MenuItem";
    String fullName = db + ":" + space + "." + celDocName;
    DocumentReference parentRef = new DocumentReference(db, space, celDocName);

    context.setDatabase(db);

    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT,
        new EntityReference(space, EntityType.SPACE, new EntityReference(db, EntityType.WIKI)));
    DocumentReference mItemDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myMenuItemDoc"), docRefPrev = new DocumentReference(context.getDatabase(), "mySpace",
            "DocPrev"), docRefNext = new DocumentReference(context.getDatabase(), "mySpace",
                "DocNext");
    XWikiDocument doc = new XWikiDocument(mItemDocRef), docPrev = new XWikiDocument(docRefPrev),
        docNext = new XWikiDocument(docRefNext);
    BaseObject menuItemItemDoc = new BaseObject(), menuItemPrev = new BaseObject(),
        menuItemNext = new BaseObject();

    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);

    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    DocumentReference menuItemClassRef = getNavClassConfig().getMenuItemClassRef(
        context.getDatabase());
    menuItemItemDoc.setXClassReference(menuItemClassRef);
    menuItemPrev.setXClassReference(menuItemClassRef);
    menuItemNext.setXClassReference(menuItemClassRef);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);

    context.setDoc(doc);

    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();

    TreeNode tnPrev = new TreeNode(docRefPrev, parentRef, 0), tnItem = new TreeNode(mItemDocRef,
        parentRef, 1);
    List<TreeNode> nodes = new ArrayList<>();
    nodes.add(tnPrev);
    nodes.add(tnItem);

    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(fullName))).andReturn(nodes).times(
        2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(fullName), same(context))).andReturn(
        new ArrayList<TreeNode>()).times(2);

    expect(mockRightService.hasAccessLevel(eq("view"), isA(String.class), isA(String.class), same(
        context))).andReturn(true).anyTimes();
    replayDefault();
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, false);
    assertEquals("null expected.", null, prevMenuItem);
    verifyDefault();
  }

  @Test
  public void testGetParentKey() throws XWikiException {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";

    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference("mySpace", wikiRef);
    DocumentReference docRef = new DocumentReference(docName, spaceRef);
    assertEquals(wikiName + ":", treeNodeService.getParentKey(wikiRef, true));
    assertEquals(wikiName + ":" + spaceName + ".", treeNodeService.getParentKey(spaceRef, true));
    assertEquals(wikiName + ":" + spaceName + "." + docName, treeNodeService.getParentKey(docRef,
        true));
    assertEquals("", treeNodeService.getParentKey(wikiRef, false));
    assertEquals(spaceName + ".", treeNodeService.getParentKey(spaceRef, false));
    assertEquals(spaceName + "." + docName, treeNodeService.getParentKey(docRef, false));
  }

  @Test
  public void testEnableMappedMenuItems() {
    treeNodeService.enableMappedMenuItems();
    assertTrue(context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null);
    assertTrue(((GetMappedMenuItemsForParentCommand) context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY)).isActive());
  }

  @Test
  public void testGetParentEntityRef() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference docRef = new DocumentReference(wikiName, spaceName, docName);
    XWikiDocument theDoc = new XWikiDocument(docRef);
    EntityReference parentReference = new SpaceReference(spaceName, new WikiReference(wikiName));
    theDoc.setParentReference(parentReference);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(theDoc).anyTimes();
    replayDefault();
    EntityReference entityRef = treeNodeService.getParentEntityRef(docRef);
    assertEquals(parentReference, entityRef);
    verifyDefault();
  }

  @Test
  public void testGetTreeNodeForDocRef_parentSpaceRef() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference docRef = new DocumentReference(wikiName, spaceName, docName);
    SpaceReference spaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    int pos = 2;
    TreeNode expectedTreeNode = new TreeNode(docRef, spaceRef, pos, "");
    XWikiDocument navDoc1 = createNavDoc(expectedTreeNode);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(navDoc1).anyTimes();
    replayDefault();
    TreeNode treeNodeTest = treeNodeService.getTreeNodeForDocRef(docRef);
    assertEquals(expectedTreeNode, treeNodeTest);
    assertEquals(spaceRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testGetTreeNodeForDocRef_parentSpaceRef_partName_mainNav() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference docRef = new DocumentReference(wikiName, spaceName, docName);
    SpaceReference spaceRef = new SpaceReference(spaceName, new WikiReference(wikiName));
    int pos = 2;
    TreeNode expectedTreeNode = new TreeNode(docRef, spaceRef, pos, "mainNav");
    XWikiDocument navDoc1 = createNavDoc(expectedTreeNode);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(navDoc1).anyTimes();
    replayDefault();
    TreeNode treeNodeTest = treeNodeService.getTreeNodeForDocRef(docRef);
    assertEquals(expectedTreeNode, treeNodeTest);
    assertEquals(spaceRef, treeNodeTest.getParentRef());
    assertEquals("mainNav", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void testGetTreeNodeForDocRef_parentDocRef() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference docRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 3;
    TreeNode expectedTreeNode = new TreeNode(docRef, parentRef, pos);
    XWikiDocument navDoc1 = createNavDoc(expectedTreeNode);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(navDoc1).anyTimes();
    replayDefault();
    TreeNode treeNodeTest = treeNodeService.getTreeNodeForDocRef(docRef);
    assertEquals(expectedTreeNode, treeNodeTest);
    assertEquals(parentRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testGetTreeNodeForDocRef_noTreeNodeObj() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference docRef = new DocumentReference(wikiName, spaceName, docName);
    XWikiDocument navDoc1 = new XWikiDocument(docRef);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(navDoc1).anyTimes();
    replayDefault();
    assertNull(treeNodeService.getTreeNodeForDocRef(docRef));
    verifyDefault();
  }

  @Test
  public void testGetSiblingTreeNodes() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 3;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).anyTimes();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 1;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> expectedTreeNodes = Arrays.asList(treeNode1, moveTreeNode, treeNode2, treeNode3);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(
        expectedTreeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"), isA(String.class),
        same(context))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals(expectedTreeNodes, treeNodeService.getSiblingTreeNodes(moveDocRef));
    verifyDefault();
  }

  @Test
  public void testMoveTreeNodeAfter() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 4;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).anyTimes();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 1;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 3;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> treeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3, moveTreeNode);
    List<TreeNode> expectedTreeNodes = Arrays.asList(treeNode1, moveTreeNode, treeNode2, treeNode3);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(treeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    replayDefault();
    assertEquals(expectedTreeNodes, treeNodeService.moveTreeNodeAfter(moveTreeNode, treeNode1));
    verifyDefault();
  }

  @Test
  public void testMoveTreeNodeAfter_nullAfter() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 4;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).anyTimes();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 1;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 3;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> treeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3, moveTreeNode);
    List<TreeNode> expectedTreeNodes = Arrays.asList(moveTreeNode, treeNode1, treeNode2, treeNode3);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(treeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    replayDefault();
    assertEquals(expectedTreeNodes, treeNodeService.moveTreeNodeAfter(moveTreeNode, null));
    verifyDefault();
  }

  @Test
  public void testMoveTreeNodeAfter_afterItemNotInList() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 4;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).anyTimes();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 1;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 3;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> treeNodes = Arrays.asList(treeNode2, treeNode3, moveTreeNode);
    List<TreeNode> expectedTreeNodes = Arrays.asList(moveTreeNode, treeNode2, treeNode3);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(treeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    replayDefault();
    assertEquals(expectedTreeNodes, treeNodeService.moveTreeNodeAfter(moveTreeNode, treeNode1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 1;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2);
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3);
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc3 = new Capture<>();
    wiki.saveDocument(capture(capDoc3), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc3 = capDoc3.getValue();
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(2, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder_minorEdits() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 1;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2);
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3);
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(true), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(true), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc3 = new Capture<>();
    wiki.saveDocument(capture(capDoc3), isA(String.class), eq(true), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes, true);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc3 = capDoc3.getValue();
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(2, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder_nullObjects() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 5;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1);
    XWikiDocument navDoc2 = new XWikiDocument(docRef2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2);
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3);
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(true), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(true), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes, true);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder_rightOrder() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 0;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 1;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 2;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2);
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3);
    // expecting NO savings
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes);
    XWikiDocument savedDoc1 = navDoc1;
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = navDoc2;
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc3 = navDoc3;
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(2, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder_exception_onGetDoc() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 5;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1).once();
    expect(wiki.getDocument(eq(docRef2), same(context))).andThrow(new XWikiException()).once();
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3).once();
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc3 = new Capture<>();
    wiki.saveDocument(capture(capDoc3), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc3 = capDoc3.getValue();
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testStoreOrder_exception_onSaveDoc() throws Exception {
    String spaceName = "mySpace";
    DocumentReference parentRef = new DocumentReference(context.getDatabase(), spaceName, "name");
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 3;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    DocumentReference docRef3 = new DocumentReference(context.getDatabase(), spaceName, "Doc3");
    Integer oldPos3 = 1;
    TreeNode treeNode3 = new TreeNode(docRef3, parentRef, oldPos3);
    List<TreeNode> newTreeNodes = Arrays.asList(treeNode1, treeNode2, treeNode3);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2);
    XWikiDocument navDoc3 = createNavDoc(treeNode3);
    expect(wiki.getDocument(eq(docRef3), same(context))).andReturn(navDoc3);
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(false), same(context));
    expectLastCall().andThrow(new XWikiException());
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc3 = new Capture<>();
    wiki.saveDocument(capture(capDoc3), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.storeOrder(newTreeNodes);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc3 = capDoc3.getValue();
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(2, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    verifyDefault();
  }

  @Test
  public void testMoveTreeDocAfter() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 2;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).atLeastOnce();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 0;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1).atLeastOnce();
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 1;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2).atLeastOnce();
    List<TreeNode> treeNodes = Arrays.asList(treeNode1, treeNode2, moveTreeNode);
    List<TreeNode> expectedTreeNodes = Arrays.asList(treeNode1, moveTreeNode, treeNode2);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(treeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    // expecting correct savings
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    Capture<XWikiDocument> capDoc3 = new Capture<>();
    wiki.saveDocument(capture(capDoc3), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.moveTreeDocAfter(moveDocRef, docRef1);
    // first node should not be saved because it does not change.
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    TreeNode expTreeNode2 = expectedTreeNodes.get(1);
    assertEquals(expTreeNode2.getDocumentReference(), savedDoc2.getDocumentReference());
    XWikiDocument savedDoc3 = capDoc3.getValue();
    BaseObject menuItemObj3 = savedDoc3.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(2, menuItemObj3.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    TreeNode expTreeNode3 = expectedTreeNodes.get(2);
    assertEquals(expTreeNode3.getDocumentReference(), savedDoc3.getDocumentReference());
    verifyDefault();
  }

  @Test
  public void testMoveTreeDocAfter_insertAfterNull() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    DocumentReference parentRef = new DocumentReference(wikiName, spaceName, "myParent");
    int pos = 1;
    TreeNode moveTreeNode = new TreeNode(moveDocRef, parentRef, pos);
    XWikiDocument moveDoc = createNavDoc(moveTreeNode);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).atLeastOnce();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    Integer oldPos1 = 0;
    TreeNode treeNode1 = new TreeNode(docRef1, parentRef, oldPos1);
    XWikiDocument navDoc1 = createNavDoc(treeNode1);
    expect(wiki.getDocument(eq(docRef1), same(context))).andReturn(navDoc1).atLeastOnce();
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), spaceName, "Doc2");
    Integer oldPos2 = 2;
    TreeNode treeNode2 = new TreeNode(docRef2, parentRef, oldPos2);
    XWikiDocument navDoc2 = createNavDoc(treeNode2);
    expect(wiki.getDocument(eq(docRef2), same(context))).andReturn(navDoc2).atLeastOnce();
    List<TreeNode> treeNodes = Arrays.asList(treeNode1, moveTreeNode, treeNode2);
    List<TreeNode> expectedTreeNodes = Arrays.asList(moveTreeNode, treeNode1, treeNode2);
    String parentKey = wikiName + ":" + spaceName + ".myParent";
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey))).andReturn(treeNodes);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))).andReturn(
        Collections.<TreeNode>emptyList());
    // expecting correct savings
    Capture<XWikiDocument> capDoc1 = new Capture<>();
    wiki.saveDocument(capture(capDoc1), isA(String.class), eq(false), same(context));
    expectLastCall().andThrow(new XWikiException());
    Capture<XWikiDocument> capDoc2 = new Capture<>();
    wiki.saveDocument(capture(capDoc2), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeService.moveTreeDocAfter(moveDocRef, null);
    XWikiDocument savedDoc1 = capDoc1.getValue();
    BaseObject menuItemObj1 = savedDoc1.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(0, menuItemObj1.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    XWikiDocument savedDoc2 = capDoc2.getValue();
    BaseObject menuItemObj2 = savedDoc2.getXObject(getNavClassConfig().getMenuItemClassRef(
        context.getDatabase()));
    assertEquals(1, menuItemObj2.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1));
    TreeNode expTreeNode2 = expectedTreeNodes.get(1);
    assertEquals(expTreeNode2.getDocumentReference(), savedDoc2.getDocumentReference());
    // third node should not be saved because it does not change.
    verifyDefault();
  }

  @Test
  public void testMoveTreeDocAfter_NoTreeNode() throws Exception {
    String wikiName = context.getDatabase();
    String spaceName = "mySpace";
    String docName = "myDoc";
    DocumentReference moveDocRef = new DocumentReference(wikiName, spaceName, docName);
    XWikiDocument moveDoc = new XWikiDocument(moveDocRef);
    expect(wiki.getDocument(eq(moveDocRef), same(context))).andReturn(moveDoc).atLeastOnce();
    DocumentReference docRef1 = new DocumentReference(context.getDatabase(), spaceName, "Doc1");
    replayDefault();
    treeNodeService.moveTreeDocAfter(moveDocRef, docRef1);
    verifyDefault();
  }

  private XWikiDocument createNavDoc(TreeNode treeNode1) {
    XWikiDocument navDoc = new XWikiDocument(treeNode1.getDocumentReference());
    navDoc.setParentReference(treeNode1.getParentRef());
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavClassConfig().getMenuItemClassRef(context.getDatabase()));
    menuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD, treeNode1.getPosition());
    String partName = treeNode1.getPartName();
    if ("".equals(partName)) {
      partName = null;
    }
    menuItemObj.setStringValue(INavigationClassConfig.MENU_PART_FIELD, partName);
    navDoc.addXObject(menuItemObj);
    return navDoc;
  }

  private BaseObject createNavObj(int toLevel, XWikiDocument doc) {
    BaseObject navObj = new BaseObject();
    navObj.setXClassReference(getNavClassConfig().getNavigationConfigClassRef(
        context.getDatabase()));
    navObj.setStringValue("menu_element_name", "mainMenu");
    navObj.setIntValue("to_hierarchy_level", toLevel);
    navObj.setDocumentReference(doc.getDocumentReference());
    navObj.setDocumentReference(doc.getDocumentReference());
    return navObj;
  }

  private CellsClasses getCellsClasses() {
    return (CellsClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.celCellsClasses");
  }

  private INavigationClassConfig getNavClassConfig() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
