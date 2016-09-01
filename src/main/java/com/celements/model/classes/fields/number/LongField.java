package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;



@Immutable
public final class LongField extends NumberField<Long> {

  public static class Builder extends NumberField.Builder<Builder, Long> {

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public LongField build() {
      return new LongField(getThis());
    }

  }

  protected LongField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<Long> getType() {
    return Long.class;
  }

}
