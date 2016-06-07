package com.celements.nextfreedoc;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
import com.xpn.xwiki.XWikiContext;

@Component("nextfreedoc")
public class NextFreeDocScriptService implements ScriptService {

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public DocumentReference getNextTitledPageDocRef(String space, String title) {
    if ((space != null) && (title != null) && !"".equals(space) && !"".equals(title)) {
      return new NextFreeDocNameCommand().getNextTitledPageDocRef(space, title, getContext());
    }
    return null;
  }

  public String getNextUntitledPageFullName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageFullName(space, getContext());
    }
    return "";
  }

  public String getNextUntitledPageName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageName(space, getContext());
    }
    return "";
  }
}
