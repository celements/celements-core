package com.celements.common;

import javax.validation.constraints.NotNull;

public interface ValueGetter<T> {

  @NotNull
  T getValue();

}
