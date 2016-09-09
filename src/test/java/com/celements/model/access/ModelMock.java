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

@Component(ModelMock.NAME)
public class ModelMock implements ModelAccessStrategy {

  public static final String NAME = "modelAccessMock";

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
      ModelMock modelAccess = (ModelMock) Utils.getComponent(ModelAccessStrategy.class, NAME);
      registerComponentMock(ModelAccessStrategy.class, "default", modelAccess);
      return modelAccess;
    } catch (ComponentRepositoryException exc) {
      throw new RuntimeException("failed to register ModelAccessStub");
    }
  }

  private Map<DocumentReference, Map<String, MockDoc>> mockDocs = new HashMap<>();

  private Map<String, MockDoc> getMockedDocs(DocumentReference docRef) {
    if (!mockDocs.containsKey(docRef)) {
      mockDocs.put(docRef, new HashMap<String, MockDoc>());
    }
    return mockDocs.get(docRef);
  }

  public boolean isMocked(DocumentReference docRef, String lang) {
    return getMockedDocs(docRef).containsKey(lang);
  }

  public boolean isMocked(DocumentReference docRef) {
    return isMocked(docRef, DEFAULT_LANG);
  }

  public MockDoc getMockedDoc(DocumentReference docRef, String lang) {
    checkIsMocked(docRef, lang);
    return getMockedDocs(docRef).get(lang);
  }

  public MockDoc getMockedDoc(DocumentReference docRef) {
    return getMockedDoc(docRef, DEFAULT_LANG);
  }

  public MockDoc mockDoc(DocumentReference docRef, String lang, XWikiDocument doc) {
    checkIsNotMocked(docRef, lang);
    MockDoc mockDoc = new MockDoc(doc);
    getMockedDocs(docRef).put(lang, mockDoc);
    return mockDoc;
  }

  public MockDoc mockDoc(DocumentReference docRef, XWikiDocument doc) {
    return mockDoc(docRef, DEFAULT_LANG, doc);
  }

  public MockDoc mockDoc(DocumentReference docRef) {
    return mockDoc(docRef, createDocument(docRef, DEFAULT_LANG));
  }

  public MockDoc removeMockedDoc(DocumentReference docRef, String lang) {
    checkIsMocked(docRef, lang);
    return getMockedDocs(docRef).remove(lang);
  }

  public MockDoc removeMockedDoc(DocumentReference docRef) {
    return removeMockedDoc(docRef, DEFAULT_LANG);
  }

  public void removeAllMockedDocs() {
    mockDocs.clear();
  }

  private void checkIsMocked(DocumentReference docRef, String lang) {
    Preconditions.checkState(isMocked(docRef, lang), "doc not mocked: " + modelUtils.serializeRef(
        docRef) + "-" + lang);
  }

  private void checkIsNotMocked(DocumentReference docRef, String lang) {
    Preconditions.checkState(!isMocked(docRef, lang), "doc already mocked: "
        + modelUtils.serializeRef(docRef) + "-" + lang);
  }

  @Override
  public XWikiDocument getDocument(DocumentReference docRef, String lang) {
    return getMockedDoc(docRef, lang).doc();
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef, String lang) {
    return docCreator.createWithoutDefaults(docRef, lang);
  }

  @Override
  public boolean exists(DocumentReference docRef, String lang) {
    return isMocked(docRef, lang);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    expectSave(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectSave(DocumentReference docRef, String lang) throws DocumentSaveException {
    MockDoc mockDoc = getMockedDoc(docRef, lang);
    if (!mockDoc.throwSaveExc) {
      mockDoc.saved++;
    } else {
      throw new DocumentSaveException(mockDoc.doc().getDocumentReference());
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    expectDelete(doc.getDocumentReference(), doc.getLanguage());
  }

  private void expectDelete(DocumentReference docRef, String lang) throws DocumentDeleteException {
    MockDoc mockDoc = getMockedDoc(docRef, lang);
    if (!mockDoc.throwDeleteExc) {
      mockDoc.deleted++;
    } else {
      throw new DocumentDeleteException(mockDoc.doc().getDocumentReference());
    }
  }

  @Override
  public List<String> getTranslations(DocumentReference docRef) {
    return new ArrayList<>(getMockedDocs(docRef).keySet());
  }

  public class MockDoc {

    XWikiDocument doc;

    long saved;
    boolean throwSaveExc;

    long deleted;
    boolean throwDeleteExc;

    MockDoc(XWikiDocument doc) {
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
