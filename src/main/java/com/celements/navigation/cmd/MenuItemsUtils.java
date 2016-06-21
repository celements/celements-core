package com.celements.navigation.cmd;

import javax.validation.constraints.NotNull;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.web.Utils;

public class MenuItemsUtils {

  private MenuItemsUtils() {
  }

  public static EntityReference resolveParentRef(@NotNull String parentFN) {
    return (parentFN.isEmpty()) ? null
        : Utils.getComponent(IWebUtilsService.class).resolveEntityReference(parentFN, getEntityType(
            parentFN));
  }

  public static EntityType getEntityType(String parentFN) {
    return (parentFN.contains(".")) ? EntityType.DOCUMENT : EntityType.SPACE;
  }

}
