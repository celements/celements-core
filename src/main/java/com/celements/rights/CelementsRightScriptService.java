package com.celements.rights;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.rights.access.RightsAccessScriptService;
import com.celements.rights.publication.EPubUnpub;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated use RightsAccessScriptService instead
 */
@Component("celementsright")
@Deprecated
public class CelementsRightScriptService implements ScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsRightScriptService.class);

  @Requirement(RightsAccessScriptService.NAME)
  ScriptService rightsAccess;

  @Requirement
  Execution execution;

  private RightsAccessScriptService getRightsAccess() {
    return (RightsAccessScriptService) rightsAccess;
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
    return hasAccessLevel(right, username, getWebUtils().getRefLocalSerializer().serialize(docname),
        EPubUnpub.PUBLISHED);
  }

  /**
   * @deprecated use RightsAccessScriptService.hasAccessLevel instead
   */
  @Deprecated
  public boolean hasAccessLevelUnpublished(String right, String username,
      DocumentReference docname) {
    return hasAccessLevel(right, username, getWebUtils().getRefLocalSerializer().serialize(docname),
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

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }
}
