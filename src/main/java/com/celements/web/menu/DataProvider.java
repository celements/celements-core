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
package com.celements.web.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

public class DataProvider {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      MenuClasses.class);
  private static DataProvider instance;

  private DataProvider() {}
  
  public static DataProvider getInstance() {
    if (instance == null) {
      instance = new DataProvider();
    }
    return instance;
  }

  public List<BaseObject> getMenuHeaders(XWikiContext context) {
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    addMenuHeaders(menuHeadersMap, context);
    context.setDatabase("celements2web");
    addMenuHeaders(menuHeadersMap, context);
    context.setDatabase(context.getOriginalDatabase());
    ArrayList<BaseObject> resultList = new ArrayList<BaseObject>();
    resultList.addAll(menuHeadersMap.values());
    if (mLogger.isDebugEnabled()) {
      mLogger.debug("getMenuHeaders returning: "
        + Arrays.deepToString(resultList.toArray()));
    }
    return resultList;
  }

  boolean hasview(String menuDocFullName, XWikiContext context
      ) throws XWikiException {
    String database = context.getDatabase();
    context.setDatabase("celements2web");
    boolean centralView = !context.getWiki().exists(menuDocFullName, context)
      || context.getWiki().getRightService().hasAccessLevel("view", context.getUser(),
        menuDocFullName, context);
    context.setDatabase(context.getOriginalDatabase());
    boolean localView = !context.getWiki().exists(menuDocFullName, context)
      || context.getWiki().getRightService().hasAccessLevel("view", context.getUser(),
        menuDocFullName, context);
    context.setDatabase(database);
    return centralView && localView;
  }

  void addMenuHeaders(SortedMap<Integer, BaseObject> menuHeadersMap,
      XWikiContext context) {
    try {
      List<String> result = context.getWiki().search(getHeadersHQL(), context);
      if (mLogger.isDebugEnabled()) {
        mLogger.debug("addMenuHeaders reseved for " + context.getDatabase()
          + ": " + Arrays.deepToString(result.toArray()));
      }
      for(String fullName : result) {
        if (hasview(fullName, context)) {
          for(BaseObject obj : context.getWiki().getDocument(fullName,
              context).getObjects("Celements.MenuBarHeaderItemClass")) {
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
