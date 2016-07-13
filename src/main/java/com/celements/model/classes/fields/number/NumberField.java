package com.celements.model.classes.fields.number;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class NumberField<T extends Number> extends AbstractClassField<T> {

  private final Integer size;

  public abstract static class Builder<B extends Builder<B, T>, T extends Number> extends
      AbstractClassField.Builder<B, T> {

    private Integer size;

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    public B size(@Nullable Integer val) {
      size = val;
      return getThis();
    }

  }

  protected NumberField(@NotNull Builder<?, T> builder) {
    super(builder);
    this.size = builder.size;
  }

  public Integer getSize() {
    return size;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    NumberClass element = new NumberClass();
    element.setNumberType(getType().getSimpleName().toLowerCase());
    if (size != null) {
      element.setSize(size);
    }
    return element;
  }

}
