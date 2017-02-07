package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum ERobot implements ValueGetter<String>{

  INDEX("index"),
  NOINDEX("noindex"),
  FOLLOW("follow"),
  NOFOLLOW("nofollow"),
  NOODP("noodp"),
  NOARCHIVE("noarchive"),
  NOSNIPPET("nosnippet"),
  NOIMAGEINDEX("noimageindex"),
  NOCACHE("nocache");

  private final static ReverseMap<ERobot, String> ID_MAP = new ReverseMap<>(ERobot.values());
  
  private final String identifier;

  private ERobot(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<ERobot> getRobot(@Nullable String identifier) {
   return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return identifier;
  }
}
