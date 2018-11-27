package com.celements.navigation.presentation;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

@ComponentRole
public interface IPresentationTypeRole<T extends PresentationNodeData> {

  public void writeNodeContent(StringBuilder writer, DocumentReference docRef, T nodeData);

  public void writeNodeContent(StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, T nodeData);

  public String getDefaultCssClass();

  public String getEmptyDictionaryKey();

  public SpaceReference getPageLayoutForDoc(DocumentReference docRef);

}
