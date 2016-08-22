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
 *
 */
package com.celements.store;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.python.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheFactory;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLink;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.XWikiCacheStoreInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

/**
 * A proxy store implementation that caches Documents when they are first fetched and subsequently
 * return them from a
 * cache. It delegates all write and search operations to an underlying store without doing any
 * caching on them.
 *
 * @version $Id$
 */
@Component(DocumentCacheStore.COMPONENT_NAME)
public class DocumentCacheStore implements XWikiCacheStoreInterface {

  private final static Logger LOGGER = LoggerFactory.getLogger(DocumentCacheStore.class);
  private final static Logger LOGGER_DL = LoggerFactory.getLogger(DocumentLoader.class);

  public static final String COMPONENT_NAME = "DocumentCacheStore";

  public static final String PARAM_DOC_CACHE_CAPACITY = "xwiki.store.cache.capacity";
  public static final String PARAM_EXIST_CACHE_CAPACITY = "xwiki.store.cache.pageexistcapacity";
  public static final String BACKING_STORE_STRATEGY = "celements.store.cache.storeStrategy";

  @Requirement("xwikiproperties")
  private ConfigurationSource config;

  @Requirement
  private CacheManager cacheManager;

  @Requirement
  private Execution execution;

  @Requirement("default")
  private EntityReferenceSerializer<String> serializerDefault;

  /**
   * Lazy initialized according to backing store strategy configuration.
   * The store field is immutable in the following way:
   * even though it could be initialized in multiple threads because of missing memory visibility,
   * it always results in the same value. Thus a thread could not tell if it was multiple times
   * initialized. Hence no volatile modificator is needed.
   */
  private XWikiStoreInterface store;

  /**
   * CAUTION: Lazy initialized of cache thus volatile is needed.
   */
  private volatile Cache<XWikiDocument> docCache;

  /**
   * CAUTION: Lazy initialized of cache thus volatile is needed.
   */
  private volatile Cache<Boolean> existCache;

  private final ConcurrentMap<String, DocumentLoader> documentLoaderMap = new ConcurrentHashMap<>();

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  void initalize() {
    if ((this.docCache == null) || (this.existCache == null)) {
      synchronized (this) {
        try {
          if (this.docCache == null) {
            initializeDocCache();
          }
          if (this.existCache == null) {
            initializeExistCache();
          }
        } catch (CacheException cacheExp) {
          LOGGER.error("Failed to initialize document cache.", cacheExp);
          throw new RuntimeException("FATAL: Failed to initialize document cache.", cacheExp);
        }
      }
    }
  }

  private void initializeExistCache() throws CacheException {
    CacheConfiguration config = new CacheConfiguration();
    config.setConfigurationId("xwiki.store.pageexistcache");
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    lru.setMaxEntries(getExistCacheCapacity());
    config.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
    existCache = getCacheFactory().newCache(config);
  }

  private CacheFactory getCacheFactory() {
    try {
      return cacheManager.getCacheFactory();
    } catch (ComponentLookupException exp) {
      throw new RuntimeException("Failed to get cache factory component", exp);
    }
  }

  private int getExistCacheCapacity() {
    int existCacheCapacity = 10000;
    String existsCapacity = getContext().getWiki().Param(PARAM_EXIST_CACHE_CAPACITY);
    if (existsCapacity != null) {
      try {
        existCacheCapacity = Integer.parseInt(existsCapacity);
      } catch (NumberFormatException exp) {
        LOGGER.warn("Failed to read '{}' using default '{}'", PARAM_EXIST_CACHE_CAPACITY,
            existCacheCapacity, exp);
      }
    }
    int docCacheCapacity = getDocCacheCapacity();
    if (existCacheCapacity < docCacheCapacity) {
      LOGGER.warn("WARNING: document exists cache capacity is smaller configured than docCache "
          + "capacity. Ignoring exists cache configuration '{}' and using doc cache capacity '{}'"
          + " instead.", existCacheCapacity, docCacheCapacity);
      existCacheCapacity = docCacheCapacity;
    }
    return existCacheCapacity;
  }

