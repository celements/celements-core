package com.celements.pagetype.classes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.xpn.xwiki.web.Utils;

public class PageTypePropertiesClassTest extends AbstractComponentTest {

  private PageTypePropertiesClass pageTypePropertiesClass;

  @Before
  public void prepareTest() throws Exception {

    pageTypePropertiesClass = (PageTypePropertiesClass) Utils.getComponent(ClassDefinition.class,
        PageTypePropertiesClass.CLASS_DEF_HINT);

  }

  @Test
  public void testGetName() {
    String expectedStr = "Celements2.PageType";
    assertEquals(expectedStr, pageTypePropertiesClass.getName());
  }

  @Test
  public void testGetClassSpaceName() {
    String expectedStr = "Celements2";
    assertEquals(expectedStr, pageTypePropertiesClass.getClassSpaceName());
  }

  @Test
  public void testGetClassDocName() {
    String expectedStr = "PageType";
    assertEquals(expectedStr, pageTypePropertiesClass.getClassDocName());
  }

  @Test
  public void test_isInternalMapping() {
    assertTrue(pageTypePropertiesClass.isInternalMapping());
  }
}
