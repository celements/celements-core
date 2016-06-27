package com.celements.navigation;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.python.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.MoreObjects;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class NavigationConfig {

  private final static Logger LOGGER = LoggerFactory.getLogger(NavigationConfig.class);

  public static final int UNLIMITED_ITEMS_PER_PAGE = -1;
  public static final int DEFAULT_MIN_LEVEL = 1;
  public static final int DEFAULT_MAX_LEVEL = 100;
  public static final String PAGE_MENU_DATA_TYPE = "pageMenu";
  public static final String LIST_LAYOUT_TYPE = "list";

  private final String configName;
  private final boolean enabled;
  private final int fromHierarchyLevel;
  private final int toHierarchyLevel;
  private final int showInactiveToLevel;
  private final String menuPart;
  private final SpaceReference nodeSpaceRef;
  private final String dataType;
  private final String layoutType;
  private final int nrOfItemsPerPage;
  private final String presentationTypeHint;
  private final String cmCssClass;

  public NavigationConfig() {
    this(false, null, null, null, null, null, null, null, null, null, null, null);
  }

  public NavigationConfig(@Nullable String configName, @Nullable Integer fromHierarchyLevel,
      @Nullable Integer toHierarchyLevel, @Nullable Integer showInactiveToLevel,
      @Nullable String menuPart, @Nullable String dataType, @Nullable SpaceReference nodeSpaceRef,
      @Nullable String layoutType, @Nullable Integer nrOfItemsPerPage,
      @Nullable String presentationTypeHint, @Nullable String cmCssClass) {
    this(true, configName, fromHierarchyLevel, toHierarchyLevel, showInactiveToLevel, menuPart,
        dataType, nodeSpaceRef, layoutType, nrOfItemsPerPage, presentationTypeHint, cmCssClass);
  }

  // TODO extend with "@NotNull NavigationConfig defaultConfig" parameter to
  // TODO allow to overlay multiple NavigationConfigs
  private NavigationConfig(boolean enabled, String configName, Integer fromHierarchyLevel,
      Integer toHierarchyLevel, Integer showInactiveToLevel, String menuPart, String dataType,
      SpaceReference nodeSpaceRef, String layoutType, Integer nrOfItemsPerPage,
      String presentationTypeHint, String cmCssClass) {
    this.enabled = enabled;
    this.configName = Strings.nullToEmpty(configName);
    this.fromHierarchyLevel = Math.max(MoreObjects.firstNonNull(fromHierarchyLevel,
        DEFAULT_MIN_LEVEL), DEFAULT_MIN_LEVEL);
    this.toHierarchyLevel = MoreObjects.firstNonNull(toHierarchyLevel, DEFAULT_MAX_LEVEL);
    this.showInactiveToLevel = MoreObjects.firstNonNull(showInactiveToLevel, DEFAULT_MIN_LEVEL - 1);
    this.menuPart = Strings.nullToEmpty(menuPart);
    this.nodeSpaceRef = (nodeSpaceRef != null) ? new SpaceReference(nodeSpaceRef.clone()) : null;
    this.dataType = MoreObjects.firstNonNull(Strings.emptyToNull(dataType), PAGE_MENU_DATA_TYPE);
    this.layoutType = MoreObjects.firstNonNull(layoutType, LIST_LAYOUT_TYPE);
    this.nrOfItemsPerPage = Math.max(MoreObjects.firstNonNull(nrOfItemsPerPage,
        UNLIMITED_ITEMS_PER_PAGE), UNLIMITED_ITEMS_PER_PAGE);
    this.presentationTypeHint = MoreObjects.firstNonNull(Strings.emptyToNull(presentationTypeHint),
        "default");
    this.cmCssClass = Strings.nullToEmpty(cmCssClass);
  }

  @NotNull
  public String getConfigName() {
    return configName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getFromHierarchyLevel() {
    return fromHierarchyLevel;
  }

  public int getToHierarchyLevel() {
    return toHierarchyLevel;
  }

  public int getShowInactiveToLevel() {
    return showInactiveToLevel;
  }

  @NotNull
  public String getMenuPart() {
    return menuPart;
  }

  @Nullable
  public SpaceReference getNodeSpaceRef() {
    return (nodeSpaceRef == null) ? null : new SpaceReference(nodeSpaceRef.clone());
  }

  @NotNull
  public String getDataType() {
    return dataType;
  }

  @NotNull
  public String getLayoutType() {
    return layoutType;
  }

  public int getNrOfItemsPerPage() {
    if (nrOfItemsPerPage > 0) {
      return nrOfItemsPerPage;
    } else {
      return UNLIMITED_ITEMS_PER_PAGE;
    }
  }

  @Nullable
  public IPresentationTypeRole getPresentationType() {
    try {
      LOGGER.info("setPresentationType to [" + presentationTypeHint + "].");
      return Utils.getComponent(IWebUtilsService.class).lookup(IPresentationTypeRole.class,
          presentationTypeHint);
    } catch (ComponentLookupException failedToLoadException) {
      LOGGER.error("setPresentationType failed to load IPresentationTypeRole for hint ["
          + presentationTypeHint + "].", failedToLoadException);
    }
    return null;
  }

  @NotNull
  public String getCssClass() {
    return cmCssClass;
  }

}
