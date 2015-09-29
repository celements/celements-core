package com.celements.parents;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IDocParentProviderRole {

  /**
   * get only parents for docRef NOT including docRef itself
   * @param docRef
   * @return parents of docRef
   */
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef);

}
