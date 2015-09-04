package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentLoadException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentLoadException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentLoadException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
