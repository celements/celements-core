package com.celements.store;

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.EntityReference;

import com.celements.model.metadata.DocumentMetaData;
import com.xpn.xwiki.store.XWikiStoreInterface;

public interface MetaDataStoreExtension extends XWikiStoreInterface {

  @NotNull
  public Set<DocumentMetaData> listDocumentMetaData(@Nullable EntityReference filterRef);

}
