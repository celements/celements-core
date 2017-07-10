package com.celements.pagetype.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;
import com.google.common.base.Optional;

@Component(PageTypeClassPackage.NAME)
public class PageTypeClassPackage extends AbstractClassPackage {

  public static final String NAME = "pagetype";

  @Requirement
  private List<PageTypeClassDefinition> classDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDefs);
  }

  @Override
  protected Optional<String> getLegacyName() {
    return Optional.of("celPageTypeClasses");
  }

}
