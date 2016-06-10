package com.celements.web.plugin.cmd;

public class SendValidationFailedException extends Exception {

  private static final long serialVersionUID = 1L;

  public SendValidationFailedException(String message) {
    super(message);
  }

  public SendValidationFailedException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
