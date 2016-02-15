package com.celements.filebase.exceptions;


public class FileBaseLoadException extends Exception {

  private static final long serialVersionUID = -2592241907630457229L;

  private String fileName;

  public FileBaseLoadException(String fileName) {
    super(fileName != null ? fileName : "null");
    this.fileName = fileName;
  }

  public FileBaseLoadException(String fileName, Throwable cause) {
    super(fileName != null ? fileName : "null", cause);
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

}
