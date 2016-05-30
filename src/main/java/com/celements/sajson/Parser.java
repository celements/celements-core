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

import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class Parser {

  private ILexicalParser<? extends IGenericLiteral> lexParser;
  private JsonParser parser;
  private static JsonFactory factory = new JsonFactory();
  private Stack<ECommand> workerStack = new Stack<ECommand>();
  private String lastKey = "";
  private String lastValue = "";

  private static Log LOGGER = LogFactory.getFactory().getInstance(Parser.class);

  public static <T extends IGenericLiteral> Parser createLexicalParser(T initLiteral,
      IEventHandler<T> eventHandler) {
    return new Parser(initLiteral, new LexicalParser<T>(initLiteral, eventHandler));
  }

  <T extends IGenericLiteral> Parser(T initLiteral, ILexicalParser<T> lexParser) {
    this.lexParser = lexParser;
  }

  public void parse(String jsonExpression) throws JsonParseException, IOException {
    StringReader jsonReader = new StringReader(jsonExpression);
    try {
      parser = factory.createJsonParser(jsonReader);
      workerStack.clear();
      JsonToken nextToken = parser.nextToken();
      lexParser.initEvent();
      while (parser.hasCurrentToken()) {
        switch (nextToken) {
          case VALUE_TRUE:
          case VALUE_FALSE:
            boolean boolValue = parser.getBooleanValue();
            lastValue = Boolean.toString(boolValue);
            lexParser.booleanEvent(boolValue);
            impliciteCloseProperty();
            break;
          case VALUE_STRING:
            lastValue = parser.getText();
            lexParser.stringEvent(lastValue);
            impliciteCloseProperty();
            break;
          case START_ARRAY:
            checkIllegalStackState(ECommand.DICTIONARY_COMMAND, nextToken);
            workerStack.push(ECommand.ARRAY_COMMAND);
            lexParser.openArrayEvent();
            break;
          case END_ARRAY:
            checkStackState(ECommand.ARRAY_COMMAND);
            lexParser.closeArrayEvent();
            workerStack.pop();
            impliciteCloseProperty();
            break;
          case START_OBJECT:
            checkIllegalStackState(ECommand.DICTIONARY_COMMAND, nextToken);
            workerStack.push(ECommand.DICTIONARY_COMMAND);
            lexParser.openDictionaryEvent();
            break;
          case END_OBJECT:
            checkStackState(ECommand.DICTIONARY_COMMAND);
            lexParser.closeDictionaryEvent();
            workerStack.pop();
            impliciteCloseProperty();
            break;
          case FIELD_NAME:
            checkStackState(ECommand.DICTIONARY_COMMAND);
            workerStack.push(ECommand.PROPERTY_COMMAND);
            lastKey = parser.getText();
            lexParser.openPropertyEvent(lastKey);
            break;
          default:
            LOGGER.warn("unkown token [" + nextToken + "] lastKey [" + lastKey + "] lastValue ["
                + lastValue + "].");
            break;
        }
        nextToken = parser.nextToken();
      }
      lexParser.finishEvent();
    } finally {
      jsonReader.close();
    }
  }

  private void checkIllegalStackState(ECommand illegalCommand, JsonToken token) {
    if (!workerStack.isEmpty() && workerStack.peek() == illegalCommand) {
      throw new IllegalStateException("Found illegal " + token + " inside " + illegalCommand);
    }
  }

  private void checkStackState(ECommand expectedCommand) {
    if (workerStack.isEmpty() || workerStack.peek() != expectedCommand) {
      throw new IllegalStateException("Expecting " + expectedCommand + " but found "
          + workerStack.peek() + " Last key [" + lastKey + "] last value [" + lastValue + "]");
    }
  }

  private void impliciteCloseProperty() {
    if (!workerStack.isEmpty() && workerStack.peek() == ECommand.PROPERTY_COMMAND) {
      lexParser.closePropertyEvent();
      workerStack.pop();
    }
  }

}
