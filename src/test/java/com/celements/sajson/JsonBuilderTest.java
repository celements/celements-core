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
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class JsonBuilderTest {

  private JsonBuilder builder;

  @Before
  public void prepareTest() throws Exception {
    builder = new JsonBuilder();
  }

  @Test
  public void test_create() {
    builder = new JsonBuilder();
    assertTrue(builder.getCommandStack().isEmpty());
    assertTrue(builder.getJSON().isEmpty());
    assertTrue(builder.isOnFirstElement());
  }

  @Test
  public void test_create_clone() {
    builder.openDictionary();
    builder.openProperty("k");
    JsonBuilder clone = new JsonBuilder(builder);
    assertNotSame(builder.getCommandStack(), clone.getCommandStack());
    assertEquals(new ArrayList<>(builder.getCommandStack()), new ArrayList<>(
        clone.getCommandStack()));
    assertEquals(builder.getJSONWithoutCheck(), clone.getJSONWithoutCheck());
    assertEquals(builder.isOnFirstElement(), clone.isOnFirstElement());

    // modify original
    builder.addValue("v");
    builder.closeDictionary();
    assertNotEquals(builder.getJSONWithoutCheck(), clone.getJSONWithoutCheck());
    assertNotEquals(builder.isOnFirstElement(), clone.isOnFirstElement());
  }

  @Test
  public void test_onFirstElement() {
    assertTrue("firstElement must be initialized to true.", builder.isOnFirstElement());
  }

  @Test
  public void test_openArray() {
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
  public void test_openArray_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.openArray();
    assertEquals("Expecting after openArray topmost element on stack" + " must be openArray.",
        ECommand.ARRAY_COMMAND, builder.getCommandStack().peek());
    assertTrue("openArray must add ', [' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", ["));
    assertTrue("After openArray firstElement must be true.", builder.isOnFirstElement());
  }

  @Test
  public void test_openDictionary() {
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
  public void test_openDictionary_notFirstElement() {
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
  public void test_openProperty() {
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
  public void test_openProperty_notFirstElement() {
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
  public void test_closeArray() {
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must add ']' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("]"));
    assertTrue("closeArray must remove the ARRAY_COMMAND from stack.",
        builder.getCommandStack().isEmpty());
    assertFalse("After closeArray firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_closeArray_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openArray();
    builder.closeArray();
    assertTrue("closeArray must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeArray firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_closeDictionary() {
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must add '}' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("}"));
    assertTrue("closeDictionary must remove the DICTIONARY_COMMAND from stack.",
        builder.getCommandStack().isEmpty());
    assertFalse("After closeDictionary firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_closeDictionary_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.openDictionary();
    builder.closeDictionary();
    assertTrue("closeDictionary must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After closeDictionary firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_closeProperty() {
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
  public void test_openProperty_illegalInArray() {
    builder.openArray();
    try {
      builder.openProperty("key");
      fail("Expecting IllegalStateException. Properties may" + "only be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_openDictionary_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openDictionary();
      fail("Expecting IllegalStateException. Dictionaries cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_openArray_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.openArray();
      fail("Expecting IllegalStateException. Arrays cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_addValue_string_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addValue("asdf");
      fail("Expecting IllegalStateException. Strings cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_addValue_bool_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addValue(false);
      fail("Expecting IllegalStateException. Booleans cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_addValue_number_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addValue(5L);
      fail("Expecting IllegalStateException. Numbers cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_addValue_int_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addValue(2);
      fail("Expecting IllegalStateException. Integers cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_addValue_null_illegalInDictionary() {
    builder.openDictionary();
    try {
      builder.addValue(null);
      fail("Expecting IllegalStateException. Null cannot" + "be added to a dictionary.");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void test_closeArray_onlyAfterOpenArray() {
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
  public void test_closeDictionary_onlyAfterOpenDictionary() {
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
  public void test_closeProperty_onlyAfterOpenProperty() {
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
  public void test_getJSON_NotEmptyStack() {
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
  public void test_addValue_string() {
    builder.addValue("testValue");
    assertTrue("addValue must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("\"testValue\""));
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_string_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addValue("testValue");
    assertTrue("addValue must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_string_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addValue("testValue");
    assertTrue("addValue must add ', \"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", \"testValue\""));
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_bool() {
    builder.addValue(true);
    assertTrue("addValue must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("true"));
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_bool_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addValue(true);
    assertTrue("addValue must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_bool_null() {
    builder.addValue(null);
    assertTrue("addValue must add '\"testValue\"' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("null"));
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_bool_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addValue(true);
    assertTrue("addValue must add ', true' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", true"));
    assertFalse("After addValue firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_null() {
    builder.addValue(null);
    assertTrue("addNull must add 'null' to the json expression.",
        builder.getJSONWithoutCheck().endsWith("null"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_null_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addValue(null);
    assertTrue("addNull must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_null_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addValue(null);
    assertTrue("addNull must add ', null' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", null"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_number() {
    builder.addValue(23409876);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '23409876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_number_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addValue(23409876);
    assertTrue("addValue must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_number_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addValue(23409876);
    assertTrue("addValue must add ', 23409876' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", 23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_long() {
    builder.addValue(234098763455237134L);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '234098763455237134L' to the json expression. '" + unfinishedJSON
        + "'", unfinishedJSON.endsWith("234098763455237134"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_float() {
    builder.addValue(4.3f);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '4.3' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("4.3"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_double() {
    builder.addValue(341.431d);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '341.431' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("341.431"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_BigDecimal() {
    builder.addValue(new BigDecimal(BigInteger.valueOf(23409876), 5));
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '234.09876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("234.09876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_int() {
    builder.addValue(23409876);
    String unfinishedJSON = builder.getJSONWithoutCheck();
    assertTrue("addValue must add '23409876' to the json expression. '" + unfinishedJSON + "'",
        unfinishedJSON.endsWith("23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_int_implicitOpenPropertyClosing() {
    builder.openDictionary();
    builder.openProperty("myKey");
    builder.addValue(23409876);
    assertTrue("addValue must remove the PROPERTY_COMMAND from stack.",
        builder.getCommandStack().peek() == ECommand.DICTIONARY_COMMAND);
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_addValue_int_notFirstElement() {
    builder.setOnFirstElement(false);
    builder.addValue(23409876);
    assertTrue("addValue must add ', 23409876' to the json expression.",
        builder.getJSONWithoutCheck().endsWith(", 23409876"));
    assertFalse("After addNull firstElement must be false.", builder.isOnFirstElement());
  }

  @Test
  public void test_getJSON() {
    builder.openDictionary();
    builder.openProperty("theId");
    builder.addValue("myString");
    builder.closeDictionary();
    Object jsonTest = "{\"theId\" : \"myString\"}";
    assertEquals("Expecting " + jsonTest, jsonTest, builder.getJSON());
  }

  @Test
  public void test_getJSON_addProperty() {
    builder.openDictionary();
    builder.addProperty("theKey", "TheValue");
    builder.closeDictionary();
    Object jsonTest = "{\"theKey\" : \"TheValue\"}";
    assertEquals("Expecting " + jsonTest, jsonTest, builder.getJSON());
  }

  @Test
  public void test_addProperty_otherBuilder() {
    JsonBuilder other = new JsonBuilder();
    other.openDictionary();
    other.addProperty("a", "b");
    other.addProperty("c", "d");
    other.closeDictionary();
    builder.openDictionary();
    builder.addProperty("other", other);
    builder.closeDictionary();
    String expected = "{\"other\" : {\"a\" : \"b\", \"c\" : \"d\"}}";
    assertEquals(expected, builder.getJSON());
  }

}
