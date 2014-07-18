package com.celements.nextfreedoc;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

@ComponentRole
public interface INextFreeDocRole {
  
  public static final String UNTITLED_NAME = "untitled";

  public DocumentReference getNextTitledPageDocRef(SpaceReference spaceRef, String title);

  public DocumentReference getNextUntitledPageDocRef(SpaceReference spaceRef);

}
