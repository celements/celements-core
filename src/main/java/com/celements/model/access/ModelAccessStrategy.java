package com.celements.model.access;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ModelAccessStrategy {

  public boolean exists(@NotNull DocumentReference docRef, @NotNull String lang);

  @NotNull
  public XWikiDocument getDocument(@NotNull DocumentReference docRef, @NotNull String lang);

  @NotNull
  public XWikiDocument createDocument(DocumentReference docRef, @NotNull String lang);

  public void saveDocument(@NotNull XWikiDocument doc, @Nullable String comment,
      boolean isMinorEdit) throws DocumentSaveException;

  public void deleteDocument(@NotNull XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException;

  @NotNull
  public List<String> getTranslations(@NotNull DocumentReference docRef);

}
