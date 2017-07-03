package com.celements.marshalling;

import javax.annotation.concurrent.Immutable;

import com.celements.rights.access.EAccessLevel;
import com.google.common.base.Optional;

@Immutable
public final class AccessLevelMarshaller extends EnumMarshaller<EAccessLevel> {

  public AccessLevelMarshaller() {
    super(EAccessLevel.class);
  }

  @Override
  public String serialize(EAccessLevel val) {
    return val.getIdentifier();
  }

  @Override
  public Optional<EAccessLevel> resolve(String val) {
    return EAccessLevel.getAccessLevel(val);
  }

}
