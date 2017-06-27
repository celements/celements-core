package com.celements.marshalling;

import com.celements.rights.access.EAccessLevel;
import com.google.common.base.Optional;

public class AccessLevelMarshaller extends EnumMarshaller<EAccessLevel> {

  public AccessLevelMarshaller() {
    super(EAccessLevel.class);
  }

  @Override
  public Object serialize(EAccessLevel val) {
    return val.getIdentifier();
  }

  @Override
  public Optional<EAccessLevel> resolve(Object val) {
    return EAccessLevel.getAccessLevel(val.toString());
  }

}
