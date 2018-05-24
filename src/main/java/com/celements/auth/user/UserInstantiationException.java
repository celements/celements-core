package com.celements.auth.user;

public class UserInstantiationException extends Exception {

  private static final long serialVersionUID = -5349680105776140376L;

  public UserInstantiationException(String message) {
    super(message);
  }

  public UserInstantiationException(Exception exc) {
    super(exc);
  }

  public UserInstantiationException(String message, Exception exc) {
    super(message, exc);
  }

}
