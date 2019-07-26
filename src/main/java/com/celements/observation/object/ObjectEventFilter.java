package com.celements.observation.object;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Objects;
import java.util.Optional;

import org.xwiki.observation.event.filter.EventFilter;

import com.celements.model.classes.ClassIdentity;
import com.celements.observation.event.EventOperation;

public class ObjectEventFilter implements EventFilter {

  private final EventOperation operation;
  private final ClassIdentity classId;

  public ObjectEventFilter(EventOperation operation, ClassIdentity classId) {
    this.operation = operation;
    this.classId = classId;
  }

  public Optional<EventOperation> getOperation() {
    return Optional.ofNullable(operation);
  }

  public Optional<ClassIdentity> getClassId() {
    return Optional.ofNullable(classId);
  }

  @Override
  public String getFilter() {
    return getOperation().map(Enum::name).orElse(".*") + ":"
        + getClassId().map(ClassIdentity::serialize).orElse(".*");
  }

  @Override
  public boolean matches(EventFilter filter) {
    return tryCast(filter, ObjectEventFilter.class)
        .map(other -> isAnyAbsentOrEquals(this.getOperation(), other.getOperation())
            && isAnyAbsentOrEquals(this.getClassId(), other.getClassId()))
        .orElse(false);
  }

  private static boolean isAnyAbsentOrEquals(Optional<?> obj1, Optional<?> obj2) {
    return !obj1.isPresent() || !obj2.isPresent() || Objects.equals(obj1.get(), obj2.get());
  }

  @Override
  public int hashCode() {
    return Objects.hash(operation, classId);
  }

  @Override
  public boolean equals(Object obj) {
    return tryCast(obj, ObjectEventFilter.class)
        .map(other -> (this.operation == other.operation)
            && Objects.equals(this.classId, other.classId))
        .orElse(false);
  }

  @Override
  public String toString() {
    return "ObjectEventFilter [operation=" + operation + ", classId=" + classId + "]";
  }

}
