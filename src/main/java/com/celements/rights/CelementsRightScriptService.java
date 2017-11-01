package com.celements.rights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.RightsAccessScriptService;
import com.celements.rights.publication.EPubUnpub;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * @deprecated use RightsAccessScriptService instead
 */
@Component("celementsright")
@Deprecated
public class CelementsRightScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(CelementsRightScriptService.class);

  @Requirement(RightsAccessScriptService.NAME)
  private ScriptService rightsAccess;

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

  /**
   * @deprecated use RightsAccessScriptService.isPublishActive instead
   */
  @Deprecated
  public boolean publicationActivated(DocumentReference forDoc) {
    return getRightsAccess().isPublishActive(forDoc);
  }

  /**
   * @deprecated use RightsAccessScriptService.hasAccessLevel instead
   */
  @Deprecated
  public boolean hasAccessLevelPublished(String right, String username, DocumentReference docname) {
    return hasAccessLevel(right, username, modelUtils.serializeRefLocal(docname),
        EPubUnpub.PUBLISHED);
  }

  /**
   * @deprecated use RightsAccessScriptService.hasAccessLevel instead
   */
  @Deprecated
  public boolean hasAccessLevelUnpublished(String right, String username,
      DocumentReference docname) {
    return hasAccessLevel(right, username, modelUtils.serializeRefLocal(docname),
        EPubUnpub.UNPUBLISHED);
  }

  /**
   * @deprecated use RightsAccessScriptService.hasAccessLevel instead
   */
  @Deprecated
  boolean hasAccessLevel(String right, String username, String docname, EPubUnpub unpublished) {
    XWikiRightService rightService = getContext().getWiki().getRightService();
    if (rightService instanceof CelementsRightServiceImpl) {
      // TODO replace with overridePubUnpub on PublicationService
      getContext().put("overridePubCheck", unpublished);
    } else {
      LOGGER.warn("Needs CelementsRightServiceImpl for publish / unpublish to work");
    }
    try {
      return rightService.hasAccessLevel(right, username, docname, getContext());
    } catch (XWikiException xwe) {
      LOGGER.error("hasAccessLevelPublished: Exception while checking access level for " + "right="
          + right + ", username=" + username + ", docname=" + docname, xwe);
      return false;
    }
  }

  private RightsAccessScriptService getRightsAccess() {
    return (RightsAccessScriptService) rightsAccess;
  }

}
