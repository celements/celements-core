package com.celements.filebase.exceptions;

public class FileNotExistsException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private String fileName;

  public FileNotExistsException(String fileName) {
    super(fileName != null ? fileName : "null");
    this.fileName = fileName;
  }

  public FileNotExistsException(String fileName, Throwable cause) {
    super(fileName != null ? fileName : "null", cause);
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

}
