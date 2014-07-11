package com.celements.navigation.listener;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class TreeNodeDocumentCreatedListenerTest extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "TreeNodeDocumentCreatedListener";
  private TreeNodeDocumentCreatedListener eventListener;

  @Before
  public void setUp_TreeNodeDocumentCreatedListenerTest() throws Exception {
    eventListener = getTreeNodeDocumentCreatedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeDocumentCreatedListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  @Test
  public void testGetEvents() {
    List<String> expectedEventClassList = Arrays.asList(new DocumentCreatedEvent(
        ).getClass().getName());
    replayDefault();
    List<Event> actualEventList = eventListener.getEvents();
    assertEquals(expectedEventClassList.size(), actualEventList.size());
    for (Event actualEvent : actualEventList) {
      assertTrue("Unexpected Event [" + actualEvent.getClass().getName() + "] found.",
          expectedEventClassList.contains(actualEvent.getClass().getName()));
    }
    verifyDefault();
  }

  private TreeNodeDocumentCreatedListener getTreeNodeDocumentCreatedListener() {
    return (TreeNodeDocumentCreatedListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
