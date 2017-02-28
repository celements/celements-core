package com.celements.metatag.enums;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum EViewport implements ValueGetter<String> {
  WIDTH("width"), HEIGHT("height"), INITIAL_SCALE("initial-scale"), MINIMUM_SCALE(
      "minimum-scale"), MAXIMUM_SCALE("maximum-scale"), USER_SCALABLE("user-scalable");

  private final static ReverseMap<EViewport, String> ID_MAP = new ReverseMap<>(EViewport.values());

  private final String identifier;

  private EViewport(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<EViewport> getViewport(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
