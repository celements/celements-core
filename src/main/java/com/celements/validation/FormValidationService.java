package com.celements.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.web.service.IWebUtilsService;

@Component
public class FormValidationService implements IFormValidationRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      FormValidationService.class);

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Map<String, IRequestValidationRuleRole> requestValidationRules;

  @Requirement
  private Map<String, IFieldValidationRuleRole> fieldValidationRules;

  void injectValidationRules(Map<String, IRequestValidationRuleRole> requestValidationRules,
      Map<String, IFieldValidationRuleRole> fieldValidationRules) {
    this.requestValidationRules = requestValidationRules;
    this.fieldValidationRules = fieldValidationRules;
  }

  public Map<String, Set<String>> validateRequest() {
    LOGGER.trace("validateRequest() called");
    Map<String, String[]> requestMap = webUtils.getRequestParameterMap();
    return validateMap(requestMap);
  }

  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap) {
    Map<String, Set<String>> validationMap = new HashMap<String, Set<String>>();
    Map<RequestParameter, String[]> convertedMap = convertMapKeys(requestMap);
    for (IRequestValidationRuleRole validationRule : requestValidationRules.values()) {
      LOGGER.trace("Calling validateRequest() for rule '" + validationRule + "'");
      mergeMaps(validationRule.validateRequest(convertedMap), validationMap);
    }
    return validationMap;
  }

  Map<RequestParameter, String[]> convertMapKeys(Map<String, String[]> requestMap) {
    Map<RequestParameter, String[]> retMap = new HashMap<RequestParameter, String[]>();
    for (String key : requestMap.keySet()) {
      RequestParameter requestparam = RequestParameter.create(key);
      if (requestparam != null) {
        retMap.put(requestparam, requestMap.get(key));
      }
    }
    return retMap;
  }

  void mergeMaps(Map<String, Set<String>> merge, Map<String, Set<String>> to) {
    for (String key : merge.keySet()) {
      Set<String> set = merge.get(key);
      Set<String> toSet = to.get(key);
      if (toSet == null) {
        to.put(key, new HashSet<String>(set));
      } else {
        toSet.addAll(set);
      }
    }
  }

  public Set<String> validateField(String className, String fieldName, String value) {
    Set<String> validationSet = new HashSet<String>();
    for (IFieldValidationRuleRole validationRule : fieldValidationRules.values()) {
      validationSet.addAll(validationRule.validateField(className, fieldName, value));
    }
    return validationSet;
  }

}