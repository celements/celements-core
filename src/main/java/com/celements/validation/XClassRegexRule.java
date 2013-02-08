package com.celements.validation;

import org.xwiki.component.annotation.Component;

@Component("XClassRegexValidation")
public class XClassRegexRule implements IValidationRuleRole {

  public String validateField(String className, String fieldName, String value) {
    // TODO get Regex Validation code from DocFormCommand.class
    return null;
  }

}
