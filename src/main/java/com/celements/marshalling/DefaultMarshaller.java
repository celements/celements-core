package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;

@Immutable
public final class DefaultMarshaller extends AbstractMarshaller<String> {

  public DefaultMarshaller() {
    super(String.class);
  }

  @Override
  public String serialize(String val) {
    return checkNotNull(val);
  }

  @Override
  public Optional<String> resolve(String val) {
    return Optional.of(val);
  }

}
