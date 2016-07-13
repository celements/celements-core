package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@Immutable
public final class DoubleField extends NumberField<Double> {

  public static class Builder extends NumberField.Builder<Builder, Double> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
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
