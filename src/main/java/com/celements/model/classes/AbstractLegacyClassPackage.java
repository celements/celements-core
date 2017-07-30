package com.celements.model.classes;

import static com.celements.common.classes.IClassCollectionRole.*;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Requirement;

import com.celements.common.classes.IClassCollectionRole;

@SuppressWarnings("deprecation")
public abstract class AbstractLegacyClassPackage extends AbstractClassPackage {

  @Requirement
  private List<IClassCollectionRole> classCollections;

  public abstract @NotNull String getLegacyName();

  @Override
  public boolean isActivated() {
    return super.isActivated() || isActivatedLegacyFallback();
  }

  /**
   * fallback for legacy XWikiPreferences config (see CELDEV-516)
   */
  private boolean isActivatedLegacyFallback() {
    verifyLegacyName();
    boolean ret = containsLegacyName(context.getXWikiContext().getWiki().Param(ACTIVATED_PARAM))
        || containsLegacyName(context.getXWikiContext().getWiki().getXWikiPreference(
            ACTIVATED_XWIKIPREF, context.getXWikiContext()));
    if (ret) {
      LOGGER.warn("{}: legacy fallback activated", getName());
    }
    return ret;
  }

  private boolean containsLegacyName(String preferences) {
    return ("," + preferences + ",").contains("," + getLegacyName() + ",");
  }

  private void verifyLegacyName() {
    for (IClassCollectionRole classColl : classCollections) {
      if (classColl.getConfigName().equals(getLegacyName())) {
        return;
      }
    }
    throw new IllegalStateException("Unable to activate legacy fallback for "
        + "inaccessible ClassCollection '" + getLegacyName() + "'");
  }

}
