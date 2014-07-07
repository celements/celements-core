package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.SkinConfigObjCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Component("legacyskin")
public class LegacySkinScriptService implements ScriptService{

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public com.xpn.xwiki.api.Object getSkinConfigObj() {
    BaseObject skinConfigObj = new SkinConfigObjCommand().getSkinConfigObj();
    if (skinConfigObj != null) {
      return skinConfigObj.newObjectApi(skinConfigObj, getContext());
    } else {
      return null;
    }
  }
  
  public int showRightPanels() {
    return getPrepareVelocityContextService().showRightPanels();
  }
  
  public int showLeftPanels() {
    return getPrepareVelocityContextService().showLeftPanels();
  }

  public List<String> getRightPanels() {
    return getPrepareVelocityContextService().getRightPanels();
  }

  public List<String> getLeftPanels() {
    return getPrepareVelocityContextService().getLeftPanels();
  }
  
  private IPrepareVelocityContext getPrepareVelocityContextService() {
    return Utils.getComponent(IPrepareVelocityContext.class);
  }
}
