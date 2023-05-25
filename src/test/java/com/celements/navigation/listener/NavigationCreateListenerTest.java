package com.celements.navigation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationCreatedEvent;
import com.xpn.xwiki.web.Utils;

public class NavigationCreateListenerTest extends AbstractComponentTest {

  private NavigationCreateListener listener;

  @Before
  public void setUp_NavigationCreateListenerTest() throws Exception {
    listener = (NavigationCreateListener) Utils.getComponent(EventListener.class,
        NavigationCreateListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("NavigationCreateListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    WikiReference wikiRef = new WikiReference("myWiki");
    assertEquals(Utils.getComponent(INavigationClassConfig.class).getNavigationConfigClassRef(
        wikiRef), listener.getRequiredObjClassRef(wikiRef));
  }

  @Test
  public void testCreatingEvent() {
    Event event = listener.getCreatingEvent(null);
    assertNull(event);
  }

  @Test
  public void testCreatedEvent() {
    Event event = listener.getCreatedEvent(null);
    assertNotNull(event);
    assertSame(NavigationCreatedEvent.class, event.getClass());
    assertTrue(event.matches(new NavigationCreatedEvent()));
    assertNotSame(listener.getCreatedEvent(null), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
