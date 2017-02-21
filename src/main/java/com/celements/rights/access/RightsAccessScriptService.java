package com.celements.rights.access;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

import com.celements.rights.publication.EPubUnpub;
import com.celements.rights.publication.IPublicationServiceRole;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;

@Component(RightsAccessScriptService.NAME)
public class RightsAccessScriptService implements ScriptService {

  public static final String NAME = "rightsAccess";

  @Requirement
  IPublicationServiceRole pubSrv;

  @Requirement
  IRightsAccessFacadeRole rightsAccess;

  public EPubUnpub getEPubUnpub(String name) {
    return EPubUnpub.valueOf(name);
  }

  public EAccessLevel getEAccessLevel(String xwikiRight) {
    return EAccessLevel.getAccessLevel(xwikiRight).orNull();
  }

  public XWikiUser getGuestUser() {
    return new XWikiUser(XWikiRightService.GUEST_USER_FULLNAME);
  }

  public XWikiUser getUser(String username) {
    return new XWikiUser(username);
  }

  public boolean isPublishActive(DocumentReference forDoc) {
    return pubSrv.isPublishActive(forDoc);
  }

  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level) {
    return rightsAccess.hasAccessLevel(ref, level);
  }

  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user) {
    return rightsAccess.hasAccessLevel(ref, level, user);
  }

  public boolean hasAccessLevel(DocumentReference docRef, EAccessLevel level, XWikiUser user,
      EPubUnpub unpublished) {
    pubSrv.overridePubUnpub(unpublished);
    return hasAccessLevel(docRef, level, user);
  }

}
