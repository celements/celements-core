package com.celements.common.observation.listener;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.observation.event.Event;

public abstract class AbstractRemoteEventListener<S, D> extends AbstractEventListener<S, D> {

  @Override
  protected void onLocalEvent(Event event, S source, D data) {
    onEventInternal(event, source, data);
  }

  @Override
  protected void onRemoteEvent(Event event, S source, D data) {
    onEventInternal(event, source, data);
  }

  protected abstract void onEventInternal(@NotNull Event event, @NotNull S source,
      @Nullable D data);

}
