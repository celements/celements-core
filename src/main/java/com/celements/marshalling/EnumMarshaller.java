package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Optional;

public class EnumMarshaller<E extends Enum<E>> extends AbstractMarshaller<E> {

  public EnumMarshaller(Class<E> token) {
    super(token);
  }

  @Override
  public String serialize(E val) {
    return val.name();
  }

  @Override
  public Optional<E> resolve(String val) {
    E enumValue = null;
    try {
      enumValue = Enum.valueOf(getToken(), checkNotNull(val));
    } catch (IllegalArgumentException exc) {
      LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
    }
    return Optional.fromNullable(enumValue);
  }

}
