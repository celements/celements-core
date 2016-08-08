package com.celements.model.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ObjectPropertyReference;

import com.celements.model.access.IModelAccessFacade;
import com.google.common.base.Objects;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultXObjectUpdateService implements IXObjectUpdateRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultXObjectUpdateService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IModelUtils modelUtils;

  @Override
  @Deprecated
  public boolean updateFromMap(XWikiDocument doc, Map<String, Object> fieldMap) {
    boolean hasChanged = false;
    Map<String, BaseObject> objMap = getObjectMap(doc, fieldMap.keySet());
    for (String field : fieldMap.keySet()) {
      ObjectPropertyReference fieldRef = resolveFieldRef(field, doc);
      BaseObject obj = objMap.get(fieldRef.getParent().getName());
      Object newVal = fieldMap.get(field);
      Object oldVal = modelAccess.getProperty(obj, fieldRef.getName());
      if (!ObjectUtils.equals(oldVal, newVal)) {
        modelAccess.setProperty(obj, fieldRef.getName(), newVal);
        hasChanged = true;
        LOGGER.trace("update: '" + field + "' has changed from '" + oldVal + "' to '" + newVal
            + "'");
      }
    }
    LOGGER.info("update: for doc " + doc + "' and map '" + fieldMap + "' hasChanged '" + hasChanged
        + "'");
    return hasChanged;
  }

  private Map<String, BaseObject> getObjectMap(XWikiDocument doc, Set<String> fields) {
    Map<String, BaseObject> ret = new HashMap<>();
    for (String field : fields) {
      ObjectPropertyReference fieldRef = resolveFieldRef(field, doc);
      String className = fieldRef.getParent().getName();
      if (!ret.containsKey(className)) {
        BaseObject obj = modelAccess.getOrCreateXObject(doc, modelUtils.resolveRef(className,
            DocumentReference.class));
        ret.put(className, obj);
      }
    }
    LOGGER.trace("getObjectMap: got for doc '" + doc + "': " + ret.keySet());
    return ret;
  }

  private ObjectPropertyReference resolveFieldRef(String field, XWikiDocument doc) {
    return modelUtils.resolveRef(field, ObjectPropertyReference.class, doc.getDocumentReference());
  }

  @Override
  public boolean update(XWikiDocument doc, Set<ClassFieldValue<?>> fieldValues) {
    boolean hasChanged = false;
    for (ClassFieldValue<?> fieldValue : fieldValues) {
      Object oldVal = modelAccess.getProperty(doc, fieldValue.getField());
      if (!Objects.equal(oldVal, fieldValue.getValue())) {
        modelAccess.setProperty(doc, fieldValue);
        hasChanged = true;
        LOGGER.trace("update: '{}' has changed from '{}'", fieldValue.getField(), oldVal);
      }
    }
    LOGGER.info("update: for doc '{}' hasChanged '{}'", doc, hasChanged);
    return hasChanged;
  }

}
