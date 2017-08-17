package com.celements.convert.bean;

import static com.google.common.base.Preconditions.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;

import com.celements.convert.classes.XObjectConverter;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

/**
 * <p>
 * Converts an XObject to a Bean
 * </p>
 * IMPORTANT: call {@link #initialize(ClassDefinition, Class)} exactly once per component
 * instantiation, otherwise {@link #apply(BaseObject)} will throw an {@link IllegalStateException}.
 * When used as a {@link Requirement}, using {@link Initializable} is suitable.
 */
@Component(XObjectBeanConverter.NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XObjectBeanConverter<T> extends XObjectConverter<T> implements
    BeanConverter<BaseObject, T> {

  public static final String NAME = "xobjectbean";

  private ClassDefinition classDef;
  private Class<T> token;

  @Requirement(BeanFieldAccessor.NAME)
  private FieldAccessor<T> beanAccessor;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public XObjectBeanConverter<T> initialize(ClassDefinition classDef, Class<T> token) {
    checkState(this.classDef == null, "already initialized");
    this.classDef = checkNotNull(classDef);
    this.token = checkNotNull(token);
    return this;
  }

  @Override
  protected ClassDefinition getClassDef() {
    checkState(classDef != null, "not initialized");
    return classDef;
  }

  @Override
  public T createInstance() {
    checkState(token != null, "not initialized");
    return Utils.getComponent(token);
  }

  @Override
  protected FieldAccessor<T> getToFieldAccessor() {
    return beanAccessor;
  }

}
