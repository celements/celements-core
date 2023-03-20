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
package com.celements.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;

import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestParam;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

import one.util.streamex.EntryStream;

@Component("XClassRegexValidation")
public class XClassRegexRule implements IRequestValidationRule, IFieldValidationRuleRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(XClassRegexRule.class);

  private static final String PROPERTY_FIELD_VAL_REGEX = "validationRegExp";
  private static final String PROPERTY_FIELD_VAL_MSG = "validationMessage";

  @Requirement
  ConfigurationSource configSrc;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public List<ValidationResult> validate(List<DocFormRequestParam> params) {
    List<ValidationResult> ret = new ArrayList<>();
    for (DocFormRequestParam param : params) {
      if (param.getValues().isEmpty()) {
        validateField(param.getKey(), "").forEach(ret::add);
      } else {
        for (String value : param.getValues()) {
          validateField(param.getKey(), value).forEach(ret::add);
        }
      }
    }
    LOGGER.trace("Returning validation map: {}", ret);
    return ret;
  }

  public Stream<ValidationResult> validateField(DocFormRequestKey key, String value) {
    Map<ValidationType, Set<String>> result = validateField(key.getClassRef().serialize(),
        key.getFieldName(), value);
    return EntryStream.of(result != null ? result : ImmutableMap.of())
        .flatMapValues(Collection::stream)
        .mapKeyValue((type, msg) -> new ValidationResult(type, key.getKeyString(), msg));
  }

  @Override
  public Map<ValidationType, Set<String>> validateField(String className, String fieldName,
      String value) {
    Map<ValidationType, Set<String>> ret = null;
    PropertyClass propertyClass = getBaseClassProperty(className, fieldName);
    if (propertyClass != null) {
      ret = matchPropertyValidationRegex(propertyClass, value);
    } else {
      if (!ignoreInvalidKey()) {
        ret = getErrorStringInMap("cel_validation_xclassregex_invalidkey");
      }
      LOGGER.warn("invalid class/field key '{}'", className + "." + fieldName);
    }
    LOGGER.trace("Returning validation map for field '{}' and value '{}': {}", className + "."
        + fieldName, value, ret);
    return ret;
  }

  private PropertyClass getBaseClassProperty(String className, String fieldName) {
    PropertyClass propertyClass = null;
    try {
      BaseClass bclass = getContext().getWiki().getDocument(webUtils.resolveDocumentReference(
          className), getContext()).getXClass();
      if (bclass != null) {
        propertyClass = (PropertyClass) bclass.getField(fieldName);
      }
    } catch (XWikiException exc) {
      LOGGER.error("Cannot get document '{}'", className, exc);
    }
    return propertyClass;
  }

  private Map<ValidationType, Set<String>> matchPropertyValidationRegex(PropertyClass propertyClass,
      String value) {
    Map<ValidationType, Set<String>> ret = null;
    String regex = getFieldFromProperty(propertyClass, PROPERTY_FIELD_VAL_REGEX);
    String validationMsg = getFieldFromProperty(propertyClass, PROPERTY_FIELD_VAL_MSG);
    if (!regex.isEmpty() && !matchesRegex(regex, value)) {
      ret = getErrorStringInMap(validationMsg);
    }
    return ret;
  }

  String getFieldFromProperty(PropertyClass propertyClass, String fieldName) {
    String field = "";
    BaseProperty property = (BaseProperty) propertyClass.getField(fieldName);
    if ((property != null) && (property.getValue() != null)) {
      field = property.getValue().toString();
    }
    return field;
  }

  private boolean matchesRegex(String regex, String str) {
    if ((regex != null) && !regex.trim().equals("")) {
      try {
        return getContext().getUtil().match(regex, str);
      } catch (MalformedPerl5PatternException exc) {
        LOGGER.error("Failed to execute validation regex for string '{}' and regex '{}'", str,
            regex, exc);
      }
    }
    return false;
  }

  private Map<ValidationType, Set<String>> getErrorStringInMap(String str) {
    Map<ValidationType, Set<String>> map = new EnumMap<>(ValidationType.class);
    Set<String> validationSet = new HashSet<>();
    validationSet.add(str);
    map.put(ValidationType.ERROR, validationSet);
    return map;
  }

  private boolean ignoreInvalidKey() {
    return configSrc.getProperty("celements.validation.xClassRegex.ignoreInvalidKey", true);
  }

}
