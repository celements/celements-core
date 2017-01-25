package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ENameNonStandard {

  CREATOR("creator"),
  GOOGLEBOT("googlebot"),
  PUBLISHER("publisher"),
  ROBOTS("robots"),
  SLURP("slurp"),
  VIEWPORT("viewport");

  public final static String ATTRIB_NAME = ENameStandard.ATTRIB_NAME;
  public final static String ATTRIB_NAME_ALT = ENameStandard.ATTRIB_NAME_ALT;
  private final static Map<String, ENameNonStandard> ID_MAP = new HashMap<>();

  private final String identifier;

  private ENameNonStandard(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ENameNonStandard getName(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ENameNonStandard accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
