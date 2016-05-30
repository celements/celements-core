package com.celements.filebase.exceptions;

public class FileBaseLoadException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  public FileBaseLoadException(String message) {
    super(message);
  }

  public FileBaseLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileBaseLoadException(Throwable cause) {
    super(cause);
  }

}
