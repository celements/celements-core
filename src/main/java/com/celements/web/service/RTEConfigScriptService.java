package com.celements.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.rteConfig.RTEConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService{

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      RTEConfigScriptService.class);
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public String getRTEConfigField(String name) {
    try {
      return new RTEConfig().getRTEConfigField(name);
    } catch (XWikiException exp) {
      LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
    }
    return "";
  }
}
