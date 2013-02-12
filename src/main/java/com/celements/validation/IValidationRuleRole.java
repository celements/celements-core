package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IValidationRuleRole {

  /**
   * validates any form fields in the given map
   * 
   * @param requestMap
   * @return empty map if all values pass validation, else set of validation messages
   *         (dictionary key possible) for the invalid keys
   */
  public Map<String, Set<String>> validate(Map<RequestParameter, String[]> requestMap);

  /**
   * validates the given class name, field name and value
   * 
   * @param className
   * @param fieldName
   * @param value
   * @return empty set if value passes validation, else set of validation messages
   *         (dictionary key possible)
   */
  public Set<String> validateField(String className, String fieldName, String value);

}
