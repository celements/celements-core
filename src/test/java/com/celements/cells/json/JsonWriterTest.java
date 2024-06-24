package com.celements.cells.json;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class JsonWriterTest extends AbstractComponentTest {

  private JsonWriter writer;

  @Before
  public void prepareTest() throws Exception {
    writer = new JsonWriter();
  }

  @Test
  public void test_openLevel() {
    writer.openLevel("div");
    writer.closeLevel();
    assertEquals("{\"tagName\" : \"div\", \"attributes\" : {}}", writer.getAsString());
  }

  @Test
  public void test_openLevel_nested() {
    writer.openLevel("div");
    writer.openLevel("div");
    writer.closeLevel();
    writer.closeLevel();
    assertEquals("{\"tagName\" : \"div\", \"attributes\" : {},"
        + " \"cell\" : {\"tagName\" : \"div\", \"attributes\" : {}}}",
        writer.getAsString());
  }

}
