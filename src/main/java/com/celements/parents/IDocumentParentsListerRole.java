package com.celements.parents;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IDocumentParentsListerRole {

  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc);

}
