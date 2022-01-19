package com.celements.cells;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.classes.CellClass;
import com.celements.cells.classes.PageLayoutPropertiesClass;

@ComponentRole
public interface ICellsClassConfig {

  String CELEMENTS_CELL_CLASS_SPACE = "Celements";
  @Deprecated
  String CELEMENTS_CELL_CLASS_NAME = "CellClass";
  @Deprecated
  String CELEMENTS_CELL_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + CELEMENTS_CELL_CLASS_NAME;
  @Deprecated
  String CELLCLASS_TAGNAME_FIELD = CellClass.FIELD_TAG_NAME.getName();
  @Deprecated
  String CELLCLASS_IDNAME_FIELD = CellClass.FIELD_ID_NAME.getName();
  String PAGE_DEP_CELL_CONFIG_CLASS_DOC = "PageDepCellConfigClass";

  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#DOC_NAME}
   */
  @Deprecated
  String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#CLASS_FN}
   */
  @Deprecated
  String PAGE_LAYOUT_PROPERTIES_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;
  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#FIELD_LAYOUT_TYPE}
   */
  @Deprecated
  String LAYOUT_TYPE_FIELD = "layout_type";
  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#EDITOR_LAYOUT_VALUE}
   */
  @Deprecated
  String EDITOR_LAYOUT_VALUE = "editorLayout";
  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#PAGE_LAYOUT_VALUE}
   */
  @Deprecated
  String PAGE_LAYOUT_VALUE = "pageLayout";
  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#FIELD_LAYOUT_DOCTYPE}
   */
  @Deprecated
  String LAYOUT_DOCTYPE_FIELD = "doctype";

  @Deprecated
  DocumentReference getCellClassRef(String wikiName);

  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#CLASS_REF}
   */
  @Deprecated
  DocumentReference getPageLayoutPropertiesClassRef(String wikiName);

  DocumentReference getGroupCellClassRef(String wikiName);

  DocumentReference getPageDepCellConfigClassRef(String wikiName);

  DocumentReference getTranslationBoxCellConfigClassRef(String wikiName);

}
