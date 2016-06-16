package com.celements.model.util;

import java.util.Collection;
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
    return field;
  }

  public boolean isField(ClassField<?> otherField) {
    return field.equals(otherField);
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

  @SuppressWarnings("unchecked")
  public static <T> ClassFieldValue<T> select(ClassField<T> toSelect,
      Collection<ClassFieldValue<?>> in) {
    ClassFieldValue<T> ret = null;
    for (ClassFieldValue<?> fieldVal : in) {
      if (toSelect.equals(fieldVal.field)) {
        ret = (ClassFieldValue<T>) fieldVal;
      }
    }
    return ret;
  }

}
