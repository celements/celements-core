package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component(ModelAccessStub.NAME)
public class ModelAccessStub extends DefaultModelAccessFacade {

  public static final String NAME = "modelAccessStub";

  public static ModelAccessStub get() {
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    if (modelAccess instanceof ModelAccessStub) {
      return (ModelAccessStub) modelAccess;
    } else {
      return init();
    }
  }

  public static ModelAccessStub init() {
    try {
      ModelAccessStub modelAccess = (ModelAccessStub) Utils.getComponent(IModelAccessFacade.class,
          NAME);
      registerComponentMock(IModelAccessFacade.class, "default", modelAccess);
      return modelAccess;
    } catch (ComponentRepositoryException exc) {
      throw new RuntimeException("failed to register ModelAccessStub");
    }
  }

  private Map<DocumentReference, Map<String, InjectedDoc>> injDocs = new HashMap<>();

  private Map<String, InjectedDoc> getInjectedDocs(DocumentReference docRef) {
    if (!injDocs.containsKey(docRef)) {
      injDocs.put(docRef, new HashMap<String, InjectedDoc>());
    }
    return injDocs.get(docRef);
  }

  public boolean isInjected(DocumentReference docRef, String lang) {
    return getInjectedDocs(docRef).containsKey(lang);
  }

  public boolean isInjected(DocumentReference docRef) {
    return isInjected(docRef, DEFAULT_LANG);
  }

  public InjectedDoc getInjectedDoc(DocumentReference docRef, String lang) {
    checkIsInjected(docRef, lang);
    return getInjectedDocs(docRef).get(lang);
  }

  public InjectedDoc getInjectedDoc(DocumentReference docRef) {
    return getInjectedDoc(docRef, DEFAULT_LANG);
  }

  public void injectDoc(DocumentReference docRef, String lang, XWikiDocument doc) {
    checkIsNotInjected(docRef, lang);
    getInjectedDocs(docRef).put(lang, new InjectedDoc(doc));
  }

  public void injectDoc(DocumentReference docRef, XWikiDocument doc) {
    injectDoc(docRef, DEFAULT_LANG, doc);
  }

  public InjectedDoc removeInjectedDoc(DocumentReference docRef, String lang) {
    checkIsInjected(docRef, lang);
    return getInjectedDocs(docRef).remove(lang);
  }

  public InjectedDoc removeInjectedDoc(DocumentReference docRef) {
    return removeInjectedDoc(docRef, DEFAULT_LANG);
  }

  public void removeAllInjectedDocs() {
    injDocs.clear();
  }

  private void checkIsInjected(DocumentReference docRef, String lang) {
    Preconditions.checkState(isInjected(docRef, lang), "doc not injected: "
        + modelUtils.serializeRef(docRef) + "-" + lang);
  }

  private void checkIsNotInjected(DocumentReference docRef, String lang) {
    Preconditions.checkState(!isInjected(docRef, lang), "doc already injected: "
        + modelUtils.serializeRef(docRef) + "-" + lang);
  }

  @Override
  @Deprecated
  protected XWikiDocument getDocumentFromWiki(DocumentReference docRef) {
    return getDocumentFromStore(docRef, DEFAULT_LANG);
  }

  @Override
  protected XWikiDocument getDocumentFromStore(DocumentReference docRef, String lang) {
    return getInjectedDoc(docRef, lang).doc();
  }

  @Override
  protected boolean existsFromWiki(DocumentReference docRef) {
    return isInjected(docRef);
  }

  @Override
  protected boolean existsFromStore(DocumentReference docRef, String lang) {
    return isInjected(docRef, lang);
  }

  @Override
  protected XWikiDocument createDocumentInternal(DocumentReference docRef) {
    XWikiDocument doc = super.createDocumentInternal(docRef);
    injectDoc(docRef, doc);
    return doc;
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    expectSave(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectSave(DocumentReference docRef, String lang) throws DocumentSaveException {
    InjectedDoc injDoc = getInjectedDoc(docRef, lang);
    if (!injDoc.throwSaveExc) {
      injDoc.saved++;
    } else {
      throw new DocumentSaveException(injDoc.doc().getDocumentReference());
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    for (String lang : getInjectedDocs(doc.getDocumentReference()).keySet()) {
      expectDelete(doc.getDocumentReference(), lang);
    }
  }

  @Override
  public void deleteDocumentWithoutTranslations(XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException {
    expectDelete(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectDelete(DocumentReference docRef, String lang) throws DocumentDeleteException {
    InjectedDoc injDoc = getInjectedDoc(docRef, lang);
    if (!injDoc.throwDeleteExc) {
      injDoc.deleted++;
    } else {
      throw new DocumentDeleteException(injDoc.doc().getDocumentReference());
    }
  }

  @Override
  public Map<String, XWikiDocument> getTranslations(DocumentReference docRef) {
    Map<String, XWikiDocument> tMap = new HashMap<>();
    for (String lang : getInjectedDocs(docRef).keySet()) {
      tMap.put(lang, getInjectedDocs(docRef).get(lang).doc());
    }
    return tMap;
  }

  public class InjectedDoc {

    XWikiDocument doc;

    long saved;
    boolean throwSaveExc;

    long deleted;
    boolean throwDeleteExc;

    InjectedDoc(XWikiDocument doc) {
      this.doc = checkNotNull(doc);
    }

    public XWikiDocument doc() {
      return doc;
    }

    public long getSavedCount() {
      return saved;
    }

    public boolean wasSaved() {
      return saved > 0;
    }

    public void setThrowSaveException(boolean setThrow) {
      throwSaveExc = true;
    }

    public long getDeletedCount() {
      return deleted;
    }

    public boolean wasDeleted() {
      return deleted > 0;
    }

    public void setThrowDeleteException(boolean setThrow) {
      throwDeleteExc = true;
    }

  }

}
