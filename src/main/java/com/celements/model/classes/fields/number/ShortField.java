package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@Immutable
public final class ShortField extends NumberField<Short> {

  public static class Builder extends NumberField.Builder<Builder, Short> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public ShortField build() {
      return new ShortField(getThis());
    }

  }

  protected ShortField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<Short> getType() {
    return Short.class;
  }

}
