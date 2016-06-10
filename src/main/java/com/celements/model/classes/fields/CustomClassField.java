package com.celements.model.classes.fields;

import javax.annotation.Nullable;

public interface CustomClassField<T> extends ClassField<T> {

  @Nullable
  public T resolve(@Nullable Object obj);

  @Nullable
  public Object serialize(@Nullable T value);

}
