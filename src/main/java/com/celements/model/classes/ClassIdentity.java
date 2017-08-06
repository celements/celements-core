package com.celements.model.classes;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

public interface ClassIdentity {

  @NotNull
  DocumentReference getDocRef();

  @NotNull
  DocumentReference getDocRef(@NotNull WikiReference wikiRef);

}
