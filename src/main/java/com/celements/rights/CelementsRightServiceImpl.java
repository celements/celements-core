package com.celements.rights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.rights.publication.IPublicationServiceRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;

public class CelementsRightServiceImpl extends XWikiRightServiceImpl {

  private static Logger LOGGER = LoggerFactory.getLogger(CelementsRightServiceImpl.class);

  /*
   * Adds an optional check for publish and unpublish dates to determine whether or not a
   * page can be viewed.
   */
  @Override
  public boolean checkRight(String userOrGroupName, XWikiDocument doc, String accessLevel,
      boolean user, boolean allow, boolean global, XWikiContext context)
      throws XWikiRightNotFoundException, XWikiException {
    if (getPubSrv().isPublishActive() && getPubSrv().isRestrictedRightsAction(accessLevel)) {
      // default behaviour: no object -> published
      if (!getPubSrv().isPubOverride() && (getPubSrv().isUnpubOverride() || getPubSrv().isPublished(
          doc))) {
        LOGGER.info("Document published or not publish controlled.");
        return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, context);
      } else {
        LOGGER.info("Document not published, checking edit rights.");
        try {
          return super.checkRight(userOrGroupName, doc, "edit", user, allow, global, context);
        } catch (XWikiRightNotFoundException xwrnfe) {
          LOGGER.info("Rights could not be determined.");
          // If rights can not be determined default to no rights.
          return false;
        }
      }
    } else {
      if (getPubSrv().isPubUnpubOverride()) {
        LOGGER.warn("Needs CelementsRightServiceImpl for publish / unpublish to work");
      }
      return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, context);
    }
  }

  IPublicationServiceRole getPubSrv() {
    return Utils.getComponent(IPublicationServiceRole.class);
  }

  @Override
  public boolean hasProgrammingRights(XWikiDocument doc, XWikiContext context) {
    LOGGER.debug("hasProgrammingRights checking for '{}'", (doc != null)
        ? doc.getDocumentReference() : "null");
    final boolean hasRights = super.hasProgrammingRights(doc, context);
    LOGGER.info("hasProgrammingRights for '{}' returning '{}'", (doc != null)
        ? doc.getDocumentReference() : "null", hasRights);
    if (!hasRights) {
      LOGGER.trace("hasProgrammingRights FALSE for '{}' with context '{}'", (doc != null)
          ? doc.getDocumentReference() : "null", context);
    }
    return hasRights;
  }

}
