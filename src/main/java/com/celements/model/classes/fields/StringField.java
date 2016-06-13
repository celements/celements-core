package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.StringClass;

public class StringField extends AbstractClassField<String> {

  private String prettyName;
  private Integer size;
  private String validationRegExp;
  private String validationMessage;

  public StringField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public StringField setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
  }

  public Integer getSize() {
    return size;
  }

  public StringField setSize(Integer size) {
    this.size = size;
    return this;
  }

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public StringField setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public StringField setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    StringClass element = new StringClass();
    element.setName(getName());
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
