package com.celements.model.classes.fields.number;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractCelObjectField;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.NumberClass;

public abstract class NumberField<T extends Number> extends AbstractCelObjectField<T> {

  public enum NumberType {
    INTEGER, LONG, FLOAT, DOUBLE;
  }

  private String prettyName;
  private Integer size;
  private String validationRegExp;
  private String validationMessage;

  public NumberField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @NotNull
  public abstract NumberType getNumberType();

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
    element.setNumberType(getNumberType().name().toLowerCase());
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
