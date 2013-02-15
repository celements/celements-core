package com.celements.validation;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFieldValidationRuleRole {

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
