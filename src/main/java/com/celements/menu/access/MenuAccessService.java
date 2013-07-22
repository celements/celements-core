package com.celements.menu.access;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;

@Component
public class MenuAccessService implements IMenuAccessServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MenuAccessService.class);

  @Requirement
  Map<String, IMenuAccessProviderRole> accessProviderMap;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public boolean hasview(DocumentReference menuBarDocRef) {
    if (accessProviderMap != null) {
      boolean hasview = false;
      boolean allowForDeny = true;
      for (IMenuAccessProviderRole accessProvider : accessProviderMap.values()) {
        try {
          boolean newHasView = accessProvider.hasview(menuBarDocRef);
          LOGGER.debug("check has view for [" + getContext().getUser() + "] on ["
              + menuBarDocRef + "] and provider [" + accessProvider.getClass() + "]"
              + " results in [" + newHasView + "].");
          if (!newHasView && accessProvider.denyView(menuBarDocRef)) {
            LOGGER.debug("deny view for [" + getContext().getUser() + "] on ["
                + menuBarDocRef + "] and provider [" + accessProvider.getClass() + "]"
                + " thus returning false and cancel hasview check.");
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
      LOGGER.debug("deny view for [" + getContext().getUser() + "] on ["
          + menuBarDocRef + "] ends with result [" + hasview + "].");
      return hasview;
    }
    return false;
  }

}
