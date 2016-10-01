package com.celements.model.access;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentAlreadyExistsException;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.ClassFieldValue;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IModelAccessFacade {

  @NotNull
  public XWikiDocument getDocument(@NotNull DocumentReference docRef)
      throws DocumentNotExistsException;

  @NotNull
  public XWikiDocument getDocument(@NotNull DocumentReference docRef, @Nullable String lang)
      throws DocumentNotExistsException;

  @NotNull
  public XWikiDocument createDocument(@NotNull DocumentReference docRef)
      throws DocumentAlreadyExistsException;

  @NotNull
  public XWikiDocument getOrCreateDocument(@NotNull DocumentReference docRef);

  public boolean exists(@NotNull DocumentReference docRef);

  public boolean exists(@NotNull DocumentReference docRef, @Nullable String lang);

  public void saveDocument(@NotNull XWikiDocument doc) throws DocumentSaveException;

  public void saveDocument(@NotNull XWikiDocument doc, @Nullable String comment)
      throws DocumentSaveException;

  public void saveDocument(@NotNull XWikiDocument doc, @Nullable String comment,
      boolean isMinorEdit) throws DocumentSaveException;

  public void deleteDocument(@NotNull DocumentReference docRef, boolean totrash)
      throws DocumentDeleteException;

  public void deleteDocument(@NotNull XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException;

  public void deleteDocumentWithoutTranslations(@NotNull XWikiDocument doc, boolean totrash)
      throws DocumentDeleteException;

  @NotNull
  public Map<String, XWikiDocument> getTranslations(@NotNull DocumentReference docRef);

  public boolean isTranslation(@NotNull XWikiDocument doc);

  /**
   * @param docRef
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get (may not be null)
   * @return the xobject or null
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef)
      throws DocumentNotExistsException;

  /**
   * @param docRef
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get (may not be null)
   * @param key
   *          for field specific xobject filtering (null means no filtering)
   * @param value
   *          for field specific xobject filtering
   * @return the xobject or null
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public BaseObject getXObject(DocumentReference docRef, DocumentReference classRef, String key,
      Object value) throws DocumentNotExistsException;

  /**
   * @param doc
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get (may not be null)
   * @return the xobject or null
   */
  public BaseObject getXObject(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param doc
   *          to get xobject on (may not be null)
   * @param classRef
   *          type of xobject to get (may not be null)
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
   *          type of xobjects to get (may not be null)
   * @return an unmodifiable list of xobjects (without null values) or empty list
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef)
      throws DocumentNotExistsException;

  /**
   * @param docRef
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get (may not be null)
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return an unmodifiable list of xobjects (without null values) or empty list
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Object value) throws DocumentNotExistsException;

  /**
   * @param docRef
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get (may not be null)
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return an unmodifiable list of xobjects (without null values) or empty list
   * @throws DocumentNotExistsException
   *           if the document does not exist
   */
  public List<BaseObject> getXObjects(DocumentReference docRef, DocumentReference classRef,
      String key, Collection<?> values) throws DocumentNotExistsException;

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get (may not be null)
   * @return an unmodifiable list of xobjects (without null values) or empty list
   */
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get (may not be null)
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param value
   *          for field specific xobjects filtering
   * @return an unmodifiable list of xobjects (without null values) or empty list
   */
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Object value);

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @param classRef
   *          type of xobjects to get (may not be null)
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return an unmodifiable list of xobjects (without null values) or empty list
   */
  public List<BaseObject> getXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values);

  /**
   * @param doc
   *          to get xobjects on (may not be null)
   * @return
   * @return an unmodifiable map of all xobjects list
   */
  public Map<DocumentReference, List<BaseObject>> getXObjects(XWikiDocument doc);

  /**
   * @param doc
   *          to get new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create (may not be null)
   * @param key
   *          for field specific xobject filtering (null means no filtering)
   * @param value
   *          for field specific xobject filtering
   * @return newly created xobject with set key - value
   */
  public BaseObject newXObject(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param doc
   *          to get or create new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create (may not be null)
   * @return already existing or newly created xobject
   */
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef);

  /**
   * @param doc
   *          to get or create new xobject on (may not be null)
   * @param classRef
   *          type of xobjects to create (may not be null)
   * @return already existing or newly created xobject
   */
  public BaseObject getOrCreateXObject(XWikiDocument doc, DocumentReference classRef, String key,
      Object value);

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
   *          type of xobjects to remove (may not be null)
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
   *          type of xobjects to remove (may not be null)
   * @param key
   *          for field specific xobjects filtering (null means no filtering)
   * @param values
   *          for field specific xobjects filtering
   * @return true if doc has changed
   */
  public boolean removeXObjects(XWikiDocument doc, DocumentReference classRef, String key,
      Collection<?> values);

  public Object getProperty(DocumentReference docRef, DocumentReference classRef, String name)
      throws DocumentNotExistsException;

  public Object getProperty(XWikiDocument doc, DocumentReference classRef, String name);

  /**
   * Reads out the property value for the given BaseObject and name
   *
   * @param obj
   * @param name
   * @return
   */
  public Object getProperty(BaseObject obj, String name);

  @Nullable
  public <T> T getProperty(@NotNull DocumentReference docRef, @NotNull ClassField<T> field)
      throws DocumentNotExistsException;

  @Nullable
  public <T> T getProperty(@NotNull XWikiDocument doc, @NotNull ClassField<T> field);

  @NotNull
  public <T> Optional<T> getFieldValue(@NotNull XWikiDocument doc, @NotNull ClassField<T> field);

  @NotNull
  public <T> Optional<T> getFieldValue(@NotNull DocumentReference docRef,
      @NotNull ClassField<T> field) throws DocumentNotExistsException;

  @NotNull
  public <T> Optional<T> getFieldValue(@NotNull XWikiDocument doc, @NotNull ClassField<T> field,
      T ignoreValue);

  @NotNull
  public <T> Optional<T> getFieldValue(@NotNull DocumentReference docRef,
      @NotNull ClassField<T> field, T ignoreValue) throws DocumentNotExistsException;

  @NotNull
  public List<ClassFieldValue<?>> getProperties(@NotNull XWikiDocument doc,
      @NotNull ClassDefinition classDef);

  public boolean setProperty(BaseObject obj, String name, Object value);

  public <T> XWikiDocument setProperty(@NotNull DocumentReference docRef,
      @NotNull ClassField<T> field, @Nullable T value) throws DocumentNotExistsException;

  public <T> boolean setProperty(@NotNull XWikiDocument doc, @NotNull ClassField<T> field,
      @Nullable T value);

  public <T> boolean setProperty(XWikiDocument doc, ClassFieldValue<T> fieldValue);

  /**
   * CAUTION: document.getAttachment returns "startWith" matches. Instead use
   * getAttachmentNameEqual or methods on IAttachmentServiceRole
   *
   * @param document
   * @param filename
   * @return
   * @throws AttachmentNotExistsException
   */
  public XWikiAttachment getAttachmentNameEqual(XWikiDocument document, String filename)
      throws AttachmentNotExistsException;

  /**
   * getApiDocument creates a com.xpn.xwiki.api.Document for <code>doc</code>
   *
   * @param doc
   * @return an api Document object or null
   * @throws NoAccessRightsException
   *           if current context user has no view rights
   */
  public Document getApiDocument(XWikiDocument doc) throws NoAccessRightsException;

  /**
   * getApiObject creates a com.xpn.xwiki.api.Object for <code>obj</code>
   *
   * @param obj
   * @return
   * @throws NoAccessRightsException
   *           if current context user has no view rights
   */
  public com.xpn.xwiki.api.Object getApiObject(BaseObject obj) throws NoAccessRightsException;

  /**
   * getApiObject creates a com.xpn.xwiki.api.Object for <code>obj</code>
   *
   * @param obj
   * @return
   **/
  public com.xpn.xwiki.api.Object getApiObjectWithoutRightCheck(BaseObject obj);

  /**
   * getApiObjects creates for each valid BaseObject in <code>objs</code> a
   * com.xpn.xwiki.api.Object. A BaseObject is valid if it is not null, has a correct
   * DocumentReference set and the context user has view rights an that document. Invalid
   * BaseObjects are omitted, thus the returned list may be smaller.
   *
   * @param objs
   * @return
   */
  public List<com.xpn.xwiki.api.Object> getApiObjects(List<BaseObject> objs);

  /**
   * getApiObjects creates for each valid BaseObject in <code>objs</code> a
   * com.xpn.xwiki.api.Object. A BaseObject is valid if it is not null.
   *
   * @param objs
   * @return
   */
  public List<com.xpn.xwiki.api.Object> getApiObjectsWithoutRightChecks(List<BaseObject> objs);

}
