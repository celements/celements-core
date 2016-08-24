package com.celements.model.classes.metadata;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.suigeneris.jrcs.rcs.Version;
import org.xwiki.model.reference.DocumentReference;

public interface DocumentMetaData {

  @NotNull
  public DocumentReference getDocRef();

  @NotNull
  public String getLanguage();

  @Nullable
  public Version getVersion();

}
