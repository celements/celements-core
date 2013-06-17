package com.celements.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.web.service.IWebUtilsService;

@Component
public class FormValidationService implements IFormValidationServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      FormValidationService.class);

  private static final MapHandler<String, ValidationType, String> MAPHANDLER =
      new MapHandler<String, ValidationType, String>();

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Map<String, IRequestValidationRuleRole> requestValidationRules;

  @Requirement
  private Map<String, IFieldValidationRuleRole> fieldValidationRules;

  void injectValidationRules(
      Map<String, IRequestValidationRuleRole> requestValidationRules,
      Map<String, IFieldValidationRuleRole> fieldValidationRules) {
    this.requestValidationRules = requestValidationRules;
    this.fieldValidationRules = fieldValidationRules;
  }

  public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
    LOGGER.trace("validateRequest() called");
    Map<String, String[]> requestMap = webUtils.getRequestParameterMap();
    return validateMap(requestMap);
  }

  public Map<String, Map<ValidationType, Set<String>>> validateMap(
      Map<String, String[]> requestMap) {
    Map<String, Map<ValidationType, Set<String>>> ret =
        new HashMap<String, Map<ValidationType, Set<String>>>();
    Map<RequestParameter, String[]> convertedRequestMap = convertMapKeys(requestMap);
    for (IRequestValidationRuleRole validationRule : requestValidationRules.values()) {
      LOGGER.trace("Calling validateRequest() for rule '" + validationRule + "'");
      MAPHANDLER.mergeMultiMaps(validationRule.validateRequest(convertedRequestMap), ret);
    }
    return ret;
  }

  Map<RequestParameter, String[]> convertMapKeys(Map<String, String[]> requestMap) {
    Map<RequestParameter, String[]> convMap = new HashMap<RequestParameter, String[]>();
    for (String key : requestMap.keySet()) {
      RequestParameter requestparam = RequestParameter.create(key);
      if (requestparam != null) {
        convMap.put(requestparam, requestMap.get(key));
      }
    }
    return convMap;
  }

  public Map<ValidationType, Set<String>> validateField(String className,
      String fieldName, String value) {
    Map<ValidationType, Set<String>> ret = new HashMap<ValidationType, Set<String>>();
    for (IFieldValidationRuleRole validationRule : fieldValidationRules.values()) {
      LOGGER.trace("Calling validateField() for rule '" + validationRule + "'");
      MAPHANDLER.mergeMaps(validationRule.validateField(className, fieldName, value), ret);
    }
    return ret;
  }

}
