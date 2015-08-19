package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class ClassDocumentLoadException extends DocumentLoadException {

  private static final long serialVersionUID = -2592241907630457229L;

  public ClassDocumentLoadException(DocumentReference docRef) {
    super(docRef);
  }

  public ClassDocumentLoadException(DocumentReference docRef, String message) {
    super(docRef, message);
  }

  public ClassDocumentLoadException(DocumentReference docRef, String message,
      Throwable cause) {
    super(docRef, message, cause);
  }

  public ClassDocumentLoadException(DocumentReference docRef, Throwable cause) {
    super(docRef, cause);
  }

}
