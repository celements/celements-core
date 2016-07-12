package com.celements.model.classes.fields;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface CustomClassField<T> extends ClassField<T> {

  @Nullable
  public Object serialize(@Nullable T value);

  @Nullable
  public T resolve(@Nullable Object obj);

}
