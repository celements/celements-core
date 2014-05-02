package com.celements.navigation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.listener.TreeNodeDocumentDeletedListener;
import com.xpn.xwiki.web.Utils;

public class TreeNodeDocumentDeletedListenerTest extends AbstractBridgedComponentTestCase {
  
  private TreeNodeDocumentDeletedListener eventListener;

  @Before
  public void setUp_TreeNodeDocumentDeletedListenerTest() throws Exception {
    eventListener = getTreeNodeDocumentDeletedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeDocumentDeletedListener());
  }

  private TreeNodeDocumentDeletedListener getTreeNodeDocumentDeletedListener() {
    return (TreeNodeDocumentDeletedListener) Utils.getComponent(
        EventListener.class, "TreeNodeDocumentDeletedListener");
  }

}
