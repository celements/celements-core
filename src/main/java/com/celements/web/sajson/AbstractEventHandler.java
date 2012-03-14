package com.celements.web.sajson;

/**
 * AbstractEventHandler defines for all "Value-Events" an default implementation
 * which throws an IllegalArgumentException. It is assumed that a concrete implementation
 * overwrites the value-event methods which are expected and can be handled.
 * 
 * @author fabian
 *
 * @param <T>
 */
public abstract class AbstractEventHandler<T extends IGenericLiteral>
  implements IEventHandler<T> {

  public void stringEvent(String value) {
    throw new IllegalArgumentException("received unsupported stringEvent (value: ["
        + value + "].");
  }

  public void booleanEvent(boolean value) {
    throw new IllegalArgumentException("received unsupported booleanEvent (value: ["
        + value + "].");
  }

}
