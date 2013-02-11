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
  Map<String, IValidationRuleRole> validationRules;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public Map<String, Set<String>> validateRequest() {
    LOGGER.trace("validateRequest() called");
    XWikiRequest request = getContext().getRequest();
    Map<String, String[]> requestMap = convertRequestMap(request.getParameterMap());
    return validateMap(requestMap);
  }

  private Map<String, String[]> convertRequestMap(Map<?, ?> requestMap) {
    Map<String, String[]> convertedMap = new HashMap<String, String[]>();
    for (Object keyObj : requestMap.keySet()) {
      String key = keyObj.toString();
      String[] value = getValueAsStringArray(requestMap.get(keyObj));
      convertedMap.put(key, value);
    }
    return convertedMap;
  }

  private String[] getValueAsStringArray(Object value) {
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

  //  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap) {
  //    Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
  //    for (String key : requestMap.keySet()) {
  //      Set<String> validationSet = new HashSet<String>();
  //      for (String value : requestMap.get(key)) {
  //        Field field = resolveFieldNameFromParam(key);
  //        if (field != null) {
  //          validationSet.addAll(validateField(field.getClassName(), field.getFieldName(),
  //              value));
  //        }
  //      }
  //      if (!validationSet.isEmpty()) {
  //        resultMap.put(key, validationSet);
  //      }
  //    }
  //    return resultMap;
  //  }

  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap) {
    Map<String, Set<String>> validationMap = new HashMap<String, Set<String>>();
    Map<FieldName, String[]> convertedMap = convertMapKeyToField(requestMap);
    for (IValidationRuleRole validationRule : validationRules.values()) {
      mergeMaps(validationRule.validate(convertedMap), validationMap);
    }
    return validationMap;
  }

  private void mergeMaps(Map<String, Set<String>> mergeMap,
      Map<String, Set<String>> toMap) {
    for (String key : mergeMap.keySet()) {
      Set<String> set = mergeMap.get(key);
      Set<String> toSet = toMap.get(key);
      if (toSet == null) {
        toMap.put(key, set);
      } else {
        toSet.addAll(set);
      }
    }
  }

  private Map<FieldName, String[]> convertMapKeyToField(Map<String, String[]> requestMap) {
    Map<FieldName, String[]> retMap = new HashMap<FieldName, String[]>();
    for (String key : requestMap.keySet()) {
      FieldName field = resolveFieldNameFromParam(key);
      if (field != null) {
        retMap.put(field, requestMap.get(key));
      }
    }
    return retMap;
  }

  FieldName resolveFieldNameFromParam(String paramName) {
    if (isValidRequestParam(paramName)) {
      int pos = includesDocName(paramName) ? 1 : 0;
      String[] paramSplit = paramName.split("_");
      String className = paramSplit[pos];
      String fieldName = paramSplit[pos + 2];
      for (int i = pos + 3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return new FieldName(className, fieldName);
    }
    LOGGER.debug("request parameter is not valid '" + paramName + "'");
    return null;
  }

  boolean isValidRequestParam(String key) {
    return key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}-?(\\d)*_(.*)");
  }

  boolean includesDocName(String key) {
    return key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}-?(\\d)*_(.*)");
  }

  public Set<String> validateField(String className, String fieldName, String value) {
    Set<String> validationSet = new HashSet<String>();
    for (IValidationRuleRole validationRule : validationRules.values()) {
      validationSet.addAll(validationRule.validateField(className, fieldName, value));
    }
    return validationSet;
  }

}