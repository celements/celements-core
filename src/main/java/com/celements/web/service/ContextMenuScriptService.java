package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.contextmenu.ContextMenuBuilderApi;
import com.celements.web.plugin.cmd.ContextMenuCSSClassesCommand;
import com.xpn.xwiki.XWikiContext;

@Component("contextMenu")
public class ContextMenuScriptService implements ScriptService {

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getAllContextMenuCSSClassesAsJSON() {
    return new ContextMenuCSSClassesCommand().getAllContextMenuCSSClassesAsJSON(
        getContext());
  }

  public ContextMenuBuilderApi getContextMenuBuilder() {
    return new ContextMenuBuilderApi(getContext());
  }

  public List<String> getAllCMcssClasses() {
    return new ContextMenuCSSClassesCommand().getCM_CSSclasses(getContext());
  }

}
