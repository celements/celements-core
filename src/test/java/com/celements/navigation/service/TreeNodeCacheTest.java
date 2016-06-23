package com.celements.navigation.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class TreeNodeCacheTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWikiStoreInterface mockStore;
  private TreeNodeCache treeNodeCache;

  @Before
  public void setUp_TreeNodeCacheTest() throws Exception {
    context = getContext();
    XWiki wiki = getWikiMock();
    mockStore = createMock(XWikiStoreInterface.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    treeNodeCache = (TreeNodeCache) Utils.getComponent(ITreeNodeCache.class);
  }

  @Test
  public void testGetMappedMenuItemsForParentCmd() {
    assertFalse(treeNodeCache.getMappedMenuItemsForParentCmd().isActive());
  }

  @Test
  public void testGetMappedMenuItemsForParentCmd_injected() {
    GetMappedMenuItemsForParentCommand testGetMenuItemCommand = new GetMappedMenuItemsForParentCommand();
    testGetMenuItemCommand.setIsActive(true);
    treeNodeCache.inject_GetMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    assertEquals(testGetMenuItemCommand, treeNodeCache.getMappedMenuItemsForParentCmd());
    assertTrue(treeNodeCache.getMappedMenuItemsForParentCmd().isActive());
  }

  @Test
  public void testGetNotMappedMenuItemsForParentCmd() {
    assertNotNull(treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }

  @Test
  public void testGetNotMappedMenuItemsForParentCmd_injected() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand = new GetNotMappedMenuItemsForParentCommand();
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    assertNotNull(treeNodeCache.getNotMappedMenuItemsForParentCmd());
    assertSame("Expecting injected cmd object", testGetMenuItemCommand,
        treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }

  @Test
  public void testGetNotMappedMenuItemsForParentCmd_singleton() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand = treeNodeCache.getNotMappedMenuItemsForParentCmd();
    assertNotNull(testGetMenuItemCommand);
    assertSame("Expecting injected cmd object", testGetMenuItemCommand,
        treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }

  @Test
  public void testGetMappedMenuItemsForParentCmd_injectedByContext() {
    GetMappedMenuItemsForParentCommand testGetMenuItemCommand = new GetMappedMenuItemsForParentCommand();
    testGetMenuItemCommand.setIsActive(true);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY,
        testGetMenuItemCommand);
    assertTrue(treeNodeCache.getMappedMenuItemsForParentCmd().isActive());

    testGetMenuItemCommand.setIsActive(false);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY,
        testGetMenuItemCommand);
    assertFalse(treeNodeCache.getMappedMenuItemsForParentCmd().isActive());
  }

  @Test
  public void testQueryCount() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand = createMockAndAddToDefault(
        GetNotMappedMenuItemsForParentCommand.class);
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    expect(testGetMenuItemCommand.queryCount()).andReturn(15);
    replayDefault();
    assertEquals(15, treeNodeCache.queryCount());
    verifyDefault();
  }

  @Test
  public void testFlushMenuItemCache() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand = createMockAndAddToDefault(
        GetNotMappedMenuItemsForParentCommand.class);
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    testGetMenuItemCommand.flushMenuItemCache(same(context));
    expectLastCall().once();
    replayDefault();
    treeNodeCache.flushMenuItemCache();
    verifyDefault();
  }

}
