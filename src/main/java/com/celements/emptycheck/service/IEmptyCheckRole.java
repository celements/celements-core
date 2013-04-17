package com.celements.emptycheck.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IEmptyCheckRole {

  public boolean isEmptyRTEDocument(DocumentReference docRef);

  public boolean isEmptyDocument(DocumentReference docRef);

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef);

}
