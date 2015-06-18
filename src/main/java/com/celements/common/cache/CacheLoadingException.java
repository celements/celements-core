package com.celements.common.cache;

public class CacheLoadingException extends Exception {

  private static final long serialVersionUID = 1L;

  public CacheLoadingException() {
    super();
  }

  public CacheLoadingException(String message) {
    super(message);
  }

  public CacheLoadingException(String message, Throwable cause) {
    super(message, cause);
  }

  public CacheLoadingException(Throwable cause) {
    super(cause);
  }

}
