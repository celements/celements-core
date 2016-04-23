package com.celements.pagetype.category;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractPageTypeCategory implements IPageTypeCategoryRole {

  private ImmutableSet<String> allTypeNames;

  @Override
  public Set<String> getAllTypeNames() {
    if (allTypeNames == null) {
      Set<String> allTypes = new HashSet<>();
      allTypes.add(getTypeName());
      allTypes.addAll(getDeprecatedNames());
      allTypeNames = ImmutableSet.copyOf(allTypes);
    }
    return allTypeNames;
  }

  @Override
  public Set<String> getDeprecatedNames() {
    return Collections.emptySet();
  }

}
