package com.celements.menu.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IMenuAccessServiceRole {

  public boolean hasview(DocumentReference menuBarDocRef);

}
