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
 */
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

  private static Logger LOGGER = LoggerFactory.getLogger(DocumentParentsLister.class);

  @Requirement
  Map<String, IDocParentProviderRole> docParentProviderMap;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement(XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER)
  IPageTypeProviderRole pageTypeProvider;

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef,
      boolean includeDoc) {
    ArrayList<DocumentReference> parents = new ArrayList<>();
    try {
      setParent(parents, docRef);
      boolean hasMore = !parents.isEmpty();
      while (hasMore) {
        setPrimaryParents(parents);
        hasMore = setSecondaryParent(parents);
      }
      if (!parents.isEmpty() && !includeDoc) {
        parents.remove(0);
      }
    } catch (XDocRecursionException recExp) {
      LOGGER.info("Recursion in document parents found [" + recExp + "].");
    }
    return parents;
  }

  private void setPrimaryParents(List<DocumentReference> parents) throws XDocRecursionException {
    LOGGER.debug("setPrimaryParents started with: {}", parents);
    for (String providerName : getPrimaryProviderNames()) {
      IDocParentProviderRole provider = docParentProviderMap.get(providerName);
      DocumentReference docRef = Iterables.getLast(parents);
      for (DocumentReference parentRef : provider.getDocumentParentsList(docRef)) {
        setParent(parents, parentRef);
      }
    }
    LOGGER.debug("setPrimaryParents finished with: {}", parents);
  }

  private boolean setSecondaryParent(List<DocumentReference> parents)
      throws XDocRecursionException {
    LOGGER.debug("setSecondaryParent started with: {}", parents);
    DocumentReference docRef = Iterables.getLast(parents);
    List<DocumentReference> nextParents = Collections.emptyList();
    Iterator<String> iter = getSecondaryProviderNames().iterator();
    while (nextParents.isEmpty() && iter.hasNext()) {
      nextParents = docParentProviderMap.get(iter.next()).getDocumentParentsList(docRef);
      nextParents = checkPageTypes(nextParents);
    }
    boolean hasNext = !nextParents.isEmpty();
    if (hasNext) {
      setParent(parents, nextParents.get(0));
      if (nextParents.size() > 1) {
        LOGGER.warn("Received multiple parents for '{}': {}", docRef, nextParents);
      }
    }
    LOGGER.debug("setSecondaryParent finished with: {}", parents);
    return hasNext;
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

  private void setParent(List<DocumentReference> parents, DocumentReference toAdd)
      throws XDocRecursionException {
    if (toAdd == null) {
      LOGGER.warn("tried to add null as parent for doc '{}'", Iterables.getFirst(parents, null));
    } else if (parents.contains(toAdd)) {
      throw new XDocRecursionException(toAdd);
    } else {
      parents.add(toAdd);
      LOGGER.trace("added '{}' to parent list", toAdd);
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
