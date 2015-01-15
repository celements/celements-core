package com.celements.webform;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IWebFormServiceRole {
    
  public boolean isFormFilled(Map<String, String[]> parameterMap, 
      Set<String> additionalFields);
}
