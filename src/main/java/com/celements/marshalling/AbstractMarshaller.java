package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.validation.constraints.NotNull;

import com.google.common.base.Function;

public abstract class AbstractMarshaller<T> implements Marshaller<T> {

  private final Class<T> token;

  public AbstractMarshaller(@NotNull Class<T> token) {
    this.token = checkNotNull(token);
  }

  @Override
  public Class<T> getToken() {
    return token;
  }

  @Override
  public Function<T, Object> getSerializer() {
    return SERIALIZER;
  }

  private final Function<T, Object> SERIALIZER = new Function<T, Object>() {

    @Override
    public Object apply(T val) {
      return serialize(val);
    }
  };

  @Override
  public Function<Object, T> getResolver() {
    return RESOLVER;
  }

  private final Function<Object, T> RESOLVER = new Function<Object, T>() {

    @Override
    public T apply(Object val) {
      return resolve(val);
    }
  };

}
