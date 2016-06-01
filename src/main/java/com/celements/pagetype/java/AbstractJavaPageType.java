package com.celements.pagetype.java;

import java.util.HashSet;
import java.util.Set;

import com.celements.pagetype.category.IPageTypeCategoryRole;

public abstract class AbstractJavaPageType implements IJavaPageTypeRole {

  @Override
  public Set<String> getCategoryNames() {
    Set<String> categories = new HashSet<>();
    for (IPageTypeCategoryRole ptCat : getCategories()) {
      categories.addAll(ptCat.getAllTypeNames());
    }
    return categories;
  }

}
