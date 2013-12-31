package com.celements.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.sajson.Builder;
import com.xpn.xwiki.XWikiContext;

@Component("mobileLogging")
public class MobileLoggingScriptService implements ScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MobileLoggingScriptService.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String dimensionAndAgentLog() {
    LOGGER.info("dimensionAndAgentLog: mobileDim ["
        + getContext().getRequest().getParameter("mobileDim") + "], userAgent ["
        + getContext().getRequest().getParameter("userAgent")
        + "], isOrientationLandscape [" + getContext().getRequest().getParameter(
            "isOrientationLandscape") + "]");
    Builder jsonBuilder = new Builder();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("message", "OK");
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }

}
