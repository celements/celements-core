package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Optional;

public class DefaultMarshaller extends AbstractMarshaller<String> {

  public DefaultMarshaller() {
    super(String.class);
  }

  @Override
  public Object serialize(String val) {
    return checkNotNull(val);
  }

  @Override
  public Optional<String> resolve(Object val) {
    return Optional.of(val.toString());
  }

}