  private void initializeDocCache() throws CacheException {
    CacheConfiguration config = new CacheConfiguration();
    config.setConfigurationId("xwiki.store.pagecache");
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    lru.setMaxEntries(getDocCacheCapacity());
    config.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
    docCache = getCacheFactory().newCache(config);
  }

  private int getDocCacheCapacity() {
    int docCacheCapacity = 100;
    String capacity = getContext().getWiki().Param(PARAM_DOC_CACHE_CAPACITY);
    if (capacity != null) {
      try {
        docCacheCapacity = Integer.parseInt(capacity);
      } catch (NumberFormatException exp) {
        LOGGER.warn("Failed to read xwiki.store.cache.capacity using default '{}'",
            docCacheCapacity, exp);
      }
    }
    return docCacheCapacity;
  }

  @Override
  public void initCache(int docCacheCapacity, int existCacheCapacity, XWikiContext context)
      throws XWikiException {
    LOGGER.info("initCache externally called. This is not supported. The document cache initializes"
        + " automatically on start.");
  }

  @Override
  public XWikiStoreInterface getStore() {
    return Utils.getComponent(XWikiStoreInterface.class);
  }

  public XWikiStoreInterface getBackingStore() {
    if (this.store == null) {
      String backingStoreHint = getBackingStoreHint();
      setStore(Utils.getComponent(XWikiStoreInterface.class, backingStoreHint));
      LOGGER.info("backing store initialized '{}' for hint '{}'", this.store, backingStoreHint);
    }
    return this.store;
  }

  private String getBackingStoreHint() {
    String strategy = config.getProperty(BACKING_STORE_STRATEGY, "default");
    if (COMPONENT_NAME.equals(strategy)) {
      strategy = "default";
    }
    LOGGER.debug("get backing store hint '{}'", strategy);
    return strategy;
  }

  @Override
  public synchronized void setStore(XWikiStoreInterface store) {
    this.store = store;
  }

  @Override
  public void saveXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException {
    saveXWikiDoc(doc, context, true);
  }

  @Override
  public void saveXWikiDoc(XWikiDocument doc, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getBackingStore().saveXWikiDoc(doc, context, bTransaction);
    doc.setStore(this.store);
    removeDocFromCache(doc, true);
  }

  @Override
  public synchronized void flushCache() {
    LOGGER.warn("flushCache may lead to serious memory visibility problems.");
    if (this.docCache != null) {
      this.docCache.dispose();
      this.docCache = null;
    }
    if (this.existCache != null) {
      this.existCache.dispose();
      this.existCache = null;
    }
  }

  String getKey(DocumentReference docRef) {
    DocumentReference cacheDocRef = new DocumentReference(getContext().getDatabase(),
        docRef.getLastSpaceReference().getName(), docRef.getName());
    return serializerDefault.serialize(cacheDocRef);
  }

  String getKeyWithLang(DocumentReference docRef, String language) {
    if (Strings.isNullOrEmpty(language)) {
      return getKey(docRef);
    } else {
      return getKey(docRef) + ":" + language;
    }
  }

  String getKeyWithLang(XWikiDocument doc) {
    String language = doc.getLanguage();
    if (language.isEmpty() || language.equals(doc.getDefaultLanguage())) {
      language = "";
    }
    return getKeyWithLang(doc.getDocumentReference(), language);
  }

  private DocumentLoader getDocumentLoader(String key) {
    DocumentLoader docLoader = documentLoaderMap.get(key);
    if (docLoader == null) {
      LOGGER.debug("create document loader for '{}' in thread '{}'", key,
          Thread.currentThread().getId());
      docLoader = new DocumentLoader(key);
      DocumentLoader setDocLoader = documentLoaderMap.putIfAbsent(key, docLoader);
      if (setDocLoader != null) {
        docLoader = setDocLoader;
        LOGGER.info("replace with existing from map for key '{}' in thread '{}'", key,
            Thread.currentThread().getId());
      }
    }
    return docLoader;
  }

