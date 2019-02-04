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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableMap;

/**
 * Simple Api for JSON The JSON Builder helps do generate a valid JSON expression based on
 * event handling.
 *
 * @author fabian
 */

@NotThreadSafe
public class Builder {

  private static final Map<Pattern, String> JSON_REPLACEMENTS = ImmutableMap.of(Pattern.compile(
      "\\\\"), "\\\\\\\\", Pattern.compile("\""), "\\\\\"", Pattern.compile("\n"), "\\\\n",
      Pattern.compile("\r"), "\\\\r", Pattern.compile("\t"), "\\\\t");

  private final Deque<ECommand> workerStack;
  private final StringBuilder jsonOutput;
  private boolean onFirstElement;

  public Builder() {
    workerStack = new ArrayDeque<>();
    jsonOutput = new StringBuilder();
    onFirstElement = true;
  }

  public String getJSON() {
    if (!workerStack.isEmpty()) {
      throw new IllegalStateException(workerStack.peek() + " is still open.");
    }
    return jsonOutput.toString();
  }

  public void openArray() {
    checkNoDictionary();
    workerStack.push(ECommand.ARRAY_COMMAND);
    addOpeningPart("[");
    onFirstElement = true;
  }

  private void addOpeningPart(String openingPart) {
    if (!onFirstElement) {
      jsonOutput.append(", ");
    }
    jsonOutput.append(openingPart);
  }

  private void checkNoDictionary() {
    if (!workerStack.isEmpty() && (workerStack.peek() == ECommand.DICTIONARY_COMMAND)) {
      throw new IllegalStateException("Cannot added to a dictionary.");
    }
  }

  public void closeArray() {
    checkForOpenCommand(ECommand.ARRAY_COMMAND);
    jsonOutput.append("]");
    workerStack.pop();
    implicitCloseProperty();
  }

  public void openDictionary() {
    checkNoDictionary();
    workerStack.push(ECommand.DICTIONARY_COMMAND);
    addOpeningPart("{");
    onFirstElement = true;
  }

  public void closeDictionary() {
    checkForOpenCommand(ECommand.DICTIONARY_COMMAND);
    jsonOutput.append("}");
    workerStack.pop();
    implicitCloseProperty();
  }

  public void openProperty(String key) {
    if (workerStack.peek() != ECommand.DICTIONARY_COMMAND) {
      throw new IllegalStateException("Properties may only be added " + "to a dictionary but found "
          + workerStack.peek());
    }
    workerStack.push(ECommand.PROPERTY_COMMAND);
    addOpeningPart(toJSONString(key) + " : ");
    onFirstElement = true;
  }

  public void closeProperty() {
    checkForOpenCommand(ECommand.PROPERTY_COMMAND);
    if (jsonOutput.toString().endsWith(" : ")) {
      jsonOutput.append(toJSONString(null));
    }
    workerStack.pop();
    onFirstElement = false;
  }

  public void addStringProperty(String key, String value) {
    addStringProperty(key, (Object) value);
  }

  public void addStringProperty(String key, Object value) {
    openProperty(key);
    addString(value);
  }

  public void addString(String value) {
    addString((Object) value);
  }

  public void addString(Object value) {
    checkNoDictionary();
    addOpeningPart(toJSONString(value));
    implicitCloseProperty();
  }

  private void implicitCloseProperty() {
    if (!workerStack.isEmpty() && (workerStack.peek() == ECommand.PROPERTY_COMMAND)) {
      workerStack.pop();
    }
    onFirstElement = false;
  }

  public void addBoolean(Boolean value) {
    checkNoDictionary();
    if (value != null) {
      addOpeningPart(Boolean.toString(value));
    } else {
      addNull();
    }
    implicitCloseProperty();
  }

  public void addNumber(Number value) {
    checkNoDictionary();
    if (value != null) {
      addOpeningPart(value.toString());
    } else {
      addNull();
    }
    implicitCloseProperty();
  }

  /**
   * @deprecated instead use {@link #addNumber(Number)}
   */
  @Deprecated
  public void addFloat(Float value) {
    addNumber(value);
  }

  /**
   * @deprecated instead use {@link #addNumber(Number)}
   */
  @Deprecated
  public void addInteger(Integer value) {
    addNumber(value);
  }

  public void addNull() {
    checkNoDictionary();
    addOpeningPart(toJSONString(null));
    implicitCloseProperty();
  }

  /**
   * for internal use only (ONLY TESTS!!!)
   */
  Deque<ECommand> internal_getWorkerStack() {
    return workerStack;
  }

  /**
   * for internal use only (ONLY TESTS!!!)
   */
  String internal_getUnfinishedJSON() {
    return jsonOutput.toString();
  }

  private void checkForOpenCommand(ECommand expectedCmd) {
    if (workerStack.isEmpty()) {
      throw new IllegalStateException("Cannot be applied a close " + expectedCmd
          + " on an empty stack.");
    }
    if (workerStack.peek() != expectedCmd) {
      throw new IllegalStateException("Close " + expectedCmd + " cannot follow "
          + workerStack.peek());
    }
  }

  String toJSONString(Object value) {
    String outStr = Objects.toString(value);
    if (value != null) {
      for (Entry<Pattern, String> entry : JSON_REPLACEMENTS.entrySet()) {
        outStr = entry.getKey().matcher(outStr).replaceAll(entry.getValue());
      }
      outStr = new StringBuilder("\"").append(outStr).append("\"").toString();
    }
    return outStr;
  }

  /**
   * for internal use only (ONLY TESTS!!!)
   */
  void setOnFirstElement(boolean onFirstElement) {
    this.onFirstElement = onFirstElement;
  }

  boolean isOnFirstElement() {
    return onFirstElement;
  }

}
