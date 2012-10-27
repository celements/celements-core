package com.celements.pagetype.service;

import java.util.List;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;

@ComponentRole
public interface IPageTypeRole {

  public IPageTypeConfig getPageTypeConfig(String pageTypeName);

  public List<String> getPageTypesConfigNamesForCategories(Set<String> catList,
      boolean onlyVisible);

  public List<PageTypeReference> getPageTypeRefsForCategories(Set<String> catList,
      boolean onlyVisible);
}
