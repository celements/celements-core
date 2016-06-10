package com.celements.web.plugin.cmd;

public class CreatingValidationTokenFailedException extends Exception {

  private static final long serialVersionUID = 1L;

  public CreatingValidationTokenFailedException(String message) {
    super(message);
  }

  public CreatingValidationTokenFailedException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
