package com.celements.common.classes;

public class XClassCreateException extends Exception {

  private static final long serialVersionUID = -5970182308945221672L;

  public XClassCreateException(String message) {
    super(message);
  }

  public XClassCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public XClassCreateException(Throwable cause) {
    super(cause);
  }

}
