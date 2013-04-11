package com.celements.web.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IEmptyCheckRole {

  public boolean isEmptyRTEDocument(DocumentReference docRef);

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef);

  public boolean isEmptyRTEDocument(XWikiDocument localdoc);

  public boolean isEmptyRTEString(String rteContent);

  public boolean isEmptyRTEDocumentTranslated(DocumentReference docRef);

  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef);

}
