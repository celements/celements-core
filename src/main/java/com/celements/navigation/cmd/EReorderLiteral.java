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
package com.celements.navigation.cmd;

import com.celements.web.sajson.ECommand;
import com.celements.web.sajson.IGenericLiteral;

public enum EReorderLiteral implements IGenericLiteral {
  ELEMENT_ID(ECommand.VALUE_COMMAND),
  ELEM_IDS_ARRAY(ECommand.ARRAY_COMMAND, ELEMENT_ID),
  PARENT_CHILDREN_PROPERTY(ECommand.PROPERTY_COMMAND, ELEM_IDS_ARRAY),
  REQUEST_DICT(ECommand.DICTIONARY_COMMAND, PARENT_CHILDREN_PROPERTY),
  REQUEST_ARRAY(ECommand.ARRAY_COMMAND, REQUEST_DICT);
  
  
  private EReorderLiteral[] literals;
  private ECommand command;
  private int nextLiteral = 0;

  private EReorderLiteral(ECommand command, EReorderLiteral... literals) {
    this.literals = literals;
    this.command = command;
  }

  public ECommand getCommand() {
    return command;
  }

  public IGenericLiteral getNextLiteral() {
    nextLiteral = nextLiteral + 1;
    if (nextLiteral > literals.length) {
      return null;
    }
    return literals[nextLiteral - 1];
  }

  public IGenericLiteral getFirstLiteral() {
    nextLiteral = 1;
    return literals[0];
  }
}