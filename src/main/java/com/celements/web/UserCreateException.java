package com.celements.web;

public class UserCreateException extends Exception {

  private static final long serialVersionUID = -6637742987473484753L;

  private String message;

  public UserCreateException(String message) {
    this.message = message;
  }

  public UserCreateException(Exception excp) {
    super(excp);
  }

  public UserCreateException(String message, Exception excp) {
    super(excp);
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message + " - " + super.getMessage();
  }
}
