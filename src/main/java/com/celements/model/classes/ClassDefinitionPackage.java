package com.celements.model.classes;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ClassDefinitionPackage {

  public static final String CFG_SRC_KEY = "celements.classdefinition.active";

  /**
   * @return the name of the class definition package
   */
  public String getName();

  /**
   * @return true if the package is activated
   */
  public boolean isActivated();

  /**
   * @return the class definitions contained in this package
   */
  public List<? extends ClassDefinition> getClassDefinitions();

}
