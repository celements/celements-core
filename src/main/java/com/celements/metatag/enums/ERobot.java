package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ERobot {

  INDEX("index"),
  NOINDEX("noindex"),
  FOLLOW("follow"),
  NOFOLLOW("nofollow"),
  NOODP("noodp"),
  NOARCHIVE("noarchive"),
  NOSNIPPET("nosnippet"),
  NOIMAGEINDEX("noimageindex"),
  NOCACHE("nocache");

  private final static Map<String, ERobot> ID_MAP = new HashMap<>();

  private final String identifier;

  private ERobot(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ERobot getAccessLevel(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ERobot accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
