package com.celements.marshalling;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;

@Immutable
public interface Marshaller<T> {

  public @NotNull Class<T> getToken();

  public @NotNull String serialize(@NotNull T val);

  public @NotNull Function<T, String> getSerializer();

  public @NotNull Optional<T> resolve(@NotNull String val);

  public @NotNull Function<String, T> getResolver();

}
