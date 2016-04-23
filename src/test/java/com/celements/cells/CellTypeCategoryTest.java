package com.celements.cells;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.xpn.xwiki.web.Utils;


public class CellTypeCategoryTest extends AbstractComponentTest {

  private CellTypeCategory cellTypeCat;

  @Before
  public void setUp_AbstractPageTypeCategoryTest() throws Exception {
    cellTypeCat = (CellTypeCategory) Utils.getComponent(IPageTypeCategoryRole.class,
        CellTypeCategory.CELL_TYPE_CATEGORY);
  }

  @Test
  public void testGetAllTypeNames() {
    replayDefault();
    assertEquals(1, cellTypeCat.getAllTypeNames().size());
    assertTrue(cellTypeCat.getAllTypeNames().contains("celltype"));
    verifyDefault();
  }

  @Test
  public void testGetDeprecatedNames() {
    replayDefault();
    assertTrue(cellTypeCat.getDeprecatedNames().isEmpty());
    verifyDefault();
  }

}
