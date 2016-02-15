package com.celements.model.access.exception;

import org.xwiki.model.reference.AttachmentReference;

public class AttachmentNotExistsException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private AttachmentReference attRef;

  public AttachmentNotExistsException(AttachmentReference attRef) {
    super(attRef != null ? attRef.toString() : "null");
    this.attRef = attRef;
  }

  public AttachmentNotExistsException(AttachmentReference attRef, Throwable cause) {
    super(attRef != null ? attRef.toString() : "null", cause);
    this.attRef = attRef;
  }

  public AttachmentReference getAttachmentReference() {
    return attRef;
  }

}
