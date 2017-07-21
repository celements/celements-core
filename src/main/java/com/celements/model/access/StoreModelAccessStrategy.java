package com.celements.model.access;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

/**
 * Implementation of {@link ModelAccessStrategy} only accessing {@link XWikiStoreInterface}.
 *
 * @author Marc Sladek
 */
@Component
public class StoreModelAccessStrategy implements ModelAccessStrategy {

  @Requirement
  protected ModelContext context;

  @Requirement
  protected XWikiDocumentCreator docCreator;

  /**
   * @deprecated refactor calls to {@link #getStore()}
   */
  @Deprecated
  private XWiki getWiki() {
    return context.getXWikiContext().getWiki();
  }

  private XWikiStoreInterface getStore() {
    return context.getXWikiContext().getWiki().getStore();
  }

  @Override
  public boolean exists(final DocumentReference docRef, final String lang) {
    try {
      return new ContextExecutor<Boolean, XWikiException>() {

        @Override
        protected Boolean call() throws XWikiException {
          return getStore().exists(newDummyDoc(docRef, lang), context.getXWikiContext());
        }
      }.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  @Override
  public XWikiDocument getDocument(final DocumentReference docRef, final String lang) {
    try {
      return new ContextExecutor<XWikiDocument, XWikiException>() {

        @Override
        protected XWikiDocument call() throws XWikiException {
          return getStore().loadXWikiDoc(newDummyDoc(docRef, lang), context.getXWikiContext());
        }
      }.inWiki(docRef.getWikiReference()).execute();
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
    DocumentReference docRef = doc.getDocumentReference();
    try {
      new ContextExecutor<Void, XWikiException>() {

        @Override
        protected Void call() throws XWikiException {
          // TODO access store directly
          getWiki().saveDocument(doc, comment, isMinorEdit, context.getXWikiContext());
          return null;
        }
      }.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentSaveException(docRef, xwe);
    }
  }

  @Override
  public void deleteDocument(final XWikiDocument doc, final boolean totrash)
      throws DocumentDeleteException {
    DocumentReference docRef = doc.getDocumentReference();
    try {
      new ContextExecutor<Void, XWikiException>() {

        @Override
        protected Void call() throws XWikiException {
          // TODO access store directly
          getWiki().deleteDocument(doc, totrash, context.getXWikiContext());
          return null;
        }
      }.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentDeleteException(docRef, xwe);
    }
  }

  @Override
  public List<String> getTranslations(final DocumentReference docRef) {
    try {
      return new ContextExecutor<List<String>, XWikiException>() {

        @Override
        protected List<String> call() throws XWikiException {
          return getStore().getTranslationList(newDummyDoc(docRef, IModelAccessFacade.DEFAULT_LANG),
              context.getXWikiContext());
        }
      }.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  private XWikiDocument newDummyDoc(DocumentReference docRef, String lang) {
    XWikiDocument doc = new XWikiDocument(References.cloneRef(docRef, DocumentReference.class));
    doc.setLanguage(lang);
    return doc;
  }

}
