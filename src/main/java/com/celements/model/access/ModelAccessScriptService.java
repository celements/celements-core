package com.celements.model.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.rights.AccessLevel;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

@Component("modelAccess")
public class ModelAccessScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ModelAccessScriptService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext()
        .getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public Document getDocument(DocumentReference docRef) {
    Document ret = null;
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        XWikiDocument doc = modelAccess.getDocument(docRef);
        ret = doc.newDocument(getContext());
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Failed to load doc '{}'", docRef, xwe);
    }
    return ret;
  }

  public boolean exists(DocumentReference docRef) {
    return modelAccess.exists(docRef);
  }

  public com.xpn.xwiki.api.Object getObject(DocumentReference docRef,
      DocumentReference classRef) {
    return getObject(docRef, classRef, null, null);
  }

  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef) {
    return getObject(doc, classRef, null, null);
  }

  public com.xpn.xwiki.api.Object getObject(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) {
    com.xpn.xwiki.api.Object ret = null;
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        ret = toObjectApi(modelAccess.getXObject(docRef, classRef));
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Failed to load doc '{}'", docRef, xwe);
    }
    return ret;
  }

  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef,
      String key, Object value) {
    return toObjectApi(modelAccess.getXObject(doc.getDocument(), classRef, key, value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef) {
    return getObjects(docRef, classRef, null, null);
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef) {
    return getObjects(doc, classRef, null, null);
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) {
    return getObjects(docRef, classRef, key, Arrays.asList(value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef, String key, Object value) {
    return getObjects(doc, classRef, key, Arrays.asList(value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Collection<?> values) {
    List<com.xpn.xwiki.api.Object> ret = ImmutableList.of();
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        ret = toObjectApi(modelAccess.getXObjects(docRef, classRef, key, values));
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Failed to load doc '{}'", docRef, xwe);
    }
    return ret;
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef, String key, Collection<?> values) {
    return toObjectApi(modelAccess.getXObjects(doc.getDocument(), classRef, key, values));
  }

//  TODO
//  public com.xpn.xwiki.api.Object newObject(DocumentReference docRef,
//      DocumentReference classRef);
//
//  public com.xpn.xwiki.api.Object newObject(Document doc, DocumentReference classRef);
//
//  public boolean removeObject(DocumentReference docRef,
//      com.xpn.xwiki.api.Object objToRemove);
//
//  public boolean removeObject(Document doc, com.xpn.xwiki.api.Object objToRemove);
//
//  public boolean removeObjects(DocumentReference docRef,
//      List<com.xpn.xwiki.api.Object> objsToRemove);
//
//  public boolean removeObjects(Document doc, List<com.xpn.xwiki.api.Object> objsToRemove);
//
//  public boolean removeObjects(DocumentReference docRef, DocumentReference classRef);
//
//  public boolean removeObjects(Document doc, DocumentReference classRef);
//
//  public boolean removeObjects(DocumentReference docRef, DocumentReference classRef,
//      String key, Object value);
//
//  public boolean removeObjects(Document doc, DocumentReference classRef, String key,
//      Object value);
//
//  public boolean removeObjects(DocumentReference docRef, DocumentReference classRef,
//      String key, Collection<?> values);
//
//  public boolean removeObjects(Document doc, DocumentReference classRef, String key,
//      Collection<?> values);

  private com.xpn.xwiki.api.Object toObjectApi(BaseObject obj) {
    return obj.newObjectApi(obj, getContext());
  }

  private List<com.xpn.xwiki.api.Object> toObjectApi(List<BaseObject> objs) {
    List<com.xpn.xwiki.api.Object> ret = new ArrayList<>();
    for (BaseObject obj : objs) {
      ret.add(toObjectApi(obj));
    }
    return ret;
  }

  private String getCurrentUser() {
    if (getContext().getXWikiUser() != null) {
      return getContext().getXWikiUser().getUser();
    } else {
      return XWikiRightService.GUEST_USER_FULLNAME;
    }
  }

}
