package com.celements.marshalling;

import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

public class ReferenceMarshaller<T extends EntityReference> extends AbstractMarshaller<T> {

  public ReferenceMarshaller(Class<T> token) {
    super(token);
  }

  @Override
  public Object serialize(T val) {
    return getModelUtils().serializeRef(val);
  }

  @Override
  public Optional<T> resolve(Object val) {
    T reference = null;
    try {
      if (getToken() == EntityReference.class) {
        reference = getToken().cast(getModelUtils().resolveRef(val.toString()));
      } else {
        reference = getModelUtils().resolveRef(val.toString(), getToken());
      }
    } catch (IllegalArgumentException exc) {
      LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
    }
    return Optional.fromNullable(reference);
  }

  protected static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
