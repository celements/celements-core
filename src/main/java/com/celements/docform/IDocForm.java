package com.celements.docform;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IDocForm {

  public enum ResponseState {
    successful, failed, unchanged;
  }

  IDocForm initialize(DocumentReference defaultDocRef, boolean isCreateAllowed);

  void updateDocs(@NotNull List<DocFormRequestParam> requestParams);

  @NotNull
  Map<ResponseState, Set<DocumentReference>> getResponseMap(
      @NotNull List<DocFormRequestParam> requestParams);

}
