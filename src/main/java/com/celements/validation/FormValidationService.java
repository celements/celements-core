package com.celements.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

  public Map<String, String> validateRequest() {
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

  public Map<String, String> validateMap(Map<String, String[]> requestMap) {
    Map<String, String> resultMap = new HashMap<String, String>();
    for (String key : requestMap.keySet()) {
      for (String value : requestMap.get(key)) {
        String validation = validateField(key, value);
        if(validation != null) {
          resultMap.put(key, validation);
          break; //XXX ok??
        }
      }
    }
    return resultMap;
  }

  private String validateField(String key, String value) {
    if (isValidRequestKey(key)) {
      int pos = includesDocName(key) ? 1 : 0;
      String[] paramSplit = key.split("_");
      String className = paramSplit[pos];
      String fieldName = paramSplit[pos+2];
      for (int i = pos+3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return validateField(className, fieldName, value);
    }
    LOGGER.debug("requestMap key is not valid '" + key + "'");
    return null;
  }

  private boolean isValidRequestKey(String key) {
    return key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}-?(\\d)*_(.*)");
  }

  private boolean includesDocName(String key) {
    return key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}-?(\\d)*_(.*)");
  }

  public String validateField(String className, String fieldName, String value) {
    for (IValidationRuleRole validationRule : validationRules.values()) {
      String validation = validationRule.validateField(className, fieldName, value);
      if(validation != null) {
        return validation; //XXX ok??
      }
    }
    return null;
  }

}
