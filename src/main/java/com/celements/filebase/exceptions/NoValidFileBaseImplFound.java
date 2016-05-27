package com.celements.filebase.exceptions;

public class NoValidFileBaseImplFound extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private String fileBaseImplKey;

  public NoValidFileBaseImplFound(String fileBaseImplKey) {
    super(fileBaseImplKey != null ? fileBaseImplKey : "null");
    this.fileBaseImplKey = fileBaseImplKey;
  }

  public NoValidFileBaseImplFound(String fileBaseImplKey, Throwable cause) {
    super(fileBaseImplKey != null ? fileBaseImplKey : "null", cause);
    this.fileBaseImplKey = fileBaseImplKey;
  }

  public String getFileBaseImplKey() {
    return fileBaseImplKey;
  }

}
