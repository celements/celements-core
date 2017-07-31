package com.celements.model.access.object.restriction;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectBridge;

@Immutable
public class ClassRestriction<O> extends ObjectRestriction<O> {

  private final ClassReference ref;

  public ClassRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassReference ref) {
    super(bridge);
    this.ref = checkNotNull(ref);
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return ref.equals(getBridge().getObjectClassRef(obj));
  }

  public ClassReference getClassRef() {
    return ref;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getClassRef());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassRestriction) {
      ClassRestriction<?> other = (ClassRestriction<?>) obj;
      return super.equals(obj) && Objects.equals(this.getClassRef(), other.getClassRef());
    }
    return false;
  }

  @Override
  public String toString() {
    return "ClassRestriction [ref=" + getClassRef() + "]";
  }

}
