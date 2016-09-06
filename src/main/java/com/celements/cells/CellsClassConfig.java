package com.celements.cells;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

@Component
public class CellsClassConfig implements ICellsClassConfig {

  @Override
  public DocumentReference getCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE, CELEMENTS_CELL_CLASS_NAME);
  }

  @Override
  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
  }

  @Override
  public DocumentReference getGroupCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE, "GroupCellClass");
  }

  @Override
  public DocumentReference getPageDepCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        PAGE_DEP_CELL_CONFIG_CLASS_DOC);
  }

  @Override
  public DocumentReference getTranslationBoxCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        "TranslationBoxCellConfigClass");
  }

}
