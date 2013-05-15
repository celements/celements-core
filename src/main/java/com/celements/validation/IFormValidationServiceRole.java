package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFormValidationServiceRole {

  /**
   * validates any form fields in the given http request for all validationRule
   * implementations
   * 
   * @return map [KEY = request field-name / VALUE = map [KEY = validation type / VALUE =
   *         set of validation messages (dictionary keys possible)]]
   */
  public Map<String, Map<ValidationType, Set<String>>> validateRequest();

  /**
   * validates any form fields in the given Map for all validationRule implementations
   * 
   * @return map [KEY = request field-name / VALUE = map [KEY = validation type / VALUE =
   *         set of validation messages (dictionary keys possible)]]
   */
  public Map<String, Map<ValidationType, Set<String>>> validateMap(
      Map<String, String[]> requestMap);

  /**
   * validateField validates the given class name, field name and value for all
   * validationRule implementations
   * 
   * @param className
   * @param fieldName
   * @param value
   * @return map [KEY = validation type / VALUE = set of validation messages (dictionary
   *         keys possible)]]
   */
  public Map<ValidationType, Set<String>> validateField(String className,
      String fieldName, String value);

}
