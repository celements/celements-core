package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;



@Immutable
public final class ShortField extends NumberField<Short> {

  public static class Builder extends NumberField.Builder<Builder, Short> {

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
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
