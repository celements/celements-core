package com.celements.rights.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.internal.IEntityReferenceRandomCompleterRole;
import com.google.common.base.Optional;
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
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext modelContext;

  /**
   * @deprecated instead use {@link #modelContext}
   */
  @Deprecated
  private XWikiContext getContext() {
    return modelContext.getXWikiContext();
  }

  @Override
  public XWikiRightService getRightsService() {
    return getContext().getWiki().getRightService();
  }

  @Override
  @Deprecated
  public boolean hasAccessLevel(String right, XWikiUser user, EntityReference entityRef) {
    Optional<EAccessLevel> eAccessLevel = EAccessLevel.getAccessLevel(right);
    if (eAccessLevel.isPresent()) {
      return hasAccessLevel(entityRef, eAccessLevel.get(), user);
    }
    return false;
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
    if ((docRef != null) && (level != null)) {
      try {
        String userName = (user != null ? user.getUser() : XWikiRightService.GUEST_USER_FULLNAME);
        ret = getRightsService().hasAccessLevel(level.getIdentifier(), userName,
            modelUtils.serializeRef(docRef), getContext());
      } catch (XWikiException xwe) {
        // already being catched in XWikiRightServiceImpl.hasAccessLevel()
        LOGGER.error("should not happen", xwe);
      }
    }
    LOGGER.debug("hasAccessLevel: for ref '{}', level '{}' and user '{}' returned '{}'", ref, level,
        user, ret);
    return ret;
  }

  @Override
  public boolean isLoggedIn() {
    boolean ret = !XWikiRightService.GUEST_USER_FULLNAME.equals(getContext().getUser());
    LOGGER.info("isLoggedIn: {}", ret);
    return ret;
  }
}
