package com.celements.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractDocumentCreateListener extends AbstractDocumentListener {

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentCreatingEvent(), new DocumentCreatedEvent());
  }

  @Override
  protected Event getNotifyEvent(Event event, XWikiDocument doc) {
    Event notifyEvent = null;
    if (getRequiredObj(doc) != null) {
      if (event instanceof DocumentCreatingEvent) {
        notifyEvent = getCreatingEvent(doc.getDocumentReference());
      } else if (event instanceof DocumentCreatedEvent) {
        notifyEvent = getCreatedEvent(doc.getDocumentReference());
      }
    }
    return notifyEvent;
  }

  @Override
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return null;
  }

}
