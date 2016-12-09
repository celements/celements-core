package com.celements.metatag;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.EHttpEquiv;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class MetaTagApi {

  private final Map<String, String> attribs;
  private final Optional<String> content;

  // private final String name; // It should not be set if one of the attributes itemprop,
  // http-equiv or charset is also set
  // private final String itemprop; // must not be set when one of the name, http-equiv or charset
  // is already used.
  // private final String property;

  public MetaTagApi(@NotNull String attrib, @NotNull String attribValue, @NotNull String content) {
    this.attribs = ImmutableMap.of(attrib, attribValue);
    this.content = Optional.of(content);
  }

  public MetaTagApi(@NotNull Map<String, String> attribs, @NotNull String content) {
    this.attribs = ImmutableMap.copyOf(attribs);
    this.content = Optional.of(content);
  }

  public MetaTagApi(@NotNull EHttpEquiv httpEquiv, @NotNull String content) {
    this.attribs = ImmutableMap.of(EHttpEquiv.ATTRIB_NAME, httpEquiv.getIdentifier());
    this.content = Optional.of(content);
  }

  public MetaTagApi(@NotNull ECharset charset) {
    this.attribs = ImmutableMap.of(ECharset.ATTRIB_NAME, charset.getIdentifier());
    this.content = Optional.absent();
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
