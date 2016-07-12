package com.celements.model.classes.fields.number;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@Immutable
public class LongField extends NumberField<Long> {

  public static class Builder extends NumberField.Builder<Builder, Long> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
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
