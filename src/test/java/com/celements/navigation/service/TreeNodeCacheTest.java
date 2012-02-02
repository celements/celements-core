package com.celements.navigation.service;

import static org.easymock.EasyMock.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class TreeNodeCacheTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki wiki;
  private XWikiStoreInterface mockStore;
  private TreeNodeCache treeNodeCache;

  @Before
  public void setUp_TreeNodeCacheTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    treeNodeCache = new TreeNodeCache();
    treeNodeCache.execution = getComponentManager().lookup(Execution.class);
  }

  @Test
  public void testGetMappedMenuItemsForParentCmd() {
    assertFalse(treeNodeCache.getMappedMenuItemsForParentCmd().is_isActive());
  }
  
  @Test
  public void testGetMappedMenuItemsForParentCmd_injected() {
    GetMappedMenuItemsForParentCommand testGetMenuItemCommand =
      new GetMappedMenuItemsForParentCommand();
    testGetMenuItemCommand.set_isActive(true);
    treeNodeCache.inject_GetMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    assertEquals(testGetMenuItemCommand, treeNodeCache.getMappedMenuItemsForParentCmd());
    assertTrue(treeNodeCache.getMappedMenuItemsForParentCmd().is_isActive());
  }
  
  @Test
  public void testGetNotMappedMenuItemsForParentCmd() {
    assertNotNull(treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }
  
  @Test
  public void testGetNotMappedMenuItemsForParentCmd_injected() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand =
      new GetNotMappedMenuItemsForParentCommand();
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    assertNotNull(treeNodeCache.getNotMappedMenuItemsForParentCmd());
    assertSame("Expecting injected cmd object", testGetMenuItemCommand,
        treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }
  
  @Test
  public void testGetNotMappedMenuItemsForParentCmd_singleton() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand =
      treeNodeCache.getNotMappedMenuItemsForParentCmd();
    assertNotNull(testGetMenuItemCommand);
    assertSame("Expecting injected cmd object", testGetMenuItemCommand,
        treeNodeCache.getNotMappedMenuItemsForParentCmd());
  }
  
  @Test
  public void testGetMappedMenuItemsForParentCmd_injectedByContext() {
    GetMappedMenuItemsForParentCommand testGetMenuItemCommand = new GetMappedMenuItemsForParentCommand();
    testGetMenuItemCommand.set_isActive(true);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY, 
        testGetMenuItemCommand);
    assertTrue(treeNodeCache.getMappedMenuItemsForParentCmd().is_isActive());
    
    testGetMenuItemCommand.set_isActive(false);
    context.put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY, 
        testGetMenuItemCommand);
    assertFalse(treeNodeCache.getMappedMenuItemsForParentCmd().is_isActive());
  }

  @Test
  public void testQueryCount() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand =
      createMock(GetNotMappedMenuItemsForParentCommand.class);
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    expect(testGetMenuItemCommand.queryCount()).andReturn(15);
    replayAll(testGetMenuItemCommand);
    assertEquals(15, treeNodeCache.queryCount());
    verifyAll(testGetMenuItemCommand);
  }

  @Test
  public void testFlushMenuItemCache() {
    GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand =
      createMock(GetNotMappedMenuItemsForParentCommand.class);
    treeNodeCache.inject_GetNotMappedMenuItemsForParentCmd(testGetMenuItemCommand);
    testGetMenuItemCommand.flushMenuItemCache(same(context));
    expectLastCall().once();
    replayAll(testGetMenuItemCommand);
    treeNodeCache.flushMenuItemCache();
    verifyAll(testGetMenuItemCommand);
  }


  private void replayAll(Object ... mocks) {
    replay(mockStore, wiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockStore, wiki);
    verify(mocks);
  }
}
