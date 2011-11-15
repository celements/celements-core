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
package com.celements.web.plugin.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class DocMetaTagsCmd {

  public Map<String, String> getDocMetaTags(String language, String defaultLanguage,
      XWikiContext context) {
    // get DocumentSpecific Meta-Tag Keywords
    Map<String, Map<String, String>> metaTagsLangMap =
      getObjectsMappedByLanguage("Classes.MetaTag", context);
    mergeMetaTagClassObjs(metaTagsLangMap, context);
    Map<String, String> metaTags = new HashMap<String,String>();
    if (metaTagsLangMap.get(defaultLanguage) != null) {
      // get default language meta keys
      metaTags.putAll(metaTagsLangMap.get(defaultLanguage));
      if ((defaultLanguage != null) && !defaultLanguage.equals(language)
          && (metaTagsLangMap.get(language) != null)) {
        // overwrite translated keys
        metaTags.putAll(metaTagsLangMap.get(language));
      }
    }
    return metaTags;
  }

  private void mergeMetaTagClassObjs(Map<String, Map<String, String>> metaTagsLangMap,
      XWikiContext context) {
    Map<String, Map<String, String>> metaTagsLangMap2 = getObjectsMappedByLanguage(
        "Classes.MetaTagClass", context);
    for (String lang : metaTagsLangMap2.keySet()) {
      getMetaTagsForLang(metaTagsLangMap, lang).putAll(metaTagsLangMap2.get(lang));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String,Map<String,String>> getObjectsMappedByLanguage(String className,
      XWikiContext context) {
    Map<String,Map<String,String>> metaTagsLangMap =
      new HashMap<String, Map<String, String>>();
    Vector metaTagObjects = context.getDoc().getObjects(className);
    if (metaTagObjects != null) {
      for (java.lang.Object entry : metaTagObjects) {
        if (entry instanceof BaseObject) {
          BaseObject baseObject = (BaseObject)entry;
          String lang = baseObject.getStringValue("lang");
          String key = baseObject.getStringValue("key");
          if((!"".equals(lang) && (!"".equals(key)))) {
            getMetaTagsForLang(metaTagsLangMap, lang).put(key, baseObject.getStringValue(
                "value"));
          }
        }
      }
    }
    return metaTagsLangMap;
  }

  private Map<String, String> getMetaTagsForLang(
      Map<String, Map<String, String>> metaTagsLangMap, String lang) {
    if (!metaTagsLangMap.containsKey(lang)) {
      metaTagsLangMap.put(lang, new HashMap<String, String>());
    }
    return metaTagsLangMap.get(lang);
  }
  
}
