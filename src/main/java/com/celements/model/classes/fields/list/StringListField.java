package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.marshalling.DefaultMarshaller;

@Immutable
public class StringListField extends CustomListField<String> {

  public static class Builder extends CustomListField.Builder<Builder, String> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name, new DefaultMarshaller());
    }

    @Override
    public StringListField build() {
      return new StringListField(this);
    }

  }

  protected StringListField(@NotNull Builder builder) {
    super(builder);
  }

}
