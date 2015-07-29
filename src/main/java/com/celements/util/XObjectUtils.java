package com.celements.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Objects;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

public class XObjectUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(XObjectUtils.class);

  private XObjectUtils() {
  }

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @return a list of xobjects (without null values) or empty list
   */
  public static List<BaseObject> getXObjects(XWikiDocument doc,
      DocumentReference classRef) {
    return getXObjects(doc, classRef, null, null);
  }

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return a list of xobjects (without null values) or empty list
   */
  public static List<BaseObject> getXObjects(XWikiDocument doc,
      DocumentReference classRef, String key, Object value) {
    return getXObjects(doc, classRef, key, Arrays.asList(value));
  }

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return a list of xobjects (without null values) or empty list
   */
  public static List<BaseObject> getXObjects(XWikiDocument doc,
      DocumentReference classRef, String key, Collection<?> values) {
    List<BaseObject> ret = new ArrayList<>();
    for (BaseObject obj : Objects.firstNonNull(doc.getXObjects(classRef),
        Collections.<BaseObject> emptyList())) {
      if ((obj != null) && checkPropertyKeyValues(obj, key, values)) {
        ret.add(obj);
      }
    }
    return ret;
  }

  private static boolean checkPropertyKeyValues(BaseObject obj, String key,
      Collection<?> values) {
    boolean valid = (key == null);
    if (!valid) {
      BaseProperty prop = getProperty(obj, key);
      if (prop != null) {
        for (Object val : Objects.firstNonNull(values, Collections.emptyList())) {
          valid |= Objects.equal(val, prop.getValue());
        }
      }
    }
    return valid;
  }

  private static BaseProperty getProperty(BaseObject obj, String key) {
    BaseProperty prop = null;
    try {
      prop = (BaseProperty) obj.get(key);
    } catch (XWikiException xwe) {
      // does not happen since XWikiException is never thrown in BaseObject.get()
      LOGGER.error("should not happen", xwe);
    }
    return prop;
  }

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param objsToRemove
   *          xobjects to remove
   * @return true if doc has changed
   */
  public static boolean removeXObjects(XWikiDocument doc, List<BaseObject> objsToRemove) {
    boolean changed = false;
    for (BaseObject obj : new ArrayList<>(objsToRemove)) {
      if (obj != null) {
        changed |= doc.removeXObject(obj);
      }
    }
    return changed;
  }

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @return true if doc has changed
   */
  public static boolean removeXObjects(XWikiDocument doc, DocumentReference classRef) {
    return removeXObjects(doc, getXObjects(doc, classRef));
  }

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return true if doc has changed
   */
  public static boolean removeXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Object value) {
    return removeXObjects(doc, getXObjects(doc, classRef, key, value));
  }

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return true if doc has changed
   */
  public static boolean removeXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, List<Object> values) {
    return removeXObjects(doc, getXObjects(doc, classRef, key, values));
  }

}
