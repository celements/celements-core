package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public class StringField extends AbstractClassField<String> {

  private Integer size;

  public StringField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

  public Integer getSize() {
    return size;
  }

  public StringField setSize(Integer size) {
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

}
