package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;



@Immutable
public final class IntField extends NumberField<Integer> {

  public static class Builder extends NumberField.Builder<Builder, Integer> {

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public IntField build() {
      return new IntField(getThis());
    }

  }

  protected IntField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<Integer> getType() {
    return Integer.class;
  }

}
