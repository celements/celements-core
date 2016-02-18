package com.celements.rights.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface IRightsAccessFacadeRole {

  public boolean hasAccessLevel(String right, XWikiUser user, EntityReference entityRef);

  public XWikiRightService getRightsService();

}
