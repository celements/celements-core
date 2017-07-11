package com.celements.model.access.object;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;

@Immutable
public interface ObjectBridge<D, O> {

  @NotNull
  List<ClassReference> getDocClassRefs();

  @NotNull
  List<O> getObjects(@NotNull ClassReference classRef);

  int getObjectNumber(@NotNull O obj);

  @NotNull
  ClassReference getObjectClassRef(@NotNull O obj);

  @NotNull
  O cloneObject(@NotNull O obj);

  @NotNull
  O createObject(@NotNull ClassReference classRef);

  boolean removeObject(@NotNull O obj);

  @NotNull
  <T> Optional<T> getObjectField(@NotNull O obj, @NotNull ClassField<T> field);

  <T> boolean setObjectField(@NotNull O obj, @NotNull ClassField<T> field, @Nullable T value);

}
