package com.celements.cells.attribute;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.cells.attribute.DefaultCellAttribute.Builder;
import com.google.common.base.Optional;

public class DefaultAttributeBuilderTest {

  private DefaultAttributeBuilder attrBuilder;

  @Before
  public void prepareTest() throws Exception {
    attrBuilder = new DefaultAttributeBuilder();
  }

  @Test
  public void testGetAttributeBuilder() {
    Builder celAttrBuilder = attrBuilder.getAttributeBuilder("testAttribute");
    assertNotNull(celAttrBuilder);
    Builder celAttrBuilder2 = attrBuilder.getAttributeBuilder("testAttribute");
    assertSame(celAttrBuilder, celAttrBuilder2);
    Builder celAttrBuilderDiff = attrBuilder.getAttributeBuilder("testAttribute2");
    assertNotSame(celAttrBuilder, celAttrBuilderDiff);
  }

  @Test
  public void testAddEmptyAttribute() {
    String attrName = "selected";
    attrBuilder.addEmptyAttribute(attrName);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    assertFalse(cellAttrList.get(0).getValue().isPresent());
  }

  @Test
  public void testAddEmptyAttribute_empyName() {
    attrBuilder.addEmptyAttribute("");
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertTrue(cellAttrList.isEmpty());
  }

  @Test
  public void testAddNonEmptyAttribute() {
    String attrName = "myAttr";
    String attrValue = "myValue";
    attrBuilder.addNonEmptyAttribute(attrName, attrValue);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals(attrValue, theValue.get());
  }

  @Test
  public void testAddNonEmptyAttribute_mulit() {
    String attrName = "myAttr";
    String attrValue1 = "myValue1";
    attrBuilder.addNonEmptyAttribute(attrName, attrValue1);
    String attrValue2 = "myValue2";
    attrBuilder.addNonEmptyAttribute(attrName, attrValue2);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals(attrValue1 + " " + attrValue2, theValue.get());
  }

  @Test
  public void testAddNonEmptyAttribute_Iterable_empty() {
    String attrName = "class";
    attrBuilder.addNonEmptyAttribute(attrName, Collections.<String>emptyList());
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertTrue(cellAttrList.isEmpty());
  }

  @Test
  public void testAddNonEmptyAttribute_Iterable() {
    String attrName = "class";
    attrBuilder.addNonEmptyAttribute(attrName, Arrays.asList("myClass", "myClass1", "myClass2"));
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals("myClass myClass1 myClass2", theValue.get());
  }

  @Test
  public void testBuild_empty() {
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertTrue(cellAttrList.isEmpty());
  }

  @Test
  public void testAddId() {
    String attrName = "id";
    String attrValue = "myValue";
    attrBuilder.addId(attrValue);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals(attrValue, theValue.get());
  }

  @Test
  public void testAddCssClasses() {
    String attrName = "class";
    String attrValue = "myValue";
    attrBuilder.addCssClasses(attrValue);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals(attrValue, theValue.get());
  }

  @Test
  public void testAddStyles() {
    String attrName = "style";
    String attrValue = "myValue";
    attrBuilder.addStyles(attrValue);
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertNotNull(cellAttrList);
    assertFalse(cellAttrList.isEmpty());
    assertEquals(attrName, cellAttrList.get(0).getName());
    Optional<String> theValue = cellAttrList.get(0).getValue();
    assertTrue(theValue.isPresent());
    assertEquals(attrValue, theValue.get());
  }

}
