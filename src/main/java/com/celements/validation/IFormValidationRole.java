package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFormValidationRole {

  /**
   * validates any form fields in the given http request for all validationRule
   * implementations
   * 
   * @return error map (key = request field-name ; value = set of validation messages
   *         (dictionary keys possible))
   */
  public Map<String, Set<String>> validateRequest();

  /**
   * validates any form fields in the given Map for all validationRule implementations
   * 
   * @return error map (key = request field-name ; value = set of validation messages
   *         (dictionary keys possible))
   */
  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap);

  /**
   * validateField validates the given class name, field name and value for all
   * validationRule implementations
   * 
   * @param className
   * @param fieldName
   * @param value
   * @return empty set if value passes validation, else set of validation messages
   *         (dictionary key possible)
   */
  public Set<String> validateField(String className, String fieldName, String value);

}
