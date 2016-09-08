package com.celements.model.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface XWikiDocumentCreator {

  public XWikiDocument createWithoutDefaults(DocumentReference docRef, String lang);

  public XWikiDocument create(DocumentReference docRef, String lang);

  public XWikiDocument create(DocumentReference docRef);

}
