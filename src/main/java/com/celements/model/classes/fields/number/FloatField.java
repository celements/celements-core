package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@Immutable
public class FloatField extends NumberField<Float> {

  public static class Builder extends NumberField.Builder<Builder, Float> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public FloatField build() {
      return new FloatField(getThis());
    }

  }

  protected FloatField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<Float> getType() {
    return Float.class;
  }

}
