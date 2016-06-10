package com.celements.model.classes.fields;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.DateClass;

public class DateField extends AbstractCelObjectField<Date> {

  private String prettyName;
  private Integer size;
  private Integer emptyIsToday;
  private String dateFormat;
  private String validationRegExp;
  private String validationMessage;

  public DateField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public String getPrettyName() {
    return prettyName;
  }

  public DateField setPrettyName(String prettyName) {
    this.prettyName = prettyName;
    return this;
  }

  public Integer getSize() {
    return size;
  }

  public DateField setSize(Integer size) {
    this.size = size;
    return this;
  }

  public Integer getEmptyIsToday() {
    return emptyIsToday;
  }

  public DateField setEmptyIsToday(Integer emptyIsToday) {
    this.emptyIsToday = emptyIsToday;
    return this;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public DateField setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
    return this;
  }

  public String getValidationRegExp() {
    return validationRegExp;
  }

  public DateField setValidationRegExp(String validationRegExp) {
    this.validationRegExp = validationRegExp;
    return this;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public DateField setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
    return this;
  }

  @Override
  public PropertyInterface getXField() {
    DateClass element = new DateClass();
    element.setName(getName());
    if (prettyName != null) {
      element.setPrettyName(prettyName);
    }
    if (size != null) {
      element.setSize(size);
    }
    if (emptyIsToday != null) {
      element.setEmptyIsToday(emptyIsToday);
    }
    if (dateFormat != null) {
      element.setDateFormat(dateFormat);
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
