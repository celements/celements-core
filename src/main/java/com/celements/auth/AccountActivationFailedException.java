package com.celements.auth;

public class AccountActivationFailedException extends Exception {

  private static final long serialVersionUID = -6535181789228219844L;

  public AccountActivationFailedException(Throwable exp) {
    super(exp);
  }

}
