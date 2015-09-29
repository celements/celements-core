package com.celements.navigation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationUpdatedEvent;
import com.xpn.xwiki.web.Utils;

public class NavigationUpdateListenerTest extends AbstractBridgedComponentTestCase {

  private NavigationUpdateListener listener;

  @Before
  public void setUp_NavigationUpdateListenerTest() throws Exception {
    listener = (NavigationUpdateListener) Utils.getComponent(EventListener.class, 
        NavigationUpdateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("NavigationUpdateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    WikiReference wikiRef = new WikiReference("myWiki");
    assertEquals(Utils.getComponent(INavigationClassConfig.class
        ).getNavigationConfigClassRef(wikiRef), listener.getRequiredObjClassRef(wikiRef));
  }

  @Test
  public void testUpdatingEvent() {
    Event event = listener.getUpdatingEvent(null);
    assertNull(event);
  }

  @Test
  public void testUpdatedEvent() {
    Event event = listener.getUpdatedEvent(null);
    assertNotNull(event);
    assertSame(NavigationUpdatedEvent.class, event.getClass());
    assertTrue(event.matches(new NavigationUpdatedEvent()));
    assertNotSame(listener.getUpdatedEvent(null), event);
  }

  @Test
  public void testIncludeDocFields() {
    assertFalse(listener.includeDocFields());
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
