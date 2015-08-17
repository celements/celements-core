package com.celements.model.access.exception;

public class DocumentDeleteException extends DocumentAccessException {

  private static final long serialVersionUID = -2592241907630457229L;

  public DocumentDeleteException() {
    super();
  }

  public DocumentDeleteException(String message) {
    super(message);
  }

  public DocumentDeleteException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentDeleteException(Throwable cause) {
    super(cause);
  }

}
