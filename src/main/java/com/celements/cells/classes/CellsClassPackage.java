package com.celements.cells.classes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.CelementsClassPackage;

@Component(CelementsClassPackage.NAME)
@ThreadSafe
public final class CellsClassPackage extends AbstractClassPackage {

  public static final String NAME = "celCellsClasses";

  @Requirement
  private List<CellsClassDefinition> classDef;

  @Override
  public final String getName() {
    return NAME;
  }

  @Override
  public final List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDef);
  }

}
