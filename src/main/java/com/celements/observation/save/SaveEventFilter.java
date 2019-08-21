package com.celements.observation.save;

import static com.celements.common.MoreObjectsCel.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.xwiki.observation.event.filter.EventFilter;

public class SaveEventFilter<I extends Serializable> implements EventFilter, Serializable {

  private static final long serialVersionUID = -5281875646212008651L;

  private final SaveEventOperation operation;
  private final I identity;

  public SaveEventFilter(SaveEventOperation operation, I identity) {
    this.operation = operation;
    this.identity = identity;
  }

  public Optional<SaveEventOperation> getOperation() {
    return Optional.ofNullable(operation);
  }

  public Optional<I> getIdentity() {
    return Optional.ofNullable(identity);
  }

  @Override
  public String getFilter() {
    return getOperation().map(Enum::name).orElse(".*") + ":"
        + getIdentity().map(Object::toString).orElse(".*");
  }

  @Override
  public boolean matches(EventFilter filter) {
    return tryCast(filter, SaveEventFilter.class)
        .map(other -> isAnyAbsentOrEquals(this.getOperation(), other.getOperation())
            && isAnyAbsentOrEquals(this.getIdentity(), other.getIdentity()))
        .orElse(false);
  }

  private static boolean isAnyAbsentOrEquals(Optional<?> obj1, Optional<?> obj2) {
    return !obj1.isPresent() || !obj2.isPresent() || Objects.equals(obj1.get(), obj2.get());
  }

  @Override
  public int hashCode() {
    return Objects.hash(operation, identity);
  }

  @Override
  public boolean equals(Object obj) {
    return tryCast(obj, SaveEventFilter.class)
        .map(other -> (this.operation == other.operation)
            && Objects.equals(this.identity, other.identity))
        .orElse(false);
  }

  @Override
  public String toString() {
    return "SaveEventFilter [operation=" + operation + ", identity=" + identity + "]";
  }

}
