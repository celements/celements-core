package com.celements.navigation.factories;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectNavigationFactory.XOBJECT_NAV_FACTORY_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
@Singleton
public final class XObjectNavigationFactory extends AbstractNavigationFactory<DocumentReference> {

  public static final String XOBJECT_NAV_FACTORY_HINT = "xobject";

  private final static Logger LOGGER = LoggerFactory.getLogger(XObjectNavigationFactory.class);

  @Requirement
  private Execution execution;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private INavigationClassConfig navClassConfig;

  @Requirement
  private IModelAccessFacade modelAccess;

  XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  @NotNull
  protected DocumentReference getDefaultConfigReference() {
    return getContext().getDoc().getDocumentReference();
  }

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull DocumentReference configReference) {
    return loadConfigFromObject(getConfigBaseObj(configReference));
  }

  @Override
  public boolean hasNavigationConfig(@NotNull DocumentReference configReference) {
    return (getConfigBaseObj(configReference) != null);
  }

  private BaseObject getConfigBaseObj(@NotNull DocumentReference configReference) {
    BaseObject prefObj = null;
    try {
      prefObj = modelAccess.getXObject(configReference, navClassConfig.getNavigationConfigClassRef(
          configReference.getWikiReference()));
    } catch (DocumentLoadException | DocumentNotExistsException exp) {
      LOGGER.info("failed to load navigation from '{}'", configReference, exp);
    }
    return prefObj;
  }

  @NotNull
  NavigationConfig loadConfigFromObject(@Nullable BaseObject prefObj) {
    if (prefObj != null) {
      String configName = prefObj.getStringValue("menu_element_name");
      LOGGER.debug("loadConfigFromObject: configName [" + configName + "] from doc ["
          + prefObj.getDocumentReference() + "].");
      int fromHierarchyLevel = prefObj.getIntValue("from_hierarchy_level",
          NavigationConfig.DEFAULT_MIN_LEVEL);
      int toHierarchyLevel = prefObj.getIntValue("to_hierarchy_level",
          NavigationConfig.DEFAULT_MAX_LEVEL);
      int showInactiveToLevel = prefObj.getIntValue("show_inactive_to_level", 0);
      String menuPart = prefObj.getStringValue("menu_part");
      String spaceName = prefObj.getStringValue("menu_space");
      SpaceReference nodeSpaceRef = Strings.isNullOrEmpty(spaceName) ? null
          : webUtilsService.resolveSpaceReference(spaceName);
      String dataType = prefObj.getStringValue("data_type");
      String layoutType = prefObj.getStringValue("layout_type");
      int itemsPerPage = prefObj.getIntValue(INavigationClassConfig.ITEMS_PER_PAGE);
      String presentationTypeHint = prefObj.getStringValue(
          INavigationClassConfig.PRESENTATION_TYPE_FIELD);
      String cmCssClass = prefObj.getStringValue("cm_css_class");
      return new NavigationConfig(configName, fromHierarchyLevel, toHierarchyLevel,
          showInactiveToLevel, menuPart, dataType, nodeSpaceRef, layoutType, itemsPerPage,
          presentationTypeHint, cmCssClass);
    } else {
      return NavigationConfig.DEFAULTS;
    }
  }

}
