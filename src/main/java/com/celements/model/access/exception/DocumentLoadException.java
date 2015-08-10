package com.celements.model.access.exception;

public class DocumentLoadException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentLoadException() {
    super();
  }

  public DocumentLoadException(String message) {
    super(message);
  }

  public DocumentLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentLoadException(Throwable cause) {
    super(cause);
  }

}
