package com.celements.validation;

import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

@Component
public class FormValidationService implements IFormValidationRole {

  @Requirement
  Map<String, IValidationRuleRole> validationRules;

  public String validateField(String className, String fieldName, String value) {
    // TODO call validateField on all validationRules
    return null;
  }

  public Map<String, String> validateMap(Map<String, String[]> requestMap) {
    // TODO call validateField for all parameter in request parameter map (example code in DocFormCommand)
    return null;
  }

  public Map<String, String> validateRequest() {
    // TODO extract request parameter Map from context.getRequest and call validate Map (example code in DocFormCommand)
    return null;
  }

}
