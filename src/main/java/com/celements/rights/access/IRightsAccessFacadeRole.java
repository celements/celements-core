package com.celements.rights.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface IRightsAccessFacadeRole {

  /**
   * instead use hasAccessLevel(EntityReference, EAccessLevel, XWikiUser)
   */
  @Deprecated
  public boolean hasAccessLevel(String right, XWikiUser user, EntityReference entityRef);

  public XWikiRightService getRightsService();

  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level);

  public boolean hasAccessLevel(EntityReference ref, EAccessLevel level, XWikiUser user);

}
