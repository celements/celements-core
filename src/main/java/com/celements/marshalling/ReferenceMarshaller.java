package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class ReferenceMarshaller<T extends EntityReference> extends AbstractMarshaller<T> {

  public ReferenceMarshaller(Class<T> token) {
    super(token);
  }

  @Override
  public String serialize(T val) {
    return getModelUtils().serializeRef(val);
  }

  @Override
  public Optional<T> resolve(String val) {
    checkNotNull(val);
    T reference = null;
    try {
      if (getToken() == EntityReference.class) {
        reference = getToken().cast(getModelUtils().resolveRef(val));
      } else {
        reference = getModelUtils().resolveRef(val, getToken());
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
