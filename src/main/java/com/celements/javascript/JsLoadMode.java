package com.celements.javascript;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public enum JsLoadMode {

  SYNC, DEFER, ASYNC;

  private static final Logger LOGGER = LoggerFactory.getLogger(JsLoadMode.class);

  @NotNull
  public static JsLoadMode convertStoreValue(@Nullable String storeValue) {
    try {
      return valueOf(Strings.nullToEmpty(storeValue).toUpperCase());
    } catch (IllegalArgumentException exp) {
      LOGGER.info("JsLoadMode convertStoreValue failed for [{}] using SYNC", storeValue);
      return SYNC;
    }
  }

}
