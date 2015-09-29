package com.celements.parents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeProvider;
import com.celements.web.service.XDocRecursionException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
public class DocumentParentsLister implements IDocumentParentsListerRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(DocumentParentsLister.class);

  @Requirement
  Map<String, IDocParentProviderRole> docParentProviderMap;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement(XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER)
  IPageTypeProviderRole pageTypeProvider;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    ArrayList<DocumentReference> parents = new ArrayList<DocumentReference>();
    try {
      setParent(parents, docRef);
      boolean hasMore = true;
      while (hasMore) {
        setPrimaryParents(parents);
        hasMore = setSecondaryProviderParent(parents);
      }
      if (!includeDoc) {
        parents.remove(0);
      }
    } catch (XDocRecursionException recExp) {
      _LOGGER.info("Recursion in document parents found [" + recExp + "].");
    }
    return parents;
  }

  public void setPrimaryParents(List<DocumentReference> parents
      ) throws XDocRecursionException {
    for (String providerName : getPrimaryProviderNames()) {
      IDocParentProviderRole provider = docParentProviderMap.get(providerName);
      DocumentReference docRef = Iterables.getLast(parents);
      for (DocumentReference parentRef : provider.getDocumentParentsList(docRef)) {
        setParent(parents, parentRef);
      }
    }
  }

  public boolean setSecondaryProviderParent(List<DocumentReference> parents
      ) throws XDocRecursionException {
    DocumentReference docRef = Iterables.getLast(parents);
    List<DocumentReference> nextParents = Collections.emptyList();
    Iterator<String> iter = getSecondaryProviderNames().iterator();
    while (nextParents.isEmpty() && iter.hasNext()) {
      nextParents = docParentProviderMap.get(iter.next()).getDocumentParentsList(docRef);
      nextParents = checkPageTypes(nextParents);
    }
    if (!nextParents.isEmpty()) {
      setParent(parents, nextParents.get(0));
      if (nextParents.size() > 1) {
        _LOGGER.warn("Received multiple parents for '{}': {}", docRef, nextParents);
      }
      return true;
    } else {
      return false;
    }
  }

  private List<DocumentReference> checkPageTypes(List<DocumentReference> parents) {
    List<DocumentReference> ret = new ArrayList<>();
    for (DocumentReference parent : parents) {
      IPageTypeConfig pageTypeConf = pageTypeProvider.getPageTypeByReference(
          pageTypeResolver.getPageTypeRefForDocWithDefault(parent));
      if (pageTypeConf.isUnconnectedParent()) {
        ret.add(parent);
      }
    }
    return ret;
  }

  private void setParent(List<DocumentReference> parents, DocumentReference toAdd
      ) throws XDocRecursionException {
    if (!parents.contains(toAdd)) {
      parents.add(toAdd);
    } else {
      throw new XDocRecursionException(toAdd);
    }
  }

  private Set<String> getPrimaryProviderNames() {
    return ImmutableSet.of(XDocParents.DOC_PROVIDER_NAME);
  }

  private Set<String> getSecondaryProviderNames() {
    Set<String> ret = new HashSet<>(docParentProviderMap.keySet());
    ret.removeAll(getPrimaryProviderNames());
    return ret;
  }

}
