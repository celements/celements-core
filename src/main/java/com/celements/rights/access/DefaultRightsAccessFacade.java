package com.celements.rights.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.menu.access.DefaultMenuAccessProvider;
import com.celements.rights.access.internal.IEntityReferenceRandomCompleterRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class DefaultRightsAccessFacade implements IRightsAccessFacadeRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultMenuAccessProvider.class);

  @Requirement
  IEntityReferenceRandomCompleterRole randomCompleter;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public XWikiRightService getRightsService() {
    return getContext().getWiki().getRightService();
  }

  @Override
  public boolean hasAccessLevel(String right, XWikiUser user, EntityReference entityRef) {
    try {
      entityRef = randomCompleter.randomCompleteSpaceRef(entityRef);
      String entityString;
      if (entityRef.getType() == EntityType.DOCUMENT) {
        entityString = webUtilsService.getRefLocalSerializer().serialize(entityRef);
      } else {
        throw new IllegalArgumentException("unsupported entity type ["
              + entityRef.getType() + "] for [" + entityRef + "].");
      }
      return getRightsService().hasAccessLevel(right, user.getUser(), entityString,
          getContext());
    } catch (XWikiException xwe) {
      //hasAccessLevel does not throw XWikiException in current implementation.
      LOGGER.error("hasAccessLevel on rightsService has thrown an unexpected"
          + " XWikiException.", xwe);
    }
    return false;
  }

}
