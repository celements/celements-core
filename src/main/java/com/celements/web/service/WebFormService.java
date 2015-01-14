package com.celements.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebFormService implements IWebFormServiceRole {
  
  final String PARAM_XPAGE = "xpage";
  final String PARAM_CONF = "conf";
  final String PARAM_AJAX_MODE = "ajax_mode";
  final String PARAM_SKIN = "skin";
  final String PARAM_LANGUAGE = "language";
  final String PARAM_XREDIRECT = "xredirect";

  @Override
  public boolean isFormFilled(Map<String, String[]> parameterMap,
      Set<String> additionalFields) {
    boolean isFilled = false;
    if(parameterMap.size() > getIsFilledModifier(parameterMap, additionalFields)) {
      isFilled = true;
    }
    return isFilled;
  }
  
  private short getIsFilledModifier(Map<String, String[]> parameterMap, 
      Set<String> additionalFields) {
    List<String> standardParams = new ArrayList<String>();
    standardParams.add(PARAM_XPAGE);
    standardParams.add(PARAM_CONF);
    standardParams.add(PARAM_AJAX_MODE);
    standardParams.add(PARAM_SKIN);
    standardParams.add(PARAM_LANGUAGE);
    standardParams.add(PARAM_XREDIRECT);
    short modifier = 0;
    if(parameterMap.containsKey(PARAM_XPAGE) && parameterMap.containsKey(PARAM_CONF) && 
        arrayContains(parameterMap.get(PARAM_XPAGE), "overlay")) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_XPAGE) && parameterMap.containsKey(PARAM_AJAX_MODE) && 
        arrayContains(parameterMap.get(PARAM_XPAGE), "celements_ajax")) {
      modifier += 1;
      if(parameterMap.containsKey(PARAM_SKIN)) {
        modifier += 1;
      }
    }
    if(parameterMap.containsKey(PARAM_XPAGE)) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_XREDIRECT)) {
      modifier += 1;
    }
    if(parameterMap.containsKey(PARAM_LANGUAGE)) {
      modifier += 1;
    }
    if((additionalFields != null) && additionalFields.size() > 0) {
      for (String param : additionalFields) {
        if(!standardParams.contains(param) && parameterMap.containsKey(param)) {
          modifier += 1;
        }
      }
    }
    return modifier;
  }
  
  private boolean arrayContains(String[] array, String value) {
    Arrays.sort(array);
    return (Arrays.binarySearch(array, value) >= 0);
  }
}
