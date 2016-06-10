package com.celements.model.classes.fields;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

public abstract class AbstractCelObjectField<T> implements CelObjectField<T> {

  private final DocumentReference classRef;
  private final String name;

  public AbstractCelObjectField(@NotNull DocumentReference classRef, @NotNull String name) {
    this.classRef = Objects.requireNonNull(classRef);
    this.name = Objects.requireNonNull(Strings.emptyToNull(name));
  }

  @Override
  public DocumentReference getClassRef() {
    return new DocumentReference(classRef);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getClassRef()).append(getName()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractCelObjectField) {
      AbstractCelObjectField<?> other = (AbstractCelObjectField<?>) obj;
      return new EqualsBuilder().append(getClassRef(), other.getClassRef()).append(getName(),
          other.getName()).isEquals();
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  @Override
  public String toString(boolean local) {
    return getWebUtils().serializeRef(getClassRef(), local) + "." + getName();
  }

  protected static IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T resolveFromXFieldValue(Object obj) {
    try {
      return (T) obj;
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException("Field ill defined: " + this, ex);
    }
  }

  @Override
  public Object serializeToXFieldValue(T value) {
    return value;
  }

}
