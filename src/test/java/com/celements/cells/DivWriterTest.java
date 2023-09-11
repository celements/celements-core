/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.cells;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.cells.attribute.DefaultCellAttribute;

public class DivWriterTest {

  private DivWriter divWriter;

  @Before
  public void prepareTest() throws Exception {
    divWriter = new DivWriter();
  }

  @Test
  public void test_closeLevel_noArg() {
    divWriter.openLevel();
    divWriter.closeLevel();
    assertEquals("<div></div>", divWriter.getAsString());
  }

  @Test
  public void test_closeLevel_empty() {
    divWriter.openLevel("");
    divWriter.closeLevel();
    assertEquals("<div></div>", divWriter.getAsString());
  }

  @Test
  public void test_closeLevel_tagName() {
    String tagName = "form";
    divWriter.openLevel(tagName);
    divWriter.closeLevel();
    assertEquals("<form></form>", divWriter.getAsString());
  }

  @Test
  public void test_closeLevel_voidTag() {
    String tagName = "input";
    divWriter.openLevel(tagName);
    divWriter.appendContent("content");
    divWriter.closeLevel();
    assertEquals("<input>", divWriter.getAsString());
  }

  @Test
  public void test_openLevel() {
    String idname = "newId";
    String cssClasses = "classes";
    String cssStyles = "width:100px;\nheight:10px;\n";
    divWriter.openLevel(new DefaultAttributeBuilder().addId(idname).addCssClasses(
        cssClasses).addStyles(cssStyles).build());
    String returnedString = divWriter.getAsString();
    assertTrue("Must start with '<div ' but got '" + returnedString + "'",
        returnedString.startsWith("<div "));
    assertTrue("Must end with '>' but got '" + returnedString + "'", returnedString.endsWith(">"));
    String idExpected = " id=\"" + idname + "\"";
    assertTrue("Must contain '" + idExpected + "' but got '" + returnedString + "'",
        returnedString.contains(idExpected));
    String cssClassExpected = " class=\"" + cssClasses + "\"";
    assertTrue("Must contain '" + cssClassExpected + "' but got '" + returnedString + "'",
        returnedString.contains(cssClassExpected));
    String cssStylesExpected = "width:100px;height:10px;";
    assertTrue("Must contain '" + cssStylesExpected + "' but got '" + returnedString + "'",
        returnedString.contains(cssStylesExpected));
  }

  @Test
  public void test_openLevel_cssClasses() {
    String cssClasses = "classes";
    divWriter.openLevel(new DefaultAttributeBuilder().addCssClasses(cssClasses).build());
    assertEquals("<div class=\"" + cssClasses + "\">", divWriter.getAsString());
  }

  @Test
  public void test_openLevel_id() {
    String idname = "newId";
    divWriter.openLevel(new DefaultAttributeBuilder().addId(idname).build());
    assertEquals("<div id=\"" + idname + "\">", divWriter.getAsString());
  }

  @Test
  public void test_openLevel_cssStyles_empty() {
    String idname = "newId";
    String cssClasses = "classes";
    divWriter.openLevel(new DefaultAttributeBuilder().addId(idname).addCssClasses(cssClasses)
        .build());
    assertTrue("Must start with '<div '", divWriter.getAsString().startsWith("<div "));
    assertTrue("Must end with '>'", divWriter.getAsString().endsWith(">"));
    String cssClassExpected = " class=\"" + cssClasses + "\"";
    assertTrue("Must contain '" + cssClassExpected + "'", divWriter.getAsString().contains(
        cssClassExpected));
    assertFalse("Must not contain any style values.", divWriter.getAsString().contains(
        " style=\""));
  }

  @Test
  public void test_getAsStringBuilder() {
    StringBuilder out = divWriter.getAsStringBuilder();
    assertNotNull("lacy initalization expected", out);
    assertSame("out buffer may not be reset without calling startRendering in between", out,
        divWriter.getAsStringBuilder());
  }

  @Test
  public void test_getAsString() {
    String expectedOutput = "blabla output";
    divWriter.getAsStringBuilder().append(expectedOutput);
    assertEquals("asString must return the current state of the StringBuilder (out).",
        expectedOutput, divWriter.getAsString());
  }

  @Test
  public void test_clear() {
    divWriter.getAsStringBuilder().append("blabla");
    divWriter.clear();
    assertEquals("clear must reset the output stream", "", divWriter.getAsString());
  }

  @Test
  public void test_appendContent() {
    String content = "blablabla";
    divWriter.appendContent(content);
    assertEquals("appendContent must add the given content to the output stream", content,
        divWriter.getAsString());
  }

  @Test
  public void test_openLevel_Attributes() {
    DefaultCellAttribute.Builder attrBuilder = new DefaultCellAttribute.Builder().attrName(
        "testName").addValue("testValue");
    DefaultCellAttribute.Builder attrBuilder2 = new DefaultCellAttribute.Builder().attrName(
        "testName2").addValue("testValue2");
    List<CellAttribute> attributes = Arrays.asList((CellAttribute) attrBuilder.build(),
        (CellAttribute) attrBuilder2.build());
    divWriter.openLevel(attributes);
    String returnedString = divWriter.getAsString();
    assertTrue("Must start with '<div ' but got '" + returnedString + "'",
        returnedString.startsWith("<div "));
    assertTrue("Must end with '>' but got '" + returnedString + "'", returnedString.endsWith(">"));
    String attr1Expected = "testName=\"testValue\"";
    assertTrue("Must contain '" + attr1Expected + "'", returnedString.contains(attr1Expected));
    String attr2Expected = "testName2=\"testValue2\"";
    assertTrue("Must contain '" + attr2Expected + "'", returnedString.contains(attr2Expected));
  }

  @Test
  public void test_openLevel_quotes() {
    DefaultCellAttribute.Builder attrBuilder = new DefaultCellAttribute.Builder().attrName(
        "testName").addValue("test. : -äöü\"Value");
    List<CellAttribute> attributes = Arrays.asList((CellAttribute) attrBuilder.build());
    divWriter.openLevel(attributes);
    String returnedString = divWriter.getAsString();
    assertTrue("Must start with '<div ' but got '" + returnedString + "'",
        returnedString.startsWith("<div "));
    assertTrue("Must end with '>' but got '" + returnedString + "'", returnedString.endsWith(">"));
    String attrExpected = "testName=\"test. : -&auml;&ouml;&uuml;&quot;Value\"";
    assertTrue("Must contain '" + attrExpected + "' but got '" + returnedString + "'",
        returnedString.contains(attrExpected));
  }

  @Test
  public void test_hasLevelContent() {
    assertFalse(divWriter.hasLevelContent());
    divWriter.appendContent("lol");
    assertTrue(divWriter.hasLevelContent());
    divWriter.openLevel();
    assertFalse(divWriter.hasLevelContent());
    divWriter.openLevel();
    divWriter.closeLevel();
    assertTrue(divWriter.hasLevelContent());
    divWriter.closeLevel();
    assertTrue(divWriter.hasLevelContent());
    divWriter.clear();
    assertFalse(divWriter.hasLevelContent());
  }
}
