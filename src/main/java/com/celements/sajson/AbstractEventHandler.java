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
