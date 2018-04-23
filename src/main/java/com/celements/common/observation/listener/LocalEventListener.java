package com.celements.common.observation.listener;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

  final Logger LOGGER;

  public static final String NAME = "observation.remote";

  final Map<Class<? extends Event>, Long> lastLoggedMap = new ConcurrentHashMap<>();

  public LocalEventListener() {
    LOGGER = LoggerFactory.getLogger(LocalEventListener.class);
  }

  /**
   * for test purposes only
   */
  LocalEventListener(Logger logger) {
    LOGGER = logger;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    Class<? extends Event> type = event.getClass();
    if (type.isAnnotationPresent(Local.class)) {
      LOGGER.info("skipping local event '{}'", event.getClass());
    } else {
      if (checkSerializability(type, source, data)) {
        try {
          super.onEvent(event, source, data);
        } catch (Exception exc) {
          LOGGER.error("unable to notify remote event [{}], source [{}], data [{}]", type, source,
              data, exc);
        }
      }
    }
  }

  boolean checkSerializability(Class<? extends Event> type, Object source, Object data) {
    boolean serializable = true;
    String msg = "unable to notify remote event [{0}]";
    if (!(source instanceof Serializable)) {
      msg += ", source [{1}] not serializable";
      serializable = false;
    }
    if (!(data instanceof Serializable)) {
      msg += ", data [{2}] not serializable";
      serializable = false;
    }
    if (!serializable && wasLoggedLongerThanOneHourAgo(type)) {
      lastLoggedMap.put(type, System.currentTimeMillis());
      LOGGER.warn(MessageFormat.format(msg, type.getSimpleName(), source, data));
    }
    return serializable;
  }

  private boolean wasLoggedLongerThanOneHourAgo(Class<? extends Event> type) {
    return (System.currentTimeMillis() - lastLoggedMap.getOrDefault(type, 0L)) > (1000L * 60 * 60);
  }

}
