package com.celements.metatag.enums.twitter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;

public enum ETwitterCardType implements ValueGetter<String> {

  SUMMARY("summary"),
  SUMMARY_LARGE_IMAGE("summary_large_image"),
  PLAYER("player"),
  APP("app");

  private final static ReverseMap<ETwitterCardType, String> ID_MAP = new ReverseMap<>(
      ETwitterCardType.values());

  private final String identifier;

  private ETwitterCardType(String identifier) {
    this.identifier = identifier;
  }

  private ETwitterCardType(ETwitterCardType identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static ETwitterCardType getTwitterCardType(@Nullable String identifier) {
    return ID_MAP.get(identifier).get();
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
