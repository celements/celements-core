package com.celements.model.classes;

import java.util.Collection;

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
    if (prop instanceof Collection) {
      ret = ((Collection<?>) prop).contains(getName());
    } else if (prop instanceof String) {
      ret = prop.toString().trim().equals(getName());
    }
    LOGGER.debug("{}: isActivated '{}' for property value '{}'", getName(), ret, prop);
    return ret;
  }

}
