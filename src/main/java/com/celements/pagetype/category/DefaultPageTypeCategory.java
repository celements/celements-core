package com.celements.pagetype.category;

import java.util.Set;

import org.xwiki.component.annotation.Component;

import com.google.common.collect.ImmutableSet;

@Component
public class DefaultPageTypeCategory extends AbstractPageTypeCategory {

  private Set<String> deprecatedNames = ImmutableSet.of("");

  @Override
  public String getTypeName() {
    return "pageType";
  }

  @Override
  public Set<String> getDeprecatedNames() {
    return deprecatedNames;
  }

}
