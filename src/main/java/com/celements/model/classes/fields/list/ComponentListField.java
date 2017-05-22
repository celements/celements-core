package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.marshalling.ComponentMarshaller;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class ComponentListField<T> extends CustomListField<T> {

  public static class Builder<T> extends CustomListField.Builder<Builder<T>, T> {

    public Builder(@NotNull String classDefName, @NotNull String name, @NotNull Class<T> role) {
      super(classDefName, name, new ComponentMarshaller<>(role));
      values(Utils.getComponentList(role));
    }

    @Override
    public Builder<T> getThis() {
      return this;
    }

    @Override
    public ComponentListField<T> build() {
      return new ComponentListField<>(getThis());
    }

  }

  protected ComponentListField(@NotNull Builder<T> builder) {
    super(builder);
  }

}
