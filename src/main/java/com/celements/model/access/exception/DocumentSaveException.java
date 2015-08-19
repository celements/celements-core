package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentSaveException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentSaveException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentSaveException(DocumentReference docRef, String message) {
    super(docRef, message);
  }

  public DocumentSaveException(DocumentReference docRef, String message,
      Throwable cause) {
    super(docRef, message, cause);
  }

  public DocumentSaveException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
