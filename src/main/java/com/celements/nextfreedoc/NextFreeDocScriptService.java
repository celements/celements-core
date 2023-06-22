package com.celements.nextfreedoc;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
import com.google.common.base.Strings;

@Service
public class NextFreeDocScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NextFreeDocScriptService.class);

  private final INextFreeDocRole nextFreeDocService;

  private final ModelContext context;

  @Inject
  public NextFreeDocScriptService(INextFreeDocRole nextFreeDocService, ModelContext context) {
    super();
    this.nextFreeDocService = nextFreeDocService;
    this.context = context;
  }

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

  // TODO switch to Integer and check for null and add default=12
  public DocumentReference getNextRandomPageDocRef(SpaceReference spaceRef,
      int lengthOfRandomAlphanumeric, String prefix) {
    if (spaceRef != null) {
      try {
        return nextFreeDocService.getNextRandomPageDocRef(spaceRef, lengthOfRandomAlphanumeric,
            prefix);
      } catch (Exception e) {
        LOGGER.error("getNextRandomPageDocRef failed for {}", e);
      }
    }
    return null;
  }

}
