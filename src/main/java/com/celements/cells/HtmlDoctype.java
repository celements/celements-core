package com.celements.cells;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum HtmlDoctype implements ValueGetter<String> {

  XHTML("XHTML 1.1"),
  HTML5("HTML 5");

  private String dbName;

  private static final ReverseMap<HtmlDoctype, String> DB_NAME_MAP = new ReverseMap<>(
      HtmlDoctype.values());

  private HtmlDoctype(@NotNull String dbName) {
    this.dbName = dbName;
  }

  @NotNull
  public static Optional<HtmlDoctype> getHtmlDoctype(@Nullable String dbName) {
    return DB_NAME_MAP.get(dbName);
  }

  @Override
  public String getValue() {
    return dbName;
  }

}
