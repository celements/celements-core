package com.celements.convert.bean;

import static com.google.common.base.Preconditions.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import com.celements.convert.classes.XObjectDeconverter;
import com.celements.model.access.field.FieldAccessor;
import com.celements.model.classes.ClassDefinition;
import com.xpn.xwiki.objects.BaseObject;

@Component(BeanXObjectConverter.NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class BeanXObjectConverter<T> extends XObjectDeconverter<T> implements
    BeanConverter<T, BaseObject> {

  public static final String NAME = "beanxobject";

  private ClassDefinition classDef;

  @Requirement(BeanFieldAccessor.NAME)
  private FieldAccessor<T> beanFieldConverter;

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
  protected FieldAccessor<T> getFromFieldConverter() {
    return beanFieldConverter;
  }

}