  InvalidateState removeDocFromCache(XWikiDocument doc, Boolean docExists) {
    InvalidateState returnState = InvalidateState.CACHE_MISS;
    Set<String> docKeys = new HashSet<>();
    String key = getKey(doc.getDocumentReference());
    String origKey = "";
    if (doc.getOriginalDocument() != null) {
      origKey = getKey(doc.getOriginalDocument().getDocumentReference());
      docKeys.add(origKey);
      docKeys.add(getKeyWithLang(doc.getOriginalDocument()));
    }
    docKeys.add(key);
    docKeys.add(getKeyWithLang(doc));
    for (String k : docKeys) {
      InvalidateState invState = invalidateDocCache(k);
      if (invState == InvalidateState.REMOVED) {
        returnState = InvalidateState.REMOVED;
      } else if (returnState != InvalidateState.REMOVED) {
        returnState = invState;
      }
    }
    if (getExistCache() != null) {
      if ((doc.getTranslation() == 0) || (docExists == Boolean.TRUE)) {
        getExistCache().remove(origKey);
        updateExistsCache(key, docExists);
      }
      updateExistsCache(getKeyWithLang(doc), docExists);
    }
    return returnState;
  }

  private void updateExistsCache(String key, Boolean docExists) {
    if (docExists == null) {
      getExistCache().remove(key);
    } else {
      getExistCache().set(key, docExists);
    }
  }

  InvalidateState invalidateDocCache(String key) {
    InvalidateState invalidState = InvalidateState.CACHE_MISS;
    final DocumentLoader docLoader = documentLoaderMap.get(key);
    boolean invalidateDocLoader = (docLoader != null);
    if (invalidateDocLoader) {
      invalidState = docLoader.invalidate();
    }
    XWikiDocument oldCachedDoc = null;
    if (getDocCache() != null) {
      oldCachedDoc = getDocFromCache(key);
      if (oldCachedDoc != null) {
        synchronized (oldCachedDoc) {
          oldCachedDoc = getDocFromCache(key);
          if (oldCachedDoc != null) {
            getDocCache().remove(key);
            invalidState = InvalidateState.REMOVED;
          }
        }
      }
    }
    return invalidState;
  }

  @Override
  public XWikiDocument loadXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException {
    LOGGER.trace("Cache: begin for docRef '{}' in cache", doc.getDocumentReference());
    String key = getKey(doc.getDocumentReference());
    String keyWithLang = getKeyWithLang(doc);
    if (doesNotExistsForKey(key) || doesNotExistsForKey(keyWithLang)) {
      LOGGER.debug("Cache: The document {} does not exist, return an empty one", keyWithLang);
      doc.setStore(this.store);
      doc.setNew(true);
      // Make sure to always return a document with an original version, even for one that does
      // not exist. This allows to write more generic code.
      doc.setOriginalDocument(new XWikiDocument(doc.getDocumentReference()));
      return doc;
    } else {
      LOGGER.debug("Cache: Trying to get doc '{}' from cache", keyWithLang);
      XWikiDocument cachedoc = getDocFromCache(keyWithLang);
      if (cachedoc != null) {
        LOGGER.debug("Cache: got doc '{}' from cache", keyWithLang);
      } else {
        cachedoc = getDocumentLoader(keyWithLang).loadDocument(keyWithLang, doc, context);
      }
      LOGGER.trace("Cache: end for doc '{}' in cache", keyWithLang);
      return cachedoc;
    }
  }

  private boolean doesNotExistsForKey(String key) {
    return getExistCache().get(key) == Boolean.FALSE;
  }

  /**
   * getCache is private, thus for tests we need getDocFromCache to check the cache state
   */
  XWikiDocument getDocFromCache(String key) {
    return getDocCache().get(key);
  }

  @Override
  public void deleteXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException {
    getBackingStore().deleteXWikiDoc(doc, context);
    removeDocFromCache(doc, false);
  }

