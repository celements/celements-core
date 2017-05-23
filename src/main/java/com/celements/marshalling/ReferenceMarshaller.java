package com.celements.marshalling;

import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
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
  public T resolve(Object val) {
    if (getToken() == EntityReference.class) {
      return getToken().cast(getModelUtils().resolveRef(val.toString()));
    } else {
      return getModelUtils().resolveRef(val.toString(), getToken());
    }
  }

  protected static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
