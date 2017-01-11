/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestKeyParser;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * DocFormCommand handles validation of a request with document/object fields and ensures
 * that they are correctly prepared for save. IMPORTANT: use exactly ONE instance of this
 * class for each XWikiContext (each request).
 *
 * @author fabian
 */
public class DocFormCommand {

  private static Logger LOGGER = LoggerFactory.getLogger(DocFormCommand.class);

  private final Map<String, BaseObject> changedObjects = new HashMap<>();
  private final Map<String, XWikiDocument> changedDocs = new HashMap<>();

  /**
   * @Deprecated: since 2.59 instead use variable in
   *              {@link #updateDocFromMap(DocumentReference, Map, XWikiContext)}
   */
  @Deprecated
  public Set<XWikiDocument> updateDocFromMap(String fullname, Map<String, String[]> data,
      XWikiContext context) throws XWikiException {
    return updateDocFromMap(getModelUtils().resolveRef(fullname, DocumentReference.class), data,
        context);
  }

  public Set<XWikiDocument> updateDocFromMap(DocumentReference docRef, Map<String, String[]> data,
      XWikiContext context) throws XWikiException {
    XWikiDocument doc = getUpdateDoc(docRef, context);
    String template = Strings.nullToEmpty(context.getRequest().getParameter("template")).trim();
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("updateDocFromMap: updating doc '{}' with template '{}' and request " + "'{}'",
          doc, template, context.getRequest().getParameterMap());
    } else {
      LOGGER.debug("updateDocFromMap: updating doc '{}' with template '{}'", doc, template);
    }
    if (doc.isNew() && !template.isEmpty()) {
      DocumentReference templRef = getModelUtils().resolveRef(template, DocumentReference.class);
      try {
        doc.readFromTemplate(templRef, context);
      } catch (XWikiException e) {
        if (e.getCode() == XWikiException.ERROR_XWIKI_APP_DOCUMENT_NOT_EMPTY) {
          context.put("exception", e);
        }
        LOGGER.error("Exception reading doc " + docRef + " from template " + templRef, e);
      }
    }
    DocFormRequestKeyParser parser = new DocFormRequestKeyParser();
    for (DocFormRequestKey key : parser.parse(data.keySet(), docRef)) {
      String value = collapse(data.get(key.getKeyString()));
      LOGGER.debug("updateDocFromMap: request key '{}' with value '{}'", key, value);
      if (key.isWhiteListed()) {
        XWikiDocument tSaveDoc = getTranslatedDoc(key.getDocRef(), context);
        if (key.getFieldName().equals("content")) {
          tSaveDoc.setContent(value);
        } else if (key.getFieldName().equals("title")) {
          tSaveDoc.setTitle(value);
        } else {
          LOGGER.info("updateDocFromMap: unknown field name in key '{}'", key);
        }
      } else {
        XWikiDocument saveDoc = getUpdateDoc(key.getDocRef(), context);
        setOrRemoveObj(saveDoc, key, value, context);
      }
    }
    return new HashSet<>(changedDocs.values());
  }

  public Map<String, String[]> prepareMapForDocUpdate(Map<String, ?> map) {
    Map<String, String[]> recompMap = new HashMap<>();
    for (String key : map.keySet()) {
      if (map.get(key) instanceof String[]) {
        recompMap.put(key, (String[]) map.get(key));
      } else if (map.get(key) instanceof String) {
        recompMap.put(key, new String[] { (String) map.get(key) });
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("recompiled map '{}' to '{}'", map, recompMap);
    }
    return recompMap;
  }

  public Map<String, Set<DocumentReference>> saveXWikiDocCollection(
      Collection<XWikiDocument> xdocs) {
    Set<DocumentReference> savedSuccessfully = new HashSet<>();
    Set<DocumentReference> saveFailed = new HashSet<>();
    for (XWikiDocument xdoc : xdocs) {
      if (notNewOrCreateAllowed(xdoc)) {
        try {
          getModelAccess().saveDocument(xdoc, "updateAndSaveDocFromRequest");
          savedSuccessfully.add(xdoc.getDocumentReference());
        } catch (DocumentSaveException dse) {
          LOGGER.error("Exception saving document {}.", xdoc, dse);
          saveFailed.add(xdoc.getDocumentReference());
        }
      } else {
        saveFailed.add(xdoc.getDocumentReference());
      }
    }
    Map<String, Set<DocumentReference>> docs = new HashMap<>();
    docs.put("successful", savedSuccessfully);
    docs.put("failed", saveFailed);
    return docs;
  }

  public boolean notNewOrCreateAllowed(XWikiDocument xdoc) {
    return !xdoc.isNew() || "true".equals(getContext().getRequest().get("createIfNotExists"));
  }

  XWikiDocument getUpdateDoc(DocumentReference docRef, XWikiContext context) throws XWikiException {
    XWikiDocument doc = getModelAccess().getOrCreateDocument(docRef);
    if (doc.isNew() && "".equals(doc.getDefaultLanguage())) {
      doc.setDefaultLanguage(context.getLanguage());
    }
    if (!changedDocs.containsKey(serialize(docRef) + ";" + doc.getDefaultLanguage())) {
      LOGGER.debug("getUpdateDoc: [" + serialize(docRef) + ";" + doc.getDefaultLanguage()
          + "] with doc language [" + doc.getLanguage() + "].");
      applyCreationDateFix(doc, context);
      changedDocs.put(serialize(docRef) + ";" + doc.getDefaultLanguage(), doc);
    } else {
      doc = changedDocs.get(serialize(docRef) + ";" + doc.getDefaultLanguage());
    }
    return doc;
  }

  XWikiDocument getTranslatedDoc(DocumentReference docRef, XWikiContext context)
      throws XWikiException {
    XWikiDocument tdoc;
    if (!changedDocs.containsKey(serialize(docRef) + ";" + context.getLanguage())) {
      XWikiDocument doc = getUpdateDoc(docRef, context);
      tdoc = new AddTranslationCommand().getTranslatedDoc(doc, context.getLanguage());
      changedDocs.put(serialize(docRef) + ";" + context.getLanguage(), tdoc);
    } else {
      tdoc = changedDocs.get(serialize(docRef) + ";" + context.getLanguage());
    }
    return tdoc;
  }

  XWikiDocument setOrRemoveObj(XWikiDocument doc, DocFormRequestKey key, String value,
      XWikiContext context) throws XWikiException {
    BaseObject obj = changedObjects.get(getObjCacheKey(key));
    if (obj == null) {
      obj = doc.getXObject(key.getClassRef(), key.getObjNb());
      if ((obj == null) || (key.getObjNb() < 0)) {
        obj = doc.newXObject(key.getClassRef(), context);
        LOGGER.debug("setOrRemoveObj: new obj for key '{}'", key);
      }
      changedObjects.put(getObjCacheKey(key), obj);
      LOGGER.debug("setOrRemoveObj: got obj for key '{}'", key);
    }
    if (obj != null) {
      if (key.isRemove()) {
        boolean success = doc.removeXObject(obj);
        LOGGER.debug("setOrRemoveObj: removing obj for key '{}' was success '{}'", key, success);
      } else {
        obj.set(key.getFieldName(), value, context);
        LOGGER.debug("setOrRemoveObj: set value '{}' for key '{}'", value, key);
      }
    }
    return doc;
  }

  private String getObjCacheKey(DocFormRequestKey key) {
    return serialize(key.getDocRef()) + DocFormRequestKeyParser.KEY_DELIM + serialize(
        key.getClassRef()) + DocFormRequestKeyParser.KEY_DELIM + key.getObjNb();
  }

  void applyCreationDateFix(XWikiDocument doc, XWikiContext context) {
    // FIXME Should be done when xwiki saves a new document. Unfortunately it is done
    // when you first get a document and than cached.
    if (doc.isNew()) {
      doc.setCreationDate(new Date());
      doc.setCreator(context.getUser());
    }
  }

  /**
   * @deprecated since 2.2.0 instead use FormValidationService
   */
  @Deprecated
  public Map<String, String> validateRequest(XWikiContext context) {
    XWikiRequest request = context.getRequest();
    Map<String, String> result = new HashMap<>();
    for (Object paramObj : request.getParameterMap().keySet()) {
      String param = paramObj.toString();

      String validate = validateParameter(param, request.get(param), context);
      if (validate != null) {
        result.put(param, validate);
      }
    }

    return result;
  }

  /**
   * @deprecated since 2.2.0 instead use FormValidationService
   */
  @Deprecated
  private String validateParameter(String param, String value, XWikiContext context) {
    if (param.matches(getFindObjectFieldInRequestRegex())) {
      int pos = 0;
      if (param.matches(getRequestParamIncludesDocNameRegex())) {
        pos = 1;
      }
      String[] paramSplit = param.split(DocFormRequestKeyParser.KEY_DELIM);
      String className = paramSplit[pos];
      String fieldName = paramSplit[pos + 2];
      for (int i = pos + 3; i < paramSplit.length; i++) {
        fieldName += DocFormRequestKeyParser.KEY_DELIM + paramSplit[i];
      }
      return validateField(className, fieldName, value, context);
    }
    return null;
  }

  String getFindObjectFieldInRequestRegex() {
    return "([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}[-^]?(\\d)*_(.*)";
  }

  private String getRequestParamIncludesDocNameRegex() {
    return "([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}[-^]?(\\d)*_(.*)";
  }

  /**
   * @deprecated since 2.2.0 instead use FormValidationService
   */
  @Deprecated
  public String validateField(String className, String fieldName, String value,
      XWikiContext context) {
    String isValidResult = null;
    BaseClass bclass = getBaseClass(className, context);

    if (bclass != null) {
      PropertyClass propclass = (PropertyClass) bclass.getField(fieldName);

      String regexp = getFieldFromProperty(propclass, "validationRegExp");
      String validationMsg = getFieldFromProperty(propclass, "validationMessage");
      if ((regexp != null) && !regexp.trim().equals("")) {
        try {
          if (!context.getUtil().match(regexp, value)) {
            isValidResult = validationMsg;
          }
        } catch (MalformedPerl5PatternException exp) {
          LOGGER.error("Failed to execute validation regex for field [" + fieldName + "] in class ["
              + className + "].", exp);
          isValidResult = validationMsg;
        }
      }
    }

    return isValidResult;
  }

  /**
   * @deprecated since 2.2.0 instead use FormValidationService
   */
  @Deprecated
  String getFieldFromProperty(PropertyClass propclass, String field) {
    BaseProperty prop = (BaseProperty) propclass.getField(field);
    if ((prop != null) && (prop.getValue() != null)) {
      return prop.getValue().toString();
    }
    return "";
  }

  private BaseClass getBaseClass(String className, XWikiContext context) {
    BaseClass bclass = null;
    className = Strings.nullToEmpty(className);
    if (!className.isEmpty()) {
      DocumentReference bclassDocRef = getModelUtils().resolveRef(className,
          DocumentReference.class);
      try {
        bclass = getModelAccess().getDocument(bclassDocRef).getXClass();
      } catch (DocumentNotExistsException exp) {
        LOGGER.error("Cannot get document class [" + className + "].", exp);
      }
    }
    return bclass;
  }

  String collapse(String[] value) {
    List<String> valueList = new ArrayList<>(Arrays.asList(value));
    valueList.removeAll(Arrays.asList((String) null));
    valueList.removeAll(Arrays.asList(""));
    return StringUtils.join(valueList.toArray(), "|");
  }

  private String serialize(DocumentReference docRef) {
    return getModelUtils().serializeRef(docRef);
  }

  Map<String, BaseObject> getChangedObjects() {
    return changedObjects;
  }

  Map<String, XWikiDocument> getChangedDocs() {
    return changedDocs;
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

}
