package com.celements.rights.access;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.user.api.XWikiRightService;

@ComponentRole
public interface IRightsAccessFacade {

  public XWikiRightService getRightsService();

}
