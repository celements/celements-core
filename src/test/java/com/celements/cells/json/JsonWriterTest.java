package com.celements.cells.json;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;

public class JsonWriterTest {

  private JsonWriter writer;

  @Before
  public void prepareTest() throws Exception {
    writer = new JsonWriter();
  }

  @Test
  public void test_openLevel() {
    writer.openLevel("div");
    writer.closeLevel();
    assertEquals("{\"node1\" : {\"tagName\" : \"div\", \"attributes\" : {}}}",
        writer.getAsString());
  }

  @Test
  public void test_openLevel_nested() {
    writer.openLevel("div");
    writer.openLevel("div");
    writer.closeLevel();
    writer.closeLevel();
    assertEquals("{"
        + "\"node1\" : {\"tagName\" : \"div\", \"attributes\" : {},"
        + " \"node2\" : {\"tagName\" : \"div\", \"attributes\" : {}}}"
        + "}", writer.getAsString());
  }

  @Test
  public void test_openLevel_onTopLevel() {
    writer.openLevel("div");
    writer.closeLevel();
    writer.openLevel("div");
    writer.closeLevel();
    assertEquals("{"
        + "\"node1\" : {\"tagName\" : \"div\", \"attributes\" : {}},"
        + " \"node2\" : {\"tagName\" : \"div\", \"attributes\" : {}}"
        + "}", writer.getAsString());
  }

  @Test
  public void test_openLevel_attributes() {
    DefaultAttributeBuilder attrBuilder = new DefaultAttributeBuilder();
    attrBuilder.addCssClasses("cel_cell");
    List<CellAttribute> attributes = attrBuilder.build();
    writer.openLevel("div", attributes);
    writer.closeLevel();
    assertEquals("{\"node1\" : {\"tagName\" : \"div\", \"attributes\" :"
        + " {\"class\" : \"cel_cell\"}}}", writer.getAsString());
  }

}
