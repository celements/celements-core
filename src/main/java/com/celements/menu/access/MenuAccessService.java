package com.celements.menu.access;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

@Component
public class MenuAccessService implements IMenuAccessServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MenuAccessService.class);

  @Requirement
  Map<String, IMenuAccessProviderRole> accessProviderMap;

  public boolean hasview(DocumentReference menuBarDocRef) {
    if (accessProviderMap != null) {
      boolean hasview = false;
      boolean allowForDeny = true;
      for (IMenuAccessProviderRole accessProvider : accessProviderMap.values()) {
        try {
          boolean newHasView = accessProvider.hasview(menuBarDocRef);
          if (!newHasView && accessProvider.denyView(menuBarDocRef)) {
            return false;
          } else if (allowForDeny) {
            hasview |= newHasView;
          }
        } catch (NoAccessDefinedException noAccExp) {
          LOGGER.debug("skip Access Provider [" + accessProvider.getClass() + "] because"
              + " of NoAccessDefinedException on [" + menuBarDocRef + "].", noAccExp);
        } catch (Exception exp) {
          LOGGER.error("Access Provider [" + accessProvider.getClass() + "] failed for"
              + " hasview on [" + menuBarDocRef + "].", exp);
        }
      }
      return hasview;
    }
    return false;
  }

}
