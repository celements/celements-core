package com.celements.common.cache;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IDocumentReferenceCache {

  /**
   * @param ref
   *          to get doc refs for, usually {@link WikiReference} or {@link SpaceReference}
   * @return all cached doc refs
   * @throws CacheLoadingException
   */
  public Set<DocumentReference> getCachedDocRefs(EntityReference ref
      ) throws CacheLoadingException;

  /**
   * @param wikiRef
   *          to flush the cache for
   */
  public void flush(WikiReference wikiRef);

}
