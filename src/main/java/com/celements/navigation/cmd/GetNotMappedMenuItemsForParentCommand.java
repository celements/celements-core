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

import static com.celements.navigation.cmd.MenuItemsUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.TreeNode;
import com.google.common.base.MoreObjects;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class GetNotMappedMenuItemsForParentCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      GetNotMappedMenuItemsForParentCommand.class);

  private final Map<String, Map<String, List<TreeNode>>> menuItems;

  private int queryCount = 0;

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  public GetNotMappedMenuItemsForParentCommand() {
    /*
     * ConcurrentHashMap allows non synchronized reading and still prevents following
     * situation: Thread B changes the HashMap and Thread A does not synchronize on read
     * which may lead to Thread A reading an old version from its memory cache. Details
     * see JSR133
     */
    menuItems = new ConcurrentHashMap<>();
  }

  /**
   * use for tests only!!!
   */
  void injectMapForTests(String wikiName, Map<String, List<TreeNode>> myWikiTestMap) {
    menuItems.put(wikiName, myWikiTestMap);
  }

  Map<String, List<TreeNode>> internalGetMenuItemsForWiki(String wikiName) {
    return menuItems.get(wikiName);
  }

  /**
   * @deprecated instead use getTreeNodesForParentKey(String)
   */
  @Deprecated
  public List<TreeNode> getTreeNodesForParentKey(String searchParentKey, XWikiContext context) {
    return getTreeNodesForParentKey(searchParentKey);
  }

  public List<TreeNode> getTreeNodesForParentKey(String searchParentKey) {
    String wikiName = getWikiName(searchParentKey);
    LOGGER.trace("getNotMappedMenuItemsFromDatabase: for cacheKey [{}].", wikiName);
    Map<String, List<TreeNode>> wikiMenu = internalGetMenuItemsForWiki(wikiName);
    if (wikiMenu == null) {
      wikiMenu = loadMenuForWiki(wikiName);
    }
    if (wikiMenu.containsKey(searchParentKey)) {
      return wikiMenu.get(searchParentKey);
    }
    return Collections.emptyList();
  }

  synchronized Map<String, List<TreeNode>> loadMenuForWiki(String wikiName) {
    if (!menuItems.containsKey(wikiName)) {
      LOGGER.debug("loadMenuForWiki: loading for wikiName [{}].", wikiName);
      Map<String, List<TreeNode>> wikiMenuItemsMap = new HashMap<>();
      final MenuItemObjectPartNameGetter strategy = new MenuItemObjectPartNameGetter();
      queryCount = queryCount + 1;
      List<TreeNode> menu = null;
      try {
        String parentKey = "";
        String oldParentKey = "";
        int docCount = 0;
        long start = System.currentTimeMillis();
        List<Object[]> results = getFromDBForParentKey(wikiName);
        long end = System.currentTimeMillis();
        LOGGER.info("loadMenuForWiki: time for searchDocumentsNames: {} ", (end - start));
        start = System.currentTimeMillis();
        for (Object[] docData : results) {
          docCount++;
          oldParentKey = parentKey;
          // doc.fullName, doc.space, doc.parent, pos.value
          String fullName = MoreObjects.firstNonNull(docData[0], "").toString();
          String spaceName = MoreObjects.firstNonNull(docData[1], "").toString();
          String parentFN = MoreObjects.firstNonNull(docData[2], "").toString();
          LOGGER.debug("got item from db: {}", fullName);
          parentKey = getParentKey(wikiName, parentFN, spaceName);
          if (!oldParentKey.equals(parentKey) || (menu == null)) {
            if (menu != null) {
              LOGGER.debug("put menu in cache for parent [{}]", oldParentKey);
              wikiMenuItemsMap.put(oldParentKey, menu);
            }
            menu = getMenuCacheForParent(wikiMenuItemsMap, parentKey);
          }
          LOGGER.debug("put item [{}] in cache [{}]: ", fullName, parentKey);
          if ((wikiName == null) || (fullName.split("\\.").length < 2) || wikiName.isEmpty()
              || spaceName.isEmpty() || ("".equals(fullName.split("\\.")[1]))) {
            LOGGER.warn("loadMenuForWiki: skip [{}] because of null value!! " + "'{}', '{}', '{}'",
                fullName, wikiName, spaceName, fullName.split("\\.")[1]);
          } else {
            DocumentReference docRef = new DocumentReference(wikiName, spaceName, fullName.split(
                "\\.")[1]);
            TreeNode treeNode = new TreeNode(docRef, resolveParentRef(parentFN),
                (Integer) docData[3], strategy);
            menu.add(treeNode);
          }
        }
        LOGGER.info("loadMenuForWiki: '{}' documents found.", docCount);
        if (menu != null) {
          wikiMenuItemsMap.put(parentKey, menu);
        }
        for (String theParentKey : wikiMenuItemsMap.keySet()) {
          List<TreeNode> theMenu = wikiMenuItemsMap.get(theParentKey);
          wikiMenuItemsMap.put(theParentKey, Collections.unmodifiableList(theMenu));
        }
        end = System.currentTimeMillis();
        LOGGER.info("loadMenuForWiki: time for building cache: {} ", (end - start));
        LOGGER.info("loadMenuForWiki: cache size: {}", wikiMenuItemsMap.size());
        menuItems.put(wikiName, Collections.unmodifiableMap(wikiMenuItemsMap));
      } catch (XWikiException exp) {
        LOGGER.error("loadMenuForWiki failed. ", exp);
      }
    }
    return internalGetMenuItemsForWiki(wikiName);
  }

  List<Object[]> getFromDBForParentKey(String wikiName) throws XWikiException {
    String databaseBefore = getContext().getDatabase();
    try {
      List<Object[]> results = executeSearch(wikiName);
      return results;
    } finally {
      getContext().setDatabase(databaseBefore);
    }
  }

  List<Object[]> executeSearch(String wikiName) throws XWikiException {
    getContext().setDatabase(wikiName);
    String hql = getHQL();
    LOGGER.debug("Executing on db [{}] hql: {}", getContext().getDatabase(), hql);
    List<Object[]> results = getContext().getWiki().getStore().search(hql, 0, 0, getContext());
    return results;
  }

  String getHQL() {
    /*
     * select doc.XWD_FULLNAME from xwikidoc doc, xwikiobjects obj, xwikiintegers pos
     * where obj.XWO_NAME=doc.XWD_FULLNAME and obj.XWO_CLASSNAME='Celements2.MenuItem' and
     * obj.XWO_ID = pos.XWI_ID and pos.XWI_NAME = 'menu_position' and doc.XWD_TRANSLATION
     * = 0 and doc.XWD_WEB <> 'Trash' order by doc.XWD_PARENT, pos.XWI_VALUE; -> executed
     * in onecprod_customizing ->1061 rows in set (0.20 sec)
     */
    return "select doc.fullName, doc.space, doc.parent, pos.value"
        + " from XWikiDocument as doc, BaseObject as obj, IntegerProperty as pos"
        + " where obj.name=doc.fullName" + " and obj.className='Celements2.MenuItem'"
        + " and obj.id = pos.id.id" + " and pos.id.name = 'menu_position'"
        + " and doc.translation = 0" + " and doc.space <> 'Trash'"
        + " order by doc.parent, pos.value";
  }

  String getWikiCacheKey(String parentKey) {
    return getWikiName(parentKey);
  }

  private String getWikiName(String parentKey) {
    String wikiName = getContext().getDatabase();
    if (parentKey.contains(":")) {
      wikiName = parentKey.split(":", 2)[0];
    }
    return wikiName;
  }

  String getParentKey(String wikiName, String parent, String menuSpace) {
    String parentKey = "";
    if (parent != null) {
      parentKey = parent;
    }
    if (parentKey.indexOf('.') < 0) {
      parentKey = menuSpace + "." + parentKey;
    }
    if (parentKey.indexOf(':') < 0) {
      parentKey = wikiName + ":" + parentKey;
    }
    return parentKey;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.web.utils.IWebUtils#queryCount()
   */
  public int queryCount() {
    return queryCount;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.celements.web.utils.IWebUtils#flushMenuItemCache(com.xpn.xwiki.XWikiContext)
   */
  synchronized public void flushMenuItemCache(XWikiContext context) {
    /*
     * flushMenuItemCache must be synchronized to prevent following situation: 1) Thread A
     * starts loadMenuForWiki for myWiki and gets current state from DB 2) Thread B
     * changed a document and flushes menuItems for myWiki 3) Thread A stores already old
     * version of myWiki in menuItems cache
     */
    if (context != null) {
      LOGGER.debug("Entered method flushMenuItemCache with context db [{}].",
          context.getDatabase());
      menuItems.remove(context.getDatabase());
    } else {
      LOGGER.warn("skip flushMenuItemCache for context == null].");
    }
  }

  private List<TreeNode> getMenuCacheForParent(Map<String, List<TreeNode>> wikiMenuItemsMap,
      String parentKey) {
    if (wikiMenuItemsMap.containsKey(parentKey)) {
      return wikiMenuItemsMap.get(parentKey);
    } else {
      return new ArrayList<>(20);
    }
  }

}
