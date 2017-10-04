package com.celements.model.classes;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.configuration.CelementsPropertiesConfigurationSource;
import com.celements.model.context.ModelContext;

public abstract class AbstractClassPackage implements ClassPackage {

  protected Logger LOGGER = LoggerFactory.getLogger(ClassPackage.class);

  @Requirement("xwikiproperties")
  private ConfigurationSource xwikiPropertiesSource;

  @Requirement(CelementsPropertiesConfigurationSource.NAME)
  private ConfigurationSource celementsPropertiesSource;

  @Requirement("wiki")
  private ConfigurationSource wikiPreferencesSource;

  @Requirement
  protected ConfigurationSource configSrc;

  @Requirement
  protected ModelContext context;

  @Override
  public boolean isActivated() {
    return isActivated(xwikiPropertiesSource) || isActivated(celementsPropertiesSource)
        || isActivated(wikiPreferencesSource) || isActivated(configSrc);
  }

  private boolean isActivated(ConfigurationSource configSrc) {
    boolean activated = false;
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof Collection) {
      activated = ((Collection<?>) prop).contains(getName());
    } else if (prop instanceof String) {
      activated = prop.toString().trim().equals(getName());
    }
    LOGGER.debug("{}: isActivated '{}' for config source '{}'", getName(), activated,
        configSrc.getClass().getSimpleName());
    return activated;
  }

}
