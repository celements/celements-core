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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
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
 * DocFormCommand handles validation of a request with document/object fields
 * and ensures that they are correctly prepared for save.
 * 
 * IMPORTANT: use exactly ONE instance of this class for each XWikiContext (each request).
 * 
 * @author fabian
 *
 */
public class DocFormCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DocFormCommand.class);
  private Map<String, BaseObject> changedObjects = new HashMap<String, BaseObject>();
  private Map<String, XWikiDocument> changedDocs = new HashMap<String, XWikiDocument>();

  public Set<XWikiDocument> updateDocFromMap(String fullname, Map<String, String[]> data,
      XWikiContext context) throws XWikiException {
    String docSpace = fullname.split("\\.")[0];
    String docName = fullname.split("\\.")[1];
    DocumentReference docRef = new DocumentReference(context.getDatabase(), docSpace,
        docName);
    XWikiDocument doc = context.getWiki().getDocument(docRef, context);
    String template = context.getRequest().getParameter("template");
    if(doc.isNew() && !"".equals(template.trim())) {
      String templSpace = template.split("\\.")[0];
      String templName = template.split("\\.")[1];
      DocumentReference templRef = new DocumentReference(context.getDatabase(),
          templSpace, templName);
      try {
        doc.readFromTemplate(templRef, context);
      } catch (XWikiException e) {
        if (e.getCode() == XWikiException.ERROR_XWIKI_APP_DOCUMENT_NOT_EMPTY) {
            context.put("exception", e);
        }
        LOGGER.error("Exception reading doc " + docRef + " from template " + templRef, e);
      }
    }
    for (String key : data.keySet()) {
      if(key != null) {
        String[] parts = getDocFullname(key, docSpace, docName);
        DocumentReference saveDocRef = new DocumentReference(context.getDatabase(
            ), parts[0], parts[1]);
        LOGGER.debug("request key:'" + key + "'=value:" + data.get(key).length);
        if(key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_)?content")) {
          XWikiDocument tdoc = getTranslatedDoc(saveDocRef, context);
          tdoc.setContent(collapse(data.get(key)));
        } else if(key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_)?title")) {
          XWikiDocument tdoc = getTranslatedDoc(saveDocRef, context);
          tdoc.setTitle(collapse(data.get(key)));
        } else if(key.matches(getFindObjectFieldInRequestRegex())) {
          XWikiDocument saveDoc = getUpdateDoc(saveDocRef, context);
          setObjValue(saveDoc, parts[2], data.get(key), context);
        }
      }
    }
    HashSet<XWikiDocument> docSet = new HashSet<XWikiDocument>();
    docSet.addAll(changedDocs.values());
    return docSet;
  }

  String getFindObjectFieldInRequestRegex() {
    return "([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}-?(\\d)*_(.*)";
  }

  String[] getDocFullname(String key, String defaultSpace, String defaultName) {
    String docName = defaultName;
    String docSpace = defaultSpace;
    String fieldName = key;
    if(key.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1}(content|title)") ||
        key.matches(getRequestParamIncludesDocNameRegex())) {
      String docFullName = key.split("_")[0];
      docSpace = docFullName.split("\\.")[0];
      docName = docFullName.split("\\.")[1];
      fieldName = key.substring(docFullName.length() + 1);
    }
    return new String[]{ docSpace, docName, fieldName };
  }

  String getRequestParamIncludesDocNameRegex() {
    return "([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}-?(\\d)*_(.*)";
  }

  XWikiDocument setObjValue(XWikiDocument doc, String key,
      String[] value, XWikiContext context) throws XWikiException {
    String className = key.substring(0, key.indexOf("_"));
    DocumentReference classRef = getWebUtilsService().resolveDocumentReference(className);
    LOGGER.debug("key complete: " + key);
    key = key.substring(key.indexOf("_") + 1);
    Integer objNr = Integer.parseInt(key.substring(0, key.indexOf("_")));
    LOGGER.debug("key -1 part: " + key);
    key = key.substring(key.indexOf("_") + 1);
    LOGGER.debug("key -2 parts: " + key);
    BaseObject obj = null;
    if(changedObjects.containsKey(getObjCacheMapKey(doc, className, objNr))) {
      obj = changedObjects.get(getObjCacheMapKey(doc, className, objNr));
    } else {
      obj = doc.getXObject(classRef, objNr);
      if((obj == null) || (objNr < 0)) {
        obj = doc.newXObject(classRef, context);
        LOGGER.debug("newObject for classname [" + className + "] on doc [" + doc
            + "] <-> [" + obj.getNumber() + "] <-> [" + objNr + "].");
      }
      changedObjects.put(getObjCacheMapKey(doc, className, objNr), obj);
      LOGGER.debug("got object for classname [" + className + "] on doc [" + doc
          + "] <-> [" + obj.getNumber() + "] <-> [" + objNr + "].");
    }
    obj.set(key, collapse(value), context);
    return doc;
  }

  private String getObjCacheMapKey(XWikiDocument doc, String className,
      Integer objNr) {
    DocumentReference docRef = doc.getDocumentReference();
    return docRef.getLastSpaceReference().getName() + "." + docRef.getName() + "_"
        + className + "_" + objNr;
  }

  String collapse(String[] value) {
    List<String> valueList = new ArrayList<String>(Arrays.asList(value));
    valueList.removeAll(Arrays.asList((String)null));
    valueList.removeAll(Arrays.asList(""));
    return StringUtils.join(valueList.toArray(), "|");
  }

  XWikiDocument getUpdateDoc(DocumentReference docRef, XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc = context.getWiki().getDocument(docRef, context);
    if (doc.isNew() && "".equals(doc.getDefaultLanguage())) {
      doc.setDefaultLanguage(context.getLanguage());
    }
    if(!changedDocs.containsKey(getFullNameForRef(docRef) + ";" + doc.getDefaultLanguage()
        )) {
      LOGGER.debug("getUpdateDoc: [" + getFullNameForRef(docRef) + ";"
          + doc.getDefaultLanguage() + "] with doc language [" + doc.getLanguage()
          + "].");
      applyCreationDateFix(doc, context);
      changedDocs.put(getFullNameForRef(docRef) + ";" + doc.getDefaultLanguage(), doc);
    } else {
      doc = changedDocs.get(getFullNameForRef(docRef) + ";" + doc.getDefaultLanguage());
    }
    return doc;
  }
  
  XWikiDocument getTranslatedDoc(DocumentReference docRef, XWikiContext context
      ) throws XWikiException {
    XWikiDocument tdoc;
    if(!changedDocs.containsKey(getFullNameForRef(docRef) + ";" + context.getLanguage())
        ) {
      XWikiDocument doc = getUpdateDoc(docRef, context);
      tdoc = new AddTranslationCommand().getTranslatedDoc(doc, context.getLanguage());
      changedDocs.put(getFullNameForRef(docRef) + ";" + context.getLanguage(), tdoc);
    } else {
      tdoc = changedDocs.get(getFullNameForRef(docRef) + ";" + context.getLanguage());
    }
    return tdoc;
  }

  void applyCreationDateFix(XWikiDocument doc, XWikiContext context) {
    //FIXME Should be done when xwiki saves a new document. Unfortunately it is done
    //      when you first get a document and than cached.
    if(doc.isNew()) {
      doc.setCreationDate(new Date());
      doc.setCreator(context.getUser());
    }
  }

  private String getFullNameForRef(DocumentReference docRef) {
    return docRef.getLastSpaceReference().getName() + "." + docRef.getName();
  }

  public Map<String, String> validateRequest(XWikiContext context) {
    XWikiRequest request = context.getRequest();
    Map<String, String> result = new HashMap<String, String>();
    for (Object paramObj : request.getParameterMap().keySet()) {
      String param = paramObj.toString();
      
      String validate = validateParameter(param, request.get(param), context);
      if(validate != null) {
        result.put(param, validate);
      }
    }
    
    return result;
  }

  private String validateParameter(String param, String value,
      XWikiContext context) {
    if(param.matches(getFindObjectFieldInRequestRegex())){
      int pos = 0;
      if(param.matches(getRequestParamIncludesDocNameRegex())) { pos = 1; }
      String[] paramSplit = param.split("_");
      String className = paramSplit[pos];
      String fieldName = paramSplit[pos+2];
      for(int i = pos+3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return validateField(className, fieldName, value, context);
    }
    return null;
  }

  public String validateField(String className, String fieldName, String value,
      XWikiContext context) {
    String isValidResult = null;
    BaseClass bclass = getBaseClass(className, context);
    
    if(bclass != null) {
      PropertyClass propclass = (PropertyClass)bclass.getField(fieldName);

      String regexp = getFieldFromProperty(propclass, "validationRegExp");
      String validationMsg = getFieldFromProperty(propclass, "validationMessage");
      if ((regexp != null) && !regexp.trim().equals("")) {
        try {
          if(!context.getUtil().match(regexp, value)) {
            isValidResult = validationMsg;
          }
        } catch (MalformedPerl5PatternException exp) {
          LOGGER.error("Failed to execute validation regex for field [" + fieldName
              + "] in class [" + className + "].", exp);
          isValidResult = validationMsg;
        }
      }
    }
    
    return isValidResult;
  }

  String getFieldFromProperty(PropertyClass propclass, String field) {
    BaseProperty prop = (BaseProperty) propclass.getField(field);
    if ((prop != null) && (prop.getValue() != null)) {
      return prop.getValue().toString();
    }
    return "";
  }

  private BaseClass getBaseClass(String className, XWikiContext context) {
    BaseClass bclass = null;
    DocumentReference bclassDocRef = getWebUtilsService().resolveDocumentReference(
        className);
    try {
      bclass = context.getWiki().getDocument(bclassDocRef, context).getXClass();
    } catch (XWikiException exp) {
      LOGGER.error("Cannot get document class [" + className + "].", exp);
    }
    return bclass;
  }

  Map<String, BaseObject> getChangedObjects() {
    return changedObjects;
  }

  Map<String, XWikiDocument> getChangedDocs() {
    return changedDocs;
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
