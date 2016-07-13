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

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.NavigationConfig.Builder;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

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
      prefObj = modelAccess.getXObject(configReference,
          navClassConfig.getNavigationConfigClassRef(configReference.getWikiReference()));
    } catch (DocumentLoadException | DocumentNotExistsException exp) {
      LOGGER.info("failed to load navigation from '{}'", configReference, exp);
    }
    return prefObj;
  }

  @NotNull
  NavigationConfig loadConfigFromObject(@Nullable BaseObject prefObj) {
    if (prefObj != null) {
      Builder b = new NavigationConfig.Builder();
      String configName = prefObj.getStringValue(INavigationClassConfig.MENU_ELEMENT_NAME_FIELD);
      LOGGER.debug("loadConfigFromObject: configName [" + configName + "] from doc ["
          + prefObj.getDocumentReference() + "].");
      b.configName(configName);
      if (isValueSet(prefObj, INavigationClassConfig.FROM_HIERARCHY_LEVEL_FIELD)) {
        b.fromHierarchyLevel(prefObj.getIntValue(INavigationClassConfig.FROM_HIERARCHY_LEVEL_FIELD,
            NavigationConfig.DEFAULT_MIN_LEVEL));
      }
      if (isValueSet(prefObj, INavigationClassConfig.TO_HIERARCHY_LEVEL_FIELD)) {
        b.toHierarchyLevel(prefObj.getIntValue(INavigationClassConfig.TO_HIERARCHY_LEVEL_FIELD,
            NavigationConfig.DEFAULT_MAX_LEVEL));
      }
      if (isValueSet(prefObj, INavigationClassConfig.SHOW_INACTIVE_TO_LEVEL_FIELD)) {
        b.showInactiveToLevel(prefObj.getIntValue(
            INavigationClassConfig.SHOW_INACTIVE_TO_LEVEL_FIELD,
            NavigationConfig.DEFAULT_MIN_LEVEL - 1));
      }
      b.menuPart(prefObj.getStringValue(INavigationClassConfig.MENU_PART_FIELD));
      String spaceName = prefObj.getStringValue(INavigationClassConfig.MENU_SPACE_FIELD);
      if (!Strings.isNullOrEmpty(spaceName)) {
        b.nodeSpaceRef(webUtilsService.resolveSpaceReference(spaceName));
      }
      b.dataType(prefObj.getStringValue("data_type"));
      b.layoutType(prefObj.getStringValue("layout_type"));
      if (isValueSet(prefObj, INavigationClassConfig.ITEMS_PER_PAGE)) {
        b.showInactiveToLevel(prefObj.getIntValue(INavigationClassConfig.ITEMS_PER_PAGE,
            NavigationConfig.UNLIMITED_ITEMS_PER_PAGE));
      }
      b.presentationTypeHint(prefObj.getStringValue(INavigationClassConfig.PRESENTATION_TYPE_FIELD));
      b.cmCssClass(prefObj.getStringValue(INavigationClassConfig.CM_CSS_CLASS_FIELD));
      return b.build();
    } else {
      return NavigationConfig.DEFAULTS;
    }
  }

  private boolean isValueSet(BaseObject prefObj, String fieldName) {
    BaseProperty toHierarchyField = (BaseProperty) prefObj.getField(fieldName);
    return (toHierarchyField != null) && (toHierarchyField.getValue() != null);
  }

}
