package com.celements.navigation.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class TreeNodeCacheListenerTest extends AbstractBridgedComponentTestCase {
  
  private TreeNodeCacheListener eventListener;

  @Before
  public void setUp_TreeNodeCacheListenerTest() throws Exception {
    eventListener = getTreeNodeCacheListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeCacheListener());
  }

  private TreeNodeCacheListener getTreeNodeCacheListener() {
    return (TreeNodeCacheListener) Utils.getComponent(EventListener.class,
        "TreeNodeCacheListener");
  }

}
