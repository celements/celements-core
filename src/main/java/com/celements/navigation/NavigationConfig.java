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

  public static final NavigationConfig DEFAULTS = new NavigationConfig();

  public static final int UNLIMITED_ITEMS_PER_PAGE = -1;
  public static final int DEFAULT_MIN_LEVEL = 1;
  public static final int DEFAULT_MAX_LEVEL = 100;
  public static final String PAGE_MENU_DATA_TYPE = "pageMenu";
  public static final String LIST_LAYOUT_TYPE = "list";

  private final boolean enabled;
  private final ConfigValues origValues;
  private final ConfigValues ownValues;

  private class ConfigValues {

    private final String configName;
    private final Integer fromHierarchyLevel;
    private final Integer toHierarchyLevel;
    private final Integer showInactiveToLevel;
    private final String menuPart;
    private final SpaceReference nodeSpaceRef;
    private final String dataType;
    private final String layoutType;
    private final Integer nrOfItemsPerPage;
    private final String presentationTypeHint;
    private final String cmCssClass;

    private ConfigValues(@Nullable String configName, @Nullable Integer fromHierarchyLevel,
        @Nullable Integer toHierarchyLevel, @Nullable Integer showInactiveToLevel,
        @Nullable String menuPart, @Nullable String dataType, @Nullable SpaceReference nodeSpaceRef,
        @Nullable String layoutType, @Nullable Integer nrOfItemsPerPage,
        @Nullable String presentationTypeHint, @Nullable String cmCssClass) {
      this.configName = configName;
      this.fromHierarchyLevel = fromHierarchyLevel;
      this.toHierarchyLevel = toHierarchyLevel;
      this.showInactiveToLevel = showInactiveToLevel;
      this.menuPart = menuPart;
      this.nodeSpaceRef = nodeSpaceRef;
      this.dataType = dataType;
      this.layoutType = layoutType;
      this.nrOfItemsPerPage = nrOfItemsPerPage;
      this.presentationTypeHint = presentationTypeHint;
      this.cmCssClass = cmCssClass;
    }
  }

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

  private NavigationConfig(boolean enabled, String configName, Integer fromHierarchyLevel,
      Integer toHierarchyLevel, Integer showInactiveToLevel, String menuPart, String dataType,
      SpaceReference nodeSpaceRef, String layoutType, Integer nrOfItemsPerPage,
      String presentationTypeHint, String cmCssClass) {
    this.enabled = enabled;
    SpaceReference theNodeSpaceRef = (nodeSpaceRef != null) ? new SpaceReference(
        nodeSpaceRef.clone()) : null;
    this.origValues = new ConfigValues(Strings.emptyToNull(configName), fromHierarchyLevel,
        toHierarchyLevel, showInactiveToLevel, Strings.emptyToNull(menuPart), Strings.emptyToNull(
            dataType), theNodeSpaceRef, Strings.emptyToNull(layoutType), nrOfItemsPerPage,
        Strings.emptyToNull(presentationTypeHint), Strings.emptyToNull(cmCssClass));
    String theConfigName = Strings.nullToEmpty(origValues.configName);
    int theFromHierarchyLevel = Math.max(MoreObjects.firstNonNull(origValues.fromHierarchyLevel,
        DEFAULT_MIN_LEVEL), DEFAULT_MIN_LEVEL);
    Integer theToHierarchyLevel = MoreObjects.firstNonNull(origValues.toHierarchyLevel,
        DEFAULT_MAX_LEVEL);
    Integer theShowInactiveToLevel = MoreObjects.firstNonNull(origValues.showInactiveToLevel,
        DEFAULT_MIN_LEVEL - 1);
    String theMenuPart = Strings.nullToEmpty(origValues.menuPart);
    String theDataType = MoreObjects.firstNonNull(origValues.dataType, PAGE_MENU_DATA_TYPE);
    String theLayoutType = MoreObjects.firstNonNull(origValues.layoutType, LIST_LAYOUT_TYPE);
    int theNrOfItemsPerPage = Math.max(MoreObjects.firstNonNull(origValues.nrOfItemsPerPage,
        UNLIMITED_ITEMS_PER_PAGE), UNLIMITED_ITEMS_PER_PAGE);
    String thePresentationTypeHint = MoreObjects.firstNonNull(origValues.presentationTypeHint,
        "default");
    String theCmCssClass = Strings.nullToEmpty(origValues.cmCssClass);
    this.ownValues = new ConfigValues(theConfigName, theFromHierarchyLevel, theToHierarchyLevel,
        theShowInactiveToLevel, theMenuPart, theDataType, origValues.nodeSpaceRef, theLayoutType,
        theNrOfItemsPerPage, thePresentationTypeHint, theCmCssClass);
  }

  @Nullable
  final private <T> T firstNotNullOrNull(@Nullable T firstObj, @Nullable T secondObj) {
    if (firstObj != null) {
      return firstObj;
    } else {
      return secondObj;
    }
  }

  @NotNull
  final public NavigationConfig overlay(@NotNull NavigationConfig newConf) {
    boolean newEnabled = enabled || newConf.enabled;
    if (newEnabled) {
      String newConfigName = firstNotNullOrNull(newConf.origValues.configName,
          origValues.configName);
      Integer newFromHierarchyLevel = firstNotNullOrNull(newConf.origValues.fromHierarchyLevel,
          origValues.fromHierarchyLevel);
      Integer newToHierarchyLevel = firstNotNullOrNull(newConf.origValues.toHierarchyLevel,
          origValues.toHierarchyLevel);
      Integer newShowInactiveToLevel = firstNotNullOrNull(newConf.origValues.showInactiveToLevel,
          origValues.showInactiveToLevel);
      String newMenuPart = firstNotNullOrNull(newConf.origValues.menuPart, origValues.menuPart);
      String newDataType = firstNotNullOrNull(newConf.origValues.dataType, origValues.dataType);
      SpaceReference newNodeSpaceRef = firstNotNullOrNull(newConf.origValues.nodeSpaceRef,
          origValues.nodeSpaceRef);
      String newLayoutType = firstNotNullOrNull(newConf.origValues.layoutType,
          origValues.layoutType);
      Integer newNrOfItemsPerPage = firstNotNullOrNull(newConf.origValues.nrOfItemsPerPage,
          origValues.nrOfItemsPerPage);
      String newPresentationTypeHint = firstNotNullOrNull(newConf.origValues.presentationTypeHint,
          origValues.presentationTypeHint);
      String newCmCssClass = firstNotNullOrNull(newConf.origValues.cmCssClass,
          origValues.cmCssClass);
      return new NavigationConfig(true, newConfigName, newFromHierarchyLevel, newToHierarchyLevel,
          newShowInactiveToLevel, newMenuPart, newDataType, newNodeSpaceRef, newLayoutType,
          newNrOfItemsPerPage, newPresentationTypeHint, newCmCssClass);
    } else {
      return DEFAULTS;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  @NotNull
  public String getConfigName() {
    return ownValues.configName;
  }

  public int getFromHierarchyLevel() {
    return ownValues.fromHierarchyLevel;
  }

  public int getToHierarchyLevel() {
    return ownValues.toHierarchyLevel;
  }

  public int getShowInactiveToLevel() {
    return ownValues.showInactiveToLevel;
  }

  @NotNull
  public String getMenuPart() {
    return ownValues.menuPart;
  }

  @Nullable
  public SpaceReference getNodeSpaceRef() {
    return (ownValues.nodeSpaceRef == null) ? null
        : new SpaceReference(ownValues.nodeSpaceRef.clone());
  }

  @NotNull
  public String getDataType() {
    return ownValues.dataType;
  }

  @NotNull
  public String getLayoutType() {
    return ownValues.layoutType;
  }

  public int getNrOfItemsPerPage() {
    if (ownValues.nrOfItemsPerPage > 0) {
      return ownValues.nrOfItemsPerPage;
    } else {
      return UNLIMITED_ITEMS_PER_PAGE;
    }
  }

  @Nullable
  public IPresentationTypeRole getPresentationType() {
    try {
      LOGGER.info("setPresentationType to [" + ownValues.presentationTypeHint + "].");
      return Utils.getComponent(IWebUtilsService.class).lookup(IPresentationTypeRole.class,
          ownValues.presentationTypeHint);
    } catch (ComponentLookupException failedToLoadException) {
      LOGGER.error("setPresentationType failed to load IPresentationTypeRole for hint ["
          + ownValues.presentationTypeHint + "].", failedToLoadException);
    }
    return null;
  }

  @NotNull
  public String getCssClass() {
    return ownValues.cmCssClass;
  }

}
