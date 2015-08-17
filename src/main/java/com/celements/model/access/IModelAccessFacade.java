package com.celements.model.access;

import java.util.Collection;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IModelAccessFacade {

  public XWikiDocument getDocument(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException;

  public XWikiDocument createDocument(DocumentReference docRef
      ) throws DocumentLoadException, DocumentAlreadyExistsException;

  public XWikiDocument getOrCreateDocument(DocumentReference docRef
      ) throws DocumentLoadException;

  public boolean exists(DocumentReference docRef);

  public void saveDocument(XWikiDocument doc) throws DocumentSaveException;

  public void saveDocument(XWikiDocument doc, String comment) throws DocumentSaveException;

  public void saveDocument(XWikiDocument doc, String comment, boolean isMinorEdit
      ) throws DocumentSaveException;

  public void deleteDocument(DocumentReference docRef, boolean totrash
      ) throws DocumentLoadException, DocumentDeleteException;

  public void deleteDocument(XWikiDocument doc, boolean totrash
      ) throws DocumentDeleteException;

  public void deleteDocumentWithoutTranslations(XWikiDocument doc, boolean totrash
      ) throws DocumentDeleteException;

  /**
   * @param docRef
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @return the xobject or null
   * @throws DocumentLoadException
   *           if unable to load the document
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef
      ) throws DocumentLoadException, DocumentNotExistsException;

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
   * @throws DocumentLoadException
   *           if unable to load the document
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentLoadException, DocumentNotExistsException;

  /**
   * @param doc
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get
   * @return the xobject or null
   */
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef);

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
   * @throws DocumentLoadException if unable to load the document
   * @throws DocumentNotExistsException if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef
      ) throws DocumentLoadException, DocumentNotExistsException;

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
   * @throws DocumentLoadException if unable to load the document
   * @throws DocumentNotExistsException if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentLoadException, DocumentNotExistsException;

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
   * @throws DocumentLoadException if unable to load the document
   * @throws DocumentNotExistsException if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentLoadException, 
      DocumentNotExistsException;

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get
   * @return a list of xobjects (without null values) or empty list
   */
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef);

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
   * @param doc
   *          to get new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create (may not be null)
   * @return newly created xobject
   * @throws ClassDocumentLoadException
   *           if unable to load class document
   */
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef
      ) throws ClassDocumentLoadException;

  /**
   * @param doc
   *          to remove xobject on (may not be null)
   * @param objsToRemove
   *          xobject to remove
   * @return true if doc has changed
   */
  public boolean removeXObject(XWikiDocument doc, BaseObject objToRemove);

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param objsToRemove
   *          xobjects to remove
   * @return true if doc has changed
   */
  public boolean removeXObjects(XWikiDocument doc, List<BaseObject> objsToRemove);

  /**
   * @param doc
   *          to remove xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to remove
   * @return true if doc has changed
   */
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef);

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
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value);

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
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values);

  public Object getProperty(DocumentReference docRef, DocumentReference classRef,
      String name) throws DocumentLoadException, DocumentNotExistsException;

  public Object getProperty(XWikiDocument doc, DocumentReference classRef, String name);

  /**
   * Reads out the property value for the given BaseObject and name
   * @param obj
   * @param name
   * @return
   */
  public Object getProperty(BaseObject obj, String name);

  public void setProperty(BaseObject obj, String name, Object value);

}
