package com.celements.observation.save;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Objects;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.common.observation.converter.Remote;

@Remote
public class SaveEvent<I> extends AbstractFilterableEvent {

  private static final long serialVersionUID = 5283183777778144685L;

  public SaveEvent(SaveEventFilter<I> filter) {
    super(filter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public SaveEventFilter<I> getEventFilter() {
    return (SaveEventFilter<I>) super.getEventFilter();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEventFilter());
  }

  @Override
  public boolean equals(Object obj) {
    return tryCast(obj, SaveEvent.class)
        .map(other -> Objects.equals(this.getEventFilter(), other.getEventFilter()))
        .orElse(false);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [filter=" + getEventFilter() + "]";
  }

}
