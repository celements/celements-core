package com.celements.lastChanged;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

@Component("lastChange-DocumentChangesListener")
public class DocumentChangesListener implements EventListener {

  private static Logger _LOGGER = LoggerFactory.getLogger(DocumentChangesListener.class);

  @Requirement
  ILastChangedRole lastChangedSrv;

  @Override
  public String getName() {
    return "lastChange-DocumentChangesListener";
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentCreatedEvent(), new DocumentUpdatedEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if ((source != null) && (source instanceof XWikiDocument)) {
      XWikiDocument doc = (XWikiDocument) source;
      DocumentReference docRef = doc.getDocumentReference();
      ((LastChangedService) lastChangedSrv).invalidateCacheForSpaceRef(
          docRef.getLastSpaceReference());
    } else {
      _LOGGER.error("onEvent failed docref '{}'", source);
    }
  }

}
