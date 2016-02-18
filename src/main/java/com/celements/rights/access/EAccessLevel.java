package com.celements.rights.access;

import java.util.HashMap;
import java.util.Map;

public enum EAccessLevel {

  VIEW("view"), 
  COMMENT("comment"),
  EDIT("edit"),
  DELETE("delete"),
  UNDELETE("undelete"),
  REGISTER("register"),
  PROGRAMMING("programming");

  private final String identifier;
  private final static Map<String, EAccessLevel> idMap = new HashMap<>();

  private EAccessLevel(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  public static EAccessLevel getAccessLevel(String identifier) {
    if (idMap.isEmpty()) {
      for (EAccessLevel accessLevel : values()) {
        idMap.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return (idMap.get(identifier));
  }

}
