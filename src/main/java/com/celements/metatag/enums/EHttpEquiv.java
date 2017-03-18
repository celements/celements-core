package com.celements.metatag.enums;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum EHttpEquiv implements ValueGetter<String> {

  CONTENT_SECURITY_POLICY("Content-Security-Policy"),
  DEFAULT_STYLE("default-style"),
  REFRESH("refresh");

  public final static String ATTRIB_NAME = "http-equiv";
  private final static ReverseMap<EHttpEquiv, String> ID_MAP = new ReverseMap<>(
      EHttpEquiv.values());

  private final String identifier;

  private EHttpEquiv(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<EHttpEquiv> getHttpEquiv(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
