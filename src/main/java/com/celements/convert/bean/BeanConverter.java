package com.celements.convert.bean;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.convert.Converter;
import com.celements.model.classes.ClassDefinition;

/**
 * <p>
 * interface for Converters handling beans to initialize needed parameters
 * </p>
 * IMPORTANT: call {@link #initialize(ClassDefinition, Class)} exactly once per component
 * instantiation, otherwise {@link #apply(A)} will throw an {@link IllegalStateException}
 */
@ComponentRole
public interface BeanConverter<A, B> extends Converter<A, B> {

  public BeanConverter<A, B> initialize(ClassDefinition classDef, Class<B> token);

}
