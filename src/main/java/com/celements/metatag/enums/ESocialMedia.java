package com.celements.metatag.enums;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ESocialMedia {

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
  FACEBOOK_(),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_(""),
  TWITTER_("");

  private final static Map<String, ESocialMedia> ID_MAP = new HashMap<>();

  private final String identifier;

  private ESocialMedia(String identifier) {
    this.identifier = identifier;
  }

  private ESocialMedia(ESocialMedia identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ESocialMedia getAccessLevel(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ESocialMedia accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
