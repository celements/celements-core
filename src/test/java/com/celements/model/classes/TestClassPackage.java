package com.celements.model.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.google.common.base.Optional;

@Component(TestClassPackage.NAME)
public class TestClassPackage extends AbstractClassPackage {

  public static final String NAME = "Test";

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

  String legacyName;

  @Override
  protected Optional<String> getLegacyName() {
    return Optional.fromNullable(legacyName);
  }

  public void setLegacyName(String legacyName) {
    this.legacyName = legacyName;
  }

}
