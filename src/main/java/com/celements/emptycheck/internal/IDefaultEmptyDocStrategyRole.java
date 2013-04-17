package com.celements.emptycheck.internal;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IDefaultEmptyDocStrategyRole {

  public boolean isEmptyRTEDocumentTranslated(DocumentReference docRef);

  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef);

  public boolean isEmptyRTEDocument(XWikiDocument localdoc);

  public boolean isEmptyRTEString(String rteContent);

  public boolean isEmptyDocumentTranslated(DocumentReference docRef);

  public boolean isEmptyDocumentDefault(DocumentReference docRef);
}
