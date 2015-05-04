package com.celements.common.observation.converter;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.observation.remote.converter.AbstractXWikiEventConverter;

@Component("Annotation")
public class AnnotationEventConverter extends AbstractXWikiEventConverter {

  private static Logger LOGGER = LoggerFactory.getLogger(AnnotationEventConverter.class);

  @Override
  public boolean toRemote(LocalEventData localEvent, RemoteEventData remoteEvent) {
    Event event = localEvent.getEvent();
    LOGGER.trace("toRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("toRemote: serialize event '{}'", event.getClass());
      // fill the remote event
      remoteEvent.setEvent((Serializable) event);
      remoteEvent.setSource(serializeXWikiDocument((XWikiDocument) localEvent.getSource(
          )));
      remoteEvent.setData(serializeXWikiContext((XWikiContext) localEvent.getData()));
      return true;
    } else {
      LOGGER.debug("toRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  @Override
  public boolean fromRemote(RemoteEventData remoteEvent, LocalEventData localEvent) {
    Event event = (Event) remoteEvent.getEvent();
    LOGGER.trace("fromRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("fromRemote: unserialize event '{}'", event.getClass());
      // fill the local event
      XWikiContext context = unserializeXWikiContext(remoteEvent.getData());
      if (context != null) {
        localEvent.setEvent(event);
        localEvent.setSource(unserializeDocument(remoteEvent.getSource()));
        localEvent.setData(unserializeXWikiContext(remoteEvent.getData()));
      }
      return true;
    } else {
      LOGGER.debug("fromRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  private boolean shouldConvert(Class<? extends Event> eventClass) {
    return eventClass.getAnnotation(Remote.class) != null;
  }

}
