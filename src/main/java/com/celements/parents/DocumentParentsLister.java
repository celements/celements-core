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
import com.google.common.collect.Iterables;

@Component
public class DocumentParentsLister implements IDocumentParentsListerRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(DocumentParentsLister.class);

  @Requirement
  Map<String, IDocParentProviderRole> docParentProviderMap;

  @Requirement(XDocParents.DOC_PROVIDER_NAME)
  IDocParentProviderRole xDocParents;

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
      Set<String> providers = getProvidersWithout(null);
      boolean hasMore = true;
      while (hasMore) {
        setXDocParents(parents);
        hasMore = setNextProviderParent(parents, providers);
      }
      if (!includeDoc) {
        parents.remove(0);
      }
    } catch (XDocRecursionException recExp) {
      _LOGGER.info("Recursion in document parents found [" + recExp + "].");
    }
    return parents;
  }

  public void setXDocParents(List<DocumentReference> ret) throws XDocRecursionException {
    DocumentReference docRef = Iterables.getLast(ret);
    for (DocumentReference parentRef : xDocParents.getDocumentParentsList(docRef)) {
      setParent(ret, parentRef);
    }
  }

  public boolean setNextProviderParent(List<DocumentReference> parents, 
      Set<String> providers) throws XDocRecursionException {
    DocumentReference docRef = Iterables.getLast(parents);
    List<DocumentReference> nextParents = Collections.emptyList();
    String provider = null;
    Iterator<String> iter = providers.iterator();
    while (nextParents.isEmpty() && iter.hasNext()) {
      provider = iter.next();
      nextParents = docParentProviderMap.get(provider).getDocumentParentsList(docRef);
      nextParents = checkPageTypes(nextParents);
    }
    if (!nextParents.isEmpty()) {
      providers.clear();
      providers.addAll(getProvidersWithout(provider));
      setParent(parents, nextParents.get(0));
      if (nextParents.size() > 1) {
        _LOGGER.warn("Received multiple parents for '{}' from provider '{}': {}", docRef,
            provider, nextParents);
      }
      return true;
    } else {
      return false;
    }
  }

  private Set<String> getProvidersWithout(String provider) {
    Set<String> ret = new HashSet<>(docParentProviderMap.keySet());
    ret.remove(XDocParents.DOC_PROVIDER_NAME);
    ret.remove(provider);
    return ret;
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

}
