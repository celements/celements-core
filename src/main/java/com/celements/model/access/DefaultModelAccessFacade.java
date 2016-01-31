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
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.access.exception.TranslationNotExistsException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

@Component
public class DefaultModelAccessFacade implements IModelAccessFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      DefaultModelAccessFacade.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
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
  public XWikiDocument getDocument(DocumentReference docRef, String lang
      ) throws DocumentLoadException, DocumentNotExistsException,
        TranslationNotExistsException {
    checkNotNull(docRef);
    checkState(!Strings.isNullOrEmpty(lang));
    if (exists(docRef)) {
      XWikiDocument translatedDocument = getDocumentInternal(docRef);
      if (!lang.equals(translatedDocument.getDefaultLanguage())) {
        try {
          if (translatedDocument.getTranslationList(getContext()).contains(lang)) {
            translatedDocument = translatedDocument.getTranslatedDocument(lang,
                getContext());
          } else {
            throw new TranslationNotExistsException(docRef, lang);
          }
        } catch (XWikiException xwe) {
          throw new DocumentLoadException(docRef, xwe);
        }
      }
      return translatedDocument;
    } else {
      throw new DocumentNotExistsException(docRef);
    }
  }

  private XWikiDocument getDocumentInternal(DocumentReference docRef
      ) throws DocumentLoadException {
    try {
      return getContext().getWiki().getDocument(docRef, getContext());
    } catch (XWikiException xwe) {
      throw new DocumentLoadException(docRef, xwe);
    }
  }

  @Override
  public XWikiDocument createDocument(DocumentReference docRef
      ) throws DocumentLoadException, DocumentAlreadyExistsException {
    checkNotNull(docRef);
    if (!exists(docRef)) {
      return createDocumentInternal(docRef);
    } else {
      throw new DocumentAlreadyExistsException(docRef);
    }
  }

  private XWikiDocument createDocumentInternal(DocumentReference docRef
      ) throws DocumentLoadException {
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
  public XWikiDocument getOrCreateDocument(DocumentReference docRef
      ) throws DocumentLoadException {
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
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit
      ) throws DocumentSaveException {
    checkNotNull(doc);
    try {
      getContext().getWiki().saveDocument(doc, comment, isMinorEdit, getContext());
    } catch (XWikiException xwe) {
      throw new DocumentSaveException(doc.getDocumentReference(), xwe);
    }
  }

  @Override
  public void deleteDocument(DocumentReference docRef, boolean totrash
      ) throws DocumentLoadException, DocumentDeleteException {
    try {
      deleteDocument(getDocument(docRef), totrash);
    } catch (DocumentNotExistsException exc) {
      LOGGER.debug("doc trying to delete does not exist '{}'", docRef, exc);
    }
  }

  @Override
  public void deleteDocument(XWikiDocument doc, boolean totrash
      ) throws DocumentDeleteException {
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
  public void deleteDocumentWithoutTranslations(XWikiDocument doc, boolean totrash
      ) throws DocumentDeleteException {
    String dbBefore = getContext().getDatabase();
    try {
      getContext().setDatabase(webUtilsService.getWikiRef(doc).getName());
      LOGGER.debug("deleteDocument: doc '{},{}', totrash '{}' dbBefore '{}' dbNow '{}'",
          doc, doc.getLanguage(), totrash, dbBefore, getContext().getDatabase());
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
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef
      ) throws DocumentLoadException, DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocument(docRef), classRef), null);
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentLoadException, DocumentNotExistsException {
    return Iterables.getFirst(getXObjects(getDocument(docRef), classRef, key, value),
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
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef
      ) throws DocumentLoadException, DocumentNotExistsException {
    return getXObjects(getDocument(docRef), classRef);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentLoadException, DocumentNotExistsException {
    return getXObjects(getDocument(docRef), classRef, key, value);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentLoadException,
      DocumentNotExistsException {
    return getXObjects(getDocument(docRef), classRef, key, values);
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef) {
    return getXObjects(doc, classRef, null, null);
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Object value) {
    return getXObjects(doc, classRef, key, Arrays.asList(value));
  }

  @Override
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Collection<?> values) {
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

  private boolean checkPropertyKeyValues(BaseObject obj, String key,
      Collection<?> checkValues) {
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
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef
      ) throws ClassDocumentLoadException {
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
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef
      ) throws ClassDocumentLoadException {
    BaseObject obj = getXObject(doc, classRef);
    if (obj == null) {
      obj = newXObject(doc, classRef);
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
  public Object getProperty(DocumentReference docRef, DocumentReference classRef,
      String name) throws DocumentLoadException, DocumentNotExistsException {
    return getProperty(getDocument(classRef), classRef, name);
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
  public void setProperty(BaseObject obj, String name, Object value) {
    if (value instanceof Collection) {
      value = Joiner.on('|').join((Iterable<?>) value);
    }
    obj.set(name, value, getContext());
  }

}
