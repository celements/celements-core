package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum ENameStandard implements ValueGetter<String>{
  APPLICATION_NAME("application-name"),
  AUTHOR("author"),
  DESCRIPTION("description"),
  GENERATOR("generator"),
  KEYWORDS("keywords"),
  REFERRER("referrer");

  public final static String ATTRIB_NAME = "name";
  public final static String ATTRIB_NAME_ALT = "property";
  private final static ReverseMap<ENameStandard, String> ID_MAP = new ReverseMap<>(ENameStandard.values());

  private final String identifier;

  private ENameStandard(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<ENameStandard> getName(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
