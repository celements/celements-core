package com.celements.marshalling;

import com.google.common.base.Optional;

public class EnumMarshaller<E extends Enum<E>> extends AbstractMarshaller<E> {

  public EnumMarshaller(Class<E> token) {
    super(token);
  }

  @Override
  public Object serialize(E val) {
    return val.name();
  }

  @Override
  public Optional<E> resolve(Object val) {
    E enumValue = null;
    try {
      enumValue = Enum.valueOf(getToken(), val.toString());
    } catch (IllegalArgumentException exc) {
      LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
    }
    return Optional.fromNullable(enumValue);
  }

}
