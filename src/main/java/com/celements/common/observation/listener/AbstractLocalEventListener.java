package com.celements.common.observation.listener;

import org.xwiki.observation.event.Event;

public abstract class AbstractLocalEventListener<S, D> extends AbstractEventListener {
  
  protected abstract Class<S> getSourceClass();
  
  protected abstract Class<D> getDataClass();

  @Override
  protected void onLocalEvent(Event event, Object source, Object data) {
    onEvent(event, getSourceClass().cast(source), getDataClass().cast(data));
  }

  protected abstract void onEventInternal(Event event, S source, D data);

  @Override
  protected void onRemoteEvent(Event event, Object source, Object data) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("onRemoteEvent: skipped event '{}' on source '{}', data '{}'",
          event.getClass(), source, data);
    }
  }

}
