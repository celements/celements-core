package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IRequestValidationRuleRole {

  /**
   * validates any form fields in the given map
   * 
   * @param requestMap
   * @return empty map if all values pass validation, else map [KEY = request field-name /
   *         VALUE = map [KEY = validation type / VALUE = set of validation messages
   *         (dictionary keys possible)]]
   */
  public Map<String, Map<ValidationType, Set<String>>> validateRequest(
      Map<RequestParameter, String[]> requestMap);

}
