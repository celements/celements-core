package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

import com.celements.rights.AccessLevel;
import com.xpn.xwiki.user.api.XWikiUser;

public class NoAccessRightsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  private XWikiUser user;
  
  private AccessLevel expectedAccessLevel;

  public NoAccessRightsException(DocumentReference docRef, XWikiUser user,
      AccessLevel expectedAccessLevel) {
    super(docRef);
    this.user = user;
    this.expectedAccessLevel = expectedAccessLevel;
  }

  public NoAccessRightsException(DocumentReference docRef, XWikiUser user,
      AccessLevel expectedAccessLevel, Throwable cause) {
    super(docRef, cause);
    this.user = user;
    this.expectedAccessLevel = expectedAccessLevel;
  }

  public XWikiUser getUser() {
    return user;
  }

  public AccessLevel getExpectedAccessLevel() {
    return expectedAccessLevel;
  }

}
