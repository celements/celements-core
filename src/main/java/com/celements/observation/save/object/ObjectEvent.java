package com.celements.observation.object;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Objects;

import org.xwiki.observation.event.AbstractFilterableEvent;

import com.celements.common.observation.converter.Local;
import com.celements.model.classes.ClassIdentity;
import com.celements.observation.event.EventOperation;

@Local
public class ObjectEvent extends AbstractFilterableEvent {

  private static final long serialVersionUID = -6666255714705030039L;

  public ObjectEvent() {
    this(new ObjectEventFilter(null, null));
  }

  public ObjectEvent(EventOperation type) {
    this(new ObjectEventFilter(type, null));
  }

  public ObjectEvent(ClassIdentity classId) {
    this(new ObjectEventFilter(null, classId));
  }

  public ObjectEvent(EventOperation operation, ClassIdentity classId) {
    this(new ObjectEventFilter(operation, classId));
  }

  public ObjectEvent(ObjectEventFilter filter) {
    super(filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEventFilter());
  }

  @Override
  public boolean equals(Object obj) {
    return tryCast(obj, ObjectEvent.class)
        .map(other -> Objects.equals(this.getEventFilter(), other.getEventFilter()))
        .orElse(false);
  }

  @Override
  public String toString() {
    return "ObjectEvent [filter=" + getEventFilter() + "]";
  }

}
