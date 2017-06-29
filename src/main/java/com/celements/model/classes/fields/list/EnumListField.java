package com.celements.model.classes.fields.list;

import java.util.Arrays;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.marshalling.EnumMarshaller;

@Immutable
public class EnumListField<E extends Enum<E>> extends CustomListField<E> {

  public static class Builder<B extends Builder<B, E>, E extends Enum<E>> extends
      CustomListField.Builder<B, E> {

    public Builder(@NotNull String classDefName, @NotNull String name, @NotNull Class<E> enumType) {
      this(classDefName, name, new EnumMarshaller<>(enumType));
    }

    public Builder(@NotNull String classDefName, @NotNull String name,
        @NotNull EnumMarshaller<E> marshaller) {
      super(classDefName, name, marshaller);
      values(Arrays.asList(marshaller.getToken().getEnumConstants()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public B getThis() {
      return (B) this;
    }

    @Override
    public EnumListField<E> build() {
      return new EnumListField<>(getThis());
    }

  }

  protected EnumListField(@NotNull Builder<?, E> builder) {
    super(builder);
  }

}
