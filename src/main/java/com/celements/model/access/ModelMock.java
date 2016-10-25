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
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component(ModelMock.NAME)
public class ModelMock implements ModelAccessStrategy {

  public static final String NAME = "modelMock";

  @Requirement
  private XWikiDocumentCreator docCreator;

  @Requirement
  private ModelUtils modelUtils;

  public static ModelMock get() {
    ModelAccessStrategy modelAccess = Utils.getComponent(ModelAccessStrategy.class);
    if (modelAccess instanceof ModelMock) {
      return (ModelMock) modelAccess;
    } else {
      return init();
    }
  }

  public static ModelMock init() {
    try {
      ModelMock modelMock = new ModelMock();
      registerComponentMock(ModelAccessStrategy.class, "default", modelMock);
      return modelMock;
    } catch (ComponentRepositoryException exc) {
      throw new RuntimeException("failed to register ModelAccessStub");
    }
  }

  private Map<DocumentReference, Map<String, DocRecord>> docs = new HashMap<>();

  private Map<String, DocRecord> getDocs(DocumentReference docRef) {
    if (!docs.containsKey(docRef)) {
      docs.put(docRef, new HashMap<String, DocRecord>());
    }
    return docs.get(docRef);
  }

  public boolean isRegistered(DocumentReference docRef, String lang) {
    return getDocs(docRef).containsKey(lang);
  }

  public boolean isRegistered(DocumentReference docRef) {
    return isRegistered(docRef, IModelAccessFacade.DEFAULT_LANG);
  }

  public DocRecord getDocRecord(DocumentReference docRef, String lang) {
    checkIsRegistered(docRef, lang);
    return getDocs(docRef).get(lang);
  }

  public DocRecord getDocRecord(DocumentReference docRef) {
    return getDocRecord(docRef, IModelAccessFacade.DEFAULT_LANG);
  }

  public DocRecord registerDoc(DocumentReference docRef, String lang, XWikiDocument doc) {
    checkIsNotRegistered(docRef, lang);
    DocRecord mockDoc = new DocRecord(doc);
    getDocs(docRef).put(lang, mockDoc);
    return mockDoc;
  }

  public DocRecord registerDoc(DocumentReference docRef, XWikiDocument doc) {
    return registerDoc(docRef, IModelAccessFacade.DEFAULT_LANG, doc);
  }

  public DocRecord registerDoc(DocumentReference docRef, String lang) {
    return registerDoc(docRef, lang, docCreator.createWithoutDefaults(docRef, lang));
  }

  public DocRecord registerDoc(DocumentReference docRef) {
    return registerDoc(docRef, IModelAccessFacade.DEFAULT_LANG);
  }

  public DocRecord removeRegisteredDoc(DocumentReference docRef, String lang) {
    checkIsRegistered(docRef, lang);
    return getDocs(docRef).remove(lang);
  }

  public DocRecord removeRegisteredDoc(DocumentReference docRef) {
    return removeRegisteredDoc(docRef, IModelAccessFacade.DEFAULT_LANG);
  }

  public void removeAllRegisteredDocs() {
    docs.clear();
  }

  private void checkIsRegistered(DocumentReference docRef, String lang) {
    Preconditions.checkState(isRegistered(docRef, lang), getErrorMsg("doc not registered", docRef,
        lang));
  }

  private void checkIsNotRegistered(DocumentReference docRef, String lang) {
    Preconditions.checkState(!isRegistered(docRef, lang), getErrorMsg("doc already registered",
        docRef, lang));
  }

  private StringBuilder getErrorMsg(String base, DocumentReference docRef, String lang) {
    StringBuilder sb = new StringBuilder(base).append(": ").append(modelUtils.serializeRef(docRef));
    if (!Strings.isNullOrEmpty(lang)) {
      sb.append("-").append(lang);
    }
    return sb;
  }

  @Override
  public boolean exists(DocumentReference docRef, String lang) {
    return isRegistered(docRef, lang);
  }

  @Override
  public XWikiDocument getDocument(DocumentReference docRef, String lang) {
    return getDocRecord(docRef, lang).doc();
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef, String lang) {
    return registerDoc(docRef, lang).doc();
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    expectSave(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectSave(DocumentReference docRef, String lang) throws DocumentSaveException {
    DocRecord record = getDocRecord(docRef, lang);
    if (!record.throwSaveExc) {
      record.saved++;
    } else {
      throw new DocumentSaveException(record.doc().getDocumentReference());
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    expectDelete(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectDelete(DocumentReference docRef, String lang) throws DocumentDeleteException {
    DocRecord record = getDocRecord(docRef, lang);
    if (!record.throwDeleteExc) {
      record.deleted++;
    } else {
      throw new DocumentDeleteException(record.doc().getDocumentReference());
    }
  }

  @Override
  public List<String> getTranslations(DocumentReference docRef) {
    return new ArrayList<>(getDocs(docRef).keySet());
  }

  public class DocRecord {

    XWikiDocument doc;

    long saved;
    boolean throwSaveExc;

    long deleted;
    boolean throwDeleteExc;

    DocRecord(XWikiDocument doc) {
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

    @Override
    public String toString() {
      return "DocRecord [doc=" + doc() + ", savedCount=" + getSavedCount() + ", deletedCount="
          + getDeletedCount() + "]";
    }

  }

}
