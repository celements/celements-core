package com.celements.javascript;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;

public enum JsLoadMode {

  SYNC, DEFER, ASYNC;

  @NotNull
  public static JsLoadMode convertStoreValue(@Nullable String storeValue) {
    try {
      return valueOf(Strings.nullToEmpty(storeValue).toUpperCase());
    } catch (IllegalArgumentException exp) {
      return SYNC;
    }
  }

}
