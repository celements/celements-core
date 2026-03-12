package com.celements.metatag.enums;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum ECharset implements ValueGetter<String> {

  UTF8("UTF-8"),
  LATIN1("ISO-8859-1"),
  ISO8859_1(LATIN1),
  USASCII("US-ASCII"),
  DEFAULT(UTF8);

  public final static String ATTRIB_NAME = "charset";
  private final static ReverseMap<ECharset, String> ID_MAP = new ReverseMap<>(ECharset.values());

  private final String identifier;

  private ECharset(String identifier) {
    this.identifier = identifier;
  }

  private ECharset(ECharset identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<ECharset> getCharset(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }

}
