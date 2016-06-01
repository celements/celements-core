package com.celements.model.access.exception;

import org.xwiki.model.reference.DocumentReference;

public class TranslationNotExistsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  private final String lang;

  public TranslationNotExistsException(DocumentReference docRef, String lang) {
    super(docRef);
    this.lang = lang;
  }

  public TranslationNotExistsException(DocumentReference docRef, String lang, Throwable cause) {
    super(docRef, cause);
    this.lang = lang;
  }

  public String getLang() {
    return lang;
  }

}
