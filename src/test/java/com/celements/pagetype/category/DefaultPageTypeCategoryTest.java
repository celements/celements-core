package com.celements.pagetype.category;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.category.DefaultPageTypeCategory;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.xpn.xwiki.web.Utils;

public class DefaultPageTypeCategoryTest extends AbstractComponentTest {

  private DefaultPageTypeCategory ptCat;

  @Before
  public void setUp_AbstractPageTypeCategoryTest() throws Exception {
    ptCat = (DefaultPageTypeCategory) Utils.getComponent(IPageTypeCategoryRole.class);
  }

  @Test
  public void testGetAllTypeNames() {
    replayDefault();
    assertEquals(2, ptCat.getAllTypeNames().size());
    assertTrue(ptCat.getAllTypeNames().contains("pageType"));
    assertTrue(ptCat.getAllTypeNames().contains(""));
    verifyDefault();
  }

  @Test
  public void testGetDeprecatedNames() {
    replayDefault();
    assertEquals(1, ptCat.getDeprecatedNames().size());
    assertTrue(ptCat.getDeprecatedNames().contains(""));
    verifyDefault();
  }

}
