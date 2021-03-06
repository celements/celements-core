package com.celements.common.observation.listener;

import javax.validation.constraints.NotNull;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public abstract class AbstractDocumentListener extends
    AbstractLocalEventListener<XWikiDocument, Object> {

  @Override
  protected void onEventInternal(Event event, XWikiDocument doc, Object data) {
    Event notifyEvent = getNotifyEvent(event, doc);
    if (notifyEvent != null) {
      getLogger().debug("onEvent: notifying event '{}' on doc '{}'", notifyEvent.getClass(),
          doc.getDocumentReference());
      getObservationManager().notify(notifyEvent, doc, getContext());
    } else {
      getLogger().trace("onEvent: skipped notifying event for event '{}' " + "on doc '{}'",
          event.getClass(), doc.getDocumentReference());
    }
  }

  /**
   * @return next event to notify on {@link ObservationManager}, null indicating nothing
   *         to notify
   */
  protected abstract Event getNotifyEvent(@NotNull Event event, @NotNull XWikiDocument doc);

  protected BaseObject getRequiredObj(XWikiDocument doc) {
    return doc.getXObject(getRequiredObjClassRef(context.getWikiRef()));
  }

  /**
   * @param wikiRef
   * @return the class ref for the object required on the triggered doc to qualify for
   *         this listener
   */
  protected abstract DocumentReference getRequiredObjClassRef(WikiReference wikiRef);

  protected Event getCreateEvent(Event event, DocumentReference docRef) {
    Event ret = null;
    if (isEventING(event)) {
      ret = getCreatingEvent(docRef);
    } else if (isEventED(event)) {
      ret = getCreatedEvent(docRef);
    }
    return ret;
  }

  /**
   * Returns a new instance of the associated creating event or null of if listening on
   * {@link DocumentCreatingEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getCreatingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated created event or null of if listening on
   * {@link DocumentCreatedEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getCreatedEvent(DocumentReference docRef);

  protected Event getUpdateEvent(Event event, DocumentReference docRef) {
    Event ret = null;
    if (isEventING(event)) {
      ret = getUpdatingEvent(docRef);
    } else if (isEventED(event)) {
      ret = getUpdatedEvent(docRef);
    }
    return ret;
  }

  /**
   * Returns a new instance of the associated updating event or null of if listening on
   * {@link DocumentUpdatingEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getUpdatingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated updated event or null of if listening on
   * {@link DocumentUpdatedEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getUpdatedEvent(DocumentReference docRef);

  protected Event getDeleteEvent(Event event, DocumentReference docRef) {
    Event ret = null;
    if (isEventING(event)) {
      ret = getDeletingEvent(docRef);
    } else if (isEventED(event)) {
      ret = getDeletedEvent(docRef);
    }
    return ret;
  }

  /**
   * Returns a new instance of the associated deleting event or null of if listening on
   * {@link DocumentDeletingEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getDeletingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated deleted event or null of if listening on
   * {@link DocumentDeletedEvent}s is not required
   *
   * @param docRef
   * @return
   */
  protected abstract Event getDeletedEvent(DocumentReference docRef);

  private boolean isEventING(Event event) {
    return (event instanceof DocumentCreatingEvent) || (event instanceof DocumentUpdatingEvent)
        || (event instanceof DocumentDeletingEvent);
  }

  private boolean isEventED(Event event) {
    return (event instanceof DocumentCreatedEvent) || (event instanceof DocumentUpdatedEvent)
        || (event instanceof DocumentDeletedEvent);
  }

}
