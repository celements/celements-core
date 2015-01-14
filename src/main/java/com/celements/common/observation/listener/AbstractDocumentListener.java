package com.celements.common.observation.listener;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public abstract class AbstractDocumentListener extends AbstractEventListener {

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = (XWikiDocument) source;
    if ((doc != null) && isLocalEvent()) {
      getLogger().debug("onEvent: got event '{}' on doc '{}'", event.getClass(), 
          doc.getDocumentReference());
      Event notifyEvent = getNotifyEvent(event, doc);
      if (notifyEvent != null) {
        getLogger().debug("onEvent: notifying event '{}' on doc '{}'", 
            notifyEvent.getClass(), doc.getDocumentReference());
        getObservationManager().notify(notifyEvent, source, getContext());
      } else {
        getLogger().trace("onEvent: skipped notifying event for event '{}' on doc '{}'", 
            event.getClass(), doc.getDocumentReference());
      }
    } else if (getLogger().isTraceEnabled()) {
      getLogger().trace("onEvent: skipped event '{}' on source '{}', data '{}', "
          + "isLocalEvent '{}'", event.getClass(), source, data, isLocalEvent());
    }
  }

  /**
   * @param event may not be null
   * @param doc may not be null
   * @return next event to notify on {@link ObservationManager}, null indicating nothing 
   * to notify
   */
  protected abstract Event getNotifyEvent(Event event, XWikiDocument doc);

  protected BaseObject getRequiredObj(XWikiDocument doc) {
    BaseObject bObj = null;
    if (doc != null) {
      WikiReference wikiRef = webUtilsService.getWikiRef(doc);
      bObj = doc.getXObject(getRequiredObjClassRef(wikiRef));
    }
    return bObj;
  }
  
  /**
   * @param wikiRef
   * @return the class ref for the object required on the triggered doc to qualify for 
   * this listener
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
   * @param docRef
   * @return
   */
  protected abstract Event getCreatingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated created event or null of if listening on 
   * {@link DocumentCreatedEvent}s is not required
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
   * @param docRef
   * @return
   */
  protected abstract Event getUpdatingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated updated event or null of if listening on 
   * {@link DocumentUpdatedEvent}s is not required
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
   * @param docRef
   * @return
   */
  protected abstract Event getDeletingEvent(DocumentReference docRef);

  /**
   * Returns a new instance of the associated deleted event or null of if listening on 
   * {@link DocumentDeletedEvent}s is not required
   * @param docRef
   * @return
   */
  protected abstract Event getDeletedEvent(DocumentReference docRef);

  private boolean isEventING(Event event) {
    return (event instanceof DocumentCreatingEvent) 
        || (event instanceof DocumentUpdatingEvent)
        || (event instanceof DocumentDeletingEvent);
  }

  private boolean isEventED(Event event) {
    return (event instanceof DocumentCreatedEvent) 
        || (event instanceof DocumentUpdatedEvent)
        || (event instanceof DocumentDeletedEvent);
  }

  protected abstract Logger getLogger();

  void injectWebUtilsService(IWebUtilsService webUtilsService) {
    this.webUtilsService = webUtilsService;
  }

}
