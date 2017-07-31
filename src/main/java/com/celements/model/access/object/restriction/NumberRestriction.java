package com.celements.model.access.object.restriction;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.ObjectBridge;

@Immutable
public class NumberRestriction<O> extends ObjectRestriction<O> {

  private final int number;

  public NumberRestriction(@NotNull ObjectBridge<?, O> bridge, int number) {
    super(bridge);
    this.number = number;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return getBridge().getObjectNumber(obj) == getNumber();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getNumber());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NumberRestriction) {
      NumberRestriction<?> other = (NumberRestriction<?>) obj;
      return super.equals(obj) && Objects.equals(this.getNumber(), other.getNumber());
    }
    return false;
  }

  @Override
  public String toString() {
    return "NumberRestriction [number=" + getNumber() + "]";
  }

}
