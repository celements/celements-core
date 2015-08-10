package com.celements.model.access;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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
  public XWikiDocument getDocument(DocumentReference docRef) throws XWikiException {
    checkNotNull(docRef);
    return getContext().getWiki().getDocument(docRef, getContext());
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
  public void saveDocument(XWikiDocument doc) throws XWikiException {
    checkNotNull(doc);
    getContext().getWiki().saveDocument(doc, getContext());
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment) throws XWikiException {
    checkNotNull(doc);
    getContext().getWiki().saveDocument(doc, comment, getContext());
  }

  @Override
  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws XWikiException {
    checkNotNull(doc);
    getContext().getWiki().saveDocument(doc, comment, isMinorEdit, getContext());
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws XWikiException {
    return Iterables.getFirst(getXObjects(getDocument(docRef), classRef), null);
  }

  @Override
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws XWikiException {
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
      ) throws XWikiException {
    return getXObjects(getDocument(docRef), classRef);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) throws XWikiException {
    return getXObjects(getDocument(docRef), classRef, key, value);
  }

  @Override
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws XWikiException {
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
    List<BaseObject> ret = new ArrayList<>();
    for (BaseObject obj : MoreObjects.firstNonNull(doc.getXObjects(classRef),
        Collections.<BaseObject> emptyList())) {
      if ((obj != null) && checkPropertyKeyValues(obj, key, values)) {
        ret.add(obj);
      }
    }
    return ret;
  }

  private boolean checkPropertyKeyValues(BaseObject obj, String key,
      Collection<?> values) {
    boolean valid = (key == null);
    if (!valid) {
      BaseProperty prop = getProperty(obj, key);
      if (prop != null) {
        for (Object val : MoreObjects.firstNonNull(values, Collections.emptyList())) {
          valid |= Objects.equal(val, prop.getValue());
        }
      }
    }
    return valid;
  }

  private BaseProperty getProperty(BaseObject obj, String key) {
    BaseProperty prop = null;
    try {
      prop = (BaseProperty) obj.get(key);
    } catch (XWikiException xwe) {
      // does not happen since XWikiException is never thrown in BaseObject.get()
      LOGGER.error("should not happen", xwe);
    }
    return prop;
  }

  @Override
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef)
      throws XWikiException {
    checkNotNull(doc);
    checkNotNull(classRef);
    return doc.newXObject(classRef, getContext());
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

  private String toString(EntityReference ref) {
    return "'" + webUtilsService.serializeRef(ref) + "'";
  }

}
