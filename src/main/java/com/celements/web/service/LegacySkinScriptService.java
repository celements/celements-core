package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;

@Component("legacyskin")
public class LegacySkinScriptService implements ScriptService {

  @Requirement
  private Execution execution;

  @Requirement
  private IPrepareVelocityContext prepareVelocityContext;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public int showRightPanels() {
    return prepareVelocityContext.showRightPanels();
  }

  public int showLeftPanels() {
    return prepareVelocityContext.showLeftPanels();
  }

  public List<String> getRightPanels() {
    return prepareVelocityContext.getRightPanels();
  }

  public List<String> getLeftPanels() {
    return prepareVelocityContext.getLeftPanels();
  }
}
