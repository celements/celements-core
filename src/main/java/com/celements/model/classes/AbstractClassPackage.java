package com.celements.model.classes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

public abstract class AbstractClassPackage implements ClassPackage {

  protected Logger LOGGER = LoggerFactory.getLogger(ClassPackage.class);

  @Requirement
  protected ConfigurationSource configSrc;

  @Override
  public boolean isActivated() {
    boolean ret = false;
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof List) {
      ret = ((List<?>) prop).contains(getName());
    }
    LOGGER.debug("{}: isActivated '{}' for property value '{}'", getName(), ret, prop);
    return ret;
  }

}
