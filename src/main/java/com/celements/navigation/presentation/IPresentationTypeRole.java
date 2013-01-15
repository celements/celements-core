package com.celements.navigation.presentation;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigation;

@ComponentRole
public interface IPresentationTypeRole {

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, int numItem,
      INavigation navigation);

  public String getDefaultCssClass();

  public String getEmptyDictionaryKey();

}
