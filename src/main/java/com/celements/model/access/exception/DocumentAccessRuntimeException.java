package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class DocumentAccessRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 2808580562362027580L;

  private final DocumentReference docRef;

  public DocumentAccessRuntimeException(DocumentReference docRef) {
    super(docRef != null ? docRef.toString() : "null");
    this.docRef = docRef;
  }

  public DocumentAccessRuntimeException(DocumentReference docRef, Throwable cause) {
    super(docRef != null ? docRef.toString() : "null", cause);
    this.docRef = docRef;
  }

  public DocumentReference getDocumentReference() {
    return docRef;
  }

}
