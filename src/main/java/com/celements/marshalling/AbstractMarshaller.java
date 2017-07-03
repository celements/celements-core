package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

public abstract class AbstractMarshaller<T> implements Marshaller<T> {

  protected static final Logger LOGGER = LoggerFactory.getLogger(Marshaller.class);

  private final Class<T> token;

  public AbstractMarshaller(@NotNull Class<T> token) {
    this.token = checkNotNull(token);
  }

  @Override
  public Class<T> getToken() {
    return token;
  }

  @Override
  public Function<T, String> getSerializer() {
    return SERIALIZER;
  }

  private final Function<T, String> SERIALIZER = new Function<T, String>() {

    @Override
    public String apply(T val) {
      return serialize(val);
    }
  };

  @Override
  public Function<String, T> getResolver() {
    return RESOLVER;
  }

  private final Function<String, T> RESOLVER = new Function<String, T>() {

    @Override
    public T apply(String val) {
      return resolve(val).orNull();
    }
  };

}
