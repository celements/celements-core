package com.celements.model.access.exception;

import org.xwiki.model.reference.EntityReference;

public class DocumentMetaDataLoadException extends DocumentAccessRuntimeException {

  private static final long serialVersionUID = 5250275367530659705L;

  public DocumentMetaDataLoadException(EntityReference ref) {
    super(ref);
  }

  public DocumentMetaDataLoadException(EntityReference ref, Throwable cause) {
    super(ref, cause);
  }

}
