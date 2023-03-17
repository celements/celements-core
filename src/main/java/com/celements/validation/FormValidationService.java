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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.docform.DocFormRequestKeyParser;
import com.celements.docform.DocFormRequestParam;
import com.celements.model.context.ModelContext;
import com.celements.web.service.IWebUtilsService;

import one.util.streamex.StreamEx;

@Component
public class FormValidationService implements IFormValidationServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormValidationService.class);

  private static final MapHandler<String, ValidationType, String> MAPHANDLER = new MapHandler<>();

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Map<String, IRequestValidationRule> requestValidationRules;

  @Requirement
  private Map<String, IRequestValidationRuleRole> legacyRequestValidationRules;

  @Requirement
  private Map<String, IFieldValidationRuleRole> fieldValidationRules;

  @Requirement
  private ModelContext context;

  void injectValidationRules(
      Map<String, IRequestValidationRule> requestValidationRules,
      Map<String, IRequestValidationRuleRole> legacyRequestValidationRules,
      Map<String, IFieldValidationRuleRole> fieldValidationRules) {
    this.requestValidationRules = requestValidationRules;
    this.legacyRequestValidationRules = legacyRequestValidationRules;
    this.fieldValidationRules = fieldValidationRules;
  }

  @Override
  public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
    return validateMap(webUtils.getRequestParameterMap());
  }

  @Override
  public Map<String, Map<ValidationType, Set<String>>> validateMap(
      Map<String, String[]> requestMap) {
    Map<String, Map<ValidationType, Set<String>>> ret = new HashMap<>();
    DocFormRequestKeyParser parser = new DocFormRequestKeyParser(context.getDocRef()
        .orElseThrow(IllegalStateException::new));
    for (ValidationResult v : validate(parser.parseParameterMap(requestMap))) {
      ret.computeIfAbsent(v.getName(), k -> new EnumMap<>(ValidationType.class))
          .computeIfAbsent(v.getType(), k -> new HashSet<>())
          .add(v.getMessage());
    }
    return ret;
  }

  @Override
  public List<ValidationResult> validate(List<DocFormRequestParam> params) {
    List<ValidationResult> ret = validateLegacy(params);
    for (IRequestValidationRule validationRule : requestValidationRules.values()) {
      LOGGER.trace("validateRequest - for rule: {}", validationRule);
      ret.addAll(validationRule.validate(params));
    }
    LOGGER.trace("validateRequest - params [{}], result [{}]", params, ret);
    return ret;
  }

  private List<ValidationResult> validateLegacy(List<DocFormRequestParam> params) {
    Map<RequestParameter, String[]> paramMap = StreamEx.of(params).mapToEntry(
        p -> RequestParameter.create(p.getKey().getKeyString()),
        p -> p.getValues().toArray(new String[0]))
        .filterKeys(Objects::nonNull)
        .toMap();
    List<ValidationResult> ret = new ArrayList<>();
    for (IRequestValidationRuleRole validationRule : legacyRequestValidationRules.values()) {
      LOGGER.error("validateRequest - for rule: {}", validationRule);
      validationRule.validateRequest(paramMap).forEach((name, x) -> x.forEach((type, msgs) -> msgs
          .forEach(msg -> ret.add(new ValidationResult(type, name, msg)))));
    }
    return ret;
  }

  @Override
  public Map<ValidationType, Set<String>> validateField(String className, String fieldName,
      String value) {
    Map<ValidationType, Set<String>> ret = new EnumMap<>(ValidationType.class);
    for (IFieldValidationRuleRole validationRule : fieldValidationRules.values()) {
      LOGGER.trace("validateField - for rule: {}", validationRule);
      MAPHANDLER.mergeMaps(validationRule.validateField(className, fieldName, value), ret);
    }
    return ret;
  }

}
