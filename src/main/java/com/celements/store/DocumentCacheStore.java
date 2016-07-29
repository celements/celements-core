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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

  public static final String BACKING_STORE_STRATEGY = "celements.store.cache.storeStrategy";

  @Requirement
  private ConfigurationSource config;

  @Requirement
  private CacheManager cacheManager;

  @Requirement
  private Execution execution;

  @Requirement("default")
  private EntityReferenceSerializer<String> serializer_default;

  /**
   * Lazy initialized according to backing store strategy configuration.
   */
  private XWikiStoreInterface store;

  private Cache<XWikiDocument> cache;

  private Cache<Boolean> pageExistCache;

  private final ConcurrentMap<String, DocumentLoader> documentLoaderMap = new ConcurrentHashMap<>();

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  private void initalizeCache() {
    if ((this.cache == null) || (this.pageExistCache == null)) {
      synchronized (this) {
        try {
          if (this.cache == null) {
            initializePageCache();
          }
          if (this.pageExistCache == null) {
            initializePageExistCache();
          }
        } catch (CacheException cacheExp) {
          LOGGER.error("Failed to initialize document cache.", cacheExp);
          throw new RuntimeException("FATAL: Failed to initialize document cache.", cacheExp);
        }
      }
    }
  }

  private void initializePageExistCache() throws CacheException {
    CacheConfiguration cacheConfiguration;
    cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setConfigurationId("xwiki.store.pageexistcache");
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    lru.setMaxEntries(getPageExistCacheCapacity());
    cacheConfiguration.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
    Cache<Boolean> pageExistcache = getCacheFactory().newCache(cacheConfiguration);
    setPageExistCache(pageExistcache);
  }

  CacheFactory getCacheFactory() {
    try {
      return cacheManager.getCacheFactory();
    } catch (ComponentLookupException exp) {
      throw new RuntimeException("Failed to get cache factory component", exp);
    }
  }

  private int getPageExistCacheCapacity() {
    int pageExistCacheCapacity = 10000;
    String existsCapacity = getContext().getWiki().Param("xwiki.store.cache.pageexistcapacity");
    if (existsCapacity != null) {
      try {
        pageExistCacheCapacity = Integer.parseInt(existsCapacity);
      } catch (NumberFormatException exp) {
        LOGGER.warn("Failed to read xwiki.store.cache.pageexistcapacity using default '{}'",
            pageExistCacheCapacity, exp);
      }
    }
    return pageExistCacheCapacity;
  }

  private void initializePageCache() throws CacheException {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setConfigurationId("xwiki.store.pagecache");
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    lru.setMaxEntries(getPageCacheCapacity());
    cacheConfiguration.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
    Cache<XWikiDocument> pageCache = getCacheFactory().newCache(cacheConfiguration);
    setCache(pageCache);
  }

  private int getPageCacheCapacity() {
    int cacheCapacity = 100;
    String capacity = getContext().getWiki().Param("xwiki.store.cache.capacity");
    if (capacity != null) {
      try {
        cacheCapacity = Integer.parseInt(capacity);
      } catch (NumberFormatException exp) {
        LOGGER.warn("Failed to read xwiki.store.cache.capacity using default '{}'", cacheCapacity,
            exp);
      }
    }
    return cacheCapacity;
  }

  @Override
  public void initCache(int capacity, int pageExistCacheCapacity, XWikiContext context)
      throws XWikiException {
    LOGGER.info("initCache externally called. This is not supported. The document cache initializes"
        + " automatically on start.");
  }

  @Override
  public XWikiStoreInterface getStore() {
    if (this.store == null) {
      synchronized (this) {
        if (this.store == null) {
          this.store = Utils.getComponent(XWikiStoreInterface.class, getBackingStoreHint());
        }
      }
    }
    return this.store;
  }

  private String getBackingStoreHint() {
    String strategy = config.getProperty(BACKING_STORE_STRATEGY, "default");
    if (COMPONENT_NAME.equals(strategy)) {
      strategy = "default";
    }
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
    String key = getKey(doc);
    getStore().saveXWikiDoc(doc, context, bTransaction);
    doc.setStore(this.store);

    // We need to flush so that caches
    // on the cluster are informed about the change
    getCache().remove(key);
    getPageExistCache().remove(key);

    /*
     * We do not want to save the document in the cache at this time. If we did, this would
     * introduce the
     * possibility for cache incoherence if the document is not saved in the database properly. In
     * addition, the
     * attachments uploaded to the document stay with it so we want the document in it's current
     * form to be garbage
     * collected as soon as the request is complete.
     */
  }

  @Override
  public void flushCache() {
    if (this.cache != null) {
      this.cache.dispose();
      this.cache = null;
    }

    if (this.pageExistCache != null) {
      this.pageExistCache.dispose();
      this.pageExistCache = null;
    }
  }

  public String getKey(XWikiDocument doc) {
    return getKey(doc.getDocumentReference(), doc.getLanguage());
  }

  public String getKey(DocumentReference docRef, String language) {
    String key = serializer_default.serialize(docRef);

    if (Strings.isNullOrEmpty(language)) {
      return key;
    } else {
      return key + ":" + language;
    }
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

  void invalidateCacheFromClusterEvent(XWikiDocument doc) {
    String key = getKey(doc);
    invalidateCacheFromClusterEvent(key);
  }

  void invalidateCacheFromClusterEvent(String key) {
    if (documentLoaderMap.containsKey(key)) {
      documentLoaderMap.remove(key);
      // TODO check handle loading document and cancel, restart, invalidate...???
      LOGGER.warn("invalidating cache for loading document '{}'", key);
    }
    if (getCache() != null) {
      getCache().remove(key);
    }
    if (getPageExistCache() != null) {
      getPageExistCache().remove(key);
    }
  }

  @Override
  public XWikiDocument loadXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException {
    LOGGER.trace("Cache: begin for docRef '{}' in cache", doc.getDocumentReference());
    String key = getKey(doc);
    LOGGER.debug("Cache: Trying to get doc '{}' from cache", key);
    XWikiDocument cachedoc = getCache().get(key);
    if (cachedoc != null) {
      doc = cachedoc;
      LOGGER.debug("Cache: got doc '{}' from cache", key);
    } else {
      doc = getDocumentLoader(key).loadDocument(key, doc, context);
    }
    doc.setFromCache(true);
    LOGGER.trace("Cache: end for doc '{}' in cache", key);
    return doc;
  }

  @Override
  public void deleteXWikiDoc(XWikiDocument doc, XWikiContext context) throws XWikiException {
    String key = getKey(doc);

    getStore().deleteXWikiDoc(doc, context);

    getCache().remove(key);
    getPageExistCache().remove(key);
    getPageExistCache().set(key, new Boolean(false));
  }

  @Override
  public List<String> getClassList(XWikiContext context) throws XWikiException {
    return getStore().getClassList(context);
  }

  /**
   * {@inheritDoc}
   *
   * @see XWikiStoreInterface#countDocuments(String, XWikiContext)
   */
  @Override
  public int countDocuments(String wheresql, XWikiContext context) throws XWikiException {
    return getStore().countDocuments(wheresql, context);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocumentReferences(wheresql, context);
  }

  /**
   * @deprecated since 2.2M2 use
   *             {@link #searchDocumentReferences(String, com.xpn.xwiki.XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocumentsNames(wheresql, context);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, int nb, int start,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocumentReferences(wheresql, nb, start, context);
  }

  /**
   * @deprecated since 2.2M2 use
   *             {@link #searchDocumentReferences(String, int, int, com.xpn.xwiki.XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocumentsNames(wheresql, nb, start, context);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> searchDocumentReferences(String wheresql, int nb, int start,
      String selectColumns, XWikiContext context) throws XWikiException {
    return getStore().searchDocumentReferences(wheresql, nb, start, selectColumns, context);
  }

  /**
   * @deprecated since 2.2M2 use
   *             {@link #searchDocumentReferences(String, int, int, String, XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String wheresql, int nb, int start, String selectColumns,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocumentsNames(wheresql, nb, start, selectColumns, context);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> searchDocumentReferences(String parametrizedSqlClause, int nb,
      int start, List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().searchDocumentReferences(parametrizedSqlClause, nb, start, parameterValues,
        context);
  }

  /**
   * @deprecated since 2.2M2 use
   *             {@link #searchDocumentReferences(String, int, int, List, XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String parametrizedSqlClause, int nb, int start,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().searchDocumentsNames(parametrizedSqlClause, nb, start, parameterValues,
        context);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> searchDocumentReferences(String parametrizedSqlClause,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().searchDocumentReferences(parametrizedSqlClause, parameterValues, context);
  }

  /**
   * @deprecated since 2.2M2 use {@link #searchDocumentReferences(String, List, XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> searchDocumentsNames(String parametrizedSqlClause, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocumentsNames(parametrizedSqlClause, parameterValues, context);
  }

  @Override
  public boolean isCustomMappingValid(BaseClass bclass, String custommapping1, XWikiContext context)
      throws XWikiException {
    return getStore().isCustomMappingValid(bclass, custommapping1, context);
  }

  @Override
  public boolean injectCustomMapping(BaseClass doc1class, XWikiContext context)
      throws XWikiException {
    return getStore().injectCustomMapping(doc1class, context);
  }

  @Override
  public boolean injectCustomMappings(XWikiDocument doc, XWikiContext context)
      throws XWikiException {
    return getStore().injectCustomMappings(doc, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbyname, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      boolean,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbyname, customMapping, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean, int,
   *      int,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname, int nb,
      int start, XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbyname, nb, start, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      boolean, int, int,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, int nb, int start, XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbyname, customMapping, nb, start, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocuments(wheresql, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, int, int,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, nb, start, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      boolean, boolean, int,
   *      int, com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbyname,
      boolean customMapping, boolean checkRight, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbyname, customMapping, checkRight, nb,
        start, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean, int,
   *      int, java.util.List,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage, int nb,
      int start, List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbylanguage, nb, start, parameterValues,
        context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, java.util.List,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      boolean, int, int,
   *      java.util.List, com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage,
      boolean customMapping, int nb, int start, List<?> parameterValues, XWikiContext context)
      throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbylanguage, customMapping, nb, start,
        parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, int, int,
   *      java.util.List,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, int nb, int start,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, nb, start, parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#searchDocuments(java.lang.String, boolean,
   *      boolean, boolean, int,
   *      int, java.util.List, com.xpn.xwiki.XWikiContext)
   */
  @Override
  public List<XWikiDocument> searchDocuments(String wheresql, boolean distinctbylanguage,
      boolean customMapping, boolean checkRight, int nb, int start, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getStore().searchDocuments(wheresql, distinctbylanguage, customMapping, checkRight, nb,
        start, parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see XWikiStoreInterface#countDocuments(String, List, XWikiContext)
   */
  @Override
  public int countDocuments(String parametrizedSqlClause, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getStore().countDocuments(parametrizedSqlClause, parameterValues, context);
  }

  @Override
  public XWikiLock loadLock(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getStore().loadLock(docId, context, bTransaction);
  }

  @Override
  public void saveLock(XWikiLock lock, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getStore().saveLock(lock, context, bTransaction);
  }

  @Override
  public void deleteLock(XWikiLock lock, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getStore().deleteLock(lock, context, bTransaction);
  }

  @Override
  public List<XWikiLink> loadLinks(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getStore().loadLinks(docId, context, bTransaction);
  }

  /**
   * @since 2.2M2
   */
  @Override
  public List<DocumentReference> loadBacklinks(DocumentReference documentReference,
      boolean bTransaction, XWikiContext context) throws XWikiException {
    return getStore().loadBacklinks(documentReference, bTransaction, context);
  }

  /**
   * @deprecated since 2.2M2 use {@link #loadBacklinks(DocumentReference, boolean, XWikiContext)}
   */
  @Override
  @Deprecated
  public List<String> loadBacklinks(String fullName, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    return getStore().loadBacklinks(fullName, context, bTransaction);
  }

  @Override
  public void saveLinks(XWikiDocument doc, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getStore().saveLinks(doc, context, bTransaction);
  }

  @Override
  public void deleteLinks(long docId, XWikiContext context, boolean bTransaction)
      throws XWikiException {
    getStore().deleteLinks(docId, context, bTransaction);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#search(java.lang.String, int, int,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public <T> List<T> search(String sql, int nb, int start, XWikiContext context)
      throws XWikiException {
    return getStore().search(sql, nb, start, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#search(java.lang.String, int, int,
   *      java.lang.Object[][],
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public <T> List<T> search(String sql, int nb, int start, Object[][] whereParams,
      XWikiContext context) throws XWikiException {
    return getStore().search(sql, nb, start, whereParams, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#search(java.lang.String, int, int, java.util.List,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public <T> List<T> search(String sql, int nb, int start, List<?> parameterValues,
      XWikiContext context) throws XWikiException {
    return getStore().search(sql, nb, start, parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#search(java.lang.String, int, int,
   *      java.lang.Object[][],
   *      java.util.List, com.xpn.xwiki.XWikiContext)
   */
  @Override
  public <T> List<T> search(String sql, int nb, int start, Object[][] whereParams,
      List<?> parameterValues, XWikiContext context) throws XWikiException {
    return getStore().search(sql, nb, start, whereParams, parameterValues, context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#cleanUp(com.xpn.xwiki.XWikiContext)
   */
  @Override
  public synchronized void cleanUp(XWikiContext context) {
    getStore().cleanUp(context);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#isWikiNameAvailable(java.lang.String,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public boolean isWikiNameAvailable(String wikiName, XWikiContext context) throws XWikiException {
    synchronized (wikiName) {
      return getStore().isWikiNameAvailable(wikiName, context);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#createWiki(java.lang.String,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public void createWiki(String wikiName, XWikiContext context) throws XWikiException {
    synchronized (wikiName) {
      getStore().createWiki(wikiName, context);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see com.xpn.xwiki.store.XWikiStoreInterface#deleteWiki(java.lang.String,
   *      com.xpn.xwiki.XWikiContext)
   */
  @Override
  public void deleteWiki(String wikiName, XWikiContext context) throws XWikiException {
    synchronized (wikiName) {
      getStore().deleteWiki(wikiName, context);
      flushCache();
    }
  }

  @Override
  public boolean exists(XWikiDocument doc, XWikiContext context) throws XWikiException {
    String key = getKey(doc);
    try {
      Boolean result = getPageExistCache().get(key);

      if (result != null) {
        return result;
      }
    } catch (Exception e) {
    }

    boolean result = getStore().exists(doc, context);
    getPageExistCache().set(key, new Boolean(result));

    return result;
  }

  public Cache<XWikiDocument> getCache() {
    // make sure cache is initialized
    initalizeCache();
    return this.cache;
  }

  public void setCache(Cache<XWikiDocument> cache) {
    this.cache = cache;
  }

  public Cache<Boolean> getPageExistCache() {
    // make sure cache is initialized
    initalizeCache();
    return this.pageExistCache;
  }

  public void setPageExistCache(Cache<Boolean> pageExistCache) {
    this.pageExistCache = pageExistCache;
  }

  @Override
  public List<String> getCustomMappingPropertyList(BaseClass bclass) {
    return getStore().getCustomMappingPropertyList(bclass);
  }

  @Override
  public synchronized void injectCustomMappings(XWikiContext context) throws XWikiException {
    getStore().injectCustomMappings(context);
  }

  @Override
  public void injectUpdatedCustomMappings(XWikiContext context) throws XWikiException {
    getStore().injectUpdatedCustomMappings(context);
  }

  @Override
  public List<String> getTranslationList(XWikiDocument doc, XWikiContext context)
      throws XWikiException {
    return getStore().getTranslationList(doc, context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QueryManager getQueryManager() {
    return getStore().getQueryManager();
  }

  private class DocumentLoader {

    private XWikiDocument loadedDoc;
    private final String key;

    public DocumentLoader(String key) {
      this.key = key;
    }

    /**
     * IMPORTANT: do not change anything on the synchronization of this method.
     * e.g. do not change to lazy initialization of lodedDoc or similar. It is not working.
     * It is a very delicate case and very likely memory visibility breaks in 1 out of 100'000
     * document loads.
     */
    synchronized XWikiDocument loadDocument(String key, XWikiDocument doc, XWikiContext context)
        throws XWikiException {
      if (!this.key.equals(key)) {
        throw new RuntimeException(
            "DocumentLoader illegally used with a different key (registered key:" + this.key
                + ", loading doc key: " + key + ").");
      }
      if (loadedDoc == null) {
        // if a thread is just between the document cache miss and getting the documentLoader
        // when the documentLoader removes itself from the map, then a new documentLoader is
        // generated. Therefore we double check here that still no document is in cache.
        loadedDoc = getCache().get(key);
        if (loadedDoc == null) {
          LOGGER_DL.trace("DocumentLoader-{}: Trying to get doc '{}' for real",
              Thread.currentThread().getId(), key);
          // IMPORTANT: do not clone here. Creating new document is much faster.
          loadedDoc = new XWikiDocument(doc.getDocumentReference());
          loadedDoc.setLanguage(doc.getLanguage());
          loadedDoc = store.loadXWikiDoc(loadedDoc, context);
          loadedDoc.setStore(store);
          LOGGER_DL.info("DocumentLoader-{}: put doc '{}' in cache", Thread.currentThread().getId(),
              key);
          // XXX check if this is an possible unsafe publication over the cache
          getCache().set(key, loadedDoc);
          getPageExistCache().set(key, new Boolean(!loadedDoc.isNew()));
        }
        documentLoaderMap.remove(key);
      }
      return loadedDoc;
    }

  }

}
