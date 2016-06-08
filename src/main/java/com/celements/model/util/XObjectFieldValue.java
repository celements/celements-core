package com.celements.model.util;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.python.google.common.base.Objects;
import org.xwiki.model.reference.EntityReference;

public class XObjectFieldValue<T> extends XObjectField<T> {

  private final T value;

  public XObjectFieldValue(@NotNull XObjectField<T> field, @Nullable T value) {
    super(field.getClassRef(), field.getName(), field.getToken());
    this.value = value;
  }

  public XObjectFieldValue(@NotNull XObjectField<T> field,
      @NotNull Map<XObjectField<?>, ?> fieldMap) {
    this(field, field.resolveFromXOjectValue(fieldMap.get(field)));
  }

  public T getValue() {
    return value;
  }

  public Object serializeToXObjectValue() {
    Object ret = getValue();
    if (EntityReference.class.isAssignableFrom(getToken())) {
      ret = getWebUtils().serializeRef((EntityReference) getValue());
    } else {
      ret = getValue();
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(getValue()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      if (obj instanceof XObjectFieldValue) {
        XObjectFieldValue<?> other = (XObjectFieldValue<?>) obj;
        return Objects.equal(getValue(), other.getValue());
      } else {
        return getValue() == null;
      }
    }
    return false;
  }

  @Override
  public XObjectFieldValue<T> clone() {
    return new XObjectFieldValue<>(new XObjectField<>(getClassRef(), getName(), getToken()),
        getValue());
  }

  @Override
  public String toString() {
    return toString(true);
  }

  @Override
  public String toString(boolean local) {
    return super.toString(local) + ": " + value;
  }

}
