package com.celements.common.cache;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IDocumentReferenceCache<K> {

  /**
   * @param wikiRef
   *          the wiki in which to look up
   * @return all cached doc refs in the provided wiki
   * @throws CacheLoadingException
   */
  public Set<DocumentReference> getCachedDocRefs(WikiReference wikiRef
      ) throws CacheLoadingException;

  /**
   * Tries to determine the wiki itself via given key or context. If this behaviour is 
   * not desired, use {@link #getCachedDocRefs(WikiReference, Object)}
   * 
   * @param key
   *          the cache key
   * @return all cached doc refs for the given key
   * @throws CacheLoadingException
   */
  public Set<DocumentReference> getCachedDocRefs(K key) throws CacheLoadingException;

  /**
   * @param wikiRef
   *          the wiki in which to look the key up
   * @param key
   *          the cache key
   * @return all cached doc refs for the given key in the provided wiki
   * @throws CacheLoadingException
   */
  public Set<DocumentReference> getCachedDocRefs(WikiReference wikiRef, K key
      ) throws CacheLoadingException;

  /**
   * @param wikiRef
   *          to flush the cache for
   */
  public void flush(WikiReference wikiRef);

}
