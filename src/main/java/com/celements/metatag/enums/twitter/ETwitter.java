package com.celements.metatag.enums.twitter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum ETwitter {

  // the Twitter (https://dev.twitter.com/cards/markup). Add more if needed.
  TWITTER_CARD("twitter:card"),
  TWITTER_SITE("twitter:site"),
  TWITTER_SITE_ID("twitter:site:id"),
  TWITTER_CREATOR("twitter:creator"),
  TWITTER_CREATOR_ID("twitter:creator:id"),
  TWITTER_DESCRIPTION("twitter:description"), // If missing, Twitter falls back to og:description
  TWITTER_TITLE("twitter:title"), // If missing, Twitter falls back to og:title
  TWITTER_IMAGE("twitter:image"), // If missing, Twitter falls back to og:image
  TWITTER_IMAGE_ALT("twitter:image:alt");

  public final static String ATTRIB_NAME = "name";
  private final static Map<String, ETwitter> ID_MAP = new HashMap<>();

  private final String identifier;

  private ETwitter(String identifier) {
    this.identifier = identifier;
  }

  private ETwitter(ETwitter identifier) {
    this.identifier = identifier.getIdentifier();
  }

  @NotNull
  public String getIdentifier() {
    return identifier;
  }

  @Nullable
  public static ETwitter getTwitter(@Nullable String identifier) {
    if (ID_MAP.isEmpty()) {
      for (ETwitter accessLevel : values()) {
        ID_MAP.put(accessLevel.getIdentifier(), accessLevel);
      }
    }
    return ID_MAP.get(identifier);
  }
}
