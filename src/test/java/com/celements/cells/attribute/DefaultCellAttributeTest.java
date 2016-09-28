package com.celements.cells.attribute;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.cells.attribute.DefaultCellAttribute.Builder;

public class DefaultCellAttributeTest {

  private Builder cellAttrBuilder;

  @Before
  public void prepareTest() throws Exception {
    cellAttrBuilder = new DefaultCellAttribute.Builder();
  }

  @Test
  public void testGetName() {
    String attrName = "myName";
    cellAttrBuilder.attrName(attrName);
    DefaultCellAttribute cellAttr = cellAttrBuilder.build();
    assertEquals(attrName, cellAttr.getName());
  }

  @Test
  public void testGetName_empty() {
    try {
      cellAttrBuilder.build();
      fail("no attribute name given. 'build' must fail with IllegalStateException");
    } catch (IllegalStateException exp) {
      // expected
    }
  }

  @Test
  public void testGetValue() {
    cellAttrBuilder.attrName("myName");
    String attrValue = "myValue";
    cellAttrBuilder.addValue(attrValue);
    DefaultCellAttribute cellAttr = cellAttrBuilder.build();
    assertTrue(cellAttr.getValue().isPresent());
    assertEquals(attrValue, cellAttr.getValue().get());
  }

  @Test
  public void testGetValue_multi() {
    cellAttrBuilder.attrName("myName");
    String attrValue1 = "myValue1";
    String attrValue2 = "myValue2";
    cellAttrBuilder.addValue(attrValue1);
    cellAttrBuilder.addValue(attrValue2);
    DefaultCellAttribute cellAttr = cellAttrBuilder.build();
    assertTrue(cellAttr.getValue().isPresent());
    assertEquals(attrValue1 + " " + attrValue2, cellAttr.getValue().get());
  }

  @Test
  public void testGetValue_multi_skip_equal() {
    cellAttrBuilder.attrName("myName");
    String attrValue1 = "myValue1";
    String attrValue2 = "myValue2";
    cellAttrBuilder.addValue(attrValue1);
    cellAttrBuilder.addValue(attrValue2);
    cellAttrBuilder.addValue(attrValue1);
    DefaultCellAttribute cellAttr = cellAttrBuilder.build();
    assertTrue(cellAttr.getValue().isPresent());
    assertEquals(attrValue1 + " " + attrValue2, cellAttr.getValue().get());
  }

  @Test
  public void testGetValue_empty() {
    cellAttrBuilder.attrName("myName");
    DefaultCellAttribute cellAttr = cellAttrBuilder.build();
    assertFalse(cellAttr.getValue().isPresent());
  }

  @Test
  public void testGetValue_emptyStr() {
    cellAttrBuilder.attrName("myName");
    DefaultCellAttribute cellAttr = cellAttrBuilder.addValue("").build();
    assertTrue(cellAttr.getValue().isPresent());
    assertEquals("", cellAttr.getValue().get());
  }

}
