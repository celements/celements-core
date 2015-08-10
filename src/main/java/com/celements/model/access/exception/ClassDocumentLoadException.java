package com.celements.model.access.exception;

public class ClassDocumentLoadException extends DocumentLoadException {

  private static final long serialVersionUID = -2592241907630457229L;

  public ClassDocumentLoadException() {
    super();
  }

  public ClassDocumentLoadException(String message) {
    super(message);
  }

  public ClassDocumentLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassDocumentLoadException(Throwable cause) {
    super(cause);
  }

}
