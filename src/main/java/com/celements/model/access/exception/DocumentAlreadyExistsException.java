package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentAlreadyExistsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentAlreadyExistsException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentAlreadyExistsException(DocumentReference docRef, String message) {
    super(docRef, message);
  }

  public DocumentAlreadyExistsException(DocumentReference docRef, String message,
      Throwable cause) {
    super(docRef, message, cause);
  }

  public DocumentAlreadyExistsException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
