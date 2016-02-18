package com.celements.rights.access.exceptions;

import org.xwiki.model.reference.EntityReference;

import com.celements.rights.access.EAccessLevel;
import com.xpn.xwiki.user.api.XWikiUser;

public class NoAccessRightsException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private EntityReference entityRef;

  private XWikiUser user;
  
  private EAccessLevel expectedAccessLevel;

  public NoAccessRightsException(EntityReference entityRef, XWikiUser user,
      EAccessLevel expectedAccessLevel) {
    super();
    this.entityRef = entityRef;
    this.user = user;
    this.expectedAccessLevel = expectedAccessLevel;
  }

  public NoAccessRightsException(EntityReference entityRef, XWikiUser user,
      EAccessLevel expectedAccessLevel, Throwable cause) {
    super(cause);
    this.entityRef = entityRef;
    this.user = user;
    this.expectedAccessLevel = expectedAccessLevel;
  }
  
  public EntityReference getEntityRef() {
    return entityRef;
  }

  public XWikiUser getUser() {
    return user;
  }

  public EAccessLevel getExpectedAccessLevel() {
    return expectedAccessLevel;
  }

}
