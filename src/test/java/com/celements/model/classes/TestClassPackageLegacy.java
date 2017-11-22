package com.celements.model.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

@Component(TestClassPackageLegacy.NAME)
public class TestClassPackageLegacy extends AbstractLegacyClassPackage {

  public static final String NAME = "TestLegacy";

  @Requirement
  private List<TestClassDefinitionRole> classDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDefs);
  }

  private String legacyName;

  @Override
  public String getLegacyName() {
    return legacyName;
  }

  public void setLegacyName(String legacyName) {
    this.legacyName = legacyName;
  }

}
