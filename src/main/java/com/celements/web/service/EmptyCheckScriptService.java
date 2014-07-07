package com.celements.web.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Component("nextfreedoc")
public class EmptyCheckScriptService implements ScriptService{
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public boolean isEmptyRTEDocument(DocumentReference documentRef) {
    return getEmptyCheckService().isEmptyRTEDocument(documentRef);
  }
  
  private IEmptyCheckRole getEmptyCheckService() {
    return Utils.getComponent(IEmptyCheckRole.class);
  }
}
