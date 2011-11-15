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
package com.celements.web.contextmenu;

import java.util.HashMap;

import com.celements.web.sajson.Builder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

public class ContextMenuItemApi extends Api {

  private static HashMap<XWikiContext, ContextMenuItemApi> 
    contextMenuItemApiRegistry = new HashMap<XWikiContext, ContextMenuItemApi>();
  private ContextMenuItem contextMenuItem;
  
  public ContextMenuItemApi(com.xpn.xwiki.api.Object menuItem, String elemId,
      XWikiContext context) {
    this(new ContextMenuItem(menuItem.getXWikiObject(), elemId, context), context);
  }

  public ContextMenuItemApi(ContextMenuItem contextMenuItem,
      XWikiContext context) {
    super(context);
    this.contextMenuItem = contextMenuItem;
  }

  public static ContextMenuItemApi getAPIObject(ContextMenuItem contextMenuItem,
      XWikiContext context) {
    //FIXME possible memory leak if registry is infinitly growing!!!
      if (!contextMenuItemApiRegistry.containsKey(contextMenuItem)
          && (contextMenuItem != null)) {
        contextMenuItemApiRegistry.put(context,
            new ContextMenuItemApi(contextMenuItem, context));
      }
      return contextMenuItemApiRegistry.get(contextMenuItem);
  }

  public String toJSON() {
    Builder jsonBuilder = new Builder();
    contextMenuItem.generateJSON(jsonBuilder);
    return jsonBuilder.getJSON();
  }

}
