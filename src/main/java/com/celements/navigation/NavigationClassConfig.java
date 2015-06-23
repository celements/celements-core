package com.celements.navigation;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

@Component
public class NavigationClassConfig implements INavigationClassConfig {

  @Override
  public DocumentReference getMenuNameClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_NAME_CLASS_SPACE, MENU_NAME_CLASS_DOC);
  }

  @Override
  public DocumentReference getNavigationConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, NAVIGATION_CONFIG_CLASS_SPACE,
        NAVIGATION_CONFIG_CLASS_DOC);
  }

  @Override
  public DocumentReference getNavigationConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(NAVIGATION_CONFIG_CLASS_DOC, new SpaceReference(
        NAVIGATION_CONFIG_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_ITEM_CLASS_SPACE, MENU_ITEM_CLASS_DOC);
  }

  @Override
  public DocumentReference getNewMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MAPPED_MENU_ITEM_CLASS_SPACE,
        MAPPED_MENU_ITEM_CLASS_DOC);
  }

}
