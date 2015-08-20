package com.celements.copydoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CopyDocumentService implements ICopyDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CopyDocumentService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IWebUtilsService webUtilsService;

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
    return copyAndSave(srcDoc, trgDoc, null);
  }

  @Override
  public boolean copyAndSave(XWikiDocument srcDoc, XWikiDocument trgDoc,
      Set<BaseObject> toIgnore) throws ClassDocumentLoadException, DocumentSaveException {
    boolean hasChanged = copy(srcDoc, trgDoc, toIgnore);
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
    if (!Objects.equal(srcLang, trgLang)) {
      if (set) {
        trgDoc.setLanguage(srcLang);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' language changed from '" + trgLang 
          + "' to '" + srcLang + "'");
    }
    int srcTransl = srcDoc.getTranslation();
    int trgTransl = trgDoc.getTranslation();
    if (!Objects.equal(srcTransl, trgTransl)) {
      if (set) {
        trgDoc.setTranslation(srcTransl);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' translation changed from '" + trgTransl
          + "' to '" + srcTransl + "'");
    }
    String srcTitle = srcDoc.getTitle();
    String trgTitle = trgDoc.getTitle();
    if (!Objects.equal(srcTitle, trgTitle)) {
      if (set) {
        trgDoc.setTitle(srcTitle);
      }
      hasChanged = true;
      LOGGER.trace("for doc '" + trgDoc + "' title changed from '" + trgTitle + "' to '" 
          + srcTitle + "'");
    }
    String srcContent = srcDoc.getContent();
    String trgContent = trgDoc.getContent();
    if (!Objects.equal(srcContent, trgContent)) {
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
    for (DocumentReference classRef : getAllClassRefs(srcDoc, trgDoc)) {
      List<BaseObject> srcObjs = getXObjects(srcDoc, classRef, toIgnore);
      List<BaseObject> trgObjs = getXObjects(trgDoc, classRef, toIgnore);
      hasChanged |= createOrUpdateObjects(trgDoc, srcObjs, trgObjs, set);
      hasChanged |= (set && modelAccess.removeXObjects(trgDoc, trgObjs))
          || (!set && !trgObjs.isEmpty());
    }
    return hasChanged;
  }

  Set<DocumentReference> getAllClassRefs(XWikiDocument srcDoc, XWikiDocument trgDoc) {
    Set<DocumentReference> ret = new HashSet<>();
    for (DocumentReference classRef : Iterables.concat(modelAccess.getXObjects(srcDoc
        ).keySet(), modelAccess.getXObjects(trgDoc).keySet())) {
      ret.add(webUtilsService.checkWikiRef(classRef, trgDoc));
    }
    return ret;
  }

  List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef,
      Set<BaseObject> toIgnore) {
    List<BaseObject> ret = new ArrayList<>(modelAccess.getXObjects(doc, classRef));
    if (toIgnore != null) {
      ret.removeAll(toIgnore);
    }
    return ret;
  }

  boolean createOrUpdateObjects(XWikiDocument doc, List<BaseObject> srcObjs,
      List<BaseObject> trgObjs, boolean set) throws ClassDocumentLoadException {
    boolean hasChanged = false;
    Iterator<BaseObject> trgObjIter = trgObjs.iterator();
    for (BaseObject srcObj : srcObjs) {
      BaseObject trgObj = (trgObjIter.hasNext() ? trgObjIter.next() : null);
      if (trgObj != null) {
        trgObjIter.remove();
      } else {
        DocumentReference classRef = srcObj.getXClassReference();
        if (set) {
          trgObj = modelAccess.newXObject(doc, classRef);
        }
        hasChanged = true;
        LOGGER.trace("for doc '" + doc + "' new object for '" + classRef + "'");
      }
      if (trgObj != null) {
        hasChanged |= copyObject(srcObj, trgObj, set);
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
      if (!Objects.equal(srcVal, trgVal)) {
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

}
