package com.celements.common.observation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManager;

import com.celements.common.observation.converter.Local;

/**
 * Register to {@link org.xwiki.observation.ObservationManager} for all non-local events
 * and send them to {@link RemoteObservationManager}.
 */
@Component(LocalEventListener.NAME)
public class LocalEventListener extends org.xwiki.observation.remote.internal.LocalEventListener {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalEventListener.class);
  
  public static final String NAME = "observation.remote";
  
  @Override
  public String getName() {
    return NAME;
  }

  public void onEvent(Event event, Object source, Object data) {
    if (event.getClass().isAnnotationPresent(Local.class)) {
      LOGGER.info("skipping local event '{}'", event.getClass());
    } else {
      try {
        super.onEvent(event, source, data);
      } catch (Exception exc) {
        LOGGER.error("failed to notify RemoteObservationManager", exc);
      }
    }
  }

}
