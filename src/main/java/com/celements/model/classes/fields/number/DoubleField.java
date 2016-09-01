package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;



@Immutable
public final class DoubleField extends NumberField<Double> {

  public static class Builder extends NumberField.Builder<Builder, Double> {

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public DoubleField build() {
      return new DoubleField(getThis());
    }

  }

  protected DoubleField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<Double> getType() {
    return Double.class;
  }

}
