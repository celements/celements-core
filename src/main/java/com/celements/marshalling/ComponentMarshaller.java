package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.manager.ComponentLookupException;

import com.google.common.base.Optional;
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
  public Optional<T> resolve(Object val) {
    T component = null;
    try {
      component = Utils.getComponentManager().lookup(getToken(), getRoleDefault(val.toString()));
    } catch (ComponentLookupException exc) {
      LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
    }
    return Optional.fromNullable(component);
  }

  private String getRoleDefault(String str) {
    return str.trim().isEmpty() ? "default" : str;
  }

}
