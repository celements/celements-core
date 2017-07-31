package com.celements.model.access.object.restriction;

import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.common.MoreFunctions;
import com.celements.model.access.object.ObjectBridge;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

@Immutable
public class IdentityRestriction<O> extends ObjectRestriction<O> {

  private final Set<Integer> hashCodes;

  public IdentityRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull O obj) {
    super(bridge);
    this.hashCodes = ImmutableSet.of(obj.hashCode());
  }

  public IdentityRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull Iterable<O> obj) {
    super(bridge);
    this.hashCodes = FluentIterable.from(obj).transform(MoreFunctions.hashCodeFunction()).toSet();
  }

  public Set<Integer> getHashCodes() {
    return hashCodes;
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return getHashCodes().contains(obj.hashCode());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getHashCodes());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IdentityRestriction) {
      IdentityRestriction<?> other = (IdentityRestriction<?>) obj;
      return super.equals(obj) && Objects.equals(this.getHashCodes(), other.getHashCodes());
    }
    return false;
  }

  @Override
  public String toString() {
    return "IdentityRestriction [hashCodes=" + getHashCodes() + "]";
  }

}
