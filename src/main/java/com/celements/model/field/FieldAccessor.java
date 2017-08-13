package com.celements.model.field;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;

/**
 * implementations allow to access values of any generic instance with {@link ClassField} objects
 */
@ComponentRole
public interface FieldAccessor<T> {

  public @NotNull String getName();

  public @Nullable <V> Optional<V> getValue(@NotNull T instance, @NotNull ClassField<V> field)
      throws FieldAccessException;

  public <V> boolean setValue(@NotNull T instance, @NotNull ClassField<V> field, @Nullable V value)
      throws FieldAccessException;

}
