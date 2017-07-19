package com.celements.pagetype.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractLegacyClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(PageTypeClassPackage.NAME)
public class PageTypeClassPackage extends AbstractLegacyClassPackage {

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
  public String getLegacyName() {
    return "celPageTypeClasses";
  }

}
