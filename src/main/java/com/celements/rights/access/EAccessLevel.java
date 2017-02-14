package com.celements.rights.access;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum EAccessLevel implements ValueGetter<String> {

  VIEW("view"), COMMENT("comment"), EDIT("edit"), DELETE("delete"), UNDELETE("undelete"), REGISTER(
      "register"), PROGRAMMING("programming");

  private static ReverseMap<EAccessLevel, String> ID_MAP = new ReverseMap<>(EAccessLevel.values());

  private final String identifier;

  private EAccessLevel(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<EAccessLevel> getAccessLevel(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
