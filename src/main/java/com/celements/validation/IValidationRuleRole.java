package com.celements.validation;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IValidationRuleRole {

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
