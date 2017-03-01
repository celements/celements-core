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
/**
 *
 */
package com.celements.web.contextmenu;

import com.celements.sajson.ECommand;
import com.celements.sajson.IGenericLiteral;

public enum ERequestLiteral implements IGenericLiteral {
  ELEMENT_ID(ECommand.VALUE_COMMAND),
  ELEM_IDS_ARRAY(ECommand.ARRAY_COMMAND, ELEMENT_ID),
  ELEM_ID_KEY(ECommand.PROPERTY_COMMAND, ELEM_IDS_ARRAY),
  CSS_CLASS_NAME_VALUE(ECommand.VALUE_COMMAND),
  CSS_CLASS_NAME(ECommand.PROPERTY_COMMAND, CSS_CLASS_NAME_VALUE),
  CSS_CLASS(ECommand.DICTIONARY_COMMAND, CSS_CLASS_NAME, ELEM_ID_KEY),
  REQUEST_ARRAY(ECommand.ARRAY_COMMAND, CSS_CLASS);

  private ERequestLiteral[] literals;
  private ECommand command;
  private int nextLiteral = 0;

  private ERequestLiteral(ECommand command, ERequestLiteral... literals) {
    this.literals = literals;
    this.command = command;
  }

  @Override
  public ECommand getCommand() {
    return command;
  }

  @Override
  public IGenericLiteral getNextLiteral() {
    nextLiteral = nextLiteral + 1;
    if (nextLiteral > literals.length) {
      return null;
    }
    return literals[nextLiteral - 1];
  }

  @Override
  public IGenericLiteral getFirstLiteral() {
    nextLiteral = 1;
    return literals[0];
  }

  @Override
  public IGenericLiteral getPropertyLiteralForKey(String key, IGenericLiteral placeholder) {
    return placeholder;
  }

}
