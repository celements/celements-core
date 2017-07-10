package com.celements.model.classes;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.context.ModelContext;
import com.google.common.base.Optional;

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
    return activated || checkLegacyFallback();
  }

  /**
   * fallback for legacy XWikiPreferences config (see CELDEV-516)
   */
  private boolean checkLegacyFallback() {
    boolean ret = false;
    if (getLegacyName().isPresent()) {
      String preferences = "," + context.getXWikiContext().getWiki().getXWikiPreference(
          "activated_classcollections", context.getXWikiContext()) + ",";
      ret = preferences.contains("," + getLegacyName().get() + ",");
      LOGGER.debug("{}: checkLegacyFallback '{}' for legacy name '{}'", getName(), ret,
          getLegacyName().get());
    }
    return ret;
  }

  protected Optional<String> getLegacyName() {
    return Optional.absent();
  }

}
