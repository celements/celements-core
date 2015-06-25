package com.celements.navigation.listener;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;

import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.NavigationCache;
import com.celements.navigation.event.NavigationCreatedEvent;
import com.celements.navigation.event.NavigationDeletedEvent;
import com.celements.navigation.event.NavigationUpdatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NavigationCacheFlushingListenerTest extends AbstractBridgedComponentTestCase {

  private NavigationCacheFlushingListener listener;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp_NavigationCacheFlushingListenerTest() throws Exception {
    listener = (NavigationCacheFlushingListener) Utils.getComponent(EventListener.class, 
        NavigationCache.NAME);
    listener.navCache = createMockAndAddToDefault(IDocumentReferenceCache.class);
  }

  @After
  @SuppressWarnings("unchecked")
  public void tearDown_NavigationCacheFlushingListenerTest() throws Exception {
    listener.navCache = Utils.getComponent(IDocumentReferenceCache.class, 
        NavigationCache.NAME);
  }

  @Test
  public void testGetName() {
    assertEquals(NavigationCache.NAME, listener.getName());
  }

  @Test
  public void testGetEvents() {
    assertEquals(3, listener.getEvents().size());
    assertEquals(NavigationCreatedEvent.class, listener.getEvents().get(0).getClass());
    assertEquals(NavigationUpdatedEvent.class, listener.getEvents().get(1).getClass());
    assertEquals(NavigationDeletedEvent.class, listener.getEvents().get(2).getClass());
  }

  @Test
  public void testOnEvent() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "nav");
    listener.navCache.flush(eq(docRef.getWikiReference()));
    expectLastCall().once();
    replayDefault();
    listener.onEvent(null, new XWikiDocument(docRef), null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_noDoc() throws Exception {
    replayDefault();
    listener.onEvent(null, new Object(), null);
    verifyDefault();
  }

  @Test
  public void testOnEvent_null() throws Exception {
    replayDefault();
    listener.onEvent(null, null, null);
    verifyDefault();
  }

}
