package com.celements.model.access.exception;

import org.xwiki.model.reference.EntityReference;

import com.celements.rights.AccessLevel;
import com.xpn.xwiki.user.api.XWikiUser;

public class NoAccessRightsException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private EntityReference entityRef;

  private XWikiUser user;
  
  private AccessLevel expectedAccessLevel;

  public NoAccessRightsException(EntityReference entityRef, XWikiUser user,
      AccessLevel expectedAccessLevel) {
    super();
    this.entityRef = entityRef;
    this.user = user;
    this.expectedAccessLevel = expectedAccessLevel;
  }

  public NoAccessRightsException(EntityReference entityRef, XWikiUser user,
      AccessLevel expectedAccessLevel, Throwable cause) {
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

  public AccessLevel getExpectedAccessLevel() {
    return expectedAccessLevel;
  }

}
