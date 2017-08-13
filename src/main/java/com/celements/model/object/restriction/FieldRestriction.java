package com.celements.model.object.restriction;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.ObjectBridge;
import com.google.common.collect.ImmutableSet;

@Immutable
public class FieldRestriction<O, T> extends ClassRestriction<O> {

  private final ClassField<T> field;
  private final Set<T> values;

  public FieldRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassField<T> field,
      @NotNull T value) {
    this(bridge, field, ImmutableSet.of(value));
  }

  public FieldRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassField<T> field,
      @NotNull Collection<T> values) {
    super(bridge, field.getClassDef().getClassReference());
    this.field = checkNotNull(field);
    this.values = ImmutableSet.copyOf(values);
  }

  public ClassField<T> getField() {
    return field;
  }

  public Set<T> getValues() {
    return values;
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return super.apply(obj) && values.contains(getBridge().getObjectField(obj,
        getField()).orNull());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getField(), getValues());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldRestriction) {
      FieldRestriction<?, ?> other = (FieldRestriction<?, ?>) obj;
      return super.equals(obj) && Objects.equals(this.getField(), other.getField())
          && Objects.equals(this.getValues(), other.getValues());
    }
    return false;
  }

  @Override
  public String toString() {
    return "FieldRestriction [field=" + getField() + ", values=" + getValues() + "]";
  }

}
