package com.celements.web.service;

import org.xwiki.model.reference.DocumentReference;

public class XDocRecursionException extends Exception {

  private static final long serialVersionUID = 1L;
  private DocumentReference parentRef;

  public XDocRecursionException(DocumentReference parentRef) {
    this.parentRef = parentRef;
  }

  public DocumentReference getParentRef() {
    return parentRef;
  }

}
