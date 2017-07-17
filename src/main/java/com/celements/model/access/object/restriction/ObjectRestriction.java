package com.celements.model.access.object.restriction;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.ObjectBridge;
import com.google.common.base.Predicate;

@Immutable
public abstract class ObjectRestriction<O> implements Predicate<O> {

  private final ObjectBridge<?, O> bridge;

  protected ObjectRestriction(@NotNull ObjectBridge<?, O> bridge) {
    this.bridge = checkNotNull(bridge);
  }

  public ObjectBridge<?, O> getBridge() {
    return bridge;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bridge.getObjectType());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ObjectRestriction) {
      ObjectRestriction<?> other = (ObjectRestriction<?>) obj;
      return this.bridge.getObjectType() == other.bridge.getObjectType();
    }
    return false;
  }

}
