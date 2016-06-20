package com.celements.model.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

@Component(TestClassDefinitionPackage.NAME)
public class TestClassDefinitionPackage extends AbstractClassDefinitionPackage {

  public static final String NAME = "Test";

  @Requirement
  private List<TestClassDefinitionRole> testClassDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(testClassDefs);
  }

}
