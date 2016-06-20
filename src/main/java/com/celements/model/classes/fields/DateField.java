package com.celements.model.classes.fields;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public class DateField extends AbstractClassField<Date> {

  private volatile Integer size;
  private volatile Integer emptyIsToday;
  private volatile String dateFormat;

  public DateField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<Date> getType() {
    return Date.class;
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

  @Override
  protected PropertyClass getPropertyClass() {
    DateClass element = new DateClass();
    if (size != null) {
      element.setSize(size);
    }
    if (emptyIsToday != null) {
      element.setEmptyIsToday(emptyIsToday);
    }
    if (dateFormat != null) {
      element.setDateFormat(dateFormat);
    }
    return element;
  }

}
