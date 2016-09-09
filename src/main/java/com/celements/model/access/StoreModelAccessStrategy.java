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
import com.xpn.xwiki.store.XWikiStoreInterface;

@Component
public class StoreModelAccessStrategy implements ModelAccessStrategy {

  @Requirement
  protected ModelContext context;

  @Requirement
  protected XWikiDocumentCreator docCreator;

  private XWiki getWiki() {
    return context.getXWikiContext().getWiki();
  }

  private XWikiStoreInterface getStore() {
    return context.getXWikiContext().getWiki().getStore();
  }

  @Override
  public XWikiDocument getDocument(final DocumentReference docRef, final String lang) {
    ContextExecutor<XWikiDocument, XWikiException> exec = new ContextExecutor<XWikiDocument, XWikiException>() {

      @Override
      protected XWikiDocument call() throws XWikiException {
        /*
         * XXX: this check and XWiki delegation is here because many unit tests do not yet use
         * ModelAccessStub. accessing the store directly breaks all these tests
         */
        if (isDefaultLang(lang)) {
          return getWiki().getDocument(docRef, context.getXWikiContext());
        }
        return getStore().loadXWikiDoc(newDummyDoc(docRef, lang), context.getXWikiContext());
      }
    };
    try {
      return exec.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef, String lang) {
    return docCreator.create(docRef, lang);
  }

  @Override
  public boolean exists(final DocumentReference docRef, final String lang) {
    ContextExecutor<Boolean, XWikiException> exec = new ContextExecutor<Boolean, XWikiException>() {

      @Override
      protected Boolean call() throws XWikiException {
        /*
         * XXX: this check and XWiki delegation is here because many unit tests do not yet use
         * ModelAccessStub. accessing the store directly breaks all these tests
         */
        if (isDefaultLang(lang)) {
          return getWiki().exists(docRef, context.getXWikiContext());
        }
        return getStore().exists(newDummyDoc(docRef, lang), context.getXWikiContext());
      }
    };
    try {
      return exec.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  @Override
  public void saveDocument(final XWikiDocument doc, final String comment, final boolean isMinorEdit)
      throws DocumentSaveException {
    ContextExecutor<Void, XWikiException> exec = new ContextExecutor<Void, XWikiException>() {

      @Override
      protected Void call() throws XWikiException {
        getWiki().saveDocument(doc, comment, isMinorEdit, context.getXWikiContext());
        return null;
      }
    };
    try {
      exec.inWiki(doc.getDocumentReference().getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentSaveException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public void deleteDocument(final XWikiDocument doc, final boolean totrash)
      throws DocumentDeleteException {
    ContextExecutor<Void, XWikiException> exec = new ContextExecutor<Void, XWikiException>() {

      @Override
      protected Void call() throws XWikiException {
        getWiki().deleteDocument(doc, totrash, context.getXWikiContext());
        return null;
      }
    };
    try {
      exec.inWiki(doc.getDocumentReference().getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentDeleteException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public List<String> getTranslations(final DocumentReference docRef) {
    ContextExecutor<List<String>, XWikiException> exec = new ContextExecutor<List<String>, XWikiException>() {

      @Override
      protected List<String> call() throws XWikiException {
        return getStore().getTranslationList(newDummyDoc(docRef, IModelAccessFacade.DEFAULT_LANG),
            context.getXWikiContext());
      }
    };
    try {
      return exec.inWiki(docRef.getWikiReference()).execute();
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  private boolean isDefaultLang(String lang) {
    return IModelAccessFacade.DEFAULT_LANG.equals(lang);
  }

  private XWikiDocument newDummyDoc(DocumentReference docRef, String lang) {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setLanguage(lang);
    return doc;
  }

}
