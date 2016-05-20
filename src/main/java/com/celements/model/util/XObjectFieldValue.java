package com.celements.model.util;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class XObjectFieldValue<T> extends XObjectField<T> {

  private final T value;

  public XObjectFieldValue(@NotNull XObjectField<T> field, @Nullable T value) {
    super(field.getClassRef(), field.getName(), field.getToken());
    this.value = value;
  }

  public T getValue() {
    return value;
  }

}
