package com.celements.model.access.exception;

import org.xwiki.model.reference.EntityReference;

public class DocumentAccessRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 2808580562362027580L;

  private final EntityReference ref;

  public DocumentAccessRuntimeException(EntityReference ref) {
    super(ref != null ? ref.toString() : "null");
    this.ref = ref;
  }

  public DocumentAccessRuntimeException(EntityReference ref, Throwable cause) {
    super(ref != null ? ref.toString() : "null", cause);
    this.ref = ref;
  }

  public EntityReference getReference() {
    return ref;
  }

}
