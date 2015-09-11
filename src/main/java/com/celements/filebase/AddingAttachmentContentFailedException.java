package com.celements.filebase;

import java.io.IOException;

public class AddingAttachmentContentFailedException extends Exception {

  private static final long serialVersionUID = 1L;

  public AddingAttachmentContentFailedException(IOException exp) {
    super(exp);
  }
  
}
