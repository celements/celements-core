package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

@Immutable
public final class StaticListField extends StringListField {

  public static class Builder extends StringListField.Builder<Builder> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    @Override
    public StaticListField build() {
      return new StaticListField(this);
    }

  }

  protected StaticListField(@NotNull Builder builder) {
    super(builder);
  }

}
