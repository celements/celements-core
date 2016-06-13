package com.celements.model.util;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Preconditions;

public class ClassFieldValue<T> {

  private final ClassField<T> field;
  private final T value;

  public ClassFieldValue(@NotNull ClassField<T> field, @Nullable T value) {
    this.field = Preconditions.checkNotNull(field);
    this.value = value;
  }

  public ClassField<T> getField() {
    return this.field;
  }

  public T getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassFieldValue) {
      ClassFieldValue<?> other = (ClassFieldValue<?>) obj;
      return Objects.equals(this.field, other.field) && Objects.equals(this.value, other.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean local) {
    return field.toString(local) + ": " + value;
  }

}
