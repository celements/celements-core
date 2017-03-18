package com.celements.menu.access;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiRightService;

@Component("celements.defaultMenuAccess")
public class DefaultMenuAccessProvider implements IMenuAccessProviderRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(DefaultMenuAccessProvider.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public boolean hasview(DocumentReference menuBarDocRef) throws NoAccessDefinedException,
      XWikiException {
    String database = getContext().getDatabase();
    try {
      getContext().setDatabase(getContext().getOriginalDatabase());
      if (webUtilsService.getRefDefaultSerializer().serialize(menuBarDocRef).endsWith(
          "Celements2.AdminMenu")) {
        LOGGER.debug("hasview: AdminMenu [" + getContext().getUser() + "] isAdvancedAdmin ["
            + webUtilsService.isAdvancedAdmin() + "].");
        return webUtilsService.isAdvancedAdmin();
      } else if (webUtilsService.getRefDefaultSerializer().serialize(menuBarDocRef).endsWith(
          "Celements2.LayoutMenu")) {
        LOGGER.debug("hasview: LayoutMenu [" + getContext().getUser() + "] isLayoutEditor ["
            + webUtilsService.isLayoutEditor() + "] isAdvancedAdmin ["
            + webUtilsService.isAdvancedAdmin() + "].");
        return webUtilsService.isLayoutEditor();
      }
      return hasCentralAndLocalView(menuBarDocRef);
    } finally {
      getContext().setDatabase(database);
    }
  }

  @Override
  public boolean denyView(DocumentReference menuBarDocRef) {
    return false;
  }

  private boolean hasCentralAndLocalView(DocumentReference menuBarDocRef) throws XWikiException {
    String database = getContext().getDatabase();
    getContext().setDatabase("celements2web");
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        menuBarDocRef.getLastSpaceReference().getName(), menuBarDocRef.getName());
    String menuBar2webFullName = webUtilsService.getRefDefaultSerializer().serialize(
        menuBar2webDocRef);
    boolean centralView = !getContext().getWiki().exists(menuBar2webDocRef, getContext())
        || getContext().getWiki().getRightService().hasAccessLevel("view", getContext().getUser(),
            menuBar2webFullName, getContext());
    LOGGER.debug("hasview: centralView [" + menuBar2webFullName + "] for [" + getContext().getUser()
        + "] -> [" + centralView + "] on database [" + getContext().getDatabase() + "].");
    getContext().setDatabase(getContext().getOriginalDatabase());
    DocumentReference menuBarLocalDocRef = new DocumentReference(getContext().getOriginalDatabase(),
        menuBarDocRef.getLastSpaceReference().getName(), menuBarDocRef.getName());
    String menuBarFullName = webUtilsService.getRefDefaultSerializer().serialize(
        menuBarLocalDocRef);
    boolean localView = true;
    if (getContext().getWiki().exists(menuBarLocalDocRef, getContext())) {
      localView = getContext().getWiki().getRightService().hasAccessLevel("view",
          getContext().getUser(), menuBarFullName, getContext());
    } else if (XWikiRightService.GUEST_USER_FULLNAME.equals(getContext().getUser())) {
      String menuBarLocalFullName = webUtilsService.getRefLocalSerializer().serialize(
          menuBarLocalDocRef);
      String prefParamName = "CelMenuBar-" + menuBarLocalFullName;
      String cfgFallbackName = "celements.menubar.guestview." + menuBarLocalFullName;
      localView = (getContext().getWiki().getXWikiPreferenceAsInt(prefParamName, cfgFallbackName, 0,
          getContext()) == 1);
      LOGGER.info("hasview: localView default for quest on [" + menuBarFullName + "] -> ["
          + localView + "] on database [" + getContext().getDatabase()
          + "]. Checked prefParamName [" + prefParamName + "] and cfgFallbackName ["
          + cfgFallbackName + "].");
    }
    LOGGER.debug("hasview: localView [" + menuBarFullName + "] for [" + getContext().getUser()
        + "] -> [" + localView + "] on database [" + getContext().getDatabase() + "].");
    getContext().setDatabase(database);
    boolean centralAndLocalView = centralView && localView;
    return centralAndLocalView;
  }

}
