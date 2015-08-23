package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentSaveException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentSaveException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentSaveException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
