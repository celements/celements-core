package com.celements.web.plugin.cmd;

import com.celements.model.access.exception.DocumentNotExistsException;

public class FailedToCreateTagException extends Exception {

  private static final long serialVersionUID = 1L;

  public FailedToCreateTagException(String message, DocumentNotExistsException exp) {
    super(message, exp);
  }

}
