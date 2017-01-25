package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ECharset {

  UTF8("UTF-8"),
  LATIN1("ISO-8859-1"),
  ISO8859_1(LATIN1),
  USASCII("US-ASCII"),
  DEFAULT(UTF8);

  public final static String ATTRIB_NAME = "charset";
  private final static Map<String, ECharset> ID_MAP = new HashMap<>();

  private final String identifier;

  private ECharset(String identifier) {
    this.identifier = identifier;
  }

  private ECharset(ECharset identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ECharset getCharset(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ECharset accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
