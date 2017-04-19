package com.celements.convert.bean;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.convert.Converter;
import com.celements.model.classes.ClassDefinition;

/**
 * interface for BeanConverters to initialize needed parameters
 */
@ComponentRole
public interface BeanConverter<A, B> extends Converter<A, B> {

  public BeanConverter<A, B> initialize(ClassDefinition classDef, Class<B> token);

}
