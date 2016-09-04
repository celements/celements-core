package com.celements.cells;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface ICellsClassConfig {

  public static final String CELEMENTS_CELL_CLASS_SPACE = "Celements";
  public static final String CELEMENTS_CELL_CLASS_NAME = "CellClass";
  public static final String CELEMENTS_CELL_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
      + CELEMENTS_CELL_CLASS_NAME;
  public static final String CELLCLASS_IDNAME_FIELD = "idname";
  public static final String PAGE_DEP_CELL_CONFIG_CLASS_DOC = "PageDepCellConfigClass";

  public DocumentReference getCellClassRef(String wikiName);

  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName);

  public DocumentReference getGroupCellClassRef(String wikiName);

  public DocumentReference getPageDepCellConfigClassRef(String wikiName);

  public DocumentReference getTranslationBoxCellConfigClassRef(String wikiName);

}
