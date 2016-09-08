package com.celements.model.access;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ModelAccessStrategy {

  public XWikiDocument getDocument(DocumentReference docRef, String lang);

  public XWikiDocument createDocument(DocumentReference docRef);

  public boolean exists(DocumentReference docRef, String lang);

  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException;

  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException;

  public List<String> getTranslations(DocumentReference docRef);

}
