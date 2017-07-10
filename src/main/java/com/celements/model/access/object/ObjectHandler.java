package com.celements.model.access.object;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public interface ObjectHandler<D, O> {

  @NotNull
  ObjectHandler<D, O> filter(@NotNull ClassReference classRef);

  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull T value);

  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull Collection<T> values);

  @NotNull
  ObjectHandler<D, O> filterAbsent(@NotNull ClassField<?> field);

  @NotNull
  ObjectFetcher<D, O> fetch();

  @NotNull
  ObjectEditor<D, O> edit();
}
