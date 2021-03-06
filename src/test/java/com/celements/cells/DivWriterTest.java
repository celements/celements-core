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
import com.celements.cells.attribute.DefaultCellAttribute;

public class DivWriterTest {

  private DivWriter divWriter;

  @Before
  public void prepareTest() throws Exception {
    divWriter = new DivWriter();
  }

  @Test
  public void testCloseLevel() {
    String tagName = "";
    String idname = "theId";
    divWriter.openLevel(tagName, idname, "", "");
    divWriter.closeLevel();
    assertEquals("<div id=\"theId\"></div>", divWriter.getAsString());
  }

  @Test
  public void testCloseLevel_tagName() {
    String tagName = "form";
    String idname = "theId2";
    divWriter.openLevel(tagName, idname, "", "");
    divWriter.closeLevel();
    assertEquals("<form id=\"theId2\"></form>", divWriter.getAsString());
  }

  @Test
  public void testOpenLevel() {
    String idname = "newId";
    String cssClasses = "classes";
    String cssStyles = "width:100px;\nheight:10px;\n";
    divWriter.openLevel(idname, cssClasses, cssStyles);
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
  public void testOpenLevel_id_null() {
    String cssClasses = "classes";
    divWriter.openLevel(null, cssClasses, "");
    assertEquals("<div class=\"" + cssClasses + "\">", divWriter.getAsString());
  }

  @Test
  public void testOpenLevel_id_empty() {
    String cssClasses = "classes";
    divWriter.openLevel("", cssClasses, "");
    assertEquals("<div class=\"" + cssClasses + "\">", divWriter.getAsString());
  }

  @Test
  public void testOpenLevel_cssClasses_null() {
    String idname = "newId";
    divWriter.openLevel(idname, null, "");
    assertEquals("<div id=\"" + idname + "\">", divWriter.getAsString());
  }

  @Test
  public void testOpenLevel_cssClasses_empty() {
    String idname = "newId";
    divWriter.openLevel(idname, "", "");
    assertEquals("<div id=\"" + idname + "\">", divWriter.getAsString());
  }

  @Test
  public void testOpenLevel_cssStyles_empty() {
    String idname = "newId";
    String cssClasses = "classes";
    divWriter.openLevel(idname, cssClasses, "");
    assertTrue("Must start with '<div '", divWriter.getAsString().startsWith("<div "));
    assertTrue("Must end with '>'", divWriter.getAsString().endsWith(">"));
    String cssClassExpected = " class=\"" + cssClasses + "\"";
    assertTrue("Must contain '" + cssClassExpected + "'", divWriter.getAsString().contains(
        cssClassExpected));
    assertFalse("Must not contain any style values.", divWriter.getAsString().contains(
        " style=\""));
  }

  @Test
  public void testOpenLevel_cssStyles_null() {
    String idname = "newId";
    String cssClasses = "classes";
    divWriter.openLevel(idname, cssClasses, null);
    assertTrue("Must start with '<div '", divWriter.getAsString().startsWith("<div "));
    assertTrue("Must end with '>'", divWriter.getAsString().endsWith(">"));
    String cssClassExpected = " class=\"" + cssClasses + "\"";
    assertTrue("Must contain '" + cssClassExpected + "'", divWriter.getAsString().contains(
        cssClassExpected));
    assertFalse("Must not contain any style values.", divWriter.getAsString().contains(
        " style=\""));
  }

  @Test
  public void testGetOut() {
    StringBuilder out = divWriter.getOut();
    assertNotNull("lacy initalization expected", out);
    assertSame("out buffer may not be reset without calling startRendering in between", out,
        divWriter.getOut());
  }

  @Test
  public void testGetAsString() {
    String expectedOutput = "blabla output";
    divWriter.getOut().append(expectedOutput);
    assertEquals("asString must return the current state of the StringBuilder (out).",
        expectedOutput, divWriter.getAsString());
  }

  @Test
  public void testClear() {
    divWriter.getOut().append("blabla");
    divWriter.clear();
    assertEquals("clear must reset the output stream", "", divWriter.getAsString());
  }

  @Test
  public void testAppendContent() {
    String content = "blablabla";
    divWriter.appendContent(content);
    assertEquals("appendContent must add the given content to the output stream", content,
        divWriter.getAsString());
  }

  @Test
  public void testOpenLevel_Attributes() {
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
  public void testOpenLevel_quotes() {
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
}
