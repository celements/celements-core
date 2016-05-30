package com.celements.rights.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.rights.access.internal.IEntityReferenceRandomCompleterRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class DefaultRightsAccessFacade implements IRightsAccessFacadeRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultRightsAccessFacade.class);

  @Requirement
  IEntityReferenceRandomCompleterRole randomCompleter;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public XWikiRightService getRightsService() {
    return getContext().getWiki().getRightService();
  }

  @Override
  @Deprecated
  public boolean hasAccessLevel(String right, XWikiUser user, EntityReference entityRef) {
    return hasAccessLevel(entityRef, EAccessLevel.getAccessLevel(right), user);
  }

  @Override
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level) {
    return hasAccessLevel(ref, level, getContext().getXWikiUser());
  }

  @Override
  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user) {
    boolean ret = false;
    DocumentReference docRef = null;
    EntityReference entityRef = randomCompleter.randomCompleteSpaceRef(ref);
    if (entityRef instanceof DocumentReference) {
      docRef = (DocumentReference) entityRef;
    } else if (entityRef instanceof AttachmentReference) {
      docRef = ((AttachmentReference) entityRef).getDocumentReference();
    }
    if (docRef != null) {
      try {
        ret = getRightsService().hasAccessLevel(level.getIdentifier(), (user != null
            ? user.getUser() : XWikiRightService.GUEST_USER_FULLNAME), webUtilsService.serializeRef(
                docRef), getContext());
      } catch (XWikiException xwe) {
        // already being catched in XWikiRightServiceImpl.hasAccessLevel()
        LOGGER.error("should not happen", xwe);
      }
    }
    LOGGER.debug("hasAccessLevel: for ref '{}', level '{}' and user '{}' returned '{}'", ref, level,
        user, ret);
    return ret;
  }
}
