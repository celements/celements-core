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

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.metatag.enums.ECharset;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

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

  public void collectHeaderTags() {
    metaTag.collectHeaderTags();
  }

  public void collectBodyTags() {
    metaTag.collectBodyTags();
  }
}
