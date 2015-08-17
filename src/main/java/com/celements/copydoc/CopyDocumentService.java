package com.celements.copydoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CopyDocumentService implements ICopyDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CopyDocumentService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IWebUtilsService webUtils;

  @Override
  public boolean check(XWikiDocument doc1, XWikiDocument doc2) {
    return check(doc1, doc2, null);
  }

  @Override
  public boolean check(XWikiDocument doc1, XWikiDocument doc2, Set<BaseObject> toIgnore) {
    try {
      return copyInternal(doc1, doc2, toIgnore, false);
    } catch (ClassDocumentLoadException exc) {
      LOGGER.error("should not happen", exc); // since set is false
      throw new RuntimeException("ClassDocumentLoadException should not happen");
    }
  }

  @Override
  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc
      ) throws ClassDocumentLoadException, DocumentSaveException {
    boolean hasChanged = copy(srcDoc, trgDoc);
    if (hasChanged) {
      modelAccess.saveDocument(trgDoc, "copy from " + srcDoc);
    }
    return hasChanged;
  }

  @Override
  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc
      ) throws ClassDocumentLoadException {
    return copy(srcDoc, trgDoc, null);
  }

  @Override
  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc, Set<BaseObject> toIgnore
      ) throws ClassDocumentLoadException {
    return copyInternal(srcDoc, trgDoc, toIgnore, true);
  }

  private boolean copyInternal(XWikiDocument srcDoc, XWikiDocument trgDoc,
      Set<BaseObject> toIgnore, boolean set) throws ClassDocumentLoadException {
    boolean hasChanged = false;
    hasChanged |= copyDocFields(srcDoc, trgDoc, set);
    hasChanged |= copyObjects(srcDoc, trgDoc, toIgnore, set);
    LOGGER.info("for source '" + srcDoc + "', target '" + trgDoc  + "', set '" + set 
        + "' has changed: " + hasChanged);
    return hasChanged;
  }

  private boolean copyDocFields(XWikiDocument srcDoc, XWikiDocument trgDoc, boolean set) {
    boolean hasChanged = false;
    String srcLang = srcDoc.getLanguage();
    String trgLang = trgDoc.getLanguage();
    if (!StringUtils.equals(srcLang, trgLang)) {
      if (set) {
        trgDoc.setLanguage(srcLang);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' language changed from '" + trgLang 
          + "' to '" + srcLang + "'");
    }
    int srcTransl = srcDoc.getTranslation();
    int trgTransl = trgDoc.getTranslation();
    if (srcTransl != trgTransl) {
      if (set) {
        trgDoc.setTranslation(srcTransl);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' translation changed from '" + trgTransl
          + "' to '" + srcTransl + "'");
    }
    String srcTitle = srcDoc.getTitle();
    String trgTitle = trgDoc.getTitle();
    if (!StringUtils.equals(srcTitle, trgTitle)) {
      if (set) {
        trgDoc.setTitle(srcTitle);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' title changed from '" + trgTitle + "' to '" 
          + srcTitle + "'");
    }
    String srcContent = srcDoc.getContent();
    String trgContent = trgDoc.getContent();
    if (!StringUtils.equals(srcContent, trgContent)) {
      if (set) {
        trgDoc.setContent(srcContent);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' content changed from '" + trgContent
          + "' to '" + srcContent + "'");
    }
    return hasChanged;
  }

  boolean copyObjects(XWikiDocument srcDoc, XWikiDocument trgDoc, Set<BaseObject> toIgnore,
      boolean set) throws ClassDocumentLoadException {
    boolean hasChanged = false;
    Map<DocumentReference, List<BaseObject>> srcObjMap = copyListMap(srcDoc.getXObjects(),
        toIgnore);
    Map<DocumentReference, List<BaseObject>> trgObjMap = copyListMap(trgDoc.getXObjects(),
        toIgnore);
    for (DocumentReference srcClassRef : srcObjMap.keySet()) {
      DocumentReference trgClassRef = getAsTargetClassRef(srcClassRef, trgDoc);
      Iterator<BaseObject> srcObjIter = getIter(srcObjMap.get(srcClassRef));
      Iterator<BaseObject> trgObjIter = getIter(trgObjMap.get(trgClassRef));
      hasChanged |= createOrUpdateObjects(trgDoc, trgClassRef, srcObjIter, trgObjIter, 
          set);
      hasChanged |= removeObjects(trgDoc, trgObjIter, set);
      trgObjMap.remove(trgClassRef);
    }
    hasChanged |= removeRemainingObjects(trgDoc, trgObjMap.keySet(), set);
    return hasChanged;
  }

  DocumentReference getAsTargetClassRef(DocumentReference srcClassRef,
      XWikiDocument trgDoc) {
    return new DocumentReference(srcClassRef.getName(), new SpaceReference(
        srcClassRef.getLastSpaceReference().getName(), webUtils.getWikiRef(trgDoc)));
  }

  boolean createOrUpdateObjects(XWikiDocument doc, DocumentReference classRef, 
      Iterator<BaseObject> srcObjIter, Iterator<BaseObject> trgObjIter, boolean set
      ) throws ClassDocumentLoadException {
    boolean hasChanged = false;
    while (srcObjIter.hasNext()) {
      BaseObject srcObj = srcObjIter.next();
      BaseObject trgObj = (trgObjIter.hasNext() ? trgObjIter.next() : null);
      if (trgObj == null) {
        // obj does not exist on doc, creating new
        if (set) {
          trgObj = modelAccess.newXObject(doc, classRef);
        } else {
          trgObj = new BaseObject();
          trgObj.setDocumentReference(doc.getDocumentReference());
          trgObj.setXClassReference(classRef);
        }
        hasChanged = true;
        LOGGER.trace("for doc '" + doc + "' new object for '" + classRef + "'");
      }
      hasChanged |= copyObject(srcObj, trgObj, set);
    }
    return hasChanged;
  }

  boolean removeObjects(XWikiDocument doc, Iterator<BaseObject> toRemoveObjIter, 
      boolean set) {
    boolean hasChanged = false;
    while (toRemoveObjIter.hasNext()) {
      BaseObject obj = toRemoveObjIter.next();
      if ((set && doc.removeXObject(obj)) || (!set && containsObj(doc, obj))) {
        hasChanged = true;
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("for doc '" + doc + "' removed object '" + obj + "'");
        }
      }
    }
    return hasChanged;
  }
  
  private boolean containsObj(XWikiDocument doc, BaseObject obj) {
    DocumentReference classRef = obj.getXClassReference();
    List<BaseObject> objs = doc.getXObjects(classRef);
    return CollectionUtils.isNotEmpty(objs) && objs.contains(obj);
  }

  boolean removeRemainingObjects(XWikiDocument doc, 
      Collection<DocumentReference> toRemoveClassRefs, boolean set) {
    boolean hasChanged = false;
    for (DocumentReference classRef : toRemoveClassRefs) {
      if ((set && doc.removeXObjects(classRef)) || (!set && CollectionUtils.isNotEmpty(
          doc.getXObjects(classRef)))) {
        hasChanged = true;
        LOGGER.trace("for doc '" + doc + "' removed all objects for '" + classRef + "'");
      }
    }
    return hasChanged;
  }

  @Override
  public boolean checkObject(BaseObject obj1, BaseObject obj2) {
    return copyObject(obj1, obj2, false);
  }

  @Override
  public boolean copyObject(BaseObject srcObj, BaseObject trgObj) {
    return copyObject(srcObj, trgObj, true);
  }

  boolean copyObject(BaseObject srcObj, BaseObject trgObj, boolean set) {
    boolean hasChanged = false;
    Set<String> srcProps = srcObj.getPropertyList();
    Set<String> trgProps = new HashSet<>(trgObj.getPropertyList());
    for (String name : srcProps) {
      Object srcVal = modelAccess.getProperty(srcObj, name);
      Object trgVal = modelAccess.getProperty(trgObj, name);
      if (!ObjectUtils.equals(srcVal, trgVal)) {
        if (set) {
          modelAccess.setProperty(trgObj, name, srcVal);
        }
        hasChanged = true;
        LOGGER.trace("for doc '" + trgObj.getDocumentReference() + "' field '" + name 
            + "' changed from '" + trgVal + "' to '" + srcVal + "'");
      }
      trgProps.remove(name);
    }
    for (String name : trgProps) {
      trgObj.removeField(name);
      hasChanged = true;
      LOGGER.trace("for doc '" + trgObj.getDocumentReference() + "' field '" + name 
          + "' set to null");
    }
    return hasChanged;
  }

  private <K, V> Map<K, List<V>> copyListMap(Map<K, List<V>> map, Set<V> toIgnore) {
    Map<K, List<V>> ret = new HashMap<>();
    if (map != null) {
      for (K key : map.keySet()) {
        List<V> list = new ArrayList<>();
        for (V elem : MoreObjects.firstNonNull(map.get(key), ImmutableList.<V>of())) {
          if ((elem != null) && !toIgnore.contains(elem)) {
            list.add(elem);
          }
        }
        if (!list.isEmpty()) {
          ret.put(key, list);
        }
      }
    }
    return ret;
  }

  private <T> Iterator<T> getIter(Iterable<T> iterable) {
    Iterator<T> iter;
    if (iterable != null) {
      iter = iterable.iterator();
    } else {
      iter = ImmutableList.<T>of().iterator();
    }
    return iter;
  }

}
