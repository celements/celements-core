package com.celements.rights.access;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.ImmutableMap;

public enum EAccessLevel {

  VIEW("view"), COMMENT("comment"), EDIT("edit"), DELETE("delete"), UNDELETE("undelete"), REGISTER(
      "register"), PROGRAMMING("programming");

  private static Map<String, EAccessLevel> ID_MAP;

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
    if (ID_MAP == null) {
      Map<String, EAccessLevel> map = new HashMap<>();
      for (EAccessLevel accessLevel : values()) {
        map.put(accessLevel.getIdentifier(), accessLevel);
      }
      ID_MAP = ImmutableMap.copyOf(map);
    }
    return ID_MAP.get(identifier);
  }

}
