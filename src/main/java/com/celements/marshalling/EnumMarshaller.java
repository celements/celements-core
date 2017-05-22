package com.celements.marshalling;

public class EnumMarshaller<E extends Enum<E>> extends AbstractMarshaller<E> {

  public EnumMarshaller(Class<E> token) {
    super(token);
  }

  @Override
  public Object serialize(E val) {
    return val.name();
  }

  @Override
  public E resolve(Object val) {
    return Enum.valueOf(getToken(), val.toString());
  }

}
