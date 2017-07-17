package com.celements.model.classes;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.context.ModelContext;

public abstract class AbstractClassPackage implements ClassPackage {

  protected Logger LOGGER = LoggerFactory.getLogger(ClassPackage.class);

  @Requirement
  protected ConfigurationSource configSrc;

  @Requirement
  protected ModelContext context;

  @Override
  public boolean isActivated() {
    boolean activated = false;
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof Collection) {
      activated = ((Collection<?>) prop).contains(getName());
    } else if (prop instanceof String) {
      activated = prop.toString().trim().equals(getName());
    }
    LOGGER.debug("{}: isActivated '{}' for property value '{}'", getName(), activated, prop);
    return activated;
  }

}
