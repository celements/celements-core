package com.celements.model.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(ModelAccessScriptService.NAME)
public class ModelAccessScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelAccessScriptService.class);

  public static final String NAME = "modelAccess";

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  IRightsAccessFacadeRole rightsAccess;

  public Document getDocument(DocumentReference docRef) {
    Document ret = null;
    try {
      if (rightsAccess.hasAccessLevel(docRef, EAccessLevel.VIEW)) {
        XWikiDocument doc = modelAccess.getDocument(docRef);
        ret = modelAccess.getApiDocument(doc);
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    } catch (NoAccessRightsException exc) {
      LOGGER.error("no '{}' access rights for user '{}' on doc '{}'.", exc.getExpectedAccessLevel(),
          exc.getUser(), docRef, exc);
    }
    return ret;
  }

  public Document getOrCreateDocument(DocumentReference docRef) {
    Document ret = null;
    try {
      if (rightsAccess.hasAccessLevel(docRef, EAccessLevel.VIEW) && rightsAccess.hasAccessLevel(
          docRef, EAccessLevel.EDIT)) {
        XWikiDocument doc = modelAccess.getOrCreateDocument(docRef);
        ret = modelAccess.getApiDocument(doc);
      }
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    } catch (NoAccessRightsException exc) {
      LOGGER.error("no '{}' access rights for user '{}' on doc '{}'.", exc.getExpectedAccessLevel(),
          exc.getUser(), docRef, exc);
    }
    return ret;
  }

  public boolean exists(DocumentReference docRef) {
    return modelAccess.exists(docRef);
  }

  public com.xpn.xwiki.api.Object getObject(DocumentReference docRef, DocumentReference classRef) {
    return getObject(docRef, classRef, null, null);
  }

  public com.xpn.xwiki.api.Object getObject(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) {
    com.xpn.xwiki.api.Object ret = null;
    try {
      if (rightsAccess.hasAccessLevel(docRef, EAccessLevel.VIEW)) {
        ret = modelAccess.getApiObjectWithoutRightCheck(modelAccess.getXObject(docRef, classRef));
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    }
    return ret;
  }

  /**
   * programming rights needed
   */
  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef) {
    return getObject(doc, classRef, null, null);
  }

  /**
   * programming rights needed
   */
  public com.xpn.xwiki.api.Object getObject(Document doc, DocumentReference classRef, String key,
      Object value) {
    if (doc != null) {
      return modelAccess.getApiObjectWithoutRightCheck(modelAccess.getXObject(doc.getDocument(),
          classRef, key, value));
    }
    return null;
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
      if (rightsAccess.hasAccessLevel(docRef, EAccessLevel.VIEW)) {
        ret = modelAccess.getApiObjectsWithoutRightChecks(modelAccess.getXObjects(docRef, classRef,
            key, values));
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Doc does not exist '{}'", docRef, exc);
    } catch (DocumentLoadException exc) {
      LOGGER.error("Failed to load doc '{}'", docRef, exc);
    }
    return ret;
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc, DocumentReference classRef) {
    return getObjects(doc, classRef, null, null);
  }

  public List<com.xpn.xwiki.api.Object> getObjects(Document doc, DocumentReference classRef,
      String key, Object value) {
    return getObjects(doc, classRef, key, Arrays.asList(value));
  }

  /**
   * programming rights needed
   */
  public List<com.xpn.xwiki.api.Object> getObjects(Document doc, DocumentReference classRef,
      String key, Collection<?> values) {
    return modelAccess.getApiObjectsWithoutRightChecks(modelAccess.getXObjects(doc.getDocument(),
        classRef, key, values));
  }

  /**
   * programming rights needed
   */
  public com.xpn.xwiki.api.Object newObject(Document doc, DocumentReference classRef) {
    com.xpn.xwiki.api.Object ret = null;
    if (doc != null) {
      try {
        ret = modelAccess.getApiObjectWithoutRightCheck(modelAccess.newXObject(doc.getDocument(),
            classRef));
      } catch (ClassDocumentLoadException exc) {
        LOGGER.error("Failed to create object '{}' on doc '{}'", classRef, doc, exc);
      }
    }
    return ret;
  }

  /**
   * programming rights needed
   */
  public boolean removeObject(Document doc, com.xpn.xwiki.api.Object objToRemove) {
    return modelAccess.removeXObject(doc.getDocument(), objToRemove.getXWikiObject());
  }

  /**
   * programming rights needed
   */
  public boolean removeObjects(Document doc, List<com.xpn.xwiki.api.Object> objsToRemove) {
    return modelAccess.removeXObjects(doc.getDocument(), toXObject(objsToRemove));
  }

  /**
   * programming rights needed
   */
  public boolean removeObjects(Document doc, DocumentReference classRef) {
    return removeObjects(doc, classRef, null, null);
  }

  /**
   * programming rights needed
   */
  public boolean removeObjects(Document doc, DocumentReference classRef, String key, Object value) {
    return removeObjects(doc, classRef, key, Arrays.asList(value));
  }

  /**
   * programming rights needed
   */
  public boolean removeObjects(Document doc, DocumentReference classRef, String key,
      Collection<?> values) {
    return modelAccess.removeXObjects(doc.getDocument(), classRef, key, values);
  }

  /**
   * programming rights needed
   */
  private List<BaseObject> toXObject(List<com.xpn.xwiki.api.Object> objs) {
    List<BaseObject> ret = new ArrayList<>();
    for (com.xpn.xwiki.api.Object obj : objs) {
      ret.add(obj.getXWikiObject());
    }
    return ret;
  }

}
