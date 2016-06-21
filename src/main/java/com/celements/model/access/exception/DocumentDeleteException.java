package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentDeleteException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentDeleteException(DocumentReference docRef) {
    super(docRef);
  }

  public DocumentDeleteException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
