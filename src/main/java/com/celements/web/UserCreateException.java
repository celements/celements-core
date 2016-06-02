package com.celements.web;

public class UserCreateException extends Exception {

  private static final long serialVersionUID = -6637742987473484753L;

  public UserCreateException(Exception excp) {
    super(excp);
  }
}
