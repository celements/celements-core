package com.celements.cells;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface ICellsClassConfig {

  public static final String CELEMENTS_CELL_CLASS_SPACE = "Celements";
  public static final String CELEMENTS_CELL_CLASS_NAME = "CellClass";
  public static final String CELEMENTS_CELL_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + CELEMENTS_CELL_CLASS_NAME;
  public static final String CELLCLASS_TAGNAME_FIELD = "tagname";
  public static final String CELLCLASS_IDNAME_FIELD = "idname";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_DOC = "PageDepCellConfigClass";

  public static final String PAGE_LAYOUT_PROPERTIES_CLASS_DOC = "PageLayoutPropertiesClass";
  public static final String PAGE_LAYOUT_PROPERTIES_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + PAGE_LAYOUT_PROPERTIES_CLASS_DOC;
  public static final String LAYOUT_TYPE_FIELD = "layout_type";
  public static final String EDITOR_LAYOUT_VALUE = "editorLayout";
  public static final String PAGE_LAYOUT_VALUE = "pageLayout";
  public static final String LAYOUT_DOCTYPE_FIELD = "doctype";
  public static final String DOCTYPE_HTML_5_VALUE = "HTML 5";
  public static final String DOCTYPE_XHTML_VALUE = "XHTML 1.1";

  public DocumentReference getCellClassRef(String wikiName);

  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName);

  public DocumentReference getGroupCellClassRef(String wikiName);

  public DocumentReference getPageDepCellConfigClassRef(String wikiName);

  public DocumentReference getTranslationBoxCellConfigClassRef(String wikiName);

}
