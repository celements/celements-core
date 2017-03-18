package com.celements.model.access;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.access.exception.ModelAccessRuntimeException;
import com.celements.model.access.object.XObjectHandler;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ClassFieldValue;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.Utils;

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
    LOGGER.debug("saveDocument: doc '{}, {}', comment '{}', isMinorEdit '{}'",
        modelUtils.serializeRef(doc.getDocumentReference()), doc.getLanguage(), comment,
        isMinorEdit);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("saveDocument: context db '{}' and StackTrace:", context.getWikiRef(),
          new Throwable());
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
    LOGGER.debug("deleteDocument: doc '{}, {}', totrash '{}'", modelUtils.serializeRef(
        doc.getDocumentReference()), doc.getLanguage(), totrash);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("deleteDocument: context db '{}' and StackTrace:", context.getWikiRef(),
          new Throwable());
    }
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
        LOGGER.error("failed to load existing translation '{}' for doc '{}'", lang,
            modelUtils.serializeRef(docRef), exc);
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
      doc = doc.clone();
      // fix missing docRef clone in XWikiDocument.clone()
      DocumentReference docRef = References.cloneRef(doc.getDocumentReference(),
          DocumentReference.class);
      // set invalid docRef first to circumvent equals check in setDocumentReference
      doc.setDocumentReference(new DocumentReference("$", "$", "$"));
      doc.setDocumentReference(docRef);
      doc.setMetaDataDirty(false); // set true by setDocumentReference
      doc.setFromCache(false);
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
  @Deprecated
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef),
        null);
  }

  @Override
  @Deprecated
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef, String key,
      Object value) throws DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key,
        value), null);
  }

  @Override
  @Deprecated
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef) {
    return Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(new ClassReference(
        classRef)).fetchFirst().orNull();
  }

  @Override
  @Deprecated
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    XObjectHandler objHandler = Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(
        new ClassReference(classRef));
    if (!Strings.isNullOrEmpty(key)) {
      objHandler.filter(createField(classRef, key, value), value);
    }
    return objHandler.fetchFirst().orNull();
  }

  @Override
  @Deprecated
  public Optional<BaseObject> getXObject(DocumentReference docRef, DocumentReference classRef,
      int objectNumber) throws DocumentNotExistsException {
    return getXObject(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, objectNumber);
  }

  @Override
  @Deprecated
  public Optional<BaseObject> getXObject(XWikiDocument doc, DocumentReference classRef,
      int objectNumber) {
    BaseObject bObj = null;
    List<BaseObject> objs = getXObjects(doc, classRef);
    for (BaseObject baseObject : objs) {
      if (baseObject.getNumber() == objectNumber) {
        bObj = baseObject;
        break;
      }
    }
    return Optional.fromNullable(bObj);
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef)
      throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef);
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key, value);
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentNotExistsException {
    return getXObjects(getDocumentReadOnly(docRef, DEFAULT_LANG), classRef, key, values);
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef) {
    return Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(new ClassReference(
        classRef)).fetchList();
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    return getXObjects(doc, classRef, key, Arrays.asList(value));
  }

  @Override
  @Deprecated
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values) {
    XObjectHandler objHandler = Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(
        new ClassReference(classRef));
    if (!Strings.isNullOrEmpty(key) && !values.isEmpty()) {
      objHandler.filter(classRef, key, values);
    }
    return objHandler.fetchList();
  }

  @Override
  @Deprecated
  public Map<DocumentReference, List<BaseObject>> getXObjects(XWikiDocument doc) {
    Map<ClassReference, List<BaseObject>> map = Utils.getComponent(XObjectHandler.class).onDoc(
        doc).fetchMap();
    Map<DocumentReference, List<BaseObject>> ret = new HashMap<>();
    for (ClassReference classRef : map.keySet()) {
      DocumentReference docClassRef = classRef.getDocumentReference(
          doc.getDocumentReference().getWikiReference());
      ret.put(docClassRef, map.get(classRef));
    }
    return ImmutableMap.copyOf(ret);
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
  public com.xpn.xwiki.api.Object getApiObjectWithoutRightCheck(@Nullable BaseObject obj) {
    if (obj != null) {
      return obj.newObjectApi(obj, context.getXWikiContext());
    }
    return null;
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
  @Deprecated
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef) {
    return Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(new ClassReference(
        classRef)).create().get(0);
  }

  @Override
  @Deprecated
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef) {
    return getOrCreateXObject(doc, classRef, null, null);
  }

  @Override
  @Deprecated
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    XObjectHandler objHandler = Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(
        new ClassReference(classRef));
    if (!Strings.isNullOrEmpty(key)) {
      objHandler.filter(classRef, key, value);
    }
    return Iterables.getFirst(objHandler.createIfNotExists(), objHandler.fetchFirst().get());
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
  @Deprecated
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef) {
    return !Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(new ClassReference(
        classRef)).remove().isEmpty();
  }

  @Override
  @Deprecated
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value) {
    return !Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(classRef, key,
        value).remove().isEmpty();
  }

  @Override
  @Deprecated
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values) {
    XObjectHandler objHandler = Utils.getComponent(XObjectHandler.class).onDoc(doc).filter(
        new ClassReference(classRef));
    if (!Strings.isNullOrEmpty(key) && !values.isEmpty()) {
      objHandler.filter(classRef, key, values);
    }
    return !objHandler.remove().isEmpty();
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
  public <T> Optional<T> getFieldValue(BaseObject obj, ClassField<T> field) {
    checkClassRef(obj, field);
    return Optional.fromNullable(resolvePropertyValue(field, getProperty(obj, field.getName())));
  }

  @Override
  public <T> Optional<T> getFieldValue(XWikiDocument doc, ClassField<T> field) {
    checkNotNull(doc);
    checkNotNull(field);
    return Optional.fromNullable(resolvePropertyValue(field, getProperty(doc,
        field.getClassDef().getClassRef(), field.getName())));
  }

  @Override
  public <T> Optional<T> getFieldValue(DocumentReference docRef, ClassField<T> field)
      throws DocumentNotExistsException {
    checkNotNull(docRef);
    checkNotNull(field);
    return Optional.fromNullable(resolvePropertyValue(field, getProperty(docRef,
        field.getClassDef().getClassRef(), field.getName())));
  }

  @Override
  public <T> Optional<T> getFieldValue(XWikiDocument doc, ClassField<T> field, T ignoreValue) {
    checkNotNull(ignoreValue);
    Optional<T> property = getFieldValue(doc, field);
    if (property.isPresent() && Objects.equal(property.get(), ignoreValue)) {
      property = Optional.absent();
    }
    return property;
  }

  @Override
  public <T> Optional<T> getFieldValue(DocumentReference docRef, ClassField<T> field, T ignoreValue)
      throws DocumentNotExistsException {
    checkNotNull(ignoreValue);
    Optional<T> property = getFieldValue(docRef, field);
    if (property.isPresent() && Objects.equal(property.get(), ignoreValue)) {
      property = Optional.absent();
    }
    return property;
  }

  @Override
  @Deprecated
  public <T> T getProperty(DocumentReference docRef, ClassField<T> field)
      throws DocumentNotExistsException {
    return getFieldValue(docRef, field).orNull();
  }

  @Override
  @Deprecated
  public <T> T getProperty(XWikiDocument doc, ClassField<T> field) {
    return getFieldValue(doc, field).orNull();
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
      try {
        obj.set(name, value, context.getXWikiContext());
      } catch (ClassCastException ex) {
        throw new IllegalArgumentException("Unable to set value '" + value + "' on field '"
            + modelUtils.serializeRefLocal(obj.getXClassReference()) + "." + name + "'", ex);
      }
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
    return setProperty(getOrCreateXObject(doc, field.getClassDef().getClassRef()), field, value);
  }

  @Override
  public <T> boolean setProperty(XWikiDocument doc, ClassFieldValue<T> fieldValue) {
    return setProperty(doc, fieldValue.getField(), fieldValue.getValue());
  }

  @Override
  public <T> boolean setProperty(BaseObject obj, ClassField<T> field, T value) {
    checkClassRef(obj, field);
    return setProperty(obj, field.getName(), serializePropertyValue(field, value));
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

  private void checkClassRef(BaseObject obj, ClassField<?> field) {
    DocumentReference classRef = checkNotNull(obj).getXClassReference();
    checkArgument(classRef.equals(checkNotNull(field).getClassDef().getClassRef(
        classRef.getWikiReference())), "class refs from obj and field do not match");
  }

  @Override
  public XWikiAttachment getAttachmentNameEqual(XWikiDocument doc, String filename)
      throws AttachmentNotExistsException {
    for (XWikiAttachment attach : doc.getAttachmentList()) {
      if ((attach != null) && attach.getFilename().equals(filename)) {
        return attach;
      }
    }
    LOGGER.debug("getAttachmentNameEqual: not found! file: [{}], doc: [{}]", filename,
        modelUtils.serializeRef(doc.getDocumentReference()));
    // FIXME empty or null filename leads to exception:
    // java.lang.IllegalArgumentException: An Entity Reference name cannot be null or
    // empty
    if (Strings.isNullOrEmpty(filename)) {
      throw new AttachmentNotExistsException(null);
    } else {
      throw new AttachmentNotExistsException(new AttachmentReference(filename,
          doc.getDocumentReference()));
    }
  }

}
