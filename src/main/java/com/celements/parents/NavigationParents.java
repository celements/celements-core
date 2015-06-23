package com.celements.parents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.cache.CacheLoadingException;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.navigation.NavigationCache;
import com.celements.web.service.IWebUtilsService;

@Component(NavigationParents.NAME)
public class NavigationParents implements IDocParentProviderRole {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(
      NavigationParents.class);

  public static final String NAME = "navigation";

  @Requirement(NavigationCache.NAME)
  IDocumentReferenceCache<String> navCache;

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
    List<DocumentReference> ret;
    String spaceName = docRef.getLastSpaceReference().getName();
    try {
      ret = new ArrayList<DocumentReference>();
      ret.addAll(navCache.getCachedDocRefs(webUtils.getWikiRef(docRef), spaceName));
      ret.addAll(navCache.getCachedDocRefs(webUtils.getCentralWikiRef(), spaceName));
      // TODO get potential parents located in other wikis ?
    } catch (CacheLoadingException exp) {
      LOGGER.error("Failed loading cache for docRef '{}'", docRef, exp);
      ret = Collections.emptyList();
    }
    return ret;
  }

}
