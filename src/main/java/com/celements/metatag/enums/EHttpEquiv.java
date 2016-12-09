package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum EHttpEquiv {

  CONTENT_LANGUAGE("content-language"),
  CONTENT_SECURITY_POLICY("Content-Security-Policy"),
  CONTENT_TYPE("content-type"),
  DEFAULT_STYLE("default-style"),
  REFRESH("refresh"),
  SET_COOKIE("set-cookie");

  public final static String ATTRIB_NAME = "http-equiv";
  private final static Map<String, EHttpEquiv> ID_MAP = new HashMap<>();

  private final String identifier;

  private EHttpEquiv(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static EHttpEquiv getAccessLevel(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (EHttpEquiv accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
