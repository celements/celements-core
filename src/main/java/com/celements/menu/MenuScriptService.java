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
package com.celements.menu;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.objects.BaseObject;

@Component("celMenu")
public class MenuScriptService implements ScriptService {

  @Requirement
  IMenuService menuService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<com.xpn.xwiki.api.Object> getMenuHeaders() {
    ArrayList<Object> menuHeaders = new ArrayList<com.xpn.xwiki.api.Object>();
    for (BaseObject bobj : menuService.getMenuHeaders()) {
      if (bobj != null) {
        menuHeaders.add(bobj.newObjectApi(bobj, getContext()));
      }
    }
    return menuHeaders;
  }

  public List<com.xpn.xwiki.api.Object> getSubMenuItems(Integer headerId) {
    ArrayList<Object> menuItems = new ArrayList<com.xpn.xwiki.api.Object>();
    for (BaseObject bobj : menuService.getSubMenuItems(headerId)) {
      if (bobj != null) {
        menuItems.add(bobj.newObjectApi(bobj, getContext()));
      }
    }
    return menuItems;
  }

}
