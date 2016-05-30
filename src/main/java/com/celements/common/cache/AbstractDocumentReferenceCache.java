package com.celements.common.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.query.IQueryExecutionServiceRole;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public abstract class AbstractDocumentReferenceCache<K> implements IDocumentReferenceCache<K> {

  private Map<WikiReference, Map<K, Set<DocumentReference>>> cache = new HashMap<>();

  @Requirement
  protected QueryManager queryManager;

  @Requirement
  protected IQueryExecutionServiceRole queryExecService;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public Set<DocumentReference> getCachedDocRefs(K key) throws CacheLoadingException {
    WikiReference wikiRef = null;
    if (key instanceof EntityReference) {
      wikiRef = webUtils.getWikiRef((EntityReference) key);
    }
    return getCachedDocRefs(wikiRef, key);
  }

  @Override
  public Set<DocumentReference> getCachedDocRefs(WikiReference wikiRef)
      throws CacheLoadingException {
    return ImmutableSet.copyOf(Iterables.concat(getCache(wikiRef).values()));
  }

  @Override
  public Set<DocumentReference> getCachedDocRefs(WikiReference wikiRef, K key)
      throws CacheLoadingException {
    Set<DocumentReference> ret = getCache(wikiRef).get(key);
    if (ret != null) {
      ret = ImmutableSet.copyOf(ret);
    } else {
      ret = ImmutableSet.of();
    }
    return ret;
  }

  @Override
  public synchronized void flush(WikiReference wikiRef) {
    if (wikiRef != null) {
      Map<K, Set<DocumentReference>> oldCache = cache.remove(wikiRef);
      if (getLogger().isTraceEnabled()) {
        getLogger().trace("flush: for wiki '{}': {}", wikiRef, oldCache);
      } else {
        getLogger().info("flush: for wiki '{}'", wikiRef);
      }
    }
  }

  private synchronized Map<K, Set<DocumentReference>> getCache(WikiReference wikiRef)
      throws CacheLoadingException {
    if (wikiRef == null) {
      wikiRef = webUtils.getWikiRef();
    }
    if (!cache.containsKey(wikiRef)) {
      try {
        cache.put(wikiRef, loadCache(wikiRef));
      } catch (QueryException | XWikiException exc) {
        throw new CacheLoadingException(exc);
      }
    }
    return cache.get(wikiRef);
  }

  private Map<K, Set<DocumentReference>> loadCache(WikiReference wikiRef) throws QueryException,
      XWikiException {
    getLogger().debug("loadCache: start for wiki '{}'", wikiRef);
    Map<K, Set<DocumentReference>> cache = new HashMap<>();
    for (DocumentReference docRef : executeXWQL(wikiRef)) {
      for (K key : getKeysForResult(docRef)) {
        if (!cache.containsKey(key)) {
          cache.put(key, new HashSet<DocumentReference>());
        }
        cache.get(key).add(docRef);
        getLogger().trace("loadCache: put '{}' - '{}'", key, docRef);
      }
    }
    return cache;
  }

  List<DocumentReference> executeXWQL(WikiReference wikiRef) throws QueryException {
    String xwql = "select distinct doc.fullName from Document doc, doc.object("
        + webUtils.serializeRef(getCacheClassRef(wikiRef), true) + ") as obj";
    Query query = queryManager.createQuery(xwql, Query.XWQL);
    query.setWiki(wikiRef.getName());
    return queryExecService.executeAndGetDocRefs(query);
  }

  /**
   * @return the class ref that documents will be cached for.
   */
  protected abstract DocumentReference getCacheClassRef(WikiReference wikiRef);

  /**
   * @param docRef
   *          a result element
   * @return the keys for the given result
   * @throws XWikiException
   */
  protected abstract Collection<K> getKeysForResult(DocumentReference docRef) throws XWikiException;

  protected abstract Logger getLogger();

}