  @Override
  public List<String> getClassList(XWikiContext context) throws XWikiException {
    return getBackingStore().getClassList(context);
  }

  @Override
  public int countDocuments(String wheresql, XWikiContext context) throws XWikiException {
    return getBackingStore().countDocuments(wheresql, context);
  }

  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocumentReferences(wheresql, context);
  }

  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocumentsNames(wheresql, context);
  }

  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, int nb, int start,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentReferences(wheresql, nb, start, context);
  }

  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocumentsNames(wheresql, nb, start, context);
  }

  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, int nb, int start,
      String selectColumns, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentReferences(wheresql, nb, start, selectColumns, context);
  }

  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, int nb, int start, String selectColumns,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentsNames(wheresql, nb, start, selectColumns, context);
  }

  @Override
  public List<DocumentReference> searchDocumentReferences(String parametrizedSqlClause, int nb,
      int start, List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentReferences(parametrizedSqlClause, nb, start,
        parameterValues, context);
  }

  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String parametrizedSqlClause, int nb, int start,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentsNames(parametrizedSqlClause, nb, start, parameterValues,
        context);
  }

  @Override
  public List<DocumentReference> searchDocumentReferences(String parametrizedSqlClause,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentReferences(parametrizedSqlClause, parameterValues,
        context);
  }

  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String parametrizedSqlClause, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocumentsNames(parametrizedSqlClause, parameterValues, context);
  }

  @Override
  public boolean isCustomMappingValid(BaseClass bclass, String custommapping1, XWikiContext context)
      throws XWikiException {
    return getBackingStore().isCustomMappingValid(bclass, custommapping1, context);
  }

  @Override
  public boolean injectCustomMapping(BaseClass doc1class, XWikiContext context)
      throws XWikiException {
    return getBackingStore().injectCustomMapping(doc1class, context);
  }

  @Override
  public boolean injectCustomMappings(XWikiDocument doc, XWikiContext context)
      throws XWikiException {
    return getBackingStore().injectCustomMappings(doc, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbyname, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbyname, customMapping, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname, int nb,
      int start, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbyname, nb, start, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, int nb, int start, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbyname, customMapping, nb, start,
        context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, nb, start, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, boolean checkRight, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbyname, customMapping, checkRight,
        nb, start, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, int nb,
      int start, List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbylanguage, nb, start,
        parameterValues, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, parameterValues, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage,
      boolean customMapping, int nb, int start, List<?> parameterValues, XWikiContext context)
      throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbylanguage, customMapping, nb, start,
        parameterValues, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, nb, start, parameterValues, context);
  }

  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage,
      boolean customMapping, boolean checkRight, int nb, int start, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getBackingStore().searchDocuments(wheresql, distinctbylanguage, customMapping,
        checkRight, nb, start, parameterValues, context);
  }

  @Override
  public int countDocuments(String parametrizedSqlClause, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getBackingStore().countDocuments(parametrizedSqlClause, parameterValues, context);
  }

  @Override
  public XWikiLock loadLock(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getBackingStore().loadLock(docId, context, bTransaction);
  }

  @Override
  public void saveLock(XWikiLock lock, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getBackingStore().saveLock(lock, context, bTransaction);
  }

  @Override
  public void deleteLock(XWikiLock lock, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getBackingStore().deleteLock(lock, context, bTransaction);
  }

  @Override
  public List<XWikiLink> loadLinks(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getBackingStore().loadLinks(docId, context, bTransaction);
  }

  @Override
  public List<DocumentReference> loadBacklinks(DocumentReference documentReference,
      boolean bTransaction, XWikiContext context) throws XWikiException {
    return getBackingStore().loadBacklinks(documentReference, bTransaction, context);
  }

  @Override
  @Deprecated
  public List<String> loadBacklinks(String fullName, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getBackingStore().loadBacklinks(fullName, context, bTransaction);
  }

  @Override
  public void saveLinks(XWikiDocument doc, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getBackingStore().saveLinks(doc, context, bTransaction);
  }

  @Override
  public void deleteLinks(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getBackingStore().deleteLinks(docId, context, bTransaction);
  }

  @Override
  public <T> List<T> search(String sql, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getBackingStore().search(sql, nb, start, context);
  }

  @Override
  public <T> List<T> search(String sql, int nb, int start, Object[][] whereParams,
      XWikiContext context) throws XWikiException {
    return getBackingStore().search(sql, nb, start, whereParams, context);
  }

  @Override
  public <T> List<T> search(String sql, int nb, int start, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getBackingStore().search(sql, nb, start, parameterValues, context);
  }

  @Override
  public <T> List<T> search(String sql, int nb, int start, Object[][] whereParams,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getBackingStore().search(sql, nb, start, whereParams, parameterValues, context);
  }

  @Override
  public synchronized void cleanUp(XWikiContext context) {
    getBackingStore().cleanUp(context);
  }

  @Override
  public boolean isWikiNameAvailable(String wikiName, XWikiContext context) throws XWikiException {
    return getBackingStore().isWikiNameAvailable(wikiName, context);
  }

  @Override
  public synchronized void createWiki(String wikiName, XWikiContext context) throws XWikiException {
    getBackingStore().createWiki(wikiName, context);
  }

  @Override
  public synchronized void deleteWiki(String wikiName, XWikiContext context) throws XWikiException {
    getBackingStore().deleteWiki(wikiName, context);
    flushCache();
  }

  @Override
  public boolean exists(XWikiDocument doc, XWikiContext context) throws XWikiException {
    String key = getKey(doc.getDocumentReference());
    Boolean result = getExistCache().get(key);
    if (result == null) {
      result = (getDocCache().get(key) != null);
      if (!result) {
        result = getBackingStore().exists(doc, context);
      }
      getExistCache().set(key, result);
    }
    LOGGER.info("exists return '{}' for '{}'", result, key);
    return result;
  }

  private Cache<XWikiDocument> getDocCache() {
    initalize(); // make sure cache is initialized
    return this.docCache;
  }

  private Cache<Boolean> getExistCache() {
    initalize(); // make sure cache is initialized
    return this.existCache;
  }

  @Override
  public List<String> getCustomMappingPropertyList(BaseClass bclass) {
    return getBackingStore().getCustomMappingPropertyList(bclass);
  }

  @Override
  public synchronized void injectCustomMappings(XWikiContext context) throws XWikiException {
    getBackingStore().injectCustomMappings(context);
  }

  @Override
  public void injectUpdatedCustomMappings(XWikiContext context) throws XWikiException {
    getBackingStore().injectUpdatedCustomMappings(context);
  }

  @Override
  public List<String> getTranslationList(XWikiDocument doc, XWikiContext context)
      throws XWikiException {
    return getBackingStore().getTranslationList(doc, context);
  }

  @Override
  public QueryManager getQueryManager() {
    return getBackingStore().getQueryManager();
  }

  static enum InvalidateState {

    CACHE_MISS, REMOVED, LOADING_CANCELED, LOADING_MULTI_CANCELED, LOADING_CANCEL_FAILED

  }

  private static final int _DOCSTATE_LOADING = 0;
  private static final int _DOCSTATE_FINISHED = Integer.MAX_VALUE;

  private class DocumentLoader {

    private volatile XWikiDocument loadedDoc;
    private final String key;

    /**
     * if loadingState equals _DOCSTATE_LOADING than a valid loading is about to start or in process
     * if loadingState is lower _DOCSTATE_LOADING than a loading has been successful canceled and a
     * reload will take place
     * if loadingState equals _DOCSTATE_FINISHED or is at least greater _DOCSTATE_LOADING loading
     * finished before any canceling happened
     */
    private final AtomicInteger loadingState = new AtomicInteger(_DOCSTATE_LOADING);

    private DocumentLoader(String key) {
      this.key = key;
    }

    private InvalidateState invalidate() {
      InvalidateState invalidState;
      int beforeState = loadingState.getAndDecrement();
      if (beforeState < 0) {
        if (loadedDoc != null) {
          LOGGER_DL.warn("should not happen: possible lifelock! {}", this.key);
        }
        invalidState = InvalidateState.LOADING_MULTI_CANCELED;
      } else if (beforeState == 0) {
        invalidState = InvalidateState.LOADING_CANCELED;
      } else {
        invalidState = InvalidateState.LOADING_CANCEL_FAILED;
      }
      boolean succInvalidated = beforeState <= 0;
      LOGGER_DL.debug("invalidated cache during loading document. {}", succInvalidated);
      return invalidState;
    }

    /**
     * IMPORTANT: do not change anything on the synchronization of this method.
     * It is a very delicate case and very likely memory visibility breaks in less than 1 out of
     * 100'000 document loads. Thus it is difficult to test for correctness.
     */
    private XWikiDocument loadDocument(String key, XWikiDocument doc, XWikiContext context)
        throws XWikiException {
      checkArgument(key);
      if (loadedDoc == null) {
        synchronized (this) {
          if (loadedDoc == null) {
            // if a thread is just between the document cache miss and getting the documentLoader
            // when the documentLoader removes itself from the map, then a new documentLoader is
            // generated. Therefore we double check here that still no document is in cache.
            loadedDoc = getDocCache().get(key);
            if (loadedDoc == null) {
              XWikiDocument newDoc = null;
              do {
                if ((loadingState.getAndSet(_DOCSTATE_LOADING) < _DOCSTATE_LOADING)
                    && (newDoc != null)) {
                  LOGGER_DL.info("DocumentLoader-{}: invalidated docloader '{}' reloading",
                      Thread.currentThread().getId(), key);
                }
                // use a further synchronized method call to prevent an unsafe publication of the
                // new document over the cache
                newDoc = new DocumentBuilder().buildDocument(key, doc, context);
              } while (!loadingState.compareAndSet(_DOCSTATE_LOADING, _DOCSTATE_FINISHED));
              LOGGER_DL.info("DocumentLoader-{}: put doc '{}' in cache",
                  Thread.currentThread().getId(), key);
              final String keyWithLang = getKeyWithLang(newDoc);
              if (!newDoc.isNew()) {
                getDocCache().set(keyWithLang, newDoc);
                getExistCache().set(getKey(newDoc.getDocumentReference()), true);
                getExistCache().set(keyWithLang, true);
              } else {
                LOGGER_DL.debug("DocumentLoader-{}: loading '{}' failed. Setting exists"
                    + " to FALSE for '{}'", Thread.currentThread().getId(), key, keyWithLang);
                getExistCache().set(keyWithLang, false);
              }
              loadedDoc = newDoc;
            } else {
              LOGGER_DL.info("DocumentLoader-{}: found in cache skip loding for '{}'",
                  Thread.currentThread().getId(), key);
            }
            documentLoaderMap.remove(key);
          }
        }
      }
      return loadedDoc;
    }

    private void checkArgument(String key) {
      if (!this.key.equals(key)) {
        throw new RuntimeException(
            "DocumentLoader illegally used with a different key (registered key:" + this.key
                + ", loading doc key: " + key + ").");
      }
    }

    private class DocumentBuilder {

      private synchronized XWikiDocument buildDocument(String key, XWikiDocument doc,
          XWikiContext context) throws XWikiException {
        LOGGER_DL.trace("DocumentLoader-{}: Trying to get doc '{}' for real",
            Thread.currentThread().getId(), key);
        // IMPORTANT: do not clone here. Creating new document is much faster.
        XWikiDocument buildDoc = new XWikiDocument(doc.getDocumentReference());
        buildDoc.setLanguage(doc.getLanguage());
        buildDoc = getBackingStore().loadXWikiDoc(buildDoc, context);
        buildDoc.setStore(store);
        buildDoc.setFromCache(!buildDoc.isNew());
        return buildDoc;
      }

    }
  }

}
