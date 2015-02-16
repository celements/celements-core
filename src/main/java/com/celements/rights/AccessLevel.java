package com.celements.rights;

public enum AccessLevel {

  VIEW("view"), 
  COMMENT("comment"),
  EDIT("edit"),
  DELETE("delete"),
  UNDELETE("undelete"),
  REGISTER("register"),
  PROGRAMMING("programming");

  private final String identifier;

  private AccessLevel(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

}
