package com.celements.auth.user;

public class UserCreateException extends Exception {

  private static final long serialVersionUID = -6637742987473484753L;

  public UserCreateException(String message) {
    super(message);
  }

  public UserCreateException(Exception exc) {
    super(exc);
  }

  public UserCreateException(String message, Exception exc) {
    super(message, exc);
  }

}
