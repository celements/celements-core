package com.celements.menu.access;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class MenuAccessService implements IMenuAccessServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(MenuAccessService.class);

  @Requirement
  Map<String, IMenuAccessProviderRole> accessProviderMap;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public boolean hasview(DocumentReference menuBarDocRef) {
    if (accessProviderMap != null) {
      boolean hasview = false;
      boolean allowForDeny = true;
      for (IMenuAccessProviderRole accessProvider : accessProviderMap.values()) {
        LOGGER.trace("start accessProvider [" + accessProvider.getClass()
            + "] check for menuBarDocRef [" + menuBarDocRef + "] current database ["
            + getContext().getDatabase() + "].");
        try {
          boolean newHasView = accessProvider.hasview(menuBarDocRef);
          LOGGER.debug("check has view for [" + getContext().getUser() + "] on [" + menuBarDocRef
              + "] and provider [" + accessProvider.getClass() + "]" + " results in [" + newHasView
              + "].");
          if (!newHasView && accessProvider.denyView(menuBarDocRef)) {
            LOGGER.debug("deny view for [" + getContext().getUser() + "] on [" + menuBarDocRef
                + "] and provider [" + accessProvider.getClass() + "]"
                + " thus returning false and cancel hasview check.");
            return false;
          } else if (allowForDeny) {
            hasview |= newHasView;
          }
        } catch (NoAccessDefinedException noAccExp) {
          LOGGER.debug("skip Access Provider [" + accessProvider.getClass() + "] because"
              + " of NoAccessDefinedException on [" + menuBarDocRef + "].", noAccExp);
        } catch (XWikiException exp) {
          LOGGER.error("Access Provider [" + accessProvider.getClass() + "] failed for"
              + " hasview on [" + menuBarDocRef + "].", exp);
        }
      }
      LOGGER.debug("deny view for [" + getContext().getUser() + "] on [" + menuBarDocRef
          + "] ends with result [" + hasview + "].");
      return hasview;
    }
    return false;
  }

}
