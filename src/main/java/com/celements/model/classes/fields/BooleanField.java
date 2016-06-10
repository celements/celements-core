package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BooleanClass;

public class BooleanField extends AbstractCelObjectField<Boolean> {

  private String prettyName;
  private String displayType;
  private Integer defaultValue;
  private String validationRegExp;
  private String validationMessage;

  public BooleanField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public String getPrettyName() {
    return prettyName;
  }

  public BooleanField setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
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

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public BooleanField setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public BooleanField setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    BooleanClass element = new BooleanClass();
    element.setName(getName());
    if (prettyName != null) {
      element.setPrettyName(prettyName);
    }
    if (displayType != null) {
      element.setDisplayType(displayType);
    }
    if (defaultValue != null) {
      element.setDefaultValue(defaultValue);
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
