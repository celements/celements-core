package com.celements.marshalling;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;

@Immutable
public interface Marshaller<T> {

  public @NotNull Class<T> getToken();

  public @NotNull Object serialize(@NotNull T val);

  public @NotNull Function<T, Object> getSerializer();

  public @NotNull Optional<T> resolve(@NotNull Object val);

  public @NotNull Function<Object, T> getResolver();

}
