package com.celements.cells;

import org.xwiki.component.annotation.Component;

import com.celements.pagetype.AbstractPageTypeCategory;

@Component(CellTypeCategory.CELL_TYPE_CATEGORY)
public class CellTypeCategory extends AbstractPageTypeCategory {

  public static final String CELL_TYPE_CATEGORY = "cellTypeCategory";

  @Override
  public String getTypeName() {
    return "celltype";
  }

}
