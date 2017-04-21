package com.celements.convert.bean;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;

import com.celements.convert.Converter;
import com.celements.model.classes.ClassDefinition;

/**
 * <p>
 * interface for Converters handling beans to initialize needed parameters
 * </p>
 * IMPORTANT: call {@link #initialize(ClassDefinition, Class)} exactly once per component
 * instantiation, otherwise {@link #apply(A)} will throw an {@link IllegalStateException}. When used
 * as a {@link Requirement}, using {@link Initializable} is suitable.
 */
@ComponentRole
public interface BeanConverter<A, B> extends Converter<A, B> {

  public BeanConverter<A, B> initialize(ClassDefinition classDef, Class<B> token);

}
