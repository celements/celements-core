package com.celements.navigation;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class NavigationConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationConfig.class);

  public static final NavigationConfig DEFAULTS = new NavigationConfig();

  public static final int UNLIMITED_ITEMS_PER_PAGE = -1;
  public static final int DEFAULT_MIN_LEVEL = 1;
  public static final int DEFAULT_MAX_LEVEL = 100;
  public static final String PAGE_MENU_DATA_TYPE = "pageMenu";
  public static final String LIST_LAYOUT_TYPE = "list";

  public static class Builder {

    // Required parameters
    private boolean enabled;

    // Optional parameters - initialized to Optional.absent()
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
    private LinkedHashSet<String> mainUlCSSClasses = new LinkedHashSet<>();

    public Builder() {
      enabled = true;
    }

    public Builder disable() {
      enabled = false;
      return this;
    }

    public Builder configName(@NotNull String val) {
      configName = Optional.of(Strings.emptyToNull(val));
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

    public Builder menuPart(@NotNull String val) {
      menuPart = Optional.of(Strings.emptyToNull(val));
      return this;
    }

    public Builder nodeSpaceRef(@NotNull SpaceReference val) {
      Preconditions.checkNotNull(val);
      nodeSpaceRef = Optional.of(getModelUtils().cloneRef(val, SpaceReference.class));
      return this;
    }

    public Builder dataType(@NotNull String val) {
      dataType = Optional.of(Strings.emptyToNull(val));
      return this;
    }

    public Builder layoutType(@NotNull String val) {
      layoutType = Optional.of(Strings.emptyToNull(val));
      return this;
    }

    public Builder nrOfItemsPerPage(int val) {
      if ((val == UNLIMITED_ITEMS_PER_PAGE) || (val > 0)) {
        nrOfItemsPerPage = Optional.of(val);
      }
      return this;
    }

    public Builder presentationTypeHint(@NotNull String val) {
      presentationTypeHint = Optional.of(Strings.emptyToNull(val));
      return this;
    }

    public Builder cmCssClass(@NotNull String val) {
      cmCssClass = Optional.of(Strings.emptyToNull(val));
      return this;
    }

    public Builder addMainUlCSSClass(@NotNull String val) {
      mainUlCSSClasses.add(val);
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
  private final LinkedHashSet<String> mainUlCSSClasses;

  private NavigationConfig() {
    this(new Builder().disable());
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
    this.mainUlCSSClasses = builder.mainUlCSSClasses;
  }

  @NotNull
  final public NavigationConfig overlay(@NotNull NavigationConfig newConf) {
    boolean newEnabled = enabled || newConf.enabled;
    if (newEnabled) {
      Builder b = new Builder();
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
      b.mainUlCSSClasses.addAll(mainUlCSSClasses);
      b.mainUlCSSClasses.addAll(newConf.mainUlCSSClasses);
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

  @NotNull
  public Optional<SpaceReference> getNodeSpaceRef() {
    if (nodeSpaceRef.isPresent()) {
      return Optional.of(getModelUtils().cloneRef(nodeSpaceRef.get(), SpaceReference.class));
    }
    return Optional.absent();
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

  @NotNull
  public Optional<IPresentationTypeRole> getPresentationType() {
    String thePresentationTypeHint = presentationTypeHint.or("default");
    try {
      LOGGER.info("setPresentationType to [{}].", thePresentationTypeHint);
      return Optional.of(Utils.getComponent(ComponentManager.class).lookup(
          IPresentationTypeRole.class, thePresentationTypeHint));
    } catch (ComponentLookupException failedToLoadException) {
      LOGGER.error("setPresentationType failed to load IPresentationTypeRole for hint [{}].",
          thePresentationTypeHint, failedToLoadException);
    }
    return Optional.absent();
  }

  @NotNull
  public String getCssClass() {
    return cmCssClass.or("");
  }

  @NotNull
  public Set<String> getMainUlCSSClasses() {
    return new LinkedHashSet<>(mainUlCSSClasses);
  }

  private static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
