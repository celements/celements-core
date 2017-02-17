package com.celements.metatag.enums.opengraph;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum EOpenGraph {

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
  private final static Map<String, EOpenGraph> ID_MAP = new HashMap<>();

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

  @Nullable
  public static EOpenGraph getOpenGraph(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (EOpenGraph accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
