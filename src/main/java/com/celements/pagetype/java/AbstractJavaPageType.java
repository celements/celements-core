package com.celements.pagetype.java;

import java.util.Set;
import java.util.stream.Collectors;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;

public abstract class AbstractJavaPageType implements IJavaPageTypeRole {

  @Requirement
  private IPageTypeCategoryRole defaultCategory;

  @Override
  public Set<String> getCategoryNames() {
    Set<IPageTypeCategoryRole> categories = getCategories();
    if (categories.isEmpty()) {
      categories.add(defaultCategory);
    }
    return categories.stream()
        .map(IPageTypeCategoryRole::getAllTypeNames)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.absent();
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    // default implementation: do nothing
  }

  @Override
  public boolean useInlineEditorMode() {
    return false;
  }

}
