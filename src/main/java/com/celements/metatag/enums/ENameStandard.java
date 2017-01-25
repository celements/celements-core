package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ENameStandard {
  APPLICATION_NAME("application-name"),
  AUTHOR("author"),
  DESCRIPTION("description"),
  GENERATOR("generator"),
  KEYWORDS("keywords"),
  REFERRER("referrer");

  public final static String ATTRIB_NAME = "name";
  public final static String ATTRIB_NAME_ALT = "property";
  private final static Map<String, ENameStandard> ID_MAP = new HashMap<>();

  private final String identifier;

  private ENameStandard(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ENameStandard getName(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ENameStandard accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
