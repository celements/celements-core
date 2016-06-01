package com.celements.model.access.exception;

import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.matcher.IAttachmentMatcher;

public class AttachmentNotExistsException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private DocumentReference docRef;
  private AttachmentReference attRef;
  private IAttachmentMatcher attMatcher;

  public AttachmentNotExistsException(AttachmentReference attRef) {
    super(attRef != null ? attRef.toString() : "null");
    this.attRef = attRef;
  }

  public AttachmentNotExistsException(AttachmentReference attRef, Throwable cause) {
    super(attRef != null ? attRef.toString() : "null", cause);
    this.attRef = attRef;
  }

  public AttachmentNotExistsException(IAttachmentMatcher attMatcher, DocumentReference docRef) {
    super(attMatcher != null ? attMatcher.toString() : "null");
    this.attMatcher = attMatcher;
    this.docRef = docRef;
  }

  public DocumentReference getDocumentReference() {
    if (attRef != null) {
      return attRef.getDocumentReference();
    }
    return docRef;
  }

  public AttachmentReference getAttachmentReference() {
    return attRef;
  }

  public IAttachmentMatcher getAttachmentMatcher() {
    return attMatcher;
  }

}
