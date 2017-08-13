package com.celements.model.object.restriction;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.ClassIdentity;
import com.celements.model.object.ObjectBridge;

@Immutable
public class ClassRestriction<O> extends ObjectRestriction<O> {

  private final ClassIdentity classId;

  public ClassRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassIdentity classId) {
    super(bridge);
    this.classId = checkNotNull(classId);
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return classId.equals(getBridge().getObjectClass(obj));
  }

  public ClassIdentity getClassIdentity() {
    return classId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getClassIdentity());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassRestriction) {
      ClassRestriction<?> other = (ClassRestriction<?>) obj;
      return super.equals(obj) && Objects.equals(this.getClassIdentity(), other.getClassIdentity());
    }
    return false;
  }

  @Override
  public String toString() {
    return "ClassRestriction [classId=" + getClassIdentity() + "]";
  }

}
