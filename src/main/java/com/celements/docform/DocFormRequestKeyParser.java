package com.celements.docform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.web.Utils;

public class DocFormRequestKeyParser {

  private static Logger LOGGER = LoggerFactory.getLogger(DocFormRequestKeyParser.class);

  public static final String KEY_DELIM = "_";
  public static final String REGEX_FULLNAME = "([a-zA-Z0-9]*\\:)?[a-zA-Z0-9]*\\.[a-zA-Z0-9]*";
  public static final String REGEX_OBJNB = "[-^]?(\\d)*";
  public static final String REGEX_CONTENT_TITLE = "content|title";

  /**
   * Parses a given key string to {@link DocFormRequestKey} object
   * 
   * @param key syntax: "[fullName_]className_[-|^]objNb_fieldName" whereas fullName is 
   * optional and objNb can be preceded by "-" to create or "^" to delete the object
   * @param defaultDocRef used if no fullName is provided in the key
   * @return
   */
  public DocFormRequestKey parse(String key, DocumentReference defaultDocRef) {
    List<String> keyParts = new ArrayList<String>(Arrays.asList(key.split(KEY_DELIM)));
    DocumentReference docRef = parseDocRef(keyParts, defaultDocRef);
    DocumentReference classRef = parseClassRef(keyParts, docRef);
    boolean remove = parseRemove(keyParts);
    Integer objNb = parseObjNb(keyParts);
    String fieldName = parseFieldName(keyParts);
    DocFormRequestKey ret = new DocFormRequestKey(key, docRef, classRef, remove, objNb, 
        fieldName);
    if (validate(ret)) {
      return ret;
    } else {
      throw new IllegalArgumentException("Illegal request key '" + ret  + "'");
    }
  }

  private boolean validate(DocFormRequestKey key) {
    boolean valid = (key.getDocRef() != null);
    if (!key.getFieldName().matches(REGEX_CONTENT_TITLE)) {
      valid &= (key.getClassRef() != null);
      valid &= (key.getObjNb() != null);
      if (!key.isRemove()) {
        valid &= StringUtils.isNotBlank(key.getFieldName());
      }
    }
    return valid;
  }

  private DocumentReference parseDocRef(List<String> keyParts,
      DocumentReference defaultDocRef) {
    DocumentReference ret = defaultDocRef;
    if (keyParts.size() > 1 && keyParts.get(0).matches(REGEX_FULLNAME) 
        && (keyParts.get(1).matches(REGEX_FULLNAME) 
        || keyParts.get(1).matches(REGEX_CONTENT_TITLE))) {
      ret = getWebUtils().resolveDocumentReference(keyParts.remove(0));
    }
    return ret;
  }

  private DocumentReference parseClassRef(List<String> keyParts, 
      DocumentReference docRef) {
    DocumentReference ret = null;
    if (getFirst(keyParts).matches(REGEX_FULLNAME)) {
      ret = getWebUtils().resolveDocumentReference(keyParts.remove(0), 
          getWebUtils().getWikiRef(docRef));
    }
    return ret;
  }

  private boolean parseRemove(List<String> keyParts) {
    boolean ret = false;
    String str = getFirst(keyParts);
    if (str.matches(REGEX_OBJNB) && str.startsWith("^")) {
      keyParts.set(0, str.substring(1, str.length()));
      ret = true;
    }
    return ret;
  }

  private Integer parseObjNb(List<String> keyParts) {
    Integer ret = null;
    if (getFirst(keyParts).matches(REGEX_OBJNB)) {
      String intStr = keyParts.remove(0);
      try {
        ret = Integer.parseInt(intStr);
      } catch (NumberFormatException exc) {
        LOGGER.trace("Unable to parse: " + intStr, exc);
      }
    }
    return ret;
  }

  private String parseFieldName(List<String> keyParts) {
    return StringUtils.join(keyParts.iterator(), KEY_DELIM);
  }

  private String getFirst(List<String> list) {
    if (list.size() > 0) {
      return list.get(0);
    }
    return "";
  }

  /**
   * Parses given key strings to {@link DocFormRequestKey} objects. 
   * See {@link #parse(String, DocumentReference)}
   * @param keys
   * @param defaultDocRef
   * @return
   */
  public Collection<DocFormRequestKey> parse(Collection<String> keys, 
      DocumentReference defaultDocRef) {
    Collection<DocFormRequestKey> ret = new ArrayList<DocFormRequestKey>();
    for (String keyString : keys) {
      if (StringUtils.isNotBlank(keyString)) {
        DocFormRequestKey key = parse(keyString, defaultDocRef);
        if (filterRequestKeySetForRemove(ret, key)) {
          ret.add(key);
        }
      }
    }
    return ret;
  }

  private boolean filterRequestKeySetForRemove(Collection<DocFormRequestKey> keys, 
      DocFormRequestKey key) {
    boolean ret = true;
    Iterator<DocFormRequestKey> iter = keys.iterator();
    while (iter.hasNext()) {
      DocFormRequestKey currKey = iter.next();
      if (key.sameObject(currKey)) {
        if (key.isRemove()) {
          iter.remove();
        } else if (currKey.isRemove()) {
          ret = false;
        }
      }
    }
    return ret;
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
