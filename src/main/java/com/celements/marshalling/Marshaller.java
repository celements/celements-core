package com.celements.marshalling;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;

public interface Marshaller<T> {

  public @NotNull Class<T> getToken();

  public @Nullable Object serialize(@NotNull T val);

  public @NotNull Function<T, Object> getSerializer();

  public @Nullable T resolve(@NotNull Object val);

  public @NotNull Function<Object, T> getResolver();

}
