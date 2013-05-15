package com.celements.validation;

import java.util.Map;
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
   * @return empty map if value passes validation, else map [KEY = validation type / VALUE
   *         = set of validation messages (dictionary keys possible)]
   */
  public Map<ValidationType, Set<String>> validateField(String className,
      String fieldName, String value);

}
