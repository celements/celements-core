package com.celements.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

@Component
public class FormValidationService implements IFormValidationRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      FormValidationService.class);

  @Requirement
  private Map<String, IValidationRuleRole> validationRules;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  void injectValidationRules(Map<String, IValidationRuleRole> validationRules) {
    this.validationRules = validationRules;
  }

  public Map<String, Set<String>> validateRequest() {
    LOGGER.trace("validateRequest() called");
    XWikiRequest request = getContext().getRequest();
    Map<String, String[]> requestMap = convertRequestMap(request.getParameterMap());
    return validateMap(requestMap);
  }

  Map<String, String[]> convertRequestMap(Map<?, ?> requestMap) {
    Map<String, String[]> convertedMap = new HashMap<String, String[]>();
    for (Object keyObj : requestMap.keySet()) {
      String key = keyObj.toString();
      String[] value = getValueAsStringArray(requestMap.get(keyObj));
      convertedMap.put(key, value);
    }
    return convertedMap;
  }

  String[] getValueAsStringArray(Object value) {
    if (value instanceof String) {
      LOGGER.trace("requestMap value '" + value + "'");
      return new String[] { value.toString() };
    } else if (value instanceof String[]) {
      LOGGER.trace("requestMap value '" + Arrays.toString((String[]) value) + "'");
      return (String[]) value;
    } else {
      throw new IllegalArgumentException("Invalid requestMap value type");
    }
  }

  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap) {
    Map<String, Set<String>> validationMap = new HashMap<String, Set<String>>();
    Map<RequestParameter, String[]> convertedMap = convertMapKeys(requestMap);
    for (IValidationRuleRole validationRule : validationRules.values()) {
      mergeMaps(validationRule.validate(convertedMap), validationMap);
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
    for (IValidationRuleRole validationRule : validationRules.values()) {
      validationSet.addAll(validationRule.validateField(className, fieldName, value));
    }
    return validationSet;
  }

}