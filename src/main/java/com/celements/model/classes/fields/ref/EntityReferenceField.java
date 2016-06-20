package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public abstract class EntityReferenceField<T extends EntityReference> extends AbstractClassField<T>
    implements CustomClassField<T> {

  private volatile Integer size;

  public EntityReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public Integer getSize() {
    return size;
  }

  public EntityReferenceField<T> setSize(Integer size) {
    this.size = size;
    return this;
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
      ret = getWebUtils().serializeRef(value);
    }
    return ret;
  }

  @Override
  public T resolve(Object obj) {
    T ret = null;
    if (obj != null) {
      ret = getWebUtils().resolveReference(obj.toString(), getType());
    }
    return ret;
  }

}
