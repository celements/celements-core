package com.celements.rights.access;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum EAccessLevel {

  VIEW("view"),
  COMMENT("comment"),
  EDIT("edit"),
  DELETE("delete"),
  UNDELETE("undelete"),
  REGISTER("register"),
  PROGRAMMING("programming");

  private final static Map<String, EAccessLevel> ID_MAP = new HashMap<>();

  private final String identifier;

  private EAccessLevel(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static EAccessLevel getAccessLevel(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (EAccessLevel accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }

}
