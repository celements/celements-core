package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum EReferrer {

  NO_REFFERER("no-referrer"),
  ORIGIN("origin"),
  NO_REFERRER_WHEN_DOWNGRADE("no-referrer-when-downgrade"),
  ORIGIN_WHEN_CROSSORIGIN("origin-when-crossorigin"),
  UNSAVE_URL("unsafe-URL");

  private final static Map<String, EReferrer> ID_MAP = new HashMap<>();

  private final String identifier;

  private EReferrer(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static EReferrer getReferrer(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (EReferrer accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
