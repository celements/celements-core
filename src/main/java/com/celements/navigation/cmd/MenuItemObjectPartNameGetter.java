package com.celements.navigation.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.PartNameGetter;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

final class MenuItemObjectPartNameGetter implements PartNameGetter {

  private static Logger LOGGER = LoggerFactory.getLogger(MenuItemObjectPartNameGetter.class);

  private final IModelAccessFacade modelAccess;

  private final INavigationClassConfig navConfigClass;

  public MenuItemObjectPartNameGetter() {
    modelAccess = Utils.getComponent(IModelAccessFacade.class);
    navConfigClass = Utils.getComponent(INavigationClassConfig.class);
  }

  @Override
  public String getPartName(DocumentReference docRef) {
    try {
      BaseObject cobj = modelAccess.getXObject(docRef, navConfigClass.getMenuItemClassRef(
          docRef.getWikiReference()));
      if (cobj != null) {
        return cobj.getStringValue(INavigationClassConfig.PART_NAME_FIELD);
      }
    } catch (DocumentLoadException | DocumentNotExistsException exp) {
      LOGGER.error("getPartName failed for '{}'.", docRef, exp);
    }
    return "";
  }
}
