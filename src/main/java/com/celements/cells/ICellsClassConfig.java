package com.celements.cells;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.classes.CellClass;

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

  String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  String PAGE_LAYOUT_PROPERTIES_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;
  String LAYOUT_TYPE_FIELD = "layout_type";
  String EDITOR_LAYOUT_VALUE = "editorLayout";
  String PAGE_LAYOUT_VALUE = "pageLayout";
  String LAYOUT_DOCTYPE_FIELD = "doctype";

  @Deprecated
  DocumentReference getCellClassRef(String wikiName);

  DocumentReference getPageLayoutPropertiesClassRef(String wikiName);

  DocumentReference getGroupCellClassRef(String wikiName);

  DocumentReference getPageDepCellConfigClassRef(String wikiName);

  DocumentReference getTranslationBoxCellConfigClassRef(String wikiName);

}
