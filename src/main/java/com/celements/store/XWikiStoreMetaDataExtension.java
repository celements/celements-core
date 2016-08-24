package com.celements.store;

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.model.classes.metadata.DocumentMetaData;
import com.xpn.xwiki.store.XWikiStoreInterface;

public interface XWikiStoreMetaDataExtension extends XWikiStoreInterface {

  @NotNull
  public Set<DocumentMetaData> listDocumentMetaData(@Nullable String hqlWhereClause);

}
