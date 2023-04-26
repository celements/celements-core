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
package com.celements.pagetype.service;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class PageTypeService implements IPageTypeRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageTypeService.class);

  @Requirement
  private Map<String, IPageTypeProviderRole> pageTypeProviders;

  @Requirement
  private IPageTypeClassConfig pageTypeClassConf;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private List<IPageTypeCategoryRole> pageTypeCategoryList;

  private final ConcurrentMap<String, IPageTypeCategoryRole> typeNameToCatCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, IPageTypeCategoryRole> typeNameToCatCacheDeprecated = new ConcurrentHashMap<>();

  @Override
  public void resetTypeNameToCatCache() {
    typeNameToCatCache.clear();
    typeNameToCatCacheDeprecated.clear();
  }

  private void loadTypeNameCategoryMaps() {
    if (typeNameToCatCache.isEmpty()) {
      synchronized (typeNameToCatCache) {
        if (typeNameToCatCache.isEmpty()) {
          for (IPageTypeCategoryRole typeCategory : pageTypeCategoryList) {
            IPageTypeCategoryRole beforeRegCat = typeNameToCatCache.putIfAbsent(
                typeCategory.getTypeName(), typeCategory);
            if (beforeRegCat != null) {
              LOGGER.warn("Page type category collision on category name '{}' the colliding"
                  + " category '{}' is shadowed by '{}'.", typeCategory.getTypeName(),
                  typeCategory.getClass(), beforeRegCat.getClass());
            }
            for (String deprecateName : typeCategory.getAllTypeNames()) {
              beforeRegCat = typeNameToCatCacheDeprecated.putIfAbsent(deprecateName, typeCategory);
              if (beforeRegCat != null) {
                LOGGER.warn("Page type category collision on deprecated category name '{}' the "
                    + "colliding category '{}' is shadowed by '{}'.", deprecateName,
                    typeCategory.getClass(), beforeRegCat.getClass());
              }
            }
          }
        }
      }
    }
  }

  private Map<String, IPageTypeCategoryRole> getTypeNameToCategoryMap() {
    loadTypeNameCategoryMaps();
    return Collections.unmodifiableMap(typeNameToCatCache);
  }

  private Map<String, IPageTypeCategoryRole> getTypeNameToCategoryMapIncludeDeprecated() {
    loadTypeNameCategoryMaps();
    return Collections.unmodifiableMap(typeNameToCatCacheDeprecated);
  }

  @Override
  public Optional<IPageTypeCategoryRole> getTypeCategoryForCatName(String categoryName) {
    return Optional.fromNullable(getTypeNameToCategoryMapIncludeDeprecated().get(categoryName));
  }

  @Override
  public List<String> getTypesForCategory(String categoryName, boolean onlyVisible) {
    Optional<IPageTypeCategoryRole> typeCategory = getTypeCategoryForCatName(categoryName);
    if (typeCategory.isPresent()) {
      return getPageTypesConfigNamesForCategories(typeCategory.get().getAllTypeNames(),
          onlyVisible);
    } else {
      LOGGER.warn("cannot find types category name ''", categoryName);
    }
    return Collections.emptyList();
  }

  @Override
  public IPageTypeConfig getPageTypeConfig(String pageTypeName) {
    if (pageTypeName != null) {
      PageTypeReference pageTypeRef = getPageTypeRefByConfigName(pageTypeName);
      if (pageTypeRef != null) {
        return getPageTypeConfigForPageTypeRef(pageTypeRef);
      }
    }
    return null;
  }

  @Override
  public IPageTypeConfig getPageTypeConfigForPageTypeRef(PageTypeReference pageTypeRef) {
    return getProviderForPageTypeRef(pageTypeRef).getPageTypeByReference(pageTypeRef);
  }

  @Override
  @Deprecated
  public PageTypeReference getPageTypeRefByConfigName(String pageTypeName) {
    return getPageTypeReference(pageTypeName).orNull();
  }

  @Override
  public Optional<PageTypeReference> getPageTypeReference(String pageTypeName) {
    return Optional.fromNullable(getPageTypeRefsByConfigNames().get(pageTypeName));
  }

  private IPageTypeProviderRole getProviderForPageTypeRef(PageTypeReference pageTypeRef) {
    return pageTypeProviders.get(pageTypeRef.getProviderHint());
  }

  @Override
  public List<String> getPageTypesConfigNamesForCategories(Set<String> catList,
      boolean onlyVisible) {
    List<String> pageTypeConfigNameList = new ArrayList<>();
    for (PageTypeReference pageTypeRef : getPageTypeRefsForCategories(catList, onlyVisible)) {
      pageTypeConfigNameList.add(pageTypeRef.getConfigName());
    }
    LOGGER.debug("getPageTypesConfigNamesForCategories: return " + Arrays.deepToString(
        pageTypeConfigNameList.toArray()));
    return pageTypeConfigNameList;
  }

  @Override
  public List<PageTypeReference> getPageTypeRefsForCategories(Set<String> catList,
      boolean onlyVisible) {
    if (onlyVisible) {
      Set<PageTypeReference> visiblePTSet = new HashSet<>();
      for (PageTypeReference pageTypeRef : getPageTypeRefsForCategories(catList)) {
        if (getPageTypeConfigForPageTypeRef(pageTypeRef).isVisible()) {
          visiblePTSet.add(pageTypeRef);
        }
      }
      LOGGER.debug("getPageTypeRefsForCategories: for catList [" + Arrays.deepToString(
          catList.toArray()) + "] and onlyVisible [" + onlyVisible + "] return "
          + Arrays.deepToString(visiblePTSet.toArray()));
      return new ArrayList<>(visiblePTSet);
    } else {
      return new ArrayList<>(getPageTypeRefsForCategories(catList));
    }
  }

  Map<String, PageTypeReference> getPageTypeRefsByConfigNames() {
    Map<String, PageTypeReference> pageTypeRefsMap = new HashMap<>();
    for (PageTypeReference pageTypeRef : getAllPageTypeRefs()) {
      pageTypeRefsMap.put(pageTypeRef.getConfigName(), pageTypeRef);
    }
    return pageTypeRefsMap;
  }

  private Set<PageTypeReference> getAllPageTypeRefs() {
    HashSet<PageTypeReference> pageTypeRefSet = new HashSet<>();
    for (IPageTypeProviderRole pageTypeProvider : pageTypeProviders.values()) {
      for (PageTypeReference pageTypeRef : pageTypeProvider.getPageTypes()) {
        pageTypeRefSet.add(pageTypeRef);
      }
    }
    LOGGER.debug("getAllPageTypeRefs: return " + Arrays.deepToString(pageTypeRefSet.toArray()));
    return pageTypeRefSet;
  }

  Set<PageTypeReference> getPageTypeRefsForCategories(Set<String> catList) {
    catList = new HashSet<>(catList);
    Set<PageTypeReference> filteredPTset = new HashSet<>();
    for (PageTypeReference pageTypeRef : getAllPageTypeRefs()) {
      List<String> categories = pageTypeRef.getCategories();
      if (categories.isEmpty()) {
        LOGGER.warn("getPageTypeRefsForCategories: skip pageTypeRef [" + pageTypeRef
            + "] because no categories found!");
      } else if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("getPageTypeRefsForCategories: test [" + pageTypeRef + "] for categories ["
            + Arrays.deepToString(categories.toArray()) + "] size [" + categories.size() + "].");
      }
      for (String category : pageTypeRef.getCategories()) {
        if (catList.contains(category)) {
          filteredPTset.add(pageTypeRef);
          LOGGER.trace("getPageTypeRefsForCategories: added [" + pageTypeRef + "] with category ["
              + category + "].");
        } else {
          LOGGER.trace("getPageTypeRefsForCategories: skip [" + pageTypeRef + "] with category ["
              + category + "].");
        }
      }
    }
    LOGGER.debug("getPageTypeRefsForCategories: for catList [" + Arrays.deepToString(
        catList.toArray()) + "] return " + Arrays.deepToString(filteredPTset.toArray()));
    return filteredPTset;
  }

  @Override
  public boolean setPageType(XWikiDocument doc, PageTypeReference ref) {
    checkNotNull(doc);
    checkNotNull(ref);
    try {
      BaseObject obj = modelAccess.getOrCreateXObject(doc, pageTypeClassConf.getPageTypeClassRef());
      boolean hasChanged = !ref.getConfigName().equals(modelAccess.getProperty(obj,
          IPageTypeClassConfig.PAGE_TYPE_FIELD));
      if (hasChanged) {
        modelAccess.setProperty(obj, IPageTypeClassConfig.PAGE_TYPE_FIELD, ref.getConfigName());
      }
      return hasChanged;
    } catch (ClassDocumentLoadException exc) {
      throw new IllegalStateException("Unable to load PageTypeClass", exc);
    }
  }

}
