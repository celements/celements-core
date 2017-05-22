package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.web.Utils;

public class ComponentMarshaller<T> extends AbstractMarshaller<T> {

  public ComponentMarshaller(@NotNull Class<T> role) {
    super(role);
    checkArgument(role.isAnnotationPresent(ComponentRole.class));
  }

  @Override
  public Object serialize(T val) {
    return getRoleDefault(val.getClass().getAnnotation(Component.class).value());
  }

  @Override
  public T resolve(Object val) {
    return Utils.getComponent(getToken(), getRoleDefault(val.toString()));
  }

  private String getRoleDefault(String str) {
    return str.trim().isEmpty() ? "default" : str;
  }

}
