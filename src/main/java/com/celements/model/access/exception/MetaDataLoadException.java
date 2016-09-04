package com.celements.model.access.exception;

import org.xwiki.model.reference.EntityReference;

public class MetaDataLoadException extends ModelAccessRuntimeException {

  private static final long serialVersionUID = 5250275367530659705L;

  public MetaDataLoadException(EntityReference ref) {
    super(ref);
  }

  public MetaDataLoadException(EntityReference ref, Throwable cause) {
    super(ref, cause);
  }

}
