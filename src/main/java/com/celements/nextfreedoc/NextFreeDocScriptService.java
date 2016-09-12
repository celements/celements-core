package com.celements.nextfreedoc;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;

@Component("nextfreedoc")
public class NextFreeDocScriptService implements ScriptService {

  @Requirement
  private INextFreeDocRole nextFreeDocService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Deprecated
  public DocumentReference getNextTitledPageDocRef(String space, String title) {
    if ((space != null) && (title != null) && !"".equals(space) && !"".equals(title)) {
      return new NextFreeDocNameCommand().getNextTitledPageDocRef(space, title, getContext());
    }
    return null;
  }

  @Deprecated
  public String getNextUntitledPageFullName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageFullName(space, getContext());
    }
    return "";
  }

  @Deprecated
  public String getNextUntitledPageName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageName(space, getContext());
    }
    return "";
  }

  public DocumentReference getNextTitledPageDocRef(SpaceReference spaceRef, String title) {
    if ((spaceRef != null) && !Strings.isNullOrEmpty(title)) {
      nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
    }
    return null;
  }

  public DocumentReference getNextUntitledPageDocRef(SpaceReference spaceRef) {
    if (spaceRef != null) {
      nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
    }
    return null;
  }
}
