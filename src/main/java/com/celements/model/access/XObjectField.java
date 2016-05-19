package com.celements.model.access;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

public class XObjectField<T> {

  private final DocumentReference classRef;
  private final String name;

  public XObjectField(@NotNull DocumentReference classRef, @NotNull String name) {
    this(Objects.requireNonNull(classRef).getLastSpaceReference().getName(), classRef.getName(),
        name);
  }

  public XObjectField(@NotNull String classSpace, @NotNull String className, @NotNull String name) {
    Objects.requireNonNull(Strings.emptyToNull(classSpace));
    Objects.requireNonNull(Strings.emptyToNull(className));
    Objects.requireNonNull(Strings.emptyToNull(name));
    this.classRef = new DocumentReference(className, new SpaceReference(classSpace,
        (WikiReference) null));
    this.name = name;
  }

  public DocumentReference getClassRef() {
    return classRef;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getClassRef()).append(getName()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof XObjectField) {
      XObjectField<?> other = (XObjectField<?>) obj;
      return new EqualsBuilder().append(this.getClassRef(), other.getClassRef()).append(this
          .getName(), other.getName()).isEquals();
    }
    return false;
  }

  @Override
  public String toString() {
    return Utils.getComponent(IWebUtilsService.class).serializeRef(getClassRef(), true) + "."
        + getName();
  }

}
