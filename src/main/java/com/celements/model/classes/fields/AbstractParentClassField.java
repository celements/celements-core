package com.celements.model.classes.fields;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.celements.model.util.ModelUtils;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

/**
 * Subclasses are expected to be immutable
 */
public abstract class AbstractParentClassField<T> implements ClassField<T> {

  private final String name;

  public abstract static class Builder<B extends Builder<B, T>, T> {

    private final String name;

    public Builder(@NotNull String name) {
      this.name = Objects.requireNonNull(Strings.emptyToNull(name));
    }

    public abstract B getThis();

    public abstract AbstractParentClassField<T> build();

  }

  protected AbstractParentClassField(@NotNull Builder<?, T> builder) {
    this.name = builder.name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClassDef(), name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractParentClassField) {
      AbstractParentClassField<?> other = (AbstractParentClassField<?>) obj;
      return Objects.equals(this.getClassDef(), other.getClassDef()) && Objects.equals(this.name,
          other.name);
    }
    return false;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClassDef()).append(".").append(name).toString();
  }

  protected static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
