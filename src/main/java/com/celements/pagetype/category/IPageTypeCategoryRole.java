package com.celements.pagetype.category;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IPageTypeCategoryRole {

  public String getTypeName();

  public Set<String> getDeprecatedNames();

  public Set<String> getAllTypeNames();

}
