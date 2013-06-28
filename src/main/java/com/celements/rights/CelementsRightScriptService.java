package com.celements.rights;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.rights.CelementsRightServiceImpl.PubUnpub;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

@Component("celementsright")
public class CelementsRightScriptService implements ScriptService {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsRightScriptService.class);
  
  @Requirement
  Execution execution;
  
  public boolean publicationActivated(DocumentReference forDoc) {
    XWikiRightService rightService = getContext().getWiki().getRightService();
    if(rightService instanceof CelementsRightServiceImpl) {
      return ((CelementsRightServiceImpl)rightService).isPublishActive(forDoc, 
          getContext());
    } else {
      LOGGER.warn("Needs CelementsRightServiceImpl for publish / unpublish to work");
      return false;
    }
  }
  
  public boolean hasAccessLevelPublished(String right, String username, 
      DocumentReference docname) {
    return hasAccessLevel(right, username, getWebUtils().getRefLocalSerializer(
        ).serialize(docname), CelementsRightServiceImpl.PubUnpub.PUBLISHED);
  }
  
  public boolean hasAccessLevelUnpublished(String right, String username, 
      DocumentReference docname) {
    return hasAccessLevel(right, username, getWebUtils().getRefLocalSerializer(
        ).serialize(docname), CelementsRightServiceImpl.PubUnpub.UNPUBLISHED);
  }
  
  boolean hasAccessLevel(String right, String username, String docname, 
      PubUnpub unpublished) {
    XWikiRightService rightService = getContext().getWiki().getRightService();
    if(rightService instanceof CelementsRightServiceImpl) {
      getContext().put("overridePubCheck", unpublished);
    } else {
      LOGGER.warn("Needs CelementsRightServiceImpl for publish / unpublish to work");
    }
    try {
      return rightService.hasAccessLevel(right, username, docname, getContext());
    } catch (XWikiException xwe) {
      LOGGER.error("hasAccessLevelPublished: Exception while checking access level for " +
          "right=" + right + ", username=" + username + ", docname=" + docname, xwe);
      return false;
    }
  }

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  private IWebUtilsService getWebUtils() {
    return (IWebUtilsService)Utils.getComponent(IWebUtilsService.class);
  }
}
