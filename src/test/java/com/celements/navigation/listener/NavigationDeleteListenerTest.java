package com.celements.navigation.listener;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationDeletedEvent;
import com.xpn.xwiki.web.Utils;

public class NavigationDeleteListenerTest extends AbstractComponentTest {

  private NavigationDeleteListener listener;

  @Before
  public void setUp_NavigationDeleteListenerTest() throws Exception {
    listener = (NavigationDeleteListener) Utils.getComponent(EventListener.class,
        NavigationDeleteListener.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals("NavigationDeleteListener", listener.getName());
  }

  @Test
  public void testGetRequiredObjClassRef() {
    WikiReference wikiRef = new WikiReference("myWiki");
    assertEquals(Utils.getComponent(INavigationClassConfig.class).getNavigationConfigClassRef(
        wikiRef), listener.getRequiredObjClassRef(wikiRef));
  }

  @Test
  public void testDeletingEvent() {
    Event event = listener.getDeletingEvent(null);
    assertNull(event);
  }

  @Test
  public void testDeletedEvent() {
    Event event = listener.getDeletedEvent(null);
    assertNotNull(event);
    assertSame(NavigationDeletedEvent.class, event.getClass());
    assertTrue(event.matches(new NavigationDeletedEvent()));
    assertNotSame(listener.getDeletedEvent(null), event);
  }

  @Test
  public void testGetLogger() {
    assertNotNull(listener.getLogger());
  }

}
