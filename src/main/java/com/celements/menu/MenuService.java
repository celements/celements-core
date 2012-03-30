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
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class MenuService implements IMenuService {

  private static Log mLogger = LogFactory.getFactory().getInstance(MenuClasses.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<BaseObject> getMenuHeaders() {
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    addMenuHeaders(menuHeadersMap);
    getContext().setDatabase("celements2web");
    addMenuHeaders(menuHeadersMap);
    getContext().setDatabase(getContext().getOriginalDatabase());
    ArrayList<BaseObject> resultList = new ArrayList<BaseObject>();
    resultList.addAll(menuHeadersMap.values());
    if (mLogger.isDebugEnabled()) {
      mLogger.debug("getMenuHeaders returning: "
        + Arrays.deepToString(resultList.toArray()));
    }
    return resultList;
  }

  boolean hasview(String menuDocFullName) throws XWikiException {
    String database = getContext().getDatabase();
    getContext().setDatabase("celements2web");
    boolean centralView = !getContext().getWiki().exists(menuDocFullName, getContext())
      || getContext().getWiki().getRightService().hasAccessLevel("view",
          getContext().getUser(), menuDocFullName, getContext());
    getContext().setDatabase(getContext().getOriginalDatabase());
    boolean localView = !getContext().getWiki().exists(menuDocFullName, getContext())
      || getContext().getWiki().getRightService().hasAccessLevel("view",
          getContext().getUser(), menuDocFullName, getContext());
    getContext().setDatabase(database);
    return centralView && localView;
  }

  void addMenuHeaders(SortedMap<Integer, BaseObject> menuHeadersMap) {
    try {
      List<String> result = getContext().getWiki().search(getHeadersHQL(), getContext());
      if (mLogger.isDebugEnabled()) {
        mLogger.debug("addMenuHeaders reseved for " + getContext().getDatabase()
          + ": " + Arrays.deepToString(result.toArray()));
      }
      for(String fullName : result) {
        if (hasview(fullName)) {
          for(BaseObject obj : getContext().getWiki().getDocument(fullName, getContext()
              ).getObjects("Celements.MenuBarHeaderItemClass")) {
            menuHeadersMap.put(obj.getIntValue("pos"), obj);
          }
        }
      }
    } catch (XWikiException e) {
      mLogger.error(e);
    }
  }

  String getHeadersHQL() {
    return "select obj.name"
        + " from BaseObject obj"
        + " where obj.className = 'Celements.MenuBarHeaderItemClass'";
  }

}
