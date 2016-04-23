package com.celements.pagetype;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;


public class AbstractPageTypeCategoryTest extends AbstractComponentTest {

  private TestPageTypeCategory ptCat;

  @Before
  public void setUp_AbstractPageTypeCategoryTest() throws Exception {
    ptCat = new TestPageTypeCategory();
  }

  @Test
  public void testGetAllTypeNames() {
    replayDefault();
    assertEquals(1, ptCat.getAllTypeNames().size());
    assertTrue(ptCat.getAllTypeNames().contains("TestPageType"));
    verifyDefault();
  }

  @Test
  public void testGetAllTypeNames_including_deprecated() {
    Set<String> depNames = new HashSet<>();
    depNames.add("oldName1");
    depNames.add("oldName2");
    ptCat.injectDeprecated(depNames);
    replayDefault();
    assertEquals(3, ptCat.getAllTypeNames().size());
    assertTrue(ptCat.getAllTypeNames().contains("TestPageType"));
    assertTrue(ptCat.getAllTypeNames().contains("oldName1"));
    assertTrue(ptCat.getAllTypeNames().contains("oldName2"));
    verifyDefault();
  }

  @Test
  public void testGetDeprecatedNames_empty() {
    replayDefault();
    assertNotNull(ptCat.getDeprecatedNames());
    verifyDefault();
  }

  @Test
  public void testGetDeprecatedNames() {
    Set<String> depNames = new HashSet<>();
    depNames.add("oldName1");
    depNames.add("oldName2");
    ptCat.injectDeprecated(depNames);
    replayDefault();
    assertEquals(2, ptCat.getDeprecatedNames().size());
    assertFalse(ptCat.getDeprecatedNames().contains("TestPageType"));
    assertTrue(ptCat.getDeprecatedNames().contains("oldName1"));
    assertTrue(ptCat.getDeprecatedNames().contains("oldName2"));
    verifyDefault();
  }

  private class TestPageTypeCategory extends AbstractPageTypeCategory {

    private Set<String> injectDeprecatedNames;

    public void injectDeprecated(Set<String> injectDeprecatedNames) {
      this.injectDeprecatedNames = injectDeprecatedNames;
    }

    @Override
    public String getTypeName() {
      return "TestPageType";
    }

    @Override
    public Set<String> getDeprecatedNames() {
      if (injectDeprecatedNames == null) {
        return super.getDeprecatedNames();
      } else {
        return injectDeprecatedNames;
      }
    }
    
  }

}
