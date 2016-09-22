package com.celements.pagetype.java;

import java.util.HashSet;
import java.util.Set;

import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;

public abstract class AbstractJavaPageType implements IJavaPageTypeRole {

  @Override
  public Set<String> getCategoryNames() {
    Set<String> categories = new HashSet<>();
    for (IPageTypeCategoryRole ptCat : getCategories()) {
      categories.addAll(ptCat.getAllTypeNames());
    }
    return categories;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.absent();
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    // default implementation: do nothing
  }

}
