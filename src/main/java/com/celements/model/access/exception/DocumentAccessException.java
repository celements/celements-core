package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.References;

public class DocumentAccessException extends Exception {

  private static final long serialVersionUID = -8302055770293965958L;

  private final DocumentReference docRef;

  public DocumentAccessException(DocumentReference docRef) {
    super(getMessage(docRef, null));
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, String lang) {
    super(getMessage(docRef, lang));
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, Throwable cause) {
    super(getMessage(docRef, null), cause);
    this.docRef = docRef;
  }

  public DocumentAccessException(DocumentReference docRef, String lang, Throwable cause) {
    super(getMessage(docRef, lang), cause);
    this.docRef = docRef;
  }

  private static String getMessage(DocumentReference docRef, String lang) {
    if (docRef != null) {
      return new StringBuilder(docRef.toString()).append(" - ").append(lang).toString();
    }
    return "null";
  }

  public DocumentReference getDocumentReference() {
    return References.cloneRef(docRef, DocumentReference.class);
  }

}
