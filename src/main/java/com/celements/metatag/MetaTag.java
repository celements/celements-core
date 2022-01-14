/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.metatag;

import static com.google.common.base.Strings.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

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
import com.google.common.collect.ImmutableMap;

/* ComponentInstanceSupplier in BaseObjectMetaTagProvider needs this to be a component */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MetaTag implements MetaTagRole {

  private Map<String, String> attribs;
  private String content;
  private String key;
  private String language;
  private Boolean overridable;

  public MetaTag() {
    // Bean needs default constructor
  }

  public MetaTag(@NotNull String attrib, @NotNull String attribValue, @NotNull String content) {
    this.attribs = ImmutableMap.of(attrib, attribValue);
    this.content = content;
  }

  public MetaTag(@NotNull Map<String, String> attribs, @NotNull String content) {
    this.attribs = ImmutableMap.copyOf(attribs);
    this.content = content;
  }

  public MetaTag(@NotNull ECharset charset) {
    this.attribs = ImmutableMap.of(ECharset.ATTRIB_NAME, charset.getIdentifier());
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

  @Override
  public boolean getOverridable() {
    return (overridable != null) ? overridable : false;
  }

  @Override
  public void setOverridable(Boolean overridable) {
    this.overridable = overridable;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public @NotNull Optional<String> getKeyOpt() {
    return Optional.ofNullable(getKey());
  }

  @Override
  public void setKey(String attributeKey) {
    this.key = attributeKey;
  }

  @Override
  public String getValue() {
    return content;
  }

  @Override
  public Optional<String> getValueOpt() {
    return Optional.ofNullable(getValue());
  }

  @Override
  public void setValue(String content) {
    this.content = content;
  }

  @Override
  public String getLang() {
    return language;
  }

  @Override
  public @NotNull Optional<String> getLangOpt() {
    return Optional.ofNullable(emptyToNull(getLang()));
  }

  @Override
  public void setLang(String language) {
    this.language = language;
  }

  @Override
  public @NotNull String display() {
    StringBuilder sb = new StringBuilder();
    sb.append("<meta ");
    if (attribs != null) {
      for (Entry<String, String> attribEntry : attribs.entrySet()) {
        sb.append(attribEntry.getKey()).append("=\"").append(attribEntry.getValue()).append("\" ");
      }
    }
    if (key != null) {
      sb.append(ENameStandard.ATTRIB_NAME).append("=\"").append(key).append("\" ");
    }
    if (content != null) {
      sb.append("content=\"").append(content).append("\" ");
    }
    sb.append("/>");
    return sb.toString();
  }

  @Override
  public boolean equals(Object tag) {
    if (!(tag instanceof MetaTag)) {
      return false;
    }
    final MetaTag theTag = (MetaTag) tag;
    return Objects.equals(getLangOpt(), theTag.getLangOpt())
        && Objects.equals(display(), theTag.display());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(43, 57)
        .append(getLangOpt())
        .append(display())
        .toHashCode();
  }

}
