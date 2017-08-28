package com.celements.store;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;

import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateStore;

@Singleton
@Component
public class CelHibernateStore extends XWikiHibernateStore {

  // TODO CELDEV-530 - inline&refactor XWikiHibernateStore.saveXWikiDoc
  @Override
  public void saveXWikiDoc(XWikiDocument doc, final XWikiContext context,
      final boolean bTransaction) throws XWikiException {
    // XWikiHibernateStore.saveXWikiDoc requires a mutable docRef
    DocRefMutabilityExecutor<Void> exec = new DocRefMutabilityExecutor<Void>() {

      @Override
      public Void call(XWikiDocument doc) throws XWikiException {
        CelHibernateStore.super.saveXWikiDoc(doc, context, bTransaction);
        return null;
      }
    };
    exec.execute(doc);
  }

  // TODO CELDEV-530 - inline&refactor XWikiHibernateStore.loadXWikiDoc
  // TODO CELDEV-531 - improve load performance
  @Override
  public XWikiDocument loadXWikiDoc(XWikiDocument doc, final XWikiContext context)
      throws XWikiException {
    // XWikiHibernateStore.loadXWikiDoc requires a mutable docRef
    DocRefMutabilityExecutor<XWikiDocument> exec = new DocRefMutabilityExecutor<XWikiDocument>() {

      @Override
      public XWikiDocument call(XWikiDocument doc) throws XWikiException {
        return CelHibernateStore.super.loadXWikiDoc(doc, context);
      }
    };
    return exec.execute(doc);
  }

  /**
   * DocRefMutabilityExecutor is used to execute code for an {@link XWikiDocument} within
   * {@link #call(XWikiDocument)} with a mutable {@link DocumentReference} injected. it will be set
   * immutable again after execution.
   */
  private abstract class DocRefMutabilityExecutor<T> {

    public T execute(XWikiDocument doc) throws XWikiException {
      try {
        injectMutableDocRef(doc);
        return call(doc);
      } finally {
        injectImmutableDocRef(doc);
      }
    }

    protected abstract T call(XWikiDocument doc) throws XWikiException;

    private void injectMutableDocRef(XWikiDocument doc) {
      injectRef(doc, new DocumentReference(doc.getDocumentReference()));
    }

    private void injectImmutableDocRef(XWikiDocument doc) {
      DocumentReference docRef = new ImmutableDocumentReference(doc.getDocumentReference());
      injectRefInDocAndObjects(doc, docRef);
    }

    private void injectRefInDocAndObjects(XWikiDocument doc, DocumentReference docRef) {
      injectRef(doc, docRef);
      // inject reference in objects
      for (BaseObject obj : Iterables.concat(doc.getXObjects().values())) {
        if (obj != null) {
          obj.setDocumentReference(docRef);
        }
      }
      // inject reference in parent doc
      if (doc.getOriginalDocument() != null) {
        injectRefInDocAndObjects(doc.getOriginalDocument(), docRef);
      }
    }

    @SuppressWarnings("deprecation")
    private void injectRef(XWikiDocument doc, DocumentReference docRef) {
      boolean metaDataDirty = doc.isMetaDataDirty();
      // set invalid docRef first to circumvent equals check in setDocumentReference
      doc.setDocumentReference(new DocumentReference("$", "$", "$"));
      doc.setDocumentReference(docRef);
      doc.setMetaDataDirty(metaDataDirty); // is set true by setDocumentReference
    }

  }

}
