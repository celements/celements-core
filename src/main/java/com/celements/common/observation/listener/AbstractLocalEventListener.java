package com.celements.common.observation.listener;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

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

  protected abstract void onEventInternal(@NotNull Event event, @NotNull S source,
      @Nullable D data);

}
