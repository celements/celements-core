package com.celements.common.observation.listener;

import org.xwiki.observation.event.Event;

public abstract class AbstractLocalEventListener<S, D> extends AbstractEventListener {

  @Override
  @SuppressWarnings("unchecked")
  protected void onLocalEvent(Event event, Object source, Object data) {
    onEventInternal(event, (S) source, (D) data);
  }

  @Override
  protected void onRemoteEvent(Event event, Object source, Object data) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("onRemoteEvent: skipped event '{}' on source '{}', data '{}'",
          event.getClass(), source, data);
    }
  }

  protected abstract void onEventInternal(Event event, S source, D data);

}
