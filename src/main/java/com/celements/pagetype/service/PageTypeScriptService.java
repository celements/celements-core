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

import java.util.HashSet;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.category.IPageTypeCategoryRole;

@Component("pageType")
public class PageTypeScriptService implements ScriptService {

  @Requirement
  IPageTypeRole pageTypeService;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Deprecated
  @Requirement
  IPageTypeCategoryRole pageTypeCategory;

  @Deprecated
  @Requirement("cellTypeCategory")
  IPageTypeCategoryRole cellTypeCategory;

  /**
   * @Deprecated since 2.82 instead use getAllTypesForCategory('pageType')
   */
  @Deprecated
  public List<String> getAllPageTypes() {
    return pageTypeService.getPageTypesConfigNamesForCategories(pageTypeCategory.getAllTypeNames(),
        false);
  }

  /**
   * @Deprecated since 2.82 instead use getAvailableTypesForCategory('pageType')
   */
  @Deprecated
  public List<String> getAvailablePageTypes() {
    return pageTypeService.getPageTypesConfigNamesForCategories(pageTypeCategory.getAllTypeNames(),
        true);
  }

  public List<String> getPageTypesByCategories(List<String> catList, boolean onlyVisible) {
    return pageTypeService.getPageTypesConfigNamesForCategories(new HashSet<>(catList),
        onlyVisible);
  }

  /**
   * @Deprecated since 2.82 instead use getAllTypesForCategory('celltype')
   */
  @Deprecated
  public List<String> getAllCellTypes() {
    return pageTypeService.getPageTypesConfigNamesForCategories(cellTypeCategory.getAllTypeNames(),
        false);
  }

  /**
   * @Deprecated since 2.82 instead use getAvailableTypesForCategory('celltype')
   */
  @Deprecated
  public List<String> getAvailableCellTypes() {
    return pageTypeService.getPageTypesConfigNamesForCategories(cellTypeCategory.getAllTypeNames(),
        true);
  }

  public List<String> getAvailableTypesForCategory(String categoryName) {
    return pageTypeService.getTypesForCategory(categoryName, true);
  }

  public List<String> getAllTypesForCategory(String categoryName) {
    return pageTypeService.getTypesForCategory(categoryName, false);
  }

  public IPageTypeConfig getPageTypeConfig(String pageTypeName) {
    return pageTypeService.getPageTypeConfig(pageTypeName);
  }

  public IPageTypeConfig getPageTypeConfig(PageTypeReference pageTypeRef) {
    return pageTypeService.getPageTypeConfigForPageTypeRef(pageTypeRef);
  }

  public List<PageTypeReference> getPageTypeRefsForCategories(List<String> catList,
      boolean onlyVisible) {
    return pageTypeService.getPageTypeRefsForCategories(new HashSet<>(catList), onlyVisible);
  }

  public PageTypeReference getPageTypeRef(DocumentReference docRef) {
    if (docRef != null) {
      return pageTypeResolver.getPageTypeRefForDocWithDefault(docRef);
    }
    return null;
  }

}
