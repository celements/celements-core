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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;

import com.celements.sajson.Parser;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;

public class ReorderSaveCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      ReorderSaveCommand.class);
  private ReorderSaveHandler injected_Handler;
  private IWebUtils injected_WebUtils;

  /**
   * For Tests only!!!
   * @param injectedHandler
   */
  void injected_Handler(ReorderSaveHandler injectedHandler) {
    injected_Handler = injectedHandler;
  }

  ReorderSaveHandler getHandler(XWikiContext context) {
    if (injected_Handler != null) {
      return injected_Handler;
    }
    return new ReorderSaveHandler(context);
  }

  public String reorderSave(String fullName, String structureJSON,
      XWikiContext context) {
    ReorderSaveHandler handler = getHandler(context);
    Parser jsonParser = Parser.createLexicalParser(EReorderLiteral.REQUEST_ARRAY,
        handler);
    try {
      jsonParser.parse(structureJSON);
      if (handler.isFlushCacheNeeded()) {
        getWebUtils().flushMenuItemCache(context);
      }
      return "OK";
    } catch (JsonParseException exp) {
      mLogger.error("Failed to save restructre.", exp);
    } catch (IOException exp) {
      mLogger.error("Failed to save restructre.", exp);
    }
    return "Failed";
  }

  IWebUtils getWebUtils() {
    if (injected_WebUtils != null) {
      return injected_WebUtils;
    }
    return WebUtils.getInstance();
  }

  /**
   * For Tests only!!!
   * @param injected_WebUtils
   */
  void injected_WebUtils(IWebUtils injected_WebUtils) {
    this.injected_WebUtils = injected_WebUtils;
  }

}
