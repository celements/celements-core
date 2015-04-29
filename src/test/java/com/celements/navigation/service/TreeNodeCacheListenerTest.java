package com.celements.navigation.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class TreeNodeCacheListenerTest extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "TreeNodeCacheListener";
  private TreeNodeCacheListener eventListener;

  @Before
  public void setUp_TreeNodeCacheListenerTest() throws Exception {
    eventListener = getTreeNodeCacheListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeCacheListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  private TreeNodeCacheListener getTreeNodeCacheListener() {
    return (TreeNodeCacheListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
