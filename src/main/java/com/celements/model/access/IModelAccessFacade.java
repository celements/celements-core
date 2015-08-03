package com.celements.model.access;

import java.util.Collection;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IModelAccessFacade {

  public XWikiDocument getDocument(DocumentReference docRef) throws XWikiException;

  public void saveDocument(XWikiDocument doc) throws XWikiException;

  public void saveDocument(XWikiDocument doc, String comment) throws XWikiException;

  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit)
      throws XWikiException;

  /**
   * @param docRef
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @return the xobject or null
   * @throws XWikiException
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws XWikiException;

  /**
   * @param doc
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @return the xobject or null
   */
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param docRef
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @param key
   *          for field specific xobject filtering (null means no filtering)
   * @param value
   *          for field specific xobject filtering
   * @return the xobject or null
   * @throws XWikiException
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws XWikiException;

  /**
   * @param doc
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @param key
   *          for field specific xobject filtering (null means no filtering)
   * @param value
   *          for field specific xobject filtering
   * @return the xobject or null
   */
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value);

  /**
   * @param docRef
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @return a list of xobjects (without null values) or empty list
   * @throws XWikiException
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef)
      throws XWikiException;

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @return a list of xobjects (without null values) or empty list
   */
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param docRef
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return a list of xobjects (without null values) or empty list
   * @throws XWikiException
   */
  public List<BaseObject> getXObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Object value) throws XWikiException;

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
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Object value);

  /**
   * @param docRef
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return a list of xobjects (without null values) or empty list
   * @throws XWikiException
   */
  public List<BaseObject> getXObjects(DocumentReference docRef,
      DocumentReference classRef, String key, Collection<?> values) throws XWikiException;

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
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Collection<?> values);

  /**
   * @param docRef
   *          to get new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create
   * @return newly created xobject
   * @throws XWikiException
   */
  public BaseObject newXObject(DocumentReference docRef, DocumentReference classRef)
      throws XWikiException;

  /**
   * @param doc
   *          to get new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create
   * @return newly created xobject
   * @throws XWikiException
   */
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef)
      throws XWikiException;

  /**
   * @param docRef
   *          to remove xobject on (may not be null)
   * @param objsToRemove
   *          xobject to remove
   * @return true if doc has changed
   * @throws XWikiException
   */
  public boolean removeXObject(DocumentReference docRef, BaseObject objToRemove)
      throws XWikiException;

  /**
   * @param doc
   *          to remove xobject on (may not be null)
   * @param objsToRemove
   *          xobject to remove
   * @return true if doc has changed
   */
  public boolean removeXObject(XWikiDocument doc, BaseObject objToRemove);

  /**
   * @param docRef
   *          to remove xobjects on (may not be null)
   * @param objsToRemove
   *          xobjects to remove
   * @return true if doc has changed
   * @throws XWikiException
   */
  public boolean removeXObjects(DocumentReference docRef, List<BaseObject> objsToRemove)
      throws XWikiException;

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param objsToRemove
   *          xobjects to remove
   * @return true if doc has changed
   */
  public boolean removeXObjects(XWikiDocument doc, List<BaseObject> objsToRemove);

  /**
   * @param docRef
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @return true if doc has changed
   * @throws XWikiException
   */
  public boolean removeXObjects(DocumentReference docRef, DocumentReference classRef)
      throws XWikiException;

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @return true if doc has changed
   */
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param docRef
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return true if doc has changed
   * @throws XWikiException
   */
  public boolean removeXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws XWikiException;

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
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Object value);

  /**
   * @param docRef
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return true if doc has changed
   * @throws XWikiException
   */
  public boolean removeXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws XWikiException;

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
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef,
      String key, Collection<?> values);

}
