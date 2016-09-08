package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component(ModelAccessStub.NAME)
public class ModelAccessStub implements ModelAccessStrategy {

  public static final String NAME = "modelAccessStub";

  @Requirement
  private XWikiDocumentCreator docCreator;

  @Requirement
  private ModelUtils modelUtils;

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
      registerComponentMock(ModelAccessStrategy.class, "default", modelAccess);
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

  public InjectedDoc injectDoc(DocumentReference docRef, String lang, XWikiDocument doc) {
    checkIsNotInjected(docRef, lang);
    InjectedDoc injDoc = new InjectedDoc(doc);
    getInjectedDocs(docRef).put(lang, injDoc);
    return injDoc;
  }

  public InjectedDoc injectDoc(DocumentReference docRef, XWikiDocument doc) {
    return injectDoc(docRef, DEFAULT_LANG, doc);
  }

  public InjectedDoc injectNewDoc(DocumentReference docRef) {
    return injectDoc(docRef, createDocument(docRef));
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
  public XWikiDocument getDocument(DocumentReference docRef, String lang) {
    return getInjectedDoc(docRef, lang).doc();
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef) {
    return docCreator.createWithoutDefaults(docRef);
  }

  @Override
  public boolean exists(DocumentReference docRef, String lang) {
    return isInjected(docRef, lang);
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
  public List<String> getTranslations(DocumentReference docRef) {
    return new ArrayList<>(getInjectedDocs(docRef).keySet());
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
