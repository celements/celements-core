package com.celements.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RequestParameterTest {

  @Test
  public void testCreate_null() {
    assertNull(RequestParameter.create(""));
    assertNull(RequestParameter.create("Class.Name_field"));
  }

  @Test
  public void testCreate() {
    String paramName = "Class.Name_5_field";
    RequestParameter param = RequestParameter.create(paramName);
    assertNotNull(param);
    assertEquals(paramName, param.getParameterName());
    assertEquals("Class.Name", param.getClassName());
    assertEquals(5, param.getObjectNr());
    assertEquals("field", param.getFieldName());
  }

  @Test
  public void testResolveFieldNameFromParam_withDoc() {
    String paramName = "Space.Doc_Class.Name_-123_field";
    RequestParameter param = RequestParameter.create(paramName);
    assertNotNull(param);
    assertEquals(paramName, param.getParameterName());
    assertEquals("Class.Name", param.getClassName());
    assertEquals(-123, param.getObjectNr());
    assertEquals("field", param.getFieldName());
  }

  @Test
  public void testIsValidRequestParam_noMatch() {
    String paramName = "";
    assertFalse(RequestParameter.isValidRequestParam(paramName));
    paramName = "abcd";
    assertFalse(RequestParameter.isValidRequestParam(paramName));
    paramName = "Hi_0_there";
    assertFalse(RequestParameter.isValidRequestParam(paramName));
  }

  @Test
  public void testIsValidRequestParam_match() {
    String paramName = "Space.Doc_0_field_one";
    assertTrue(RequestParameter.isValidRequestParam(paramName));
    paramName = "Space.Doc_Class.Name_3_field";
    assertTrue(RequestParameter.isValidRequestParam(paramName));
  }

  @Test
  public void testIncludesDocName_noMatch() {
    String paramName = "";
    assertFalse(RequestParameter.includesDocName(paramName));
    paramName = "abcd";
    assertFalse(RequestParameter.includesDocName(paramName));
    paramName = "Space.Doc_0_field_one";
    assertFalse(RequestParameter.includesDocName(paramName));
  }

  @Test
  public void testIncludesDocName_match() {
    String paramName = "Space.Doc_Class.Name_5_field";
    assertTrue(RequestParameter.includesDocName(paramName));
  }

  @Test
  public void testEquals() {
    String parameterName = "MyClass.Name_0_myFieldName";
    String parameterName2 = "MyClass.Name_0_myFieldName2";
    RequestParameter param1 = RequestParameter.create(parameterName);
    RequestParameter param2 = RequestParameter.create(parameterName2);
    assertEquals(param1, param1);
    assertTrue(param1.hashCode() == param1.hashCode());
    assertEquals(param2, param2);
    assertTrue(param2.hashCode() == param2.hashCode());
    assertFalse(param1.equals(param2));
    assertFalse(param1.hashCode() == param2.hashCode());
  }

}
