package com.celements.model.classes.fields.list;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

@Immutable
public final class StaticListField extends StringListField {

  public static class Builder extends StringListField.Builder<Builder> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
    }

    // override needed for backwards compatibility with older version to avoid NoSuchMethodError:
    // com.celements.model.classes.fields.list.StaticListField$Builder.values(Ljava/util/List;)
    @Override
    public Builder values(List<String> values) {
      return super.values(values);
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
