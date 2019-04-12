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
package com.celements.sajson;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class BuilderTest {

  private Builder builder;

  @Before
  public void prepareTest() throws Exception {
    builder = new Builder();
  }

  @Test
  public void test_OnFirstElement() {
    assertTrue("firstElement must be initialized to true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenArray() {
    builder.openArray();
    assertEquals("Expecting after openArray topmost element on stack" + " must be openArray.",
        ECommand.ARRAY_COMMAND, builder.getCommandStack().peek());
    assertTrue("openArray must add '[' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("["));
    assertFalse("openArray may not add ',' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", ["));
    assertTrue("After openArray firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenArray_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.openArray();
    assertEquals("Expecting after openArray topmost element on stack" + " must be openArray.",
        ECommand.ARRAY_COMMAND, builder.getCommandStack().peek());
    assertTrue("openArray must add ', [' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", ["));
    assertTrue("After openArray firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenDictionary() {
    builder.openDictionary();
    assertEquals("Expecting after openDictionary topmost element on stack"
        + " must be openDictionary.", ECommand.DICTIONARY_COMMAND,
        builder.getCommandStack().peek());
    assertTrue("openArray must add '{' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("{"));
    assertFalse("openArray may not add ',' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", {"));
    assertTrue("After openDictionary firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenDictionary_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.openDictionary();
    assertEquals("Expecting after openDictionary topmost element on stack"
        + " must be openDictionary.", ECommand.DICTIONARY_COMMAND,
        builder.getCommandStack().peek());
    assertTrue("openArray must add ', {' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", {"));
    assertTrue("After openDictionary firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenProperty() {
    builder.openDictionary();
    builder.openProperty("key");
    assertEquals("Expecting after openProperty topmost element on stack" + " must be openProperty.",
        ECommand.PROPERTY_COMMAND, builder.getCommandStack().peek());
    String unfinishedJson = builder.getJSONWithoutCheck();
    assertTrue("openProperty must add the key with a colon-separator to" + " the json expression. '"
        + unfinishedJson + "'", unfinishedJson.endsWith("\"key\" : "));
    assertFalse("openProperty may not add a leading ','.", unfinishedJson.endsWith(", \"key\" : "));
    assertTrue("After openProperty firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenProperty_notFirstElement() {
    builder.openDictionary();
    builder.setOnFirstElement(false);
    builder.openProperty("key");
    assertEquals("Expecting after openProperty topmost element on stack" + " must be openProperty.",
        ECommand.PROPERTY_COMMAND, builder.getCommandStack().peek());
    String unfinishedJson = builder.getJSONWithoutCheck();
    assertTrue("openProperty must add a leading ',' and the key with"
        + " a colon-separator to the json expression. '" + unfinishedJson + "'",
        unfinishedJson.endsWith(", \"key\" : "));
    assertTrue("After openProperty firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_CloseArray() {
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must add ']' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("]"));
    assertTrue("closeArray must remove the ARRAY_COMMAND from stack.",
        builder.getCommandStack().isEmpty());
    assertFalse("After closeArray firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_CloseArray_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeArray firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_CloseDictionary() {
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must add '}' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("}"));
    assertTrue("closeDictionary must remove the DICTIONARY_COMMAND from stack.",
        builder.getCommandStack().isEmpty());
    assertFalse("After closeDictionary firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_CloseDictionary_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeDictionary firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_CloseProperty() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.closeProperty();
    builder.getCommandStack().pop(); // remove Dictonary command
    assertTrue("closeProperty must add 'null' for an empty property" + " to the json expression.",
        builder.getJSONWithoutCheck().endsWith(" null"));
    assertTrue("closeProperty must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().isEmpty());
    assertFalse("After closeProperty firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_OpenProperty_illegalInArray() {
    builder.openArray();
    try {
      builder.openProperty("key");
      fail("Expecting IllegalStateException. Properties may" + "only be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_OpenDictionary_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openDictionary();
      fail("Expecting IllegalStateException. Dictionaries cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_OpenArray_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openArray();
      fail("Expecting IllegalStateException. Arrays cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_AddString_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addString("asdf");
      fail("Expecting IllegalStateException. Strings cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_AddBoolean_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addBoolean(false);
      fail("Expecting IllegalStateException. Booleans cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_AddNumber_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addNumber(5L);
      fail("Expecting IllegalStateException. Numbers cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_AddInteger_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addInteger(2);
      fail("Expecting IllegalStateException. Integers cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_AddNull_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addNull();
      fail("Expecting IllegalStateException. Null cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_CloseArray_onlyAfterOpenArray() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.ARRAY_COMMAND) {
        builder.getCommandStack().push(jsonCmd);
        try {
          builder.closeArray();
          fail("Expecting IllegalStateException. CloseArray cannot" + "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }

  @Test
  public void test_CloseDictionary_onlyAfterOpenDictionary() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.DICTIONARY_COMMAND) {
        builder.getCommandStack().push(jsonCmd);
        try {
          builder.closeDictionary();
          fail("Expecting IllegalStateException. CloseArray cannot" + "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }

  @Test
  public void test_CloseProperty_onlyAfterOpenProperty() {
    for (ECommand jsonCmd : ECommand.values()) {
      if (jsonCmd != ECommand.PROPERTY_COMMAND) {
        builder.getCommandStack().push(jsonCmd);
        try {
          builder.closeProperty();
          fail("Expecting IllegalStateException. CloseArray cannot" + "follow to " + jsonCmd);
        } catch (IllegalStateException e) {
          // expected
        }
      }
    }
  }

  @Test
  public void test_GetJSON_NotEmptyStack() {
    builder.getCommandStack().push(ECommand.ARRAY_COMMAND);
    try {
      builder.getJSON();
      fail("Expecting IllegalStateException. getJSON "
          + "must not be called if stack is not empty.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_toJsonString_null() {
    assertEquals("Null value must be 'null'.", "null", builder.toJsonString(null));
  }

  @Test
  public void test_toJsonString_EscapeQuotes() {
    assertEquals("String must be capselled in Quotes.", "\"aasdf38z6 ljb\"", builder.toJsonString(
        "aasdf38z6 ljb"));
    assertEquals("Double Quotes must be escaped.", "\"a\\\"b\"", builder.toJsonString("a\"b"));
  }

  @Test
  public void test_toJsonString_EscapeLineBreaks() {
    assertEquals("LineBreaks must be escaped with \\n.", "\"aasdf38z6\\n ljb\"",
        builder.toJsonString("aasdf38z6\n ljb"));
  }

  @Test
  public void test_toJsonString_EscapeCarriageReturn() {
    assertEquals("CarriageReturn must be escaped with \\r.", "\"aasdf38z6\\r ljb\"",
        builder.toJsonString("aasdf38z6\r ljb"));
  }

  @Test
  public void test_toJsonString_EscapeTabs() {
    assertEquals("Tabs must be escaped with \\t.", "\"aasdf38z6\\t ljb\"", builder.toJsonString(
        "aasdf38z6\t ljb"));
  }

  @Test
  public void test_toJsonString_EscapeBackslashes() {
    assertEquals("Backslashes must be escaped.", "\"a\\\\b\"", builder.toJsonString("a\\b"));
    assertEquals("\"{\\\"...\\\" : \\\"<a href=\\\\\\\"...\\\\\\\">...\\\"}\"",
        builder.toJsonString("{\"...\" : \"<a href=\\\"...\\\">...\"}"));
  }

  @Test
  public void test_AddString() {
    builder.addString("testValue");
    assertTrue("addString must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("\"testValue\""));
    assertFalse("After addString firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddString_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addString("testValue");
    assertTrue("addString must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addString firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddString_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addString("testValue");
    assertTrue("addString must add ', \"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", \"testValue\""));
    assertFalse("After addString firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddBoolean() {
    builder.addBoolean(true);
    assertTrue("addBoolean must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("true"));
    assertFalse("After addBoolean firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddBoolean_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addBoolean(true);
    assertTrue("addBoolean must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addBoolean firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddBoolean_null() {
    builder.addBoolean(null);
    assertTrue("addBoolean must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("null"));
    assertFalse("After addBoolean firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddBoolean_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addBoolean(true);
    assertTrue("addBoolean must add ', true' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", true"));
    assertFalse("After addBoolean firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNull() {
    builder.addNull();
    assertTrue("addNull must add 'null' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("null"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNull_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addNull();
    assertTrue("addNull must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNull_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addNull();
    assertTrue("addNull must add ', null' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", null"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber() {
    builder.addNumber(23409876);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addNumber must add '23409876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumberl_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addNumber(23409876);
    assertTrue("addNumber must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addNumber(23409876);
    assertTrue("addNumber must add ', 23409876' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", 23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber_Long() {
    builder.addNumber(234098763455237134L);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addNumber must add '234098763455237134L' to the json expression. '" + unfinishedJSON
        + "'", unfinishedJSON.endsWith("234098763455237134"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber_Float() {
    builder.addNumber(4.3f);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addNumber must add '4.3' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("4.3"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber_Double() {
    builder.addNumber(341.431d);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addNumber must add '341.431' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("341.431"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddNumber_BigDecimal() {
    builder.addNumber(new BigDecimal(BigInteger.valueOf(23409876), 5));
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addNumber must add '234.09876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("234.09876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddInteger() {
    builder.addInteger(23409876);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addInteger must add '23409876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddInteger_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addInteger(23409876);
    assertTrue("addInteger must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_AddInteger_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addInteger(23409876);
    assertTrue("addInteger must add ', 23409876' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", 23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_GetJSON() {
    builder.openDictionary();
    builder.openProperty("theId");
    builder.addString("myString");
    builder.closeDictionary();
    Object jsonTest = "{\"theId\" : \"myString\"}";
    assertEquals("Expecting " + jsonTest, jsonTest, builder.getJSON());
  }

  @Test
  public void test_GetJSON_addStringProperty() {
    builder.openDictionary();
    builder.addStringProperty("theKey", "TheValue");
    builder.closeDictionary();
    Object jsonTest = "{\"theKey\" : \"TheValue\"}";
    assertEquals("Expecting " + jsonTest, jsonTest, builder.getJSON());
  }

  @Test
  public void test_addProperty_otherBuilder() {
    builder.openDictionary();
    builder.addProperty("other", new JsonBuilder().openDictionary().addProperty("a", "b")
        .addProperty("c", "d").closeDictionary());
    builder.closeDictionary();
    String expected = "{\"other\" : {\"a\" : \"b\", \"c\" : \"d\"}}";
    assertEquals(expected, builder.getJSON());
  }

}
