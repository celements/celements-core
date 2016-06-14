package com.celements.model.classes.fields.number;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class NumberField<T extends Number> extends AbstractClassField<T> {

  private Integer size;

  public NumberField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public Integer getSize() {
    return size;
  }

  public NumberField<T> setSize(Integer size) {
    this.size = size;
    return this;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    NumberClass element = new NumberClass();
    element.setNumberType(getType().getSimpleName().toLowerCase());
    if (size != null) {
      element.setSize(size);
    }
    return element;
  }

}
