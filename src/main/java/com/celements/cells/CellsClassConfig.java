package com.celements.cells;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.classes.CellClass;
import com.celements.cells.classes.GroupCellClass;
import com.celements.cells.classes.PageDepCellConfigClass;
import com.celements.cells.classes.PageLayoutPropertiesClass;
import com.celements.cells.classes.TranslationBoxCellConfigClass;

/**
 * @deprecated since 5.4 instead use the corresponding class definitions
 */
@Component
@Deprecated
public class CellsClassConfig implements ICellsClassConfig {

  /**
   * @deprecated since 4.0 instead use {@link CellClass#CLASS_REF}
   */
  @Deprecated
  @Override
  public DocumentReference getCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE, CELEMENTS_CELL_CLASS_NAME);
  }

  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass#CLASS_REF}
   */
  @Deprecated
  @Override
  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        PAGE_LAYOUT_PROPERTIES_CLASS_DOC);
  }

  /**
   * @deprecated since 5.4 instead use {@link GroupCellClass#CLASS_REF}
   */
  @Deprecated
  @Override
  public DocumentReference getGroupCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE, "GroupCellClass");
  }

  /**
   * @deprecated since 5.4 instead use {@link PageDepCellConfigClass#CLASS_REF}
   */
  @Deprecated
  @Override
  public DocumentReference getPageDepCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        PAGE_DEP_CELL_CONFIG_CLASS_DOC);
  }

  /**
   * @deprecated since 5.4 instead use {@link TranslationBoxCellConfigClass#CLASS_REF}
   */
  @Deprecated
  @Override
  public DocumentReference getTranslationBoxCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        "TranslationBoxCellConfigClass");
  }

}
