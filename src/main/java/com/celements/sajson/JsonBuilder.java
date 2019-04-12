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

import static com.celements.sajson.ECommand.*;
import static com.google.common.base.Preconditions.*;
import static java.text.MessageFormat.format;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableMap;

/**
 * Simple Api for JSON. The builder helps do generate a valid JSON string based on event handling.
 */
@NotThreadSafe
public class JsonBuilder {

  private static final Map<Pattern, String> JSON_REPLACEMENTS = ImmutableMap.of(
      Pattern.compile("\\\\"), "\\\\\\\\",
      Pattern.compile("\""), "\\\\\"",
      Pattern.compile("\n"), "\\\\n",
      Pattern.compile("\r"), "\\\\r",
      Pattern.compile("\t"), "\\\\t");

  private final Deque<ECommand> commandStack;
  private final StringBuilder json;
  private boolean onFirstElement;

  public JsonBuilder() {
    commandStack = new ArrayDeque<>();
    json = new StringBuilder();
    onFirstElement = true;
  }

  public JsonBuilder(JsonBuilder other) {
    commandStack = new ArrayDeque<>(other.commandStack);
    json = new StringBuilder(other.json);
    onFirstElement = other.onFirstElement;
  }

  Deque<ECommand> getCommandStack() {
    return commandStack;
  }

  public String getJSON() {
    checkState(commandStack.isEmpty(), format("{0} is still open", commandStack.peek()));
    return json.toString();
  }

  String getJSONWithoutCheck() {
    return json.toString();
  }

  public boolean isOnFirstElement() {
    return onFirstElement;
  }

  public JsonBuilder openArray() {
    checkState(commandStack.peek() != DICTIONARY_COMMAND, "cannot open array on dictionary");
    commandStack.push(ARRAY_COMMAND);
    addOpeningPart("[");
    onFirstElement = true;
    return this;
  }

  public JsonBuilder openArray(String key) {
    openProperty(key);
    return openArray();
  }

  private void addOpeningPart(String openingPart) {
    if (!onFirstElement) {
      json.append(", ");
    }
    json.append(openingPart);
  }

  public JsonBuilder closeArray() {
    checkForOpenCommand(ARRAY_COMMAND);
    json.append("]");
    commandStack.pop();
    return implicitCloseProperty();
  }

  public JsonBuilder openDictionary() {
    checkState(commandStack.peek() != DICTIONARY_COMMAND, "dictionary is already open");
    commandStack.push(DICTIONARY_COMMAND);
    addOpeningPart("{");
    onFirstElement = true;
    return this;
  }

  public JsonBuilder openDictionary(String key) {
    openProperty(key);
    return openDictionary();
  }

  public JsonBuilder closeDictionary() {
    checkForOpenCommand(DICTIONARY_COMMAND);
    json.append("}");
    commandStack.pop();
    return implicitCloseProperty();
  }

  public JsonBuilder openProperty(String key) {
    checkState(commandStack.peek() == DICTIONARY_COMMAND,
        format("cannot open property on {0}", commandStack.peek()));
    commandStack.push(PROPERTY_COMMAND);
    addOpeningPart(toJsonString(key) + " : ");
    onFirstElement = true;
    return this;
  }

  public JsonBuilder addProperty(String key, Object value) {
    openProperty(key);
    return addValue(value);
  }

  public JsonBuilder addPropertyNonEmpty(String key, Object value) {
    if ((value != null) && !Objects.toString(value).trim().isEmpty()) {
      return addProperty(key, value);
    }
    return this;
  }

  public JsonBuilder addValue(Object value) {
    checkState(commandStack.peek() != DICTIONARY_COMMAND, "cannot add a value on dictionary");
    addOpeningPart(toJsonString(value));
    return implicitCloseProperty();
  }

  private JsonBuilder implicitCloseProperty() {
    if (commandStack.peek() == PROPERTY_COMMAND) {
      commandStack.pop();
    }
    onFirstElement = false;
    return this;
  }

  public JsonBuilder closeProperty() {
    checkForOpenCommand(PROPERTY_COMMAND);
    if (json.toString().endsWith(" : ")) {
      json.append(toJsonString(null));
    }
    commandStack.pop();
    onFirstElement = false;
    return this;
  }

  String toJsonString(Object value) {
    String outStr;
    if (value instanceof JsonBuilder) {
      outStr = ((JsonBuilder) value).getJSON();
    } else {
      outStr = Objects.toString(value);
    }
    if (applyJsonReplacements(value)) {
      for (Entry<Pattern, String> entry : JSON_REPLACEMENTS.entrySet()) {
        outStr = entry.getKey().matcher(outStr).replaceAll(entry.getValue());
      }
      outStr = new StringBuilder("\"").append(outStr).append("\"").toString();
    }
    return outStr;
  }

  private boolean applyJsonReplacements(Object value) {
    return (value != null)
        && !(value instanceof Boolean)
        && !(value instanceof Number)
        && !(value instanceof JsonBuilder);
  }

  private void checkForOpenCommand(ECommand command) {
    checkState(commandStack.peek() == command,
        format("{0} is open instead of {1}", commandStack.peek(), command));
  }

  /**
   * for test or internal use only
   */
  void setOnFirstElement(boolean onFirstElement) {
    this.onFirstElement = onFirstElement;
  }
}
