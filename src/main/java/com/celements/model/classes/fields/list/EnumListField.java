package com.celements.model.classes.fields.list;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;

@Immutable
public class EnumListField<E extends Enum<E>> extends CustomListField<E> {

  protected final Class<E> enumType;

  public static class Builder<E extends Enum<E>> extends CustomListField.Builder<Builder<E>, E> {

    private final Class<E> enumType;

    public Builder(@NotNull String classDefName, @NotNull String name, @NotNull Class<E> enumType) {
      super(classDefName, name);
      this.enumType = checkNotNull(enumType);
    }

    @Override
    public Builder<E> getThis() {
      return this;
    }

    @Override
    public EnumListField<E> build() {
      return new EnumListField<>(getThis());
    }

  }

  protected EnumListField(@NotNull Builder<E> builder) {
    super(builder);
    this.enumType = builder.enumType;
  }

  @Override
  protected Function<E, Object> getSerializeFunction() {
    return new Function<E, Object>() {

      @Override
      public Object apply(E val) {
        return val.name();
      }
    };
  }

  @Override
  protected Function<Object, E> getResolveFunction() {
    return new Function<Object, E>() {

      @Override
      public E apply(Object elem) {
        return Enum.valueOf(enumType, elem.toString());
      }
    };
  }

  @Override
  protected List<E> getPossibleValues() {
    return Arrays.asList(enumType.getEnumConstants());
  }

}
