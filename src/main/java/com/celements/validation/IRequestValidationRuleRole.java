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
   * @return empty map if all values pass validation, else set of validation messages
   *         (dictionary key possible) for the invalid keys
   */
  public Map<String, Set<String>> validateRequest(
      Map<RequestParameter, String[]> requestMap);

}
