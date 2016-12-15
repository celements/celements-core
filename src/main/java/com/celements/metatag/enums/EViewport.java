package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum EViewport {
  WIDTH("width"),
  HEIGHT("height"),
  INITIAL_SCALE("initial-scale"),
  MINIMUM_SCALE("minimum-scale"),
  MAXIMUM_SCALE("maximum-scale"),
  USER_SCALABLE("user-scalable");

  private final static Map<String, EViewport> ID_MAP = new HashMap<>();

  private final String identifier;

  private EViewport(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static EViewport getViewport(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (EViewport accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
