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

import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.XWikiContext;

public class ContextMenuBuilderApi extends com.xpn.xwiki.api.Api {

  private ContextMenuBuilder cmiBuilder;

  public ContextMenuBuilderApi(XWikiContext context) {
    super(context);
    cmiBuilder = new ContextMenuBuilder();
  }

  public List<ContextMenuItemApi> getCMItemsForClassAndId(String className, String elemId) {
    List<ContextMenuItemApi> cmiList = new ArrayList<>();
    for (ContextMenuItem cmiObj : cmiBuilder.getCMItems(className, elemId)) {
      cmiList.add(new ContextMenuItemApi(cmiObj, context));
    }
    return cmiList;
  }

  public void addElementsCMforClassNames(String jsonDictionary) {
    cmiBuilder.addElementsCMforClassNames(jsonDictionary);
  }

  public String getCMIjson() {
    return cmiBuilder.getJson();
  }

}
