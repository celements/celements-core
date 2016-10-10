package com.celements.navigation;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.util.ModelUtils;

@Component
public class NavigationClassConfig implements INavigationClassConfig {

  @Requirement
  ModelUtils modelUtils;

  @Override
  public DocumentReference getMenuNameClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_NAME_CLASS_SPACE, MENU_NAME_CLASS_DOC);
  }

  @Override
  public DocumentReference getMenuNameClassRef() {
    return getMenuNameClassRef((WikiReference) null);
  }

  @Override
  public DocumentReference getMenuNameClassRef(WikiReference wikiRef) {
    return new DocumentReference(MENU_NAME_CLASS_DOC, modelUtils.resolveRef(MENU_NAME_CLASS_SPACE,
        SpaceReference.class, wikiRef));
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
    return new DocumentReference(NAVIGATION_CONFIG_CLASS_DOC, modelUtils.resolveRef(
        NAVIGATION_CONFIG_CLASS_SPACE, SpaceReference.class, wikiRef));
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
    return new DocumentReference(MENU_ITEM_CLASS_DOC, modelUtils.resolveRef(MENU_ITEM_CLASS_SPACE,
        SpaceReference.class, wikiRef));
  }

  @Override
  public DocumentReference getNewMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MAPPED_MENU_ITEM_CLASS_SPACE,
        MAPPED_MENU_ITEM_CLASS_DOC);
  }

}
