package com.celements.model.classes.fields.list;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;

import com.google.common.base.Function;
import com.xpn.xwiki.web.Utils;

@Immutable
public class ComponentListField<T> extends CustomListField<T> {

  protected final Class<T> role;

  public static class Builder<T> extends CustomListField.Builder<Builder<T>, T> {

    private final Class<T> role;

    public Builder(@NotNull String classDefName, @NotNull String name, @NotNull Class<T> role) {
      super(classDefName, name);
      checkArgument(checkNotNull(role).isAnnotationPresent(ComponentRole.class));
      this.role = role;
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
    this.role = builder.role;
  }

  @Override
  protected Function<T, Object> getSerializeFunction() {
    return new Function<T, Object>() {

      @Override
      public Object apply(T val) {
        return getRoleHint(val.getClass().getAnnotation(Component.class).value());
      }
    };
  }

  @Override
  protected Function<Object, T> getResolveFunction() {
    return new Function<Object, T>() {

      @Override
      public T apply(Object elem) {
        return Utils.getComponent(role, getRoleHint(elem.toString()));
      }
    };
  }

  @Override
  protected List<T> getPossibleValues() {
    return Utils.getComponentList(role);
  }

  private String getRoleHint(String str) {
    return str.trim().isEmpty() ? "default" : str;
  }

}
