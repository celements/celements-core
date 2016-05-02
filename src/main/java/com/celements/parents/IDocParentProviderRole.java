package com.celements.parents;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IDocParentProviderRole {

  /**
   * @param docRef
   * @return parents of docRef from bottom up (does NOT include docRef itself)
   */
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef);

}
