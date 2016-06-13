package com.celements.model.classes.fields.number;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.NumberClass;

public abstract class NumberField<T extends Number> extends AbstractClassField<T> {

  private String prettyName;
  private Integer size;
  private String validationRegExp;
  private String validationMessage;

  public NumberField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public String getPrettyName() {
    return prettyName;
  }

  public NumberField<T> setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
  }

  public Integer getSize() {
    return size;
  }

  public NumberField<T> setSize(Integer size) {
    this.size = size;
    return this;
  }

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public NumberField<T> setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public NumberField<T> setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    NumberClass element = new NumberClass();
    element.setName(getName());
    element.setNumberType(getType().getSimpleName().toLowerCase());
    if (prettyName != null) {
      element.setPrettyName(prettyName);
    }
    if (size != null) {
      element.setSize(size);
    }
    if (validationRegExp != null) {
      element.setValidationRegExp(validationRegExp);
    }
    if (validationMessage != null) {
      element.setValidationMessage(validationMessage);
    }
    return element;
  }

}
