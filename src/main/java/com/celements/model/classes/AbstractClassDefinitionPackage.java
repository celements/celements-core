package com.celements.model.classes;

import java.util.List;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

public abstract class AbstractClassDefinitionPackage implements ClassDefinitionPackage {

  @Requirement
  protected ConfigurationSource configSrc;

  @Override
  public boolean isActivated() {
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof List) {
      return ((List<?>) prop).contains(getName());
    }
    return false;
  }

}
