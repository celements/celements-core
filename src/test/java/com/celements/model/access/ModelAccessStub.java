package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.Map;

import org.mockftpserver.core.IllegalStateException;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class ModelAccessStub extends DefaultModelAccessFacade {

  public class InjectedDoc {

    XWikiDocument doc;
    long saved = 0;
    long deleted = 0;

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

    public long getDeletedCount() {
      return deleted;
    }

    public boolean wasDeleted() {
      return deleted > 0;
    }

  }

  public static ModelAccessStub get() {
    return init();
  }

  public static ModelAccessStub init() {
    try {
      ModelAccessStub ret;
      IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
      if (modelAccess instanceof ModelAccessStub) {
        ret = (ModelAccessStub) modelAccess;
      } else {
        ret = new ModelAccessStub();
        ret.execution = Utils.getComponent(Execution.class);
        ret.webUtils = Utils.getComponent(IWebUtilsService.class);
        ret.rightsAccess = Utils.getComponent(IRightsAccessFacadeRole.class);
        registerComponentMock(IModelAccessFacade.class, "default", ret);
      }
      return ret;
    } catch (ComponentRepositoryException exc) {
      throw new IllegalArgumentException("failed to register ModelAccessStub");
    }
  }

  private Map<DocumentReference, Map<String, InjectedDoc>> docInject = new HashMap<>();

  private Map<String, InjectedDoc> getInjectedDocs(DocumentReference docRef) {
    if (!docInject.containsKey(docRef)) {
      docInject.put(docRef, new HashMap<String, InjectedDoc>());
    }
    return docInject.get(docRef);
  }

  public InjectedDoc injectDoc(DocumentReference docRef, XWikiDocument doc) {
    return injectDoc(docRef, DEFAULT_LANG, doc);
  }

  public InjectedDoc injectDoc(DocumentReference docRef, String lang, XWikiDocument doc) {
    return getInjectedDocs(docRef).put(lang, new InjectedDoc(doc));
  }

  public boolean isInjected(DocumentReference docRef) {
    return isInjected(docRef, DEFAULT_LANG);
  }

  public boolean isInjected(DocumentReference docRef, String lang) {
    return getInjectedDocs(docRef).containsKey(lang);
  }

  public InjectedDoc getInjectedDoc(DocumentReference docRef) {
    return getInjectedDoc(docRef, DEFAULT_LANG);
  }

  public InjectedDoc getInjectedDoc(DocumentReference docRef, String lang) {
    if (isInjected(docRef, lang)) {
      return getInjectedDocs(docRef).get(lang);
    } else {
      throw new IllegalStateException("doc not injected: " + webUtils.serializeRef(docRef) + "-"
          + lang);
    }
  }

  @Override
  protected XWikiDocument getDocumentFromWiki(DocumentReference docRef) {
    return getInjectedDoc(docRef, DEFAULT_LANG).doc();
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
    injectDoc(docRef, new XWikiDocument(docRef));
    return super.createDocumentInternal(docRef);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    getInjectedDoc(doc.getDocumentReference(), doc.getLanguage()).saved++;
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    for (String lang : getInjectedDocs(doc.getDocumentReference()).keySet()) {
      getInjectedDoc(doc.getDocumentReference(), lang).deleted++;
    }
  }

  @Override
  public void deleteDocumentWithoutTranslations(XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException {
    getInjectedDoc(doc.getDocumentReference(), doc.getLanguage()).deleted++;
  }

  @Override
  public Map<String, XWikiDocument> getTranslations(DocumentReference docRef) {
    Map<String, XWikiDocument> tMap = new HashMap<>();
    for (String lang : getInjectedDocs(docRef).keySet()) {
      tMap.put(lang, getInjectedDocs(docRef).get(lang).doc());
    }
    return tMap;
  }

}
