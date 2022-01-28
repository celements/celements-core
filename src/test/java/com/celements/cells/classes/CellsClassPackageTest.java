package com.celements.cells.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassPackage;
import com.xpn.xwiki.web.Utils;

public class CellsClassPackageTest extends AbstractComponentTest {

  private CellsClassPackage cellsClassPkg;

  @Before
  public void setUp_CellsClassPackageTest() throws Exception {
    cellsClassPkg = (CellsClassPackage) Utils.getComponent(ClassPackage.class,
        CellsClassPackage.NAME);
  }

  @Test
  public void test_getClassDefinitions() {
    CellsClassDefinition pageLayoutPropClass = Utils.getComponent(CellsClassDefinition.class,
        PageLayoutPropertiesClass.CLASS_DEF_HINT);
    replayDefault();
    List<? extends ClassDefinition> classDefs = cellsClassPkg.getClassDefinitions();
    assertTrue("At least PageLayoutPropertiesClass", classDefs.contains(pageLayoutPropClass));
    verifyDefault();
  }

  @Test
  public void test_getLegacyName() {
    replayDefault();
    assertEquals("celCellsClasses", cellsClassPkg.getLegacyName());
    verifyDefault();
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("celementsCells", cellsClassPkg.getName());
    verifyDefault();
  }

}
