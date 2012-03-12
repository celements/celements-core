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
package com.celements.web.sajson;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class JSONbuilderTest {

  private Builder builder;
  
  @Before
  public void setUp() throws Exception {
    builder = new Builder();
  }

  @Test
  public void testOnFirstElement() {
    assertTrue("firstElement must be initialized to true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenArray() {
    builder.openArray();
    assertEquals("Expecting after openArray topmost element on stack" +
            " must be openArray.", ECommand.ARRAY_COMMAND,
            builder.internal_getWorkerStack().peek());
    assertTrue("openArray must add '[' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("["));
    assertFalse("openArray may not add ',' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", ["));
    assertTrue("After openArray firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenArray_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.openArray();
    assertEquals("Expecting after openArray topmost element on stack" +
            " must be openArray.", ECommand.ARRAY_COMMAND,
            builder.internal_getWorkerStack().peek());
    assertTrue("openArray must add ', [' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", ["));
    assertTrue("After openArray firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenDictionary() {
    builder.openDictionary();
    assertEquals("Expecting after openDictionary topmost element on stack" +
            " must be openDictionary.", ECommand.DICTIONARY_COMMAND,
            builder.internal_getWorkerStack().peek());
    assertTrue("openArray must add '{' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("{"));
    assertFalse("openArray may not add ',' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", {"));
    assertTrue("After openDictionary firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenDictionary_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.openDictionary();
    assertEquals("Expecting after openDictionary topmost element on stack" +
            " must be openDictionary.", ECommand.DICTIONARY_COMMAND,
            builder.internal_getWorkerStack().peek());
    assertTrue("openArray must add ', {' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", {"));
    assertTrue("After openDictionary firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenProperty() {
    builder.openDictionary();
    builder.openProperty("key");
    assertEquals("Expecting after openProperty topmost element on stack" +
            " must be openProperty.", ECommand.PROPERTY_COMMAND,
            builder.internal_getWorkerStack().peek());
    String unfinishedJson = builder.internal_getUnfinishedJSON();
    assertTrue("openProperty must add the key with a colon-separator to"
        + " the json expression. '" + unfinishedJson + "'",
            unfinishedJson.endsWith("\"key\" : "));
    assertFalse("openProperty may not add a leading ','.",
            unfinishedJson.endsWith(", \"key\" : "));
    assertTrue("After openProperty firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testOpenProperty_notFirstElement() {
    builder.openDictionary();
    builder.setOnFirstElement(false);
    builder.openProperty("key");
    assertEquals("Expecting after openProperty topmost element on stack" +
            " must be openProperty.", ECommand.PROPERTY_COMMAND,
            builder.internal_getWorkerStack().peek());
    String unfinishedJson = builder.internal_getUnfinishedJSON();
    assertTrue("openProperty must add a leading ',' and the key with"
        + " a colon-separator to the json expression. '" + unfinishedJson + "'",
            unfinishedJson.endsWith(", \"key\" : "));
    assertTrue("After openProperty firstElement must be true.",
        builder.isOnFirstElement());
  }
  
  @Test
  public void testCloseArray() {
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must add ']' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("]"));
    assertTrue("closeArray must remove the ARRAY_COMMAND from stack.",
        builder.internal_getWorkerStack().isEmpty());
    assertFalse("After closeArray firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testCloseArray_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeArray firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testCloseDictionary() {
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must add '}' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("}"));
    assertTrue("closeDictionary must remove the DICTIONARY_COMMAND from stack.",
        builder.internal_getWorkerStack().isEmpty());
    assertFalse("After closeDictionary firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testCloseDictionary_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeDictionary firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testCloseProperty() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.closeProperty();
    builder.internal_getWorkerStack().pop(); // remove Dictonary command
    assertTrue("closeProperty must add 'null' for an empty property"
        + " to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(" null"));
    assertTrue("closeProperty must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().isEmpty());
    assertFalse("After closeProperty firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testOpenProperty_illegalInArray() {
    builder.openArray();
    try {
      builder.openProperty("key");
      fail("Expecting IllegalStateException. Properties may" +
            "only be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testOpenDictionary_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openDictionary();
      fail("Expecting IllegalStateException. Dictionaries cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testOpenArray_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openArray();
      fail("Expecting IllegalStateException. Arrays cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testAddString_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addString("asdf");
      fail("Expecting IllegalStateException. Strings cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testAddBoolean_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addBoolean(false);
      fail("Expecting IllegalStateException. Booleans cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testAddNumber_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addNumber(new BigDecimal(2.0));
      fail("Expecting IllegalStateException. Numbers cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testAddInteger_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addInteger(2);
      fail("Expecting IllegalStateException. Integers cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testAddNull_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addNull();
      fail("Expecting IllegalStateException. Null cannot" +
            "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testCloseArray_onlyAfterOpenArray() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.ARRAY_COMMAND) {
        builder.internal_getWorkerStack().push(jsonCmd);
        try {
          builder.closeArray();
          fail("Expecting IllegalStateException. CloseArray cannot" +
                "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }
  
  @Test
  public void testCloseDictionary_onlyAfterOpenDictionary() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.DICTIONARY_COMMAND) {
        builder.internal_getWorkerStack().push(jsonCmd);
        try {
          builder.closeDictionary();
          fail("Expecting IllegalStateException. CloseArray cannot" +
                "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }
  
  @Test
  public void testCloseProperty_onlyAfterOpenProperty() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.PROPERTY_COMMAND) {
        builder.internal_getWorkerStack().push(jsonCmd);
        try {
          builder.closeProperty();
          fail("Expecting IllegalStateException. CloseArray cannot" +
                "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }
  
  @Test
  public void testGetJSON_NotEmptyStack() {
    builder.internal_getWorkerStack().push(ECommand.ARRAY_COMMAND);
    try {
      builder.getJSON();
      fail("Expecting IllegalStateException. getJSON " +
            "must not be called if stack is not empty.");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
  @Test
  public void testToJSONString_null() {
    assertEquals("Null value must be 'null'.", "null",
        builder.toJSONString(null));
  }

  @Test
  public void testToJSONString_EscapeQuotes() {
    assertEquals("String must be capselled in Quotes.", "\"aasdf38z6 ljb\"",
        builder.toJSONString("aasdf38z6 ljb"));
    assertEquals("Double Quotes must be escaped.", "\"a\\\"b\"",
        builder.toJSONString("a\"b"));
  }

  @Test
  public void testToJSONString_EscapeLineBreaks() {
    assertEquals("LineBreaks must be escaped with \\n.", "\"aasdf38z6\\n ljb\"",
        builder.toJSONString("aasdf38z6\n ljb"));
  }

  @Test
  public void testToJSONString_EscapeCarriageReturn() {
    assertEquals("CarriageReturn must be escaped with \\r.", "\"aasdf38z6\\r ljb\"",
        builder.toJSONString("aasdf38z6\r ljb"));
  }

  @Test
  public void testToJSONString_EscapeTabs() {
    assertEquals("Tabs must be escaped with \\t.", "\"aasdf38z6\\t ljb\"",
        builder.toJSONString("aasdf38z6\t ljb"));
  }

  @Test
  public void testAddString() {
    builder.addString("testValue");
    assertTrue("addString must add '\"testValue\"' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("\"testValue\""));
    assertFalse("After addString firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddString_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addString("testValue");
    assertTrue("addString must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addString firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddString_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addString("testValue");
    assertTrue("addString must add ', \"testValue\"' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", \"testValue\""));
    assertFalse("After addString firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddBoolean() {
    builder.addBoolean(true);
    assertTrue("addBoolean must add '\"testValue\"' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("true"));
    assertFalse("After addBoolean firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddBoolean_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addBoolean(true);
    assertTrue("addBoolean must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addBoolean firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddBoolean_null() {
    builder.addBoolean(null);
    assertTrue("addBoolean must add '\"testValue\"' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("null"));
    assertFalse("After addBoolean firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddBoolean_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addBoolean(true);
    assertTrue("addBoolean must add ', true' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", true"));
    assertFalse("After addBoolean firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNull() {
    builder.addNull();
    assertTrue("addNull must add 'null' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith("null"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNull_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addNull();
    assertTrue("addNull must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNull_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addNull();
    assertTrue("addNull must add ', null' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", null"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNumber() {
    builder.addNumber(new BigDecimal(BigInteger.valueOf(23409876), 5));
    String unfinishedJSON = builder.internal_getUnfinishedJSON();
    assertTrue("addNumber must add '234.09876' to the json expression. '"
        + unfinishedJSON + "'", unfinishedJSON.endsWith("234.09876"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNumberl_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addNumber(new BigDecimal(BigInteger.valueOf(23409876), 5));
    assertTrue("addNumber must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddNumber_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addNumber(new BigDecimal(BigInteger.valueOf(23409876), 5));
    assertTrue("addNumber must add ', 23409876' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", 234.09876"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddInteger() {
    builder.addInteger(23409876);
    String unfinishedJSON = builder.internal_getUnfinishedJSON();
    assertTrue("addInteger must add '23409876' to the json expression. '"
        + unfinishedJSON + "'", unfinishedJSON.endsWith("23409876"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddIntegerl_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addInteger(23409876);
    assertTrue("addInteger must remove the PROPERTY_COMMAND from stack.",
        builder.internal_getWorkerStack().peek(
            ) == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testAddInteger_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addInteger(23409876);
    assertTrue("addInteger must add ', 23409876' to the json expression.",
        builder.internal_getUnfinishedJSON().endsWith(", 23409876"));
    assertFalse("After addNull firstElement must be false.",
        builder.isOnFirstElement());
  }

  @Test
  public void testGetJSON() {
    builder.openDictionary();
    builder.openProperty("theId");
    builder.addString("myString");
    builder.closeDictionary();
    Object jsonTest = "{\"theId\" : \"myString\"}";
    assertEquals("Expecting " + jsonTest , jsonTest, builder.getJSON());
  }
  
  @Test
  public void testGetJSON_addStringProperty() {
    builder.openDictionary();
    builder.addStringProperty("theKey", "TheValue");
    builder.closeDictionary();
    Object jsonTest = "{\"theKey\" : \"TheValue\"}";
    assertEquals("Expecting " + jsonTest , jsonTest, builder.getJSON());
  }
  
}
