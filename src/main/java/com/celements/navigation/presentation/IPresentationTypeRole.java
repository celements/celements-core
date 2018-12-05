package com.celements.navigation.presentation;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;

@ComponentRole
public interface IPresentationTypeRole<T extends PresentationNodeData> {

  void writeNodeContent(@NotNull ICellWriter writer, @NotNull DocumentReference docRef,
      @NotNull T nodeData);

  void writeNodeContent(@NotNull StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      @NotNull DocumentReference docRef, boolean isLeaf, int numItem, @NotNull T nodeData);

  String getDefaultCssClass();

  String getEmptyDictionaryKey();

  SpaceReference getPageLayoutForDoc(DocumentReference docRef);

}
