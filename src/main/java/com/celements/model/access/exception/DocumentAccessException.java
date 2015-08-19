package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentAccessException extends Exception {

  private static final long serialVersionUID = -8302055770293965958L;

  private final DocumentReference docRef;

  public DocumentAccessException(DocumentReference docRef) {
    super(docRef.toString());
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, Throwable cause) {
    super(docRef.toString(), cause);
    this.docRef = docRef;
  }

  public DocumentReference getDocumentReference() {
    return docRef;
  }

}
