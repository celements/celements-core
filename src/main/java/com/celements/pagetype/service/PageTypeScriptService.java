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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;

@Component("pageType")
public class PageTypeScriptService implements ScriptService {

  @Requirement
  IPageTypeRole pageTypeService;

  public List<String> getAllPageTypes() {
    return getPageTypesByCategories(Arrays.asList("", "pageType"), false);
  }

  public List<String> getAvailablePageTypes() {
    return getPageTypesByCategories(Arrays.asList("", "pageType"), true);
  }

  public List<String> getPageTypesByCategories(List<String> catList, boolean onlyVisible
      ) {
    return pageTypeService.getPageTypesConfigNamesForCategories(new HashSet<String>(
        catList), false);
  }

  public List<String> getAllCellTypes() {
    return getPageTypesByCategories(Arrays.asList("celltype"), false);
  }

  public List<String> getAvailableCellTypes() {
    return getPageTypesByCategories(Arrays.asList("celltype"), true);
  }

  public IPageTypeConfig getPageTypeConfig(String pageTypeName) {
    return pageTypeService.getPageTypeConfig(pageTypeName);
  }

  public List<PageTypeReference> getPageTypeRefsForCategories(Set<String> catList,
      boolean onlyVisible) {
    return pageTypeService.getPageTypeRefsForCategories(catList, onlyVisible);
  }

}
