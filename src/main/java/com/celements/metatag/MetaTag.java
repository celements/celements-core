package com.celements.metatag;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.EHttpEquiv;
import com.celements.metatag.enums.ENameNonStandard;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.EReferrer;
import com.celements.metatag.enums.ERobot;
import com.celements.metatag.enums.EViewport;
import com.celements.metatag.enums.opengraph.EOpenGraph;
import com.celements.metatag.enums.twitter.ETwitter;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public final class MetaTag {

  private final Map<String, String> attribs;
  private final Optional<String> content;

  public MetaTag(@NotNull String attrib, @NotNull String attribValue, @NotNull String content) {
    this.attribs = ImmutableMap.of(attrib, attribValue);
    this.content = Optional.of(content);
  }

  public MetaTag(@NotNull Map<String, String> attribs, @NotNull String content) {
    this.attribs = ImmutableMap.copyOf(attribs);
    this.content = Optional.of(content);
  }

  public MetaTag(@NotNull ECharset charset) {
    this.attribs = ImmutableMap.of(ECharset.ATTRIB_NAME, charset.getIdentifier());
    this.content = Optional.absent();
  }

  public MetaTag(@NotNull EHttpEquiv httpEquiv, @NotNull String content) {
    this(EHttpEquiv.ATTRIB_NAME, httpEquiv.getIdentifier(), content);
  }

  public MetaTag(@NotNull ENameStandard name, String content) {
    this(ImmutableMap.of(ENameStandard.ATTRIB_NAME, name.getIdentifier(),
        ENameStandard.ATTRIB_NAME_ALT, name.getIdentifier()), content);
  }

  public MetaTag(@NotNull ENameNonStandard name, String content) {
    this(ImmutableMap.of(ENameNonStandard.ATTRIB_NAME, name.getIdentifier(),
        ENameNonStandard.ATTRIB_NAME_ALT, name.getIdentifier()), content);
  }

  public MetaTag(@NotNull EReferrer referrer) {
    this(ENameStandard.REFERRER, referrer.getIdentifier());
  }

  public MetaTag(@NotNull ERobot robot) {
    this(robot, ENameNonStandard.ROBOTS);
  }

  public MetaTag(@NotNull ERobot robot, @NotNull ENameNonStandard robotname) {
    this(robotname, robot.getIdentifier());
  }

  public MetaTag(@NotNull EViewport viewport) {
    this(ENameNonStandard.VIEWPORT, viewport.getIdentifier());
  }

  public MetaTag(@NotNull EOpenGraph openGraph, @NotNull String content) {
    this(EOpenGraph.ATTRIB_NAME, openGraph.getIdentifier(), content);
  }

  public MetaTag(@NotNull ETwitter twitter, @NotNull String content) {
    this(ETwitter.ATTRIB_NAME, twitter.getIdentifier(), content);
  }

  public MetaTag(@NotNull ETwitterCardType twitterCardType) {
    this(ETwitter.TWITTER_CARD, twitterCardType.getIdentifier());
  }

  public String display() {
    StringBuilder sb = new StringBuilder();
    sb.append("<meta ");
    for (String attrib : attribs.keySet()) {
      sb.append(attrib).append("=\"").append(attribs.get(attrib)).append("\" ");
    }
    if (content.isPresent()) {
      sb.append("content=\"").append(content.get()).append("\" ");
    }
    sb.append("/>");
    return sb.toString();
  }
}
