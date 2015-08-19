package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentNotExistsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentNotExistsException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentNotExistsException(DocumentReference docRef, String message) {
    super(docRef, message);
  }

  public DocumentNotExistsException(DocumentReference docRef, String message,
      Throwable cause) {
    super(docRef, message, cause);
  }

  public DocumentNotExistsException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
