package com.celements.cells.classes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractLegacyClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(CellsClassPackage.NAME)
@ThreadSafe
public final class CellsClassPackage extends AbstractLegacyClassPackage {

  public static final String NAME = "celementsCells";
  public static final String LEGACY_NAME = "celCellsClasses";

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

  @Override
  public @NotNull String getLegacyName() {
    return LEGACY_NAME;
  }

}
