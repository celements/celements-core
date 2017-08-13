package com.celements.convert.bean;

import static com.google.common.base.Preconditions.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;

import com.celements.convert.classes.XObjectDeconverter;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.xpn.xwiki.objects.BaseObject;

/**
 * <p>
 * Converts a Bean to an XObject
 * </p>
 * IMPORTANT: call {@link #initialize(ClassDefinition, Class)} exactly once per component
 * instantiation, otherwise {@link #apply(T)} will throw an {@link IllegalStateException}. When used
 * as a {@link Requirement}, using {@link Initializable} is suitable.
 */
@Component(BeanXObjectConverter.NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class BeanXObjectConverter<T> extends XObjectDeconverter<T> implements
    BeanConverter<T, BaseObject> {

  public static final String NAME = "beanxobject";

  private ClassDefinition classDef;

  @Requirement(BeanFieldAccessor.NAME)
  private FieldAccessor<T> beanAccessor;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public BeanXObjectConverter<T> initialize(ClassDefinition classDef, Class<BaseObject> token) {
    checkState(this.classDef == null, "already initialized");
    this.classDef = checkNotNull(classDef);
    return this;
  }

  @Override
  protected ClassDefinition getClassDef() {
    checkState(classDef != null, "not initialized");
    return classDef;
  }

  @Override
  protected FieldAccessor<T> getFromFieldAccessor() {
    return beanAccessor;
  }

}
