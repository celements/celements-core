package com.celements.common.observation.listener;

import static java.text.MessageFormat.*;

import java.io.NotSerializableException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteObservationManager;
import org.xwiki.observation.remote.RemoteObservationManagerConfiguration;

import com.celements.common.observation.converter.Local;
import com.google.common.collect.ImmutableList;

/**
 * Register to {@link org.xwiki.observation.ObservationManager} for all non-local events
 * and send them to {@link RemoteObservationManager}.
 */
@Component(LocalEventListener.NAME)
public class LocalEventListener implements EventListener {

  private Logger LOGGER = LoggerFactory.getLogger(LocalEventListener.class);

  public static final String NAME = "observation.remote";

  final Map<Class<? extends Event>, LogCounter> logCountMap = new ConcurrentHashMap<>();

  @Requirement
  private ComponentManager componentManager;

  @Requirement
  private RemoteObservationManagerConfiguration configuration;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return configuration.isEnabled()
        ? ImmutableList.of(AllEvent.ALLEVENT)
        : ImmutableList.of();
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    Class<? extends Event> type = event.getClass();
    if (type.isAnnotationPresent(Local.class)) {
      LOGGER.info("skipping local event '{}'", event.getClass());
    } else {
      try {
        componentManager.lookup(RemoteObservationManager.class)
            .notify(new LocalEventData(event, source, data));
      } catch (Exception exc) {
        if (containsNotSerializableException(exc)) {
          logNotSerializableException(type, source, data, exc);
        } else {
          LOGGER.error("unable to notify remote event [{}], source [{}], data [{}]", type, source,
              data, exc);
        }
      }
    }
  }

  private boolean containsNotSerializableException(Throwable exc) {
    return (exc != null) && ((exc instanceof NotSerializableException)
        || containsNotSerializableException(exc.getCause()));
  }

  private void logNotSerializableException(Class<? extends Event> type, Object source, Object data,
      Exception exc) {
    String msg = "not serializable remote event [{0}], source [{1}], data [{2}]";
    if (!logCountMap.containsKey(type) || logCountMap.get(type).isOneHourAgo()) {
      final LogCounter replacedLogCount = logCountMap.put(type, new LogCounter());
      if (replacedLogCount != null) {
        msg += ", occured {3} times within last hour (since {4})";
        msg = format(msg, type.getSimpleName(), source, data, replacedLogCount.count,
            replacedLogCount.time);
      } else {
        msg = format(msg, type.getSimpleName(), source, data);
      }
      LOGGER.warn(msg, exc);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format(msg, type.getSimpleName(), source, data), exc);
    }
    logCountMap.get(type).increment();
  }

  static class LogCounter {

    final long time = System.currentTimeMillis();
    final AtomicLong count = new AtomicLong();

    public boolean isOneHourAgo() {
      return (System.currentTimeMillis() - time) >= (1000L * 60 * 60);
    }

    public void increment() {
      count.incrementAndGet();
    }

  }

  /**
   * for test purposes only
   */
  Logger injectLogger(Logger logger) {
    LOGGER = logger;
    return LOGGER;
  }

}
