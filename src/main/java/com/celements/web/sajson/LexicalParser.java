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

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LexicalParser<T extends IGenericLiteral>
    implements ILexicalParser<T> {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      LexicalParser.class);

  private Stack<T> workerStack = new Stack<T>();
  private T startLiteral;
  private IEventHandler<T> eventHandler;
  
  public LexicalParser(T startLiteral, IEventHandler<T> eventHandler) {
    this.startLiteral = startLiteral;
    this.eventHandler = eventHandler;
  }

  final public void initEvent() {
    workerStack.clear();
    workerStack.push(startLiteral);
    mLogger.debug("init " + startLiteral + " ; " + startLiteral.getCommand());
  }

  final public void openArrayEvent() {
    checkStackState(ECommand.ARRAY_COMMAND);
    openLiteral();
  }

  final public void closeArrayEvent() {
    if (workerStack.peek().getCommand() != ECommand.ARRAY_COMMAND) {
      T lastLiteral = workerStack.pop();
      mLogger.debug("close: " + lastLiteral + " ; " + lastLiteral.getCommand());
    }
    checkStackState(ECommand.ARRAY_COMMAND);
    closeLiteral();
  }

  final public void openDictionaryEvent() {
    checkStackState(ECommand.DICTIONARY_COMMAND);
    openLiteral();
  }
  
  public void closeDictionaryEvent() {
    if (workerStack.peek().getCommand() == ECommand.PROPERTY_COMMAND) {
      workerStack.pop(); // remove optional property command
    }
    checkStackState(ECommand.DICTIONARY_COMMAND);
    closeLiteral();
  }

  public void closePropertyEvent() {
    checkStackState(ECommand.PROPERTY_COMMAND);
    closeLiteral(); // close PROPERTY_COMMAND
    checkStackState(ECommand.DICTIONARY_COMMAND);
    advanceToNextDictionaryProperty();
  }

  public void openPropertyEvent(String key) {
    checkStackState(ECommand.PROPERTY_COMMAND);
    mLogger.debug("key: " + key + " ");
    fixPropertyLiteralOnStack(key);
    checkStackState(ECommand.PROPERTY_COMMAND);
    openLiteral();
    eventHandler.readPropertyKey(key);
  }

  /**
   * important to allow unordered property lists in dictionaries / objects
   * (see www.json.org : An object is an unordered set of name/value pairs. )
   * @param key
   */
  @SuppressWarnings("unchecked")
  private void fixPropertyLiteralOnStack(String key) {
    T placeholder = workerStack.pop(); // remove placeholder property-literal
    checkStackState(ECommand.DICTIONARY_COMMAND);
    T nextLiteral = (T) workerStack.peek().getPropertyLiteralForKey(key, placeholder);
    if (nextLiteral != null) {
      workerStack.push(nextLiteral);
      mLogger.debug("fix property literal on stack: " + workerStack.peek()
          + " ; " + workerStack.peek().getCommand());
    } else {
      throw new IllegalStateException("illegal key value [" + key + "] in dictionary ["
          + workerStack.peek() + "]. Stack: " + workerStack.toString());
    }
  }

  public void stringEvent(String value) {
    checkStackState(ECommand.VALUE_COMMAND);
    eventHandler.openEvent(workerStack.peek());
    eventHandler.stringEvent(value);
    mLogger.debug("string-value: " + value + " ");
    closeLiteral(); // close VALUE_COMMAND
  }
  
  public void booleanEvent(boolean value) {
    checkStackState(ECommand.VALUE_COMMAND);
    eventHandler.openEvent(workerStack.peek());
    eventHandler.booleanEvent(value);
    mLogger.debug("boolean-value: " + value + " ");
    closeLiteral(); // close VALUE_COMMAND
  }

  final public void finishEvent() {
    if (!workerStack.isEmpty()) {
      throw new IllegalStateException("SyntaxError: finishEvent on nonempty"
          + " stack:" + workerStack.peek());
    }
  }

  final private void checkStackState(ECommand expectedCommand) {
    if (!workerStack.isEmpty()
        && (workerStack.peek().getCommand() != expectedCommand)
        && (workerStack.peek().getCommand() == ECommand.ARRAY_COMMAND)) {
      openLiteral();
      mLogger.debug("reopen " + workerStack.peek() + " ; "
          + workerStack.peek().getCommand());
    }
    if (workerStack.isEmpty()
        || (workerStack.peek().getCommand() != expectedCommand)) {
      throw new IllegalStateException("Expecting: "
          + workerStack.peek().getCommand() + " for " + workerStack.peek()
          + " but received " + expectedCommand + ". "
          + "Stack: " + workerStack.toString());
    }
  }
  
  @SuppressWarnings("unchecked")
  private void advanceToNextDictionaryProperty() {
    T nextLiteral = (T) workerStack.peek().getNextLiteral();
    if (nextLiteral != null) {
      workerStack.push(nextLiteral);
      mLogger.debug("advance open: " + workerStack.peek()
          + " ; " + workerStack.peek().getCommand());
    }
  }

  @SuppressWarnings("unchecked")
  private void openLiteral() {
    T currentLiteral = workerStack.peek();
    workerStack.push((T)currentLiteral.getFirstLiteral());
    eventHandler.openEvent(currentLiteral);
    mLogger.debug("open " + currentLiteral + " ; " + currentLiteral.getCommand());
  }

  private void closeLiteral() {
    T lastLiteral = workerStack.pop();
    mLogger.debug("close: " + lastLiteral + " ; " + lastLiteral.getCommand());
    eventHandler.closeEvent(lastLiteral);
  }

}
