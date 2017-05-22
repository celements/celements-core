package com.celements.marshalling;

public class DefaultMarshaller extends AbstractMarshaller<String> {

  public DefaultMarshaller() {
    super(String.class);
  }

  @Override
  public Object serialize(String val) {
    return val;
  }

  @Override
  public String resolve(Object val) {
    return val.toString();
  }

}
