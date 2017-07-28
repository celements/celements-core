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
package com.celements.navigation.cmd;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.sajson.Parser;
import com.xpn.xwiki.XWikiContext;

public class ReorderSaveCommand {

  private final static Logger LOGGER = LoggerFactory.getLogger(ReorderSaveCommand.class);
  private ReorderSaveHandler injected_Handler;

  /**
   * For Tests only!!!
   *
   * @param injectedHandler
   */
  void injected_Handler(ReorderSaveHandler injectedHandler) {
    injected_Handler = injectedHandler;
  }

  ReorderSaveHandler getHandler() {
    if (injected_Handler != null) {
      return injected_Handler;
    }
    return new ReorderSaveHandler();
  }

  public String reorderSave(String fullName, String structureJSON, XWikiContext context) {
    ReorderSaveHandler handler = getHandler();
    Parser jsonParser = Parser.createLexicalParser(EReorderLiteral.REQUEST_ARRAY, handler);
    try {
      jsonParser.parse(structureJSON);
      return "OK";
    } catch (IOException exp) {
      LOGGER.error("Failed to save restructre.", exp);
    }
    return "Failed";
  }

}
