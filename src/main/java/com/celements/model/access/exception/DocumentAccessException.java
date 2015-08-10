package com.celements.model.access.exception;

public class DocumentAccessException extends Exception {

  private static final long serialVersionUID = -8302055770293965958L;

  public DocumentAccessException() {
    super();
  }

  public DocumentAccessException(String message) {
    super(message);
  }

  public DocumentAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocumentAccessException(Throwable cause) {
    super(cause);
  }

}
