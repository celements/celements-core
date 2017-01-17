package com.celements.pagetype.classes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassPackage;
import com.xpn.xwiki.web.Utils;

public class PageTypeClassPackageTest extends AbstractComponentTest {

  private PageTypeClassPackage pageTypeClassPackage;

  @Before
  public void prepareTest() throws Exception {
    pageTypeClassPackage = (PageTypeClassPackage) Utils.getComponent(ClassPackage.class,
        PageTypeClassPackage.NAME);
  }

  @Test
  public void getNameTest() {
    String expectedStr = "pagetype";
    assertEquals(expectedStr, pageTypeClassPackage.getName());
  }

}
