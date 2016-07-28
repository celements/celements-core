package com.celements.store;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

@Component(DocumentCacheStoreListener.COMPONENT_NAME)
public class DocumentCacheStoreListener implements EventListener {

  public static final String COMPONENT_NAME = "DocumentCacheStoreListener";

  @Requirement
  private RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement(DocumentCacheStore.COMPONENT_NAME)
  private XWikiStoreInterface docCacheStore;

  /**
   * {@inheritDoc}
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.xwiki.observation.EventListener#getEvents()
   */
  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentCreatedEvent(), new DocumentUpdatedEvent(),
        new DocumentDeletedEvent(), new WikiDeletedEvent());
  }

  /**
   * {@inheritDoc}
   *
   * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event,
   *      java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void onEvent(Event event, Object source, Object data) {
    // only react to remote events since local actions are already taken into account
    if (this.remoteObservationManagerContext.isRemoteState()) {
      if (event instanceof WikiDeletedEvent) {
        getDocCacheStore().flushCache();
      } else {
        XWikiDocument doc = (XWikiDocument) source;
        getDocCacheStore().invalidateCacheFromClusterEvent(doc);
      }
    }
  }

  private DocumentCacheStore getDocCacheStore() {
    return (DocumentCacheStore) docCacheStore;
  }

}
