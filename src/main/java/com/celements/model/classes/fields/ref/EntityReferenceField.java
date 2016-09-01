package com.celements.model.classes.fields.ref;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;


import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public abstract class EntityReferenceField<T extends EntityReference> extends AbstractClassField<T>
    implements CustomClassField<T> {

  private final Integer size;

  public abstract static class Builder<B extends Builder<B, T>, T extends EntityReference> extends
      AbstractClassField.Builder<B, T> {

    private Integer size;

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    public B size(@Nullable Integer val) {
      size = val;
      return getThis();
    }

  }

  protected EntityReferenceField(@NotNull Builder<?, T> builder) {
    super(builder);
    this.size = builder.size;
  }

  public Integer getSize() {
    return size;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    StringClass element = new StringClass();
    if (size != null) {
      element.setSize(size);
    }
    return element;
  }

  @Override
  public Object serialize(T value) {
    Object ret = null;
    if (value != null) {
      ret = getModelUtils().serializeRef(value);
    }
    return ret;
  }

  @Override
  public T resolve(Object obj) {
    T ret = null;
    if (obj != null) {
      ret = getModelUtils().resolveRef(obj.toString(), getType());
    }
    return ret;
  }

}
