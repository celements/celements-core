package com.celements.model.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultXObjectUpdateService implements IXObjectUpdateRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultXObjectUpdateService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  @Deprecated
  public boolean updateFromMap(XWikiDocument doc, Map<String, Object> fieldMap)
      throws ClassDocumentLoadException {
    boolean hasChanged = false;
    Map<String, BaseObject> objMap = getObjectMap(doc, fieldMap.keySet());
    for (String field : fieldMap.keySet()) {
      BaseObject obj = objMap.get(extractClassName(field));
      Object newVal = fieldMap.get(field);
      String fieldName = extractFieldName(field);
      Object oldVal = modelAccess.getProperty(obj, fieldName);
      if (!ObjectUtils.equals(oldVal, newVal)) {
        modelAccess.setProperty(obj, fieldName, newVal);
        hasChanged = true;
        LOGGER.trace("update: '" + field + "' has changed from '" + oldVal + "' to '" + newVal
            + "'");
      }
    }
    LOGGER.info("update: for doc " + doc + "' and map '" + fieldMap + "' hasChanged '" + hasChanged
        + "'");
    return hasChanged;
  }

  private Map<String, BaseObject> getObjectMap(XWikiDocument doc, Set<String> fields)
      throws ClassDocumentLoadException {
    Map<String, BaseObject> ret = new HashMap<>();
    for (String field : fields) {
      String className = extractClassName(field);
      if (!ret.containsKey(className)) {
        BaseObject obj = modelAccess.getOrCreateXObject(doc,
            webUtilsService.resolveDocumentReference(className));
        ret.put(className, obj);
      }
    }
    LOGGER.trace("getObjectMap: got for doc '" + doc + "': " + ret.keySet());
    return ret;
  }

  private String extractClassName(String str) {
    String className = "";
    try {
      className = str.substring(0, str.lastIndexOf("."));
    } catch (IndexOutOfBoundsException exc) {
      LOGGER.info("failed to extract className from '{}'");
    }
    return className;
  }

  private String extractFieldName(String str) {
    String fieldName = "";
    try {
      fieldName = str.substring(str.lastIndexOf(".") + 1);
    } catch (IndexOutOfBoundsException exc) {
      LOGGER.info("failed to extract fieldName from '{}'");
    }
    return fieldName;
  }

  @Override
  public boolean update(XWikiDocument doc, Set<XObjectFieldValue<?>> fieldValues)
      throws ClassDocumentLoadException {
    boolean hasChanged = false;
    for (XObjectFieldValue<?> field : fieldValues) {
      Object oldVal = modelAccess.getProperty(doc, field);
      if (!ObjectUtils.equals(oldVal, field.getValue())) {
        modelAccess.setProperty(doc, field);
        hasChanged = true;
        LOGGER.trace("update: '" + field + "' has changed from '" + oldVal + "'");
      }
    }
    LOGGER.info("update: for doc " + doc + "' hasChanged '" + hasChanged + "'");
    return hasChanged;
  }

}
