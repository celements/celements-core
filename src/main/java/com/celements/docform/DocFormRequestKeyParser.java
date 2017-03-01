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
  public static final String REGEX_FULLNAME = "[a-zA-Z0-9]+\\.[a-zA-Z0-9]+";
  public static final String REGEX_OBJNB = "[-^]?(\\d)*";
  public static final String REGEX_WHITELIST = "content|title";

  /**
   * Parses a given key string to {@link DocFormRequestKey} object
   *
   * @param key
   *          syntax: "[{fullName}_]{className}_[-|^]{objNb}_{fieldName}" whereas fullName
   *          is optional and objNb can be preceded by "-" to create or "^" to delete the
   *          object
   * @param defaultDocRef
   *          used if no fullName is provided in the key
   * @return key or null
   */
  public DocFormRequestKey parse(String key, DocumentReference defaultDocRef) {
    List<String> keyParts = new ArrayList<>(Arrays.asList(key.split(KEY_DELIM)));
    DocFormRequestKey ret;
    DocumentReference docRef = parseDocRef(keyParts, defaultDocRef);
    if (isWhiteListKey(keyParts)) {
      String fieldName = parseFieldName(keyParts);
      ret = new DocFormRequestKey(key, docRef, null, false, null, fieldName);
    } else if (isPropertyKey(keyParts)) {
      DocumentReference classRef = parseClassRef(keyParts);
      boolean remove = parseRemove(keyParts);
      Integer objNb = parseObjNb(keyParts);
      String fieldName = parseFieldName(keyParts);
      ret = new DocFormRequestKey(key, docRef, classRef, remove, objNb, fieldName);
    } else {
      LOGGER.info("parse: skipped key '{}'", key);
      ret = null;
    }
    if (validate(ret)) {
      return ret;
    } else {
      throw new IllegalArgumentException("Illegal request key '" + ret + "'");
    }
  }

  private DocumentReference parseDocRef(List<String> keyParts, DocumentReference defaultDocRef) {
    DocumentReference ret = null;
    if ((keyParts.size() > 1) && (keyParts.get(1).matches(REGEX_FULLNAME) || keyParts.get(
        1).matches(REGEX_WHITELIST))) {
      String fullName = keyParts.remove(0);
      if (fullName.matches(REGEX_FULLNAME)) {
        ret = getWebUtils().resolveDocumentReference(fullName);
      }
    } else {
      ret = defaultDocRef;
    }
    return ret;
  }

  private boolean isWhiteListKey(List<String> keyParts) {
    return (keyParts.size() == 1) && keyParts.get(0).matches(REGEX_WHITELIST);
  }

  private boolean isPropertyKey(List<String> keyParts) {
    return (keyParts.size() > 1) && keyParts.get(0).matches(REGEX_FULLNAME) && !keyParts.get(
        1).matches("nb"); // to skip "{classeName}_nb" keys
  }

  private DocumentReference parseClassRef(List<String> keyParts) {
    DocumentReference ret = null;
    if (getFirst(keyParts).matches(REGEX_FULLNAME)) {
      ret = getWebUtils().resolveDocumentReference(keyParts.remove(0));
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

  private boolean validate(DocFormRequestKey key) {
    boolean valid;
    if (key != null) {
      valid = (key.getDocRef() != null);
      if (!key.isWhiteListed()) {
        valid &= (key.getClassRef() != null);
        valid &= (key.getObjNb() != null);
        if (!key.isRemove()) {
          valid &= StringUtils.isNotBlank(key.getFieldName());
        }
      }
    } else {
      valid = true;
    }
    return valid;
  }

  /**
   * Parses given key strings to {@link DocFormRequestKey} objects. See
   * {@link #parse(String, DocumentReference)}
   *
   * @param keys
   * @param defaultDocRef
   * @return list of keys with no null elements
   */
  public Collection<DocFormRequestKey> parse(Collection<String> keys,
      DocumentReference defaultDocRef) {
    Collection<DocFormRequestKey> ret = new ArrayList<>();
    for (String keyString : keys) {
      if (StringUtils.isNotBlank(keyString)) {
        DocFormRequestKey key = parse(keyString, defaultDocRef);
        if ((key != null) && filterRequestKeySetForRemove(ret, key)) {
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

  private String getFirst(List<String> list) {
    if (list.size() > 0) {
      return list.get(0);
    }
    return "";
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
