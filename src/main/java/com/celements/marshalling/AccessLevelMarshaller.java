package com.celements.marshalling;

import com.celements.rights.access.EAccessLevel;

public class AccessLevelMarshaller extends EnumMarshaller<EAccessLevel> {

  public AccessLevelMarshaller() {
    super(EAccessLevel.class);
  }

  @Override
  public Object serialize(EAccessLevel val) {
    return val.getIdentifier();
  }

  @Override
  public EAccessLevel resolve(Object val) {
    return EAccessLevel.getAccessLevel(val.toString()).orNull();
  }

}
