package com.celements.webform;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;

@Component("webform")
public class WebFormScriptService implements ScriptService {
  
  @Requirement
  private IWebFormServiceRole webFormService;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  @SuppressWarnings("unchecked")
  public boolean isFormFilled() {
    return webFormService.isFormFilled(getContext().getRequest().getParameterMap(),
        Collections.<String>emptySet());
  }

  @SuppressWarnings("unchecked")
  public boolean isFormFilled(String excludeFields) {
    Set<String> excludeSet = new HashSet<String>();
    for (String field : excludeFields.split(",")) {
      if(!"".equals(field.trim()) && (field.trim().length() > 0)) {
        excludeSet.add(field);
      }
    }
    return webFormService.isFormFilled(getContext().getRequest().getParameterMap(), excludeSet);
  }

}
