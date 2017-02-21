package com.celements.metatag.enums.opengraph;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.common.ReverseMap;
import com.celements.common.ValueGetter;
import com.google.common.base.Optional;

public enum EOpenGraph implements ValueGetter<String>{

  // There are a lot more properties in the open graph protocol (see http://ogp.me/) as well as in
  OPENGRAPH_TITLE("og:title"),
  OPENGRAPH_TYPE("og:type"),
  OPENGRAPH_IMAGE("og:image"),
  OPENGRAPH_URL("og:url"),
  OPENGRAPH_OPTIONAL_AUDIO("og:audio"),
  OPENGRAPH_OPTIONAL_DESCRIPTION("og:description"),
  OPENGRAPH_OPTIONAL_DETERMINER("og:determiner"),
  OPENGRAPH_OPTIONAL_LOCALE("og:locale "),
  OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE("og:locale:alternate"),
  OPENGRAPH_OPTIONAL_SITENAME("og:site_name"),
  OPENGRAPH_OPTIONAL_VIDEO("og:video"),
  OPENGRAPH_OPTIONAL_IMAGE_WIDTH("og:image:width"),
  OPENGRAPH_OPTIONAL_IMAGE_HEIGHT("og:image:height");

  public final static String ATTRIB_NAME = "property";
  
  private final static ReverseMap<EOpenGraph, String> ID_MAP = new ReverseMap<>(EOpenGraph.values());

  private final String identifier;

  private EOpenGraph(String identifier) {
    this.identifier = identifier;
  }

  private EOpenGraph(EOpenGraph identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @NotNull
  public static Optional<EOpenGraph> getOpenGraph(@Nullable String identifier) {
    return ID_MAP.get(identifier);
  }

  @Override
  public String getValue() {
    return getIdentifier();
  }
}
