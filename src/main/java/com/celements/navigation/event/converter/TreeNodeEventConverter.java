package com.celements.navigation.event.converter;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;
import org.xwiki.observation.remote.converter.AbstractEventConverter;

@Component("TreeNodeEventConverter")
public class TreeNodeEventConverter extends AbstractEventConverter {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeEventConverter.class);

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.observation.remote.converter.AbstractEventConverter#getPriority()
   */
  public int getPriority() {
    return 1000;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.observation.remote.converter.RemoteEventConverter#fromRemote(org.xwiki.observation.remote.RemoteEventData,
   *      org.xwiki.observation.remote.LocalEventData)
   */
  public boolean fromRemote(RemoteEventData remoteEvent, LocalEventData localEvent) {
    LOGGER.debug("fromRemote remoteEvent [" + remoteEvent + "], localEvent [" + localEvent
        + "].");
    if (remoteEvent.getEvent() instanceof Event) {
      localEvent.setEvent((Event) remoteEvent.getEvent());
      if (remoteEvent.getSource() != null) {
        localEvent.setSource(remoteEvent.getSource());
      }
      if (remoteEvent.getData() != null) {
        localEvent.setData(remoteEvent.getData());
      }
      return true;
    } else {
      LOGGER.debug("fromRemote not instance of Event [" + remoteEvent.getEvent() + "].");
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.observation.remote.converter.LocalEventConverter#toRemote(org.xwiki.observation.remote.LocalEventData,
   *      org.xwiki.observation.remote.RemoteEventData)
   */
  public boolean toRemote(LocalEventData localEvent, RemoteEventData remoteEvent) {
    LOGGER.debug("toRemote remoteEvent [" + remoteEvent + "], localEvent [" + localEvent
        + "].");
    if (isSerializable(localEvent)) {
      remoteEvent.setEvent((Serializable) localEvent.getEvent());
      remoteEvent.setSource((Serializable) localEvent.getSource());
      remoteEvent.setData((Serializable) localEvent.getData());
      return true;
    } else {
      LOGGER.debug("toRemote not serializable [" + localEvent.getClass().getName()
          + "], localEvent [" + localEvent + "].");
    }

    return false;
  }

  /**
   * Indicate if a local event is fully serializable.
   * 
   * @param localEvent
   *          the local event
   * @return true is the local event is fully serializable, false otherwise.
   */
  private boolean isSerializable(LocalEventData localEvent) {
    boolean isSerializable = localEvent.getEvent() instanceof Serializable
        && isSerializable(localEvent.getData()) && isSerializable(localEvent.getSource());
    LOGGER.trace("isSerializable: [" + isSerializable + "], getEvent instanceof ["
        + (localEvent.getEvent() instanceof Serializable) + "] isSerializable getData ["
        + isSerializable(localEvent.getData()) + "] isSerializable getSource ["
        + isSerializable(localEvent.getSource()) + "].");
    return isSerializable;
  }

  /**
   * Indicate if an object is serializable.
   * 
   * @param obj
   *          the object to test
   * @return true is the object is serializable, false otherwise.
   */
  private boolean isSerializable(Object obj) {
    return obj instanceof Serializable || obj == null;
  }

}
