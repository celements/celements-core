package com.celements.common.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.query.IQueryExecutionServiceRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractDocumentReferenceCache 
    implements IDocumentReferenceCache, EventListener {

  private Map<WikiReference, Map<EntityReference, Set<DocumentReference>>> cache = 
      new HashMap<>();

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IQueryExecutionServiceRole queryExecService;

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public Set<DocumentReference> getCachedDocRefs(EntityReference ref
      ) throws CacheLoadingException {
    WikiReference wikiRef = (WikiReference) ref.extractReference(EntityType.WIKI);
    Set<DocumentReference> ret = getCache(wikiRef).get(ref);
    if (ret != null) {
      ret = Collections.unmodifiableSet(ret);
    } else {
      ret = Collections.emptySet();
    }
    return ret;
  }

  @Override
  public synchronized void flush(WikiReference wikiRef) {
    if (wikiRef != null) {
      cache.remove(wikiRef);
      getLogger().info("flush: for wiki '{}'", wikiRef);
    }
  }

  private synchronized Map<EntityReference, Set<DocumentReference>> getCache(
      WikiReference wikiRef) throws CacheLoadingException {
    if (!cache.containsKey(wikiRef)) {
      try {
        cache.put(wikiRef, loadCache(wikiRef));
      } catch (QueryException | XWikiException exc) {
        throw new CacheLoadingException(exc);
      }
    }
    return cache.get(wikiRef);
  }

  private Map<EntityReference, Set<DocumentReference>> loadCache(WikiReference wikiRef
      ) throws QueryException, XWikiException {
    Map<EntityReference, Set<DocumentReference>> cache = new HashMap<>();
    for (DocumentReference docRef : executeXWQL(wikiRef)) {
      EntityReference ref = getMergeRefForResult(docRef);
      if (ref != null) {
        if (!cache.containsKey(ref)) {
          cache.put(ref, new HashSet<DocumentReference>());
        }
        cache.get(ref).add(docRef);
      }
    }
    getLogger().trace("loadCacheForWiki: '{}': {}", wikiRef, cache);
    return cache;
  }

  List<DocumentReference> executeXWQL(WikiReference wikiRef) throws QueryException {
    String xwql = "select distinct doc.fullName from Document doc, doc.object("
        + webUtilsService.serializeRef(getCacheClassRef(), true) + ") as obj";
    Query query = queryManager.createQuery(xwql, Query.XWQL);
    query.setWiki(wikiRef.getName());
    return queryExecService.executeAndGetDocRefs(query);
  }

  /**
   * NOTE: the contained wiki ref isn't relevant
   * 
   * @return the class ref that documents will be cached for.
   */
  protected abstract DocumentReference getCacheClassRef();

  /**
   * 
   * @param docRef
   *          a result element
   * @return the ref for the given result to merge with other results, used as key for the
   *         cache
   * @throws XWikiException
   */
  protected EntityReference getMergeRefForResult(DocumentReference docRef
      ) throws XWikiException {
    return webUtilsService.getWikiRef(docRef);
  }

  @Override
  public List<Event> getEvents() {
    return getFlushingEvents();
  }

  /**
   * @return a list of events that will flush the cache when notified
   */
  protected abstract List<Event> getFlushingEvents();

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if (source instanceof XWikiDocument) {
      flush(webUtilsService.getWikiRef((XWikiDocument) source));
    } else {
      getLogger().error("unable to flush cache for source '{}'", source);
    }
  }

  protected abstract Logger getLogger();

}
