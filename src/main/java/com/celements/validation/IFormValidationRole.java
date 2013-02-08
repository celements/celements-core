package com.celements.validation;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFormValidationRole {

  /**
   * validateRequest validates any form fields in the given http request
   * 
   * @return error map (key = request field-name ; value = validation message
   *          (dictionary key possible))
   */
  public Map<String, String> validateRequest();

  /**
   * validateRequest validates any form fields in the given Map
   * 
   * @return error map (key = request field-name ; value = validation message
   *          (dictionary key possible))
   */
  public Map<String, String> validateMap(Map<String, String[]> requestMap);

  /**
   * 
   * @param className
   * @param fieldName
   * @param value
   * @return null if value passes validation else validation message
   *          (dictionary key possible)
   */
  public String validateField(String className, String fieldName, String value);

}
