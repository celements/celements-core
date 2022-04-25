package com.celements.filebase.uri;

import javax.validation.constraints.NotNull;

import com.celements.filebase.references.FileReference;

public class FileNotExistException extends Exception {

  private static final long serialVersionUID = 1L;

  private final FileReference fileReference;

  public FileNotExistException(@NotNull FileReference fileRef) {
    super("Url fileRef [" + fileRef + "] does not exist.");
    this.fileReference = fileRef;
  }

  public FileNotExistException(@NotNull String message, @NotNull FileReference fileRef) {
    super(message);
    this.fileReference = fileRef;
  }

  public FileNotExistException(@NotNull String message, @NotNull FileReference fileRef,
      Exception wrapExp) {
    super(message, wrapExp);
    this.fileReference = fileRef;
  }

  public FileReference getFileReference() {
    return fileReference;
  }
}
