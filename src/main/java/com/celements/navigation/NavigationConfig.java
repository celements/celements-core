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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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

  public static class Builder {

    // Required parameters
    private final boolean enabled;

    // Optional parameters - initialized to Optional
    private Optional<String> configName = Optional.absent();
    private Optional<Integer> fromHierarchyLevel = Optional.absent();
    private Optional<Integer> toHierarchyLevel = Optional.absent();
    private Optional<Integer> showInactiveToLevel = Optional.absent();
    private Optional<String> menuPart = Optional.absent();
    private Optional<SpaceReference> nodeSpaceRef = Optional.absent();
    private Optional<String> dataType = Optional.absent();
    private Optional<String> layoutType = Optional.absent();
    private Optional<Integer> nrOfItemsPerPage = Optional.absent();
    private Optional<String> presentationTypeHint = Optional.absent();
    private Optional<String> cmCssClass = Optional.absent();

    public Builder(boolean enabled) {
      this.enabled = enabled;
    }

    public Builder configName(@Nullable String val) {
      configName = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public Builder fromHierarchyLevel(int val) {
      fromHierarchyLevel = Optional.of(val);
      return this;
    }

    public Builder toHierarchyLevel(int val) {
      toHierarchyLevel = Optional.of(val);
      return this;
    }

    public Builder showInactiveToLevel(int val) {
      showInactiveToLevel = Optional.of(val);
      return this;
    }

    public Builder menuPart(@Nullable String val) {
      menuPart = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public Builder nodeSpaceRef(@NotNull SpaceReference val) {
      Preconditions.checkNotNull(val);
      nodeSpaceRef = Optional.of(new SpaceReference(val.clone()));
      return this;
    }

    public Builder dataType(@Nullable String val) {
      dataType = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public Builder layoutType(@Nullable String val) {
      layoutType = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public Builder nrOfItemsPerPage(int val) {
      if ((val == UNLIMITED_ITEMS_PER_PAGE) || (val > 0)) {
        nrOfItemsPerPage = Optional.of(val);
      }
      return this;
    }

    public Builder presentationTypeHint(@Nullable String val) {
      presentationTypeHint = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public Builder cmCssClass(@Nullable String val) {
      cmCssClass = Optional.fromNullable(Strings.emptyToNull(val));
      return this;
    }

    public NavigationConfig build() {
      return new NavigationConfig(this);
    }

  }

  private final boolean enabled;
  private final Optional<String> configName;
  private final Optional<Integer> fromHierarchyLevel;
  private final Optional<Integer> toHierarchyLevel;
  private final Optional<Integer> showInactiveToLevel;
  private final Optional<String> menuPart;
  private final Optional<SpaceReference> nodeSpaceRef;
  private final Optional<String> dataType;
  private final Optional<String> layoutType;
  private final Optional<Integer> nrOfItemsPerPage;
  private final Optional<String> presentationTypeHint;
  private final Optional<String> cmCssClass;

  private NavigationConfig() {
    this(new Builder(false));
  }

  private NavigationConfig(@NotNull Builder builder) {
    this.enabled = builder.enabled;
    this.configName = builder.configName;
    this.fromHierarchyLevel = builder.fromHierarchyLevel;
    this.toHierarchyLevel = builder.toHierarchyLevel;
    this.showInactiveToLevel = builder.showInactiveToLevel;
    this.menuPart = builder.menuPart;
    this.nodeSpaceRef = builder.nodeSpaceRef;
    this.dataType = builder.dataType;
    this.layoutType = builder.layoutType;
    this.nrOfItemsPerPage = builder.nrOfItemsPerPage;
    this.presentationTypeHint = builder.presentationTypeHint;
    this.cmCssClass = builder.cmCssClass;
  }

  @Nullable
  final private <T> T firstNotNullOrNull(@Nullable T firstObj, @Nullable T secondObj) {
    return Optional.fromNullable(firstObj).or(Optional.fromNullable(secondObj)).orNull();
  }

  @NotNull
  final public NavigationConfig overlay(@NotNull NavigationConfig newConf) {
    boolean newEnabled = enabled || newConf.enabled;
    if (newEnabled) {
      Builder b = new Builder(true);
      b.configName = newConf.configName.or(configName);
      b.fromHierarchyLevel = newConf.fromHierarchyLevel.or(fromHierarchyLevel);
      b.toHierarchyLevel = newConf.toHierarchyLevel.or(toHierarchyLevel);
      b.showInactiveToLevel = newConf.showInactiveToLevel.or(showInactiveToLevel);
      b.menuPart = newConf.menuPart.or(menuPart);
      b.dataType = newConf.dataType.or(dataType);
      b.nodeSpaceRef = newConf.nodeSpaceRef.or(nodeSpaceRef);
      b.layoutType = newConf.layoutType.or(layoutType);
      b.nrOfItemsPerPage = newConf.nrOfItemsPerPage.or(nrOfItemsPerPage);
      b.presentationTypeHint = newConf.presentationTypeHint.or(presentationTypeHint);
      b.cmCssClass = newConf.cmCssClass.or(cmCssClass);
      return b.build();
    } else {
      return DEFAULTS;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  @NotNull
  public String getConfigName() {
    return configName.or("");
  }

  public int getFromHierarchyLevel() {
    return Math.max(fromHierarchyLevel.or(DEFAULT_MIN_LEVEL), DEFAULT_MIN_LEVEL);
  }

  public int getToHierarchyLevel() {
    return toHierarchyLevel.or(DEFAULT_MAX_LEVEL);
  }

  public int getShowInactiveToLevel() {
    return showInactiveToLevel.or(DEFAULT_MIN_LEVEL - 1);
  }

  @NotNull
  public String getMenuPart() {
    return menuPart.or("");
  }

  @Nullable
  public SpaceReference getNodeSpaceRef() {
    if (nodeSpaceRef.isPresent()) {
      return new SpaceReference(nodeSpaceRef.get().clone());
    }
    return null;
  }

  @NotNull
  public String getDataType() {
    return dataType.or(PAGE_MENU_DATA_TYPE);
  }

  @NotNull
  public String getLayoutType() {
    return layoutType.or(LIST_LAYOUT_TYPE);
  }

  public int getNrOfItemsPerPage() {
    return Math.max(nrOfItemsPerPage.or(UNLIMITED_ITEMS_PER_PAGE), UNLIMITED_ITEMS_PER_PAGE);
  }

  @Nullable
  public IPresentationTypeRole getPresentationType() {
    String thePresentationTypeHint = presentationTypeHint.or("default");
    try {
      LOGGER.info("setPresentationType to [" + thePresentationTypeHint + "].");
      return Utils.getComponent(IWebUtilsService.class).lookup(IPresentationTypeRole.class,
          thePresentationTypeHint);
    } catch (ComponentLookupException failedToLoadException) {
      LOGGER.error("setPresentationType failed to load IPresentationTypeRole for hint ["
          + thePresentationTypeHint + "].", failedToLoadException);
    }
    return null;
  }

  @NotNull
  public String getCssClass() {
    return cmCssClass.or("");
  }

}
