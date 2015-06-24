package com.celements.navigation;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface INavigationClassConfig {

  public static final String MENU_NAME_CLASS_DOC = "MenuName";
  public static final String MENU_NAME_CLASS_SPACE = "Celements2";
  public static final String MENU_NAME_CLASS = MENU_NAME_CLASS_SPACE + "."
      + MENU_NAME_CLASS_DOC;
  public static final String MENU_NAME_FIELD = "menu_name";
  public static final String MENU_NAME_IMAGE_FIELD = "image";
  public static final String MENU_NAME_TOOLTIP_FIELD = "tooltip";
  public static final String MENU_NAME_LANG_FIELD = "lang";

  public static final String MAPPED_MENU_ITEM_CLASS_SPACE = "Classes";
  public static final String MAPPED_MENU_ITEM_CLASS_DOC = "MenuItemClass";
  public static final String MAPPED_MENU_ITEM_CLASS = MAPPED_MENU_ITEM_CLASS_SPACE + "."
        + MAPPED_MENU_ITEM_CLASS_DOC;

  public static final String NAVIGATION_CONFIG_CLASS_DOC = "NavigationConfigClass";
  public static final String NAVIGATION_CONFIG_CLASS_SPACE = MENU_NAME_CLASS_SPACE;
  public static final String NAVIGATION_CONFIG_CLASS = NAVIGATION_CONFIG_CLASS_SPACE
        + "." + NAVIGATION_CONFIG_CLASS_DOC;

  public static final String MENU_ELEMENT_NAME_FIELD = "menu_element_name";
  public static final String FROM_HIERARCHY_LEVEL_FIELD = "from_hierarchy_level";
  public static final String TO_HIERARCHY_LEVEL_FIELD = "to_hierarchy_level";
  public static final String SHOW_INACTIVE_TO_LEVEL_FIELD = "show_inactive_to_level";
  public static final String MENU_SPACE_FIELD = "menu_space";
  public static final String MENU_PART_FIELD = "menu_part";
  public static final String CM_CSS_CLASS_FIELD = "cm_css_class";
  public static final String LAYOUT_TYPE_FIELD = "layout_type";
  public static final String PRESENTATION_TYPE_FIELD = "presentation_type";
  public static final String ITEMS_PER_PAGE = "itemsPerPage";

  public static final String MENU_ITEM_CLASS_DOC = "MenuItem";
  public static final String MENU_ITEM_CLASS_SPACE = MENU_NAME_CLASS_SPACE;
  public static final String MENU_ITEM_CLASS = MENU_ITEM_CLASS_SPACE + "."
        + MENU_ITEM_CLASS_DOC;
  public static final String MENU_POSITION_FIELD = "menu_position";
  public static final String PART_NAME_FIELD = "part_name";
  public static final String TARGET_FIELD = "link_target";

  public DocumentReference getMenuNameClassRef(String wikiName);

  public DocumentReference getNavigationConfigClassRef(String wikiName);

  public DocumentReference getNavigationConfigClassRef(WikiReference wikiRef);

  public DocumentReference getMenuItemClassRef(String wikiName);

  public DocumentReference getNewMenuItemClassRef(String wikiName);

}
