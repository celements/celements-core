package com.celements.model.access.field;

public class FieldAccessException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FieldAccessException(String msg) {
    super(msg);
  }

  public FieldAccessException(Throwable cause) {
    super(cause);
  }

  public FieldAccessException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
