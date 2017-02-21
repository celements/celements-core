package com.celements.metatag;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.metatag.enums.ECharset;
import com.google.common.base.Optional;

@Component(MetaTagScriptService.COMPONENT_HINT)
public class MetaTagScriptService implements ScriptService {

  public static final String COMPONENT_HINT = "metatag";

  @Requirement
  MetaTagServiceRole metaTag;

  public @NotNull MetaTag getCharsetMetaTag(@NotNull String charsetStr) {
    Optional<ECharset> charset = ECharset.getCharset(charsetStr);
    if (charset.isPresent()) {
      return new MetaTag(charset.get());
    }
    return new MetaTag(ECharset.ATTRIB_NAME, charsetStr, null);
  }

  public void addMetaTagToCollector(@NotNull String attributeName, @NotNull String attributeValue,
      @Nullable String content) {
    metaTag.addMetaTagToCollector(new MetaTag(attributeName, attributeValue, Strings.nullToEmpty(
        content)));
  }

  public void addMetaTagToCollector(@NotNull Map<String, String> attributes,
      @Nullable String content) {
    metaTag.addMetaTagToCollector(new MetaTag(attributes, Strings.nullToEmpty(content)));
  }

  public @NotNull String displayCollectedMetaTags() {
    return metaTag.displayCollectedMetaTags();
  }
}
