package com.celements.model.access;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class LegacyModelAccessStrategy implements ModelAccessStrategy {

  @Requirement
  protected ModelContext context;

  @Requirement
  protected XWikiDocumentCreator docCreator;

  private XWiki getWiki() {
    return context.getXWikiContext().getWiki();
  }

  @Override
  public boolean exists(final DocumentReference docRef, final String lang) {
    return getWiki().exists(docRef, context.getXWikiContext());
  }

  @Override
  public XWikiDocument getDocument(DocumentReference docRef, String lang) {
    try {
      return getWiki().getDocument(docRef, context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef, String lang) {
    return docCreator.create(docRef, lang);
  }

  @Override
  public void saveDocument(final XWikiDocument doc, final String comment, final boolean isMinorEdit)
      throws DocumentSaveException {
    try {
      getWiki().saveDocument(doc, comment, isMinorEdit, context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new DocumentSaveException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public void deleteDocument(final XWikiDocument doc, final boolean totrash)
      throws DocumentDeleteException {
    try {
      getWiki().deleteDocument(doc, totrash, context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new DocumentDeleteException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public List<String> getTranslations(final DocumentReference docRef) {
    try {
      return getWiki().getDocument(docRef, context.getXWikiContext()).getTranslationList(
          context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  private XWikiDocument newDummyDoc(DocumentReference docRef, String lang) {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setLanguage(lang);
    return doc;
  }

}
