package com.celements.navigation.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.observation.EventListener;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class TreeNodeDocumentCreatedListenerTest extends AbstractBridgedComponentTestCase {
  
  private TreeNodeDocumentCreatedListener eventListener;

  @Before
  public void setUp_TreeNodeDocumentCreatedListenerTest() throws Exception {
    eventListener = getTreeNodeDocumentCreatedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeDocumentCreatedListener());
  }

  private TreeNodeDocumentCreatedListener getTreeNodeDocumentCreatedListener() {
    return (TreeNodeDocumentCreatedListener) Utils.getComponent(
        EventListener.class, "TreeNodeDocumentCreatedListener");
  }

}
