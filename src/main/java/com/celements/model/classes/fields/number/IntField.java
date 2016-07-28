package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@Immutable
public final class IntField extends NumberField<Integer> {

  public static class Builder extends NumberField.Builder<Builder, Integer> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
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
