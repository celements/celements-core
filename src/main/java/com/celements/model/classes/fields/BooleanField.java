package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.BooleanClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public class BooleanField extends AbstractClassField<Boolean> {

  private String displayType;
  private Integer defaultValue;

  public BooleanField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<Boolean> getType() {
    return Boolean.class;
  }

  public String getDisplayType() {
    return displayType;
  }

  public BooleanField setDisplayType(String displayType) {
    this.displayType = displayType;
    return this;
  }

  public Integer getDefaultValue() {
    return defaultValue;
  }

  public BooleanField setDefaultValue(int defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    BooleanClass element = new BooleanClass();
    if (displayType != null) {
      element.setDisplayType(displayType);
    }
    if (defaultValue != null) {
      element.setDefaultValue(defaultValue);
    }
    return element;
  }

}
