package com.celements.nextfreedoc;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
import com.google.common.base.Strings;

@Component("nextfreedoc")
public class NextFreeDocScriptService implements ScriptService {

  @Requirement
  private INextFreeDocRole nextFreeDocService;

  @Requirement
  private ModelContext context;

  public DocumentReference getNextTitledPageDocRef(SpaceReference spaceRef, String title) {
    if ((spaceRef != null) && !Strings.isNullOrEmpty(title)) {
      return nextFreeDocService.getNextTitledPageDocRef(spaceRef, title);
    }
    return null;
  }

  @Deprecated
  public DocumentReference getNextTitledPageDocRef(String space, String title) {
    if ((space != null) && (title != null) && !"".equals(space) && !"".equals(title)) {
      return new NextFreeDocNameCommand().getNextTitledPageDocRef(space, title,
          context.getXWikiContext());
    }
    return null;
  }

  public DocumentReference getNextUntitledPageDocRef(SpaceReference spaceRef) {
    if (spaceRef != null) {
      return nextFreeDocService.getNextUntitledPageDocRef(spaceRef);
    }
    return null;
  }

  @Deprecated
  public String getNextUntitledPageFullName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageFullName(space,
          context.getXWikiContext());
    }
    return "";
  }

  @Deprecated
  public String getNextUntitledPageName(String space) {
    if ((space != null) && !"".equals(space)) {
      return new NextFreeDocNameCommand().getNextUntitledPageName(space, context.getXWikiContext());
    }
    return "";
  }

}
