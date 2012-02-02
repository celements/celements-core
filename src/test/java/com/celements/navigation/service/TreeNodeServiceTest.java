package com.celements.navigation.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;

public class TreeNodeServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki wiki;
  private XWikiStoreInterface mockStore;
  private TreeNodeService treeNodeService;
  private ITreeNodeCache mockTreeNodeCache;
  private GetNotMappedMenuItemsForParentCommand testGetNotMenuItemCommand;
  private GetMappedMenuItemsForParentCommand testGetMenuItemCommand;

  @Before
  public void setUp_TreeNodeServiceTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    treeNodeService = new TreeNodeService();
    mockTreeNodeCache = createMock(ITreeNodeCache.class);
    treeNodeService.treeNodeCache = mockTreeNodeCache;
    treeNodeService.execution = getComponentManager().lookup(Execution.class);
    testGetNotMenuItemCommand = createMock(GetNotMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getNotMappedMenuItemsForParentCmd()).andReturn(
        testGetNotMenuItemCommand).anyTimes();
    testGetMenuItemCommand = createMock(
        GetMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getMappedMenuItemsForParentCmd()).andReturn(
        testGetMenuItemCommand).anyTimes();
  }

  @Test
  public void testGetSubNodesForParent() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Content",
        "MainPage");
    TreeNode treeNode = new TreeNode(docRef, "Content", 1);
    List<TreeNode> mockTreeNodeList = Arrays.asList(treeNode, null);
    expect(testGetNotMenuItemCommand.getTreeNodesForParentKey(eq("xwikidb:Content."),
        same(context))).andReturn(mockTreeNodeList);
    List<TreeNode> emptyList = Collections.emptyList();
    expect(testGetMenuItemCommand.getTreeNodesForParentKey(eq("xwikidb:Content."),
        same(context))).andReturn(emptyList);
    XWikiRightService mockRightService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightService).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("Content.MainPage"), same(context))).andReturn(true);
    replayAll(mockRightService);
    List<TreeNode> resultList = treeNodeService.getSubNodesForParent("", "Content", "");
    assertEquals(1, resultList.size());
    assertTrue(mockTreeNodeList.contains(treeNode));
    verifyAll(mockRightService);
  }

  @Test
  public void testFetchNodesForParentKey_mergeCombinedResult() {
    context.setDatabase("myWiki");
    String parentKey = "myWiki:mySpace.myDoc";
    TreeNode menuItem2 = createTreeNode("mySpace", "myDoc2", "mySpace", "myDoc", 2);
    TreeNode menuItem3 = createTreeNode("mySpace", "myDoc1", "mySpace", "myDoc", 3);
    TreeNode menuItem1 = createTreeNode("mySpace", "myDoc1", "mySpace", "myDoc", 1);
    TreeNode menuItem5 = createTreeNode("mySpace", "myDoc5", "mySpace", "myDoc", 5);
    List<TreeNode> mappedList = Arrays.asList(menuItem1, menuItem5);
    expect(testGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList);
    expect(testGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(Arrays.asList(menuItem2, menuItem3)).atLeastOnce();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(parentKey);
    List<TreeNode> expectedList = Arrays.asList(menuItem1, menuItem2, menuItem3,
        menuItem5);
    assertEquals("result array does not match expected size.", expectedList.size(),
        menuItemsMerged.size());
    int pos = 0;
    for (TreeNode menuItem : menuItemsMerged) {
      TreeNode expectedMenuitem = expectedList.get(pos++);
      assertEquals("Array compare failed on item " + pos, expectedMenuitem.getPosition(),
          menuItem.getPosition());
      assertSame("Array compare failed on item " + pos, expectedMenuitem, menuItem);
    }
    verifyAll();
  }

  @Test
  public void testFetchNodesForParentKey_onlyOldArray() {
    context.setDatabase("myWiki");
    String parentKey = "myWiki:mySpace.myDoc";
    TreeNode menuItem2 = createTreeNode("mySpace", "myDoc2", "mySpace", "myDoc", 2);
    TreeNode menuItem3 = createTreeNode("mySpace", "myDoc1", "mySpace", "myDoc", 3);
    List<TreeNode> oldNotMappedList = Arrays.asList(menuItem2, menuItem3);
    List<TreeNode> mappedList = Collections.emptyList();
    expect(testGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList);
    expect(testGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context)
        )).andReturn(oldNotMappedList).atLeastOnce();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(parentKey);
    assertSame("expecting old notMapped list.", oldNotMappedList, menuItemsMerged);
    verifyAll();
  }

  @Test
  public void testFetchNodesForParentKey_onlyNewMappedList() {
    context.setDatabase("myWiki");
    String parentKey = "myWiki:mySpace.myDoc";
    List<TreeNode> oldMenuItems = Collections.emptyList();
    TreeNode menuItem1 = createTreeNode("mySpace", "myDoc1", "mySpace", "myDoc", 1);
    TreeNode menuItem5 = createTreeNode("mySpace", "myDoc5", "mySpace", "myDoc", 5);
    List<TreeNode> mappedList = Arrays.asList(menuItem1, menuItem5);
    expect(testGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList);
    expect(testGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(oldMenuItems).atLeastOnce();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(parentKey);
    assertSame("expecting old notMapped list.", mappedList, menuItemsMerged);
    verifyAll();
  }
  
  @Test
  public void testFetchNodesForParentKey_noMenuItems_NPE() {
    context.setDatabase("myWiki");
    String parentKey = "myWiki:mySpace.myDoc";
    List<TreeNode> mappedList = Collections.emptyList();
    expect(testGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList);
    expect(testGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context)
        )).andReturn(null).atLeastOnce();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(parentKey);
    assertNotNull("expecting not null.", menuItemsMerged);
    assertEquals("expecting empty list.", 0, menuItemsMerged.size());
    verifyAll();
  }


  private TreeNode createTreeNode(String docSpace, String docName, String parentDocSpace,
      String parentDocName, int pos) {
    return new TreeNode(new DocumentReference(context.getDatabase(), docSpace, docName),
        parentDocSpace + "." + parentDocName, pos);
  }
  
  private void replayAll(Object ... mocks) {
    replay(mockStore, wiki, mockTreeNodeCache, testGetNotMenuItemCommand,
        testGetMenuItemCommand);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockStore, wiki, mockTreeNodeCache, testGetNotMenuItemCommand,
        testGetMenuItemCommand);
    verify(mocks);
  }

}
