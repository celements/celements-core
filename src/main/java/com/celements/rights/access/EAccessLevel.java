package com.celements.rights.access;

public enum EAccessLevel {

  VIEW("view"), 
  COMMENT("comment"),
  EDIT("edit"),
  DELETE("delete"),
  UNDELETE("undelete"),
  REGISTER("register"),
  PROGRAMMING("programming");

  private final String identifier;

  private EAccessLevel(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

}
