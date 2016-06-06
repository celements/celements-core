package com.celements.model.util;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

public class XObjectField<T> {

  private final DocumentReference classRef;
  private final String name;

  private final Class<T> token;

  public XObjectField(@NotNull DocumentReference classRef, @NotNull String name,
      @NotNull Class<T> token) {
    this.classRef = Objects.requireNonNull(classRef);
    this.name = Objects.requireNonNull(Strings.emptyToNull(name));
    this.token = Objects.requireNonNull(token);
  }

  public XObjectField(@Nullable String wiki, @NotNull String classSpace, @NotNull String className,
      @NotNull String name, @NotNull Class<T> token) {
    this(new DocumentReference(Objects.requireNonNull(Strings.emptyToNull(className)),
        new SpaceReference(Objects.requireNonNull(Strings.emptyToNull(classSpace)), getWebUtils()
            .resolveWikiReference(wiki))), name, token);
  }

  public XObjectField(@NotNull String classSpace, @NotNull String className, @NotNull String name,
      @NotNull Class<T> token) {
    this(null, classSpace, className, name, token);
  }

  public DocumentReference getClassRef() {
    return new DocumentReference(classRef);
  }

  public String getName() {
    return name;
  }

  public Class<T> getToken() {
    return token;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getClassRef()).append(getName()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof XObjectField) {
      XObjectField<?> other = (XObjectField<?>) obj;
      return new EqualsBuilder().append(getClassRef(), other.getClassRef()).append(getName(), other
          .getName()).isEquals();
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean local) {
    return getWebUtils().serializeRef(getClassRef(), local) + "." + getName();
  }

  protected static IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
