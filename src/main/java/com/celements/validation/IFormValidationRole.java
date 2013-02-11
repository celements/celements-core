package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFormValidationRole {

  /**
   * validateRequest validates any form fields in the given http request
   * 
   * @return error map (key = request field-name ; value = set of validation messages
   *         (dictionary keys possible))
   */
  public Map<String, Set<String>> validateRequest();

  /**
   * validateRequest validates any form fields in the given Map
   * 
   * @return error map (key = request field-name ; value = set of validation messages
   *         (dictionary keys possible))
   */
  public Map<String, Set<String>> validateMap(Map<String, String[]> requestMap);

  /**
   * @param className
   * @param fieldName
   * @param value
   * @return empty set if value passes validation, else set of validation messages
   *         (dictionary key possible)
   */
  public Set<String> validateField(String className, String fieldName, String value);

}
