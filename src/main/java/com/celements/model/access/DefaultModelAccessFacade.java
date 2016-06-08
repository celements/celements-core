package com.celements.model.access;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.access.exception.TranslationNotExistsException;
import com.celements.model.util.XObjectField;
import com.celements.model.util.XObjectFieldValue;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

@Component
public class DefaultModelAccessFacade implements IModelAccessFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelAccessFacade.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public XWikiDocument getDocument(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException {
    checkNotNull(docRef);
    if (exists(docRef)) {
      return getDocumentInternal(docRef);
    } else {
      throw new DocumentNotExistsException(docRef);
    }
  }

  /**
   * TODO unit test
   */
  @Override
  public XWikiDocument getDocument(DocumentReference docRef, String lang)
      throws DocumentLoadException, DocumentNotExistsException, TranslationNotExistsException {
    checkNotNull(docRef);
    checkState(!Strings.isNullOrEmpty(lang));
    XWikiDocument translatedDocument = getDocumentForReadOnly(docRef);
    String defaultLanguage = webUtilsService.getDefaultLanguage(docRef.getLastSpaceReference());
    String docDefLang = Strings.nullToEmpty(translatedDocument.getDefaultLanguage());
    if (!lang.equals(docDefLang) && (!"".equals(docDefLang) || !lang.equals(defaultLanguage))) {
      try {
        if (translatedDocument.getTranslationList(getContext()).contains(lang)) {
          translatedDocument = translatedDocument.getTranslatedDocument(lang, getContext());
        } else {
          throw new TranslationNotExistsException(docRef, lang);
        }
      } catch (XWikiException xwe) {
        throw new DocumentLoadException(docRef, xwe);
      }
    }
    // We need to clone this document first, since a cached storage would return the same
    // object for the
    // following requests, so concurrent request might get a partially modified object, or
    // worse, if an error
    // occurs during the save, the cached object will not reflect the actual document at
    // all.
    return translatedDocument.clone();
  }

  @Override
  public Document getApiDocument(XWikiDocument doc) throws NoAccessRightsException {
    if (rightsAccess.hasAccessLevel(doc.getDocumentReference(), EAccessLevel.VIEW)) {
      return doc.newDocument(getContext());
    }
    throw new NoAccessRightsException(doc.getDocumentReference(), getContext().getXWikiUser(),
        EAccessLevel.VIEW);
  }

  /**
   * returns an editable document
   */
  private XWikiDocument getDocumentInternal(DocumentReference docRef) throws DocumentLoadException {
    // We need to clone this document first, since a cached storage would return the same
    // object for the
    // following requests, so concurrent request might get a partially modified object, or
    // worse, if an error
    // occurs during the save, the cached object will not reflect the actual document at
    // all.
    return getDocumentInternalForReadOnly(docRef).clone();
  }

  /**
   * returns an xwiki document for readonly usage CAUTION: never ever change anything on
   * the returned XWikiDocument, because it is the object in cache. Thus the same object
   * will be returned for the following requests. If you change this object, concurrent
   * request might get a partially modified object, or worse, if an error occurs during
   * the save (or no save call happens), the cached object will not reflect the actual
   * document at all.
   */
  private XWikiDocument getDocumentInternalForReadOnly(DocumentReference docRef)
      throws DocumentLoadException {
    try {
      return getContext().getWiki().getDocument(docRef, getContext());
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  /**
   * returns an xwiki document for readonly usage CAUTION: never ever change anything on
   * the returned XWikiDocument, because it is the object in cache. Thus the same object
   * will be returned for the following requests. If you change this object, concurrent
   * request might get a partially modified object, or worse, if an error occurs during
   * the save (or no save call happens), the cached object will not reflect the actual
   * document at all.
   */
  private XWikiDocument getDocumentForReadOnly(DocumentReference docRef)
      throws DocumentLoadException, DocumentNotExistsException {
    checkNotNull(docRef);
    if (exists(docRef)) {
      return getDocumentInternalForReadOnly(docRef);
    } else {
      throw new DocumentNotExistsException(docRef);
    }
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef) throws DocumentLoadException,
      DocumentAlreadyExistsException {
    checkNotNull(docRef);
    if (!exists(docRef)) {
      return createDocumentInternal(docRef);
    } else {
      throw new DocumentAlreadyExistsException(docRef);
    }
  }

  private XWikiDocument createDocumentInternal(DocumentReference docRef)
      throws DocumentLoadException {
    XWikiDocument doc = getDocumentInternal(docRef);
    Date creationDate = new Date();
    doc.setDefaultLanguage(webUtilsService.getDefaultLanguage());
    doc.setLanguage("");
    doc.setCreationDate(creationDate);
    doc.setContentUpdateDate(creationDate);
    doc.setDate(creationDate);
    doc.setCreator(getContext().getUser());
    doc.setAuthor(getContext().getUser());
    doc.setTranslation(0);
    doc.setContent("");
    doc.setMetaDataDirty(true);
    return doc;
  }

  @Override
  public XWikiDocument getOrCreateDocument(DocumentReference docRef) throws DocumentLoadException {
    checkNotNull(docRef);
    if (exists(docRef)) {
      return getDocumentInternal(docRef);
    } else {
      return createDocumentInternal(docRef);
    }
  }

  @Override
  public boolean exists(DocumentReference docRef) {
    boolean exists = false;
    if (docRef != null) {
      exists = getContext().getWiki().exists(docRef, getContext());
    }
    return exists;
  }

  @Override
  public void saveDocument(XWikiDocument doc) throws DocumentSaveException {
    checkNotNull(doc);
    saveDocument(doc, "", false);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment) throws DocumentSaveException {
    checkNotNull(doc);
    saveDocument(doc, comment, false);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    checkNotNull(doc);
    try {
      String username = getContext().getUser();
      doc.setAuthor(username);
      if (doc.isNew()) {
        doc.setCreator(username);
      }
      getContext().getWiki().saveDocument(doc, comment, isMinorEdit, getContext());
    } catch (XWikiException xwe) {
      throw new DocumentSaveException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public void deleteDocument(DocumentReference docRef, boolean totrash)
      throws DocumentLoadException, DocumentDeleteException {
    try {
      deleteDocument(getDocument(docRef), totrash);
    } catch (DocumentNotExistsException exc) {
      LOGGER.debug("doc trying to delete does not exist '{}'", docRef, exc);
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    checkNotNull(doc);
    List<XWikiDocument> toDelDocs = new ArrayList<>();
    try {
      for (String lang : doc.getTranslationList(getContext())) {
        XWikiDocument tdoc = doc.getTranslatedDocument(lang, getContext());
        if ((tdoc != null) && (tdoc != doc)) {
          toDelDocs.add(tdoc);
        }
      }
    } catch (XWikiException xwe) {
      throw new DocumentDeleteException(doc.getDocumentReference(), xwe);
    }
    toDelDocs.add(doc);
    for (XWikiDocument toDel : toDelDocs) {
      deleteDocumentWithoutTranslations(toDel, totrash);
    }
  }

  @Override
  public void deleteDocumentWithoutTranslations(XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException {
    String dbBefore = getContext().getDatabase();
    try {
      getContext().setDatabase(webUtilsService.getWikiRef(doc).getName());
      LOGGER.debug("deleteDocument: doc '{},{}', totrash '{}' dbBefore '{}' dbNow '{}'", doc,
          doc.getLanguage(), totrash, dbBefore, getContext().getDatabase());
      try {
        getContext().getWiki().deleteDocument(doc, totrash, getContext());
      } catch (XWikiException xwe) {
        throw new DocumentDeleteException(doc.getDocumentReference(), xwe);
      }
    } finally {
      getContext().setDatabase(dbBefore);
    }
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws DocumentLoadException, DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentForReadOnly(docRef), classRef), null);
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef, String key,
      Object value) throws DocumentLoadException, DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentForReadOnly(docRef), classRef, key, value),
        null);
  }

  @Override
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef) {
    return Iterables.getFirst(getXObjects(doc, classRef), null);
  }

  @Override
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    return Iterables.getFirst(getXObjects(doc, classRef, key, value), null);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef)
      throws DocumentLoadException, DocumentNotExistsException {
    return getXObjects(getDocumentForReadOnly(docRef), classRef);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentLoadException, DocumentNotExistsException {
    return getXObjects(getDocumentForReadOnly(docRef), classRef, key, value);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentLoadException, DocumentNotExistsException {
    return getXObjects(getDocumentForReadOnly(docRef), classRef, key, values);
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef) {
    return getXObjects(doc, classRef, null, null);
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    return getXObjects(doc, classRef, key, Arrays.asList(value));
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values) {
    checkNotNull(doc);
    checkNotNull(classRef);
    classRef = webUtilsService.checkWikiRef(classRef, doc);
    List<BaseObject> ret = new ArrayList<>();
    for (BaseObject obj : MoreObjects.firstNonNull(doc.getXObjects(classRef),
        ImmutableList.<BaseObject>of())) {
      if ((obj != null) && checkPropertyKeyValues(obj, key, values)) {
        ret.add(obj);
      }
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  public Map<DocumentReference, List<BaseObject>> getXObjects(XWikiDocument doc) {
    Map<DocumentReference, List<BaseObject>> ret = new HashMap<>();
    for (DocumentReference classRef : doc.getXObjects().keySet()) {
      List<BaseObject> objs = getXObjects(doc, classRef);
      if (!objs.isEmpty()) {
        ret.put(classRef, objs);
      }
    }
    return Collections.unmodifiableMap(ret);
  }

  private boolean checkPropertyKeyValues(BaseObject obj, String key, Collection<?> checkValues) {
    boolean valid = (key == null);
    if (!valid) {
      checkValues = MoreObjects.firstNonNull(checkValues, Collections.emptyList());
      Object val = getProperty(obj, key);
      for (Object checkVal : checkValues) {
        valid |= Objects.equal(val, checkVal);
      }
    }
    return valid;
  }

  @Override
  public com.xpn.xwiki.api.Object getApiObject(BaseObject obj) throws NoAccessRightsException {
    com.xpn.xwiki.api.Object ret = null;
    if (obj != null) {
      try {
        if (rightsAccess.hasAccessLevel(obj.getDocumentReference(), EAccessLevel.VIEW)) {
          return getApiObjectWithoutRightCheck(obj);
        } else {
          throw new NoAccessRightsException(obj.getDocumentReference(), getContext().getXWikiUser(),
              EAccessLevel.VIEW);
        }
      } catch (IllegalStateException exp) {
        LOGGER.warn("getApiObject failed for '{}'", obj, exp);
      }
    }
    return ret;
  }

  @Override
  public com.xpn.xwiki.api.Object getApiObjectWithoutRightCheck(BaseObject obj) {
    return obj.newObjectApi(obj, getContext());
  }

  @Override
  public List<com.xpn.xwiki.api.Object> getApiObjects(List<BaseObject> objs) {
    List<com.xpn.xwiki.api.Object> ret = new ArrayList<>();
    for (BaseObject obj : objs) {
      try {
        if (obj != null) {
          com.xpn.xwiki.api.Object apiObject = getApiObject(obj);
          if (apiObject != null) {
            ret.add(apiObject);
          }
        }
      } catch (NoAccessRightsException exp) {
        LOGGER.debug("getApiObjects ommits object '{}'", obj, exp);
      }
    }
    return ret;
  }

  @Override
  public List<com.xpn.xwiki.api.Object> getApiObjectsWithoutRightChecks(List<BaseObject> objs) {
    List<com.xpn.xwiki.api.Object> ret = new ArrayList<>();
    for (BaseObject obj : objs) {
      if (obj != null) {
        com.xpn.xwiki.api.Object apiObject = getApiObjectWithoutRightCheck(obj);
        if (apiObject != null) {
          ret.add(apiObject);
        }
      }
    }
    return ret;
  }

  @Override
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef)
      throws ClassDocumentLoadException {
    checkNotNull(doc);
    checkNotNull(classRef);
    classRef = webUtilsService.checkWikiRef(classRef, doc);
    try {
      return doc.newXObject(classRef, getContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classRef, xwe);
    }
  }

  @Override
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef)
      throws ClassDocumentLoadException {
    return getOrCreateXObject(doc, classRef, null, null);
  }

  @Override
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) throws ClassDocumentLoadException {
    BaseObject obj = getXObject(doc, classRef, key, value);
    if (obj == null) {
      obj = newXObject(doc, classRef);
      if (key != null) {
        setProperty(obj, key, value);
      }
    }
    return obj;
  }

  @Override
  public boolean removeXObject(XWikiDocument doc, BaseObject objToRemove) {
    return removeXObjects(doc, Arrays.asList(objToRemove));
  }

  @Override
  public boolean removeXObjects(XWikiDocument doc, List<BaseObject> objsToRemove) {
    checkNotNull(doc);
    boolean changed = false;
    for (BaseObject obj : new ArrayList<>(objsToRemove)) {
      if (obj != null) {
        changed |= doc.removeXObject(obj);
      }
    }
    return changed;
  }

  @Override
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef) {
    return removeXObjects(doc, classRef, null, null);
  }

  @Override
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    return removeXObjects(doc, classRef, key, Arrays.asList(value));
  }

  @Override
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values) {
    return removeXObjects(doc, getXObjects(doc, classRef, key, values));
  }

  @Override
  public Object getProperty(DocumentReference docRef, DocumentReference classRef, String name)
      throws DocumentLoadException, DocumentNotExistsException {
    return getProperty(getDocumentForReadOnly(docRef), classRef, name);
  }

  @Override
  public Object getProperty(XWikiDocument doc, DocumentReference classRef, String name) {
    return getProperty(getXObject(doc, classRef), name);
  }

  @Override
  public Object getProperty(BaseObject obj, String name) {
    Object value = null;
    BaseProperty prop = getBaseProperty(obj, name);
    if (prop != null) {
      value = prop.getValue();
      if (value instanceof String) {
        // avoid comparing empty string to null
        value = Strings.emptyToNull(value.toString().trim());
      } else if (value instanceof Date) {
        // avoid returning Timestamp since Timestamp.equals(Date) always returns false
        value = new Date(((Date) value).getTime());
      }
    }
    return value;
  }

  private BaseProperty getBaseProperty(BaseObject obj, String key) {
    BaseProperty prop = null;
    try {
      if (obj != null) {
        prop = (BaseProperty) obj.get(key);
      }
    } catch (XWikiException | ClassCastException exc) {
      // does not happen since
      // XWikiException is never thrown in BaseObject.get()
      // BaseObject only contains BaseProperties
      LOGGER.error("should not happen", exc);
    }
    return prop;
  }

  @Override
  public <T> T getProperty(DocumentReference docRef, XObjectField<T> field)
      throws DocumentLoadException, DocumentNotExistsException {
    return field.resolveFromXOjectValue(getProperty(docRef, field.getClassRef(), field.getName()));
  }

  @Override
  public <T> T getProperty(XWikiDocument doc, XObjectField<T> field) {
    return field.resolveFromXOjectValue(getProperty(doc, field.getClassRef(), field.getName()));
  }

  @Override
  public boolean setProperty(BaseObject obj, String name, Object value) {
    boolean hasChange = !Objects.equal(value, getProperty(obj, name));
    if (hasChange) {
      if (value instanceof Collection) {
        value = Joiner.on('|').join((Iterable<?>) value);
      }
      obj.set(name, value, getContext());
    }
    return hasChange;
  }

  @Override
  public <T> XWikiDocument setProperty(DocumentReference docRef, XObjectFieldValue<T> field)
      throws DocumentLoadException, DocumentNotExistsException {
    XWikiDocument doc = getDocument(docRef);
    setProperty(doc, field);
    return doc;
  }

  @Override
  public <T> boolean setProperty(XWikiDocument doc, XObjectFieldValue<T> field)
      throws ClassDocumentLoadException {
    try {
      return setProperty(getOrCreateXObject(doc, field.getClassRef()), field.getName(),
          field.serializeToXObjectValue());
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException("XObjectField ill defined: " + field, ex);
    }
  }

  @Override
  public XWikiAttachment getAttachmentNameEqual(XWikiDocument document, String filename)
      throws AttachmentNotExistsException {
    for (XWikiAttachment attach : document.getAttachmentList()) {
      if ((attach != null) && attach.getFilename().equals(filename)) {
        return attach;
      }
    }
    LOGGER.debug("getAttachmentNameEqual: not found! file: [{}], doc: [{}], docref: [{}]", filename,
        document, document.getDocumentReference());
    // FIXME empty or null filename leads to exception:
    // java.lang.IllegalArgumentException: An Entity Reference name cannot be null or
    // empty
    if (Strings.isNullOrEmpty(filename)) {
      throw new AttachmentNotExistsException(null);
    } else {
      throw new AttachmentNotExistsException(new AttachmentReference(filename,
          document.getDocumentReference()));
    }
  }

}
