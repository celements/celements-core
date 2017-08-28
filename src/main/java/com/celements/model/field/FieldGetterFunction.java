package com.celements.model.field;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Function;

@Immutable
public class FieldGetterFunction<T, V> implements Function<T, V> {

  private final FieldAccessor<T> accessor;
  private final ClassField<V> field;

  public FieldGetterFunction(@NotNull FieldAccessor<T> accessor, @NotNull ClassField<V> field) {
    this.accessor = checkNotNull(accessor);
    this.field = checkNotNull(field);
  }

  @Override
  public V apply(@NotNull T instance) {
    return accessor.getValue(instance, field).orNull();
  }

}
