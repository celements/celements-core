package com.celements.copydoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

@Component
public class CopyDocumentService implements ICopyDocumentRole {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CopyDocumentService.class);

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public boolean checkChanges(XWikiDocument doc1, XWikiDocument doc2
      ) throws XWikiException {
    return copyInternal(doc1, doc2, false);
  }

  @Override
  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc)
      throws XWikiException {
    boolean hasChanged = copy(srcDoc, trgDoc);
    if (hasChanged) {
      getContext().getWiki().saveDocument(trgDoc, "copy from " + srcDoc, getContext());
    }
    return hasChanged;
  }

  @Override
  public boolean copy(XWikiDocument srcDoc, XWikiDocument trgDoc) throws XWikiException {
    return copyInternal(srcDoc, trgDoc, true);
  }

  private boolean copyInternal(XWikiDocument srcDoc, XWikiDocument trgDoc, boolean set
      ) throws XWikiException {
    boolean hasChanged = false;
    hasChanged |= copyDocFields(srcDoc, trgDoc, set);
    hasChanged |= copyObjects(srcDoc, trgDoc, set);
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

  boolean copyObjects(XWikiDocument srcDoc, XWikiDocument trgDoc, boolean set
      ) throws XWikiException {
    boolean hasChanged = false;
    Map<DocumentReference, List<BaseObject>> srcObjMap = copyListMap(srcDoc.getXObjects());
    Map<DocumentReference, List<BaseObject>> trgObjMap = copyListMap(trgDoc.getXObjects());
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
    DocumentReference classRef = new DocumentReference(srcClassRef);
    classRef.setWikiReference(trgDoc.getDocumentReference().getWikiReference());
    return classRef;
  }

  boolean createOrUpdateObjects(XWikiDocument doc, DocumentReference classRef, 
      Iterator<BaseObject> srcObjIter, Iterator<BaseObject> trgObjIter, boolean set
      ) throws XWikiException {
    boolean hasChanged = false;
    while (srcObjIter.hasNext()) {
      BaseObject srcObj = srcObjIter.next();
      BaseObject trgObj = (trgObjIter.hasNext() ? trgObjIter.next() : null);
      if (trgObj == null) {
        // obj does not exist on doc, creating new
        if (set) {
          trgObj = doc.newXObject(classRef, getContext());
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

  boolean copyObject(BaseObject srcObj, BaseObject trgObj, boolean set) {
    boolean hasChanged = false;
    Set<String> srcProps = srcObj.getPropertyList();
    Set<String> trgProps = new HashSet<String>(trgObj.getPropertyList());
    for (String name : srcProps) {
      Object srcVal = getValue(srcObj, name);
      Object trgVal = getValue(trgObj, name);
      if (!ObjectUtils.equals(srcVal, trgVal)) {
        setValue(trgObj, name, srcVal, set);
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

  @Override
  public Object getValue(BaseObject bObj, String name) {
    Object value = null;
    try {
      if ((bObj != null) && (bObj.get(name) instanceof BaseProperty)) {
          value = ((BaseProperty) bObj.get(name)).getValue();
      }
      // avoid comparing empty string to null
      if ((value instanceof String) && StringUtils.isBlank((String) value)) {
        value = null;
      }
      // assure to not return Timestamp since Timestamp.equals(Date) always returns false
      if (value instanceof Date) {
        value = new Date(((Date) value).getTime());
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Should never happen", xwe);
    }
    return value;
  }

  void setValue(BaseObject bObj, String name, Object val, boolean set) {
    if (set) {
      if (val instanceof Collection) {
        val = StringUtils.join((Collection<?>) val, "|");
      }
      bObj.set(name, val, getContext());
    }
  }

  private <K, V> Map<K, List<V>> copyListMap(Map<K, List<V>> map) {
    Map<K, List<V>> ret = new HashMap<K, List<V>>();
    if (map != null) {
      for (K key : map.keySet()) {
        if (map.get(key) != null) {
          List<V> list = new ArrayList<V>(map.get(key));
          list.removeAll(Collections.singleton(null));
          if (list.size() > 0) {
            ret.put(key, list);
          }
        }
      }
    }
    return ret;
  }

  private <T> Iterator<T> getIter(List<T> list) {
    Iterator<T> iter;
    if (list != null) {
      iter = list.iterator();
    } else {
      iter = Collections.<T>emptyList().iterator();
    }
    return iter;
  }

}
