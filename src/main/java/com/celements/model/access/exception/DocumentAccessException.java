package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentAccessException extends Exception {

  private static final long serialVersionUID = -8302055770293965958L;

  private final DocumentReference docRef;

  public DocumentAccessException(DocumentReference docRef) {
    super();
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, String message) {
    super(message);
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, String message,
      Throwable cause) {
    super(message, cause);
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, Throwable cause) {
    super(cause);
    this.docRef = docRef;
  }

  public DocumentReference getDocumentReference() {
    return docRef;
  }

}
