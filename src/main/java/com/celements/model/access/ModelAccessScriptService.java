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

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.AccessLevel;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(ModelAccessScriptService.NAME)
public class ModelAccessScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ModelAccessScriptService.class);

  public static final String NAME = "modelAccess";

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public Document getDocument(DocumentReference docRef) {
    Document ret = null;
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        XWikiDocument doc = modelAccess.getDocument(docRef);
        ret = modelAccess.getApiDocument(doc);
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    } catch (NoAccessRightsException exc) {
      LOGGER.error("no '{}' access rights for user '{}' on doc '{}'.",
          exc.getExpectedAccessLevel(), exc.getUser(), docRef, exc);
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

  public com.xpn.xwiki.api.Object getObject(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) {
    com.xpn.xwiki.api.Object ret = null;
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        ret = toObjectApi(modelAccess.getXObject(docRef, classRef));
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    }
    return ret;
  }

  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef) {
    return getObject(doc, classRef, null, null);
  }

  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef,
      String key, Object value) {
    return toObjectApi(modelAccess.getXObject(doc.getDocument(), classRef, key, value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef) {
    return getObjects(docRef, classRef, null, null);
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) {
    return getObjects(docRef, classRef, key, Arrays.asList(value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Collection<?> values) {
    List<com.xpn.xwiki.api.Object> ret = ImmutableList.of();
    try {
      if (webUtils.hasAccessLevel(docRef, AccessLevel.VIEW)) {
        ret = toObjectApi(modelAccess.getXObjects(docRef, classRef, key, values));
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    }
    return ret;
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef) {
    return getObjects(doc, classRef, null, null);
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef, String key, Object value) {
    return getObjects(doc, classRef, key, Arrays.asList(value));
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc,
      DocumentReference classRef, String key, Collection<?> values) {
    return toObjectApi(modelAccess.getXObjects(doc.getDocument(), classRef, key, values));
  }

  public com.xpn.xwiki.api.Object newObject(Document doc, DocumentReference classRef) {
    com.xpn.xwiki.api.Object ret = null;
    try {
      ret = toObjectApi(modelAccess.newXObject(doc.getDocument(), classRef));
    } catch (ClassDocumentLoadException exc) {
      LOGGER.error("Failed to create object '{}' on doc '{}'", classRef, doc, exc);
    }
    return ret;
  }

  public boolean removeObject(Document doc, com.xpn.xwiki.api.Object objToRemove) {
    return modelAccess.removeXObject(doc.getDocument(), objToRemove.getXWikiObject());
  }

  public boolean removeObjects(Document doc,
      List<com.xpn.xwiki.api.Object> objsToRemove) {
    return modelAccess.removeXObjects(doc.getDocument(), toXObject(objsToRemove));
  }

  public boolean removeObjects(Document doc, DocumentReference classRef) {
    return removeObjects(doc, classRef, null, null);
  }

  public boolean removeObjects(Document doc, DocumentReference classRef, String key,
      Object value) {
    return removeObjects(doc, classRef, key, Arrays.asList(value));
  }

  public boolean removeObjects(Document doc, DocumentReference classRef, String key,
      Collection<?> values) {
    return modelAccess.removeXObjects(doc.getDocument(), classRef, key, values);
  }

  private List<BaseObject> toXObject(List<com.xpn.xwiki.api.Object> objs) {
    List<BaseObject> ret = new ArrayList<>();
    for (com.xpn.xwiki.api.Object obj : objs) {
      ret.add(obj.getXWikiObject());
    }
    return ret;
  }

  private com.xpn.xwiki.api.Object toObjectApi(BaseObject obj) {
    com.xpn.xwiki.api.Object ret = null;
    if (obj != null) {
      ret = obj.newObjectApi(obj, getContext());
    }
    return ret;
  }

  private List<com.xpn.xwiki.api.Object> toObjectApi(List<BaseObject> objs) {
    List<com.xpn.xwiki.api.Object> ret = new ArrayList<>();
    for (BaseObject obj : objs) {
      ret.add(toObjectApi(obj));
    }
    return ret;
  }

}
