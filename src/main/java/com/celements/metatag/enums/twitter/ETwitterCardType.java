package com.celements.metatag.enums.twitter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ETwitterCardType {

  SUMMARY("summary"),
  SUMMARY_LARGE_IMAGE("summary_large_image"),
  PLAYER("player"),
  APP("app");

  private final static Map<String, ETwitterCardType> ID_MAP = new HashMap<>();

  private final String identifier;

  private ETwitterCardType(String identifier) {
    this.identifier = identifier;
  }

  private ETwitterCardType(ETwitterCardType identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ETwitterCardType getTwitterCardType(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ETwitterCardType accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
