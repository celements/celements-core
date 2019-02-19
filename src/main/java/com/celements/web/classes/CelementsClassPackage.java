package com.celements.web.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(CelementsClassPackage.NAME)
public class CelementsClassPackage extends AbstractClassPackage {

  public static final String NAME = "celements";

  @Requirement
  private List<CelementsClassDefinition> classDef;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDef);
  }
}
