package com.celements.model.access.exception;

public class DocumentNotExistsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentNotExistsException() {
    super();
  }

  public DocumentNotExistsException(String message) {
    super(message);
  }

  public DocumentNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentNotExistsException(Throwable cause) {
    super(cause);
  }

}
