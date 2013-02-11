package com.celements.validation;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class FieldName {

  private final String className;
  private final String fieldName;

  FieldName(String className, String fieldName) {
    this.className = className;
    this.fieldName = fieldName;
  }

  String getClassName() {
    return className;
  }

  String getFieldName() {
    return fieldName;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(className).append(fieldName).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldName) {
      FieldName other = (FieldName) obj;
      return new EqualsBuilder().append(className, other.className).append(fieldName,
          other.fieldName).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "Field [className=" + className + ", fieldName=" + fieldName + "]";
  }

}