package com.celements.convert;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.google.common.base.Function;

/**
 * implementations allow to convert an instance of type A to type B. they can be used as a
 * {@link Function}.
 */
@ComponentRole
public interface Converter<A, B> extends Function<A, B> {

  public @NotNull String getName();

  @Override
  public @NotNull B apply(@Nullable A input) throws ConversionException;

}
