package com.celements.navigation;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;

@Component
public class NavigationClassConfig implements INavigationClassConfig {

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public DocumentReference getMenuNameClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_NAME_CLASS_SPACE, MENU_NAME_CLASS_DOC);
  }

  @Override
  public DocumentReference getNavigationConfigClassRef() {
    return getNavigationConfigClassRef((WikiReference) null);
  }

  @Override
  public DocumentReference getNavigationConfigClassRef(String wikiName) {
    return getNavigationConfigClassRef(new WikiReference(wikiName));
  }

  @Override
  public DocumentReference getNavigationConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(NAVIGATION_CONFIG_CLASS_DOC, 
        webUtils.resolveSpaceReference(NAVIGATION_CONFIG_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getMenuItemClassRef() {
    return getMenuItemClassRef((WikiReference) null);
  }

  @Override
  public DocumentReference getMenuItemClassRef(String wikiName) {
    return getMenuItemClassRef(new WikiReference(wikiName));
  }

  @Override
  public DocumentReference getMenuItemClassRef(WikiReference wikiRef) {
    return new DocumentReference(MENU_ITEM_CLASS_DOC, 
        webUtils.resolveSpaceReference(MENU_ITEM_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getNewMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MAPPED_MENU_ITEM_CLASS_SPACE,
        MAPPED_MENU_ITEM_CLASS_DOC);
  }

}
