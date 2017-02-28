package com.celements.metatag.enums;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum ENameNonStandard implements ValueGetter<String> {

  CREATOR("creator"), GOOGLEBOT("googlebot"), PUBLISHER("publisher"), ROBOTS("robots"), SLURP(
      "slurp"), VIEWPORT("viewport");

  public final static String ATTRIB_NAME = ENameStandard.ATTRIB_NAME;
  public final static String ATTRIB_NAME_ALT = ENameStandard.ATTRIB_NAME_ALT;
  private final static ReverseMap<ENameNonStandard, String> ID_MAP = new ReverseMap<>(
      ENameNonStandard.values());

  private final String identifier;

  private ENameNonStandard(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<ENameNonStandard> getName(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
