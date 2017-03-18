package com.celements.metatag.enums;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum EReferrer implements ValueGetter<String> {

  NO_REFFERER("no-referrer"),
  ORIGIN("origin"),
  NO_REFERRER_WHEN_DOWNGRADE("no-referrer-when-downgrade"),
  ORIGIN_WHEN_CROSSORIGIN("origin-when-crossorigin"),
  UNSAVE_URL("unsafe-URL");

  private final static ReverseMap<EReferrer, String> ID_MAP = new ReverseMap<>(EReferrer.values());

  private final String identifier;

  private EReferrer(String identifier) {
    this.identifier = identifier;
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<EReferrer> getReferrer(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
