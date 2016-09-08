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
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.access.exception.ModelAccessRuntimeException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ClassFieldValue;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.util.Util;

@Component
public class DefaultModelAccessFacade implements IModelAccessFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelAccessFacade.class);

  @Requirement
  protected ModelAccessStrategy strategy;

  @Requirement
  protected IRightsAccessFacadeRole rightsAccess;

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ModelContext context;

  @Override
  public XWikiDocument getDocument(DocumentReference docRef) throws DocumentNotExistsException {
    return getDocument(docRef, DEFAULT_LANG);
  }

  @Override
  public XWikiDocument getDocument(DocumentReference docRef, String lang)
      throws DocumentNotExistsException {
    return cloneDoc(getDocumentReadOnly(docRef, lang));
  }

  @Override
  public Document getApiDocument(XWikiDocument doc) throws NoAccessRightsException {
    if (rightsAccess.hasAccessLevel(doc.getDocumentReference(), EAccessLevel.VIEW)) {
      return doc.newDocument(context.getXWikiContext());
    }
    throw new NoAccessRightsException(doc.getDocumentReference(), context.getUser(),
        EAccessLevel.VIEW);
  }

  /**
   * CAUTION: never ever change anything on the returned XWikiDocument, because it is the object in
   * cache. Thus the same object will be returned for the following requests. If you change this
   * object, concurrent request might get a partially modified object, or worse, if an error occurs
   * during the save (or no save call happens), the cached object will not reflect the actual
   * document at all.
   *
   * @param docRef
   * @param lang
   * @return an xwiki document for readonly usage
   * @throws DocumentNotExistsException
   */
  private XWikiDocument getDocumentReadOnly(DocumentReference docRef, String lang)
      throws DocumentNotExistsException {
    checkNotNull(docRef);
    lang = normalizeLang(lang);
    if (exists(docRef, lang)) {
      return strategy.getDocument(docRef, lang);
    } else {
      throw new DocumentNotExistsException(docRef);
    }
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef)
      throws DocumentAlreadyExistsException {
    checkNotNull(docRef);
    if (!exists(docRef, DEFAULT_LANG)) {
      return strategy.createDocument(docRef, DEFAULT_LANG);
    } else {
      throw new DocumentAlreadyExistsException(docRef);
    }
  }

  @Override
  public XWikiDocument getOrCreateDocument(DocumentReference docRef) {
    try {
      return getDocument(docRef, DEFAULT_LANG);
    } catch (DocumentNotExistsException exc) {
      return strategy.createDocument(docRef, DEFAULT_LANG);
    }
  }

  @Override
  public boolean exists(DocumentReference docRef) {
    return exists(docRef, DEFAULT_LANG);
  }

  @Override
  public boolean exists(DocumentReference docRef, String lang) {
    boolean exists = false;
    if (docRef != null) {
      lang = normalizeLang(lang);
      exists = strategy.exists(docRef, lang);
    }
    return exists;
  }

  @Override
  public void saveDocument(XWikiDocument doc) throws DocumentSaveException {
    saveDocument(doc, "", false);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment) throws DocumentSaveException {
    saveDocument(doc, comment, false);
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws DocumentSaveException {
    checkNotNull(doc);
    String username = context.getUserName();
    doc.setAuthor(username);
    if (doc.isNew()) {
      doc.setCreator(username);
    }
    strategy.saveDocument(doc, comment, isMinorEdit);
  }

  @Override
  public void deleteDocument(DocumentReference docRef, boolean totrash)
      throws DocumentDeleteException {
    try {
      deleteDocument(getDocument(docRef), totrash);
    } catch (DocumentNotExistsException exc) {
      LOGGER.debug("doc trying to delete does not exist '{}'", docRef, exc);
    } catch (ModelAccessRuntimeException exc) {
      throw new DocumentDeleteException(docRef, exc);
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash) throws DocumentDeleteException {
    checkNotNull(doc);
    List<XWikiDocument> toDelDocs = new ArrayList<>();
    try {
      for (String lang : doc.getTranslationList(context.getXWikiContext())) {
        XWikiDocument tdoc = doc.getTranslatedDocument(lang, context.getXWikiContext());
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
    checkNotNull(doc);
    LOGGER.debug("deleteDocument: doc '{}, {}', totrash '{}'", doc, doc.getLanguage(), totrash);
    strategy.deleteDocument(doc, totrash);

  }

  @Override
  public Map<String, XWikiDocument> getTranslations(DocumentReference docRef) {
    Map<String, XWikiDocument> transMap = new HashMap<>();
    for (String lang : strategy.getTranslations(docRef)) {
      lang = normalizeLang(lang);
      try {
        transMap.put(lang, getDocument(docRef, lang));
      } catch (DocumentNotExistsException exc) {
        LOGGER.error("failed to load existing translation '{}' for doc '{}'", lang, docRef, exc);
      }
    }
    return transMap;
  }

  @Override
  public boolean isTranslation(XWikiDocument doc) {
    return checkNotNull(doc).getTranslation() == 1;
  }

  /**
   * We need to clone this document first, since a cached storage would return the same object for
   * the following requests, so concurrent request might get a partially modified object, or worse,
   * if an error occurs during the save, the cached object will not reflect the actual document at
   * all.
   */
  private XWikiDocument cloneDoc(XWikiDocument doc) {
    if (doc.isFromCache()) {
      XWikiDocument clonedDoc = doc.clone();
      clonedDoc.setFromCache(false);
      return clonedDoc;
    }
    return doc;
  }

  private String normalizeLang(String lang) {
    lang = Util.normalizeLanguage(lang);
    lang = Strings.nullToEmpty(lang);
    if ("default".equals(lang)) {
      lang = "";
    }
    return lang;
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef),
        null);
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef, String key,
      Object value) throws DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key,
        value), null);
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
      throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key, value);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key, values);
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
    checkNotNull(classRef);
    checkState(!isTranslation(doc));
    classRef = adjustClassRef(classRef, doc);
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
    checkState(!isTranslation(doc));
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
          throw new NoAccessRightsException(obj.getDocumentReference(), context.getUser(),
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
    return obj.newObjectApi(obj, context.getXWikiContext());
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
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef) {
    checkNotNull(doc);
    checkNotNull(classRef);
    classRef = adjustClassRef(classRef, doc);
    try {
      return doc.newXObject(classRef, context.getXWikiContext());
    } catch (XWikiException xwe) {
      throw new ClassDocumentLoadException(classRef, xwe);
    }
  }

  @Override
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef) {
    return getOrCreateXObject(doc, classRef, null, null);
  }

  @Override
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
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
      throws DocumentNotExistsException {
    return getProperty(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, name);
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
  public <T> T getProperty(DocumentReference docRef, ClassField<T> field)
      throws DocumentNotExistsException {
    return resolvePropertyValue(field, getProperty(docRef, field.getClassDef().getClassRef(),
        field.getName()));
  }

  @Override
  public <T> T getProperty(XWikiDocument doc, ClassField<T> field) {
    return resolvePropertyValue(field, getProperty(doc, field.getClassDef().getClassRef(),
        field.getName()));
  }

  private <T> T resolvePropertyValue(ClassField<T> field, Object value) {
    try {
      if (field instanceof CustomClassField) {
        return ((CustomClassField<T>) field).resolve(value);
      } else {
        return field.getType().cast(value);
      }
    } catch (ClassCastException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Field '" + field + "' ill defined, expecting type '"
          + field.getType() + "' but got '" + value.getClass() + "'", ex);
    }
  }

  @Override
  public List<ClassFieldValue<?>> getProperties(XWikiDocument doc, ClassDefinition classDef) {
    List<ClassFieldValue<?>> ret = new ArrayList<>();
    for (ClassField<?> field : classDef.getFields()) {
      ret.add(new ClassFieldValue<>(castField(field), getProperty(doc, field)));
    }
    return ret;
  }

  // unchecked suppression is ok because every wildcard extends Object
  @SuppressWarnings("unchecked")
  private ClassField<Object> castField(ClassField<?> field) {
    return (ClassField<Object>) field;
  }

  @Override
  public boolean setProperty(BaseObject obj, String name, Object value) {
    boolean hasChange = !Objects.equal(value, getProperty(obj, name));
    if (hasChange) {
      if (value instanceof Collection) {
        value = Joiner.on('|').join((Collection<?>) value);
      }
      obj.set(name, value, context.getXWikiContext());
    }
    return hasChange;
  }

  @Override
  public <T> XWikiDocument setProperty(DocumentReference docRef, ClassField<T> field, T value)
      throws DocumentNotExistsException {
    XWikiDocument doc = getDocument(docRef);
    setProperty(doc, field, value);
    return doc;
  }

  @Override
  public <T> boolean setProperty(XWikiDocument doc, ClassField<T> field, T value) {
    try {
      return setProperty(getOrCreateXObject(doc, field.getClassDef().getClassRef()),
          field.getName(), serializePropertyValue(field, value));
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException("CelObjectField ill defined: " + field, ex);
    }
  }

  @Override
  public <T> boolean setProperty(XWikiDocument doc, ClassFieldValue<T> fieldValue) {
    return setProperty(doc, fieldValue.getField(), fieldValue.getValue());
  }

  private <T> Object serializePropertyValue(ClassField<T> field, T value) {
    try {
      if (field instanceof CustomClassField) {
        return ((CustomClassField<T>) field).serialize(value);
      } else {
        return value;
      }
    } catch (ClassCastException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Field '" + field + "' ill defined", ex);
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

  /**
   * forces same wikiRef to classRef as for onDoc
   */
  private DocumentReference adjustClassRef(DocumentReference classRef, XWikiDocument onDoc) {
    return modelUtils.adjustRef(classRef, DocumentReference.class, modelUtils.extractRef(
        onDoc.getDocumentReference(), WikiReference.class).or(context.getWikiRef()));
  }

}
