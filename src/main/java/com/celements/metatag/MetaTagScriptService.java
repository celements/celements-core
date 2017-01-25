package com.celements.metatag;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.metatag.enums.ECharset;

@Component(MetaTagScriptService.COMPONENT_HINT)
public class MetaTagScriptService {

  public static final String COMPONENT_HINT = "metatag";

  @Requirement
  MetaTagServiceRole metaTag;

  public @NotNull MetaTag getCharsetMetaTag(@NotNull String charsetStr) {
    ECharset charset = ECharset.getCharset(charsetStr);
    if (charset != null) {
      return new MetaTag(charset);
    }
    return new MetaTag(ECharset.ATTRIB_NAME, charsetStr, null);
  }

  public void addMetaTagToCollector(@NotNull String attributeName, @NotNull String attributeValue,
      @NotNull String content) {
    metaTag.addMetaTagToCollector(new MetaTag(attributeName, attributeValue, content));
  }

  public void addMetaTagToCollector(@NotNull Map<String, String> attributes,
      @NotNull String content) {
    metaTag.addMetaTagToCollector(new MetaTag(attributes, content));
  }

  public @NotNull String displayCollectedMetaTags() {
    return metaTag.displayCollectedMetaTags();
  }
}
