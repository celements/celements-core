package com.celements.model.access.exception;

public class DocumentAlreadyExistsException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentAlreadyExistsException() {
    super();
  }

  public DocumentAlreadyExistsException(String message) {
    super(message);
  }

  public DocumentAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
