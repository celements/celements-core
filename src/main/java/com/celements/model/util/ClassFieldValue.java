package com.celements.model.util;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    return new HashCodeBuilder().append(field.hashCode()).append(getValue()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassFieldValue) {
      ClassFieldValue<?> other = (ClassFieldValue<?>) obj;
      return new EqualsBuilder().append(this.field, other.field).append(this.value,
          other.value).isEquals();
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
