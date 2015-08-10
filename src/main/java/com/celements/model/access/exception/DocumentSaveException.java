package com.celements.model.access.exception;

public class DocumentSaveException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentSaveException() {
    super();
  }

  public DocumentSaveException(String message) {
    super(message);
  }

  public DocumentSaveException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentSaveException(Throwable cause) {
    super(cause);
  }

}
