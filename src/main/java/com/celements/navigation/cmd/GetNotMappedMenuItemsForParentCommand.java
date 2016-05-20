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

import com.celements.navigation.IPartNameGetStrategy;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class GetNotMappedMenuItemsForParentCommand {

  private static Logger LOGGER = LoggerFactory.getLogger(
      GetNotMappedMenuItemsForParentCommand.class);

  private final Map<String, Map<String, List<TreeNode>>> menuItems;
  
  private int queryCount = 0;

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext(
        ).getProperty("xwikicontext");
  }

  public GetNotMappedMenuItemsForParentCommand() {
    /* ConcurrentHashMap is needed to prevent:
     * 1) ConcurrentModificationException if Thread A iterates over menuItems and
     *    Thread B is changing it.
     * 2) Thread B changed a document and flushes menuItems for myWiki
     * 3) Thread A stores already old version of myWiki in menuItems cache
     */
    menuItems = new ConcurrentHashMap<String, Map<String, List<TreeNode>>>();
  }

  Map<String, Map<String, List<TreeNode>>> getMenuItemsCache() {
    return menuItems;
  }

  /**
   * @deprecated instead use getTreeNodesForParentKey(String
   */
  @Deprecated
  public List<TreeNode> getTreeNodesForParentKey(String searchParentKey,
      XWikiContext context) {
    return getTreeNodesForParentKey(searchParentKey);
  }

  public List<TreeNode> getTreeNodesForParentKey(String searchParentKey) {
    String wikiCacheKey = getWikiCacheKey(searchParentKey);
    LOGGER.trace("getNotMappedMenuItemsFromDatabase: for cacheKey [{}].", wikiCacheKey);
    if (!menuItems.containsKey(wikiCacheKey)) {
      loadMenuForWiki(searchParentKey);
    }
    if (menuItems.containsKey(wikiCacheKey) && (menuItems.get(wikiCacheKey) != null)
        && (menuItems.get(wikiCacheKey).get(searchParentKey) != null)){
      return menuItems.get(wikiCacheKey).get(searchParentKey);
    }
    return Collections.emptyList();
  }

  synchronized private void loadMenuForWiki(String wikiCacheKey) {
    if (!menuItems.containsKey(wikiCacheKey)) {
      LOGGER.debug("loadMenuForWiki: loading for wikiCacheKey [{}].", wikiCacheKey);
      Map<String, List<TreeNode>> wikiMenuItemsMap =
          new HashMap<String, List<TreeNode>>();
      queryCount = queryCount + 1;
      List<TreeNode> menu = null;
      try {
        //TODO: check if it is ok, that we can get documents from other
        //      spaces than the one of the parent.
        String parentKey = "";
        String oldParentKey = "";
        int docCount = 0;
        long start = System.currentTimeMillis();
        String wikiName = getWikiName(wikiCacheKey);
        List<Object[]> results = getFromDBForParentKey(wikiName);
        long end = System.currentTimeMillis();
        LOGGER.info("loadMenuForWiki: time for searchDocumentsNames: {} ", (end-start));
        start = System.currentTimeMillis();
        for (Object[] docData : results) {
          docCount++;
          LOGGER.debug("got item from db: {}", docData[0].toString());
          oldParentKey = parentKey;
          parentKey = getParentKey(wikiName, docData[2].toString(), docData[1].toString());
          if (!oldParentKey.equals(parentKey) || (menu == null)) {
            if (menu != null) {
              LOGGER.debug("put menu in cache for parent [{}]", oldParentKey);
              wikiMenuItemsMap.put(oldParentKey, menu);
            }
            menu = getMenuCacheForParent(wikiMenuItemsMap, parentKey);
          }
          LOGGER.debug("put item [{}] in cache [{}]: ", docData[0].toString(), parentKey);
          if ((wikiName == null) || (docData[1].toString() == null) ||
              (docData[0].toString().split("\\.")[1] == null)
              || "".equals(wikiName) || "".equals(docData[1].toString()) ||
                  "".equals(docData[0].toString().split("\\.")[1])) {
            LOGGER.warn("loadMenuForWiki: skip [{}] because of null value!! "
                + "'{}', '{}', '{}'", docData[0].toString(), wikiName,
                docData[1].toString(), docData[0].toString().split("\\.")[1]);
          } else {
            TreeNode treeNode = new TreeNode(new DocumentReference(wikiName,
                docData[1].toString(), docData[0].toString().split("\\.")[1]),
                docData[2].toString(), (Integer) docData[3]);
            treeNode.setPartNameGetStrategy(new IPartNameGetStrategy() {
              
              public String getPartName(String fullName, XWikiContext context) {
                try {
                  XWikiDocument itemdoc = context.getWiki().getDocument(fullName, context);
                  BaseObject cobj = itemdoc.getObject("Celements2.MenuItem");
                  if (cobj != null) {
                    return cobj.getStringValue("part_name");
                  }
                } catch (XWikiException exp) {
                  LOGGER.error("getPartName failed for '{}'.", fullName, exp);
                }
                return "";
              }
            });
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
        LOGGER.info("loadMenuForWiki: time for building cache: {} ", (end-start));
        LOGGER.info("loadMenuForWiki: cache size: {}", wikiMenuItemsMap.size());
        menuItems.put(wikiCacheKey, Collections.unmodifiableMap(wikiMenuItemsMap));
      } catch (XWikiException exp) {
        LOGGER.error("loadMenuForWiki failed. ", exp);
      }
    }
  }

  List<Object[]> getFromDBForParentKey(String wikiName)
      throws XWikiException {
    String databaseBefore = getContext().getDatabase();
    try {
      List<Object[]> results = executeSearch(wikiName);
      return results;
    } finally {
      getContext().setDatabase(databaseBefore);
    }
  }

  List<Object[]> executeSearch(String wikiName)
      throws XWikiException {
    getContext().setDatabase(wikiName);
    String hql = getHQL();
    LOGGER.debug("Executing on db [{}] hql: {}", getContext().getDatabase(), hql);
    List<Object[]> results = getContext().getWiki().getStore().search(hql, 0, 0,
        getContext());
    return results;
  }

  String getHQL() {
    /*
     * select doc.XWD_FULLNAME from xwikidoc doc, xwikiobjects obj, xwikiintegers pos
     *  where obj.XWO_NAME=doc.XWD_FULLNAME and obj.XWO_CLASSNAME='Celements2.MenuItem'
     *   and obj.XWO_ID = pos.XWI_ID and pos.XWI_NAME = 'menu_position'
     *   and doc.XWD_TRANSLATION = 0 and doc.XWD_WEB <> 'Trash'
     *   order by doc.XWD_PARENT, pos.XWI_VALUE;
     *   -> executed in onecprod_customizing
     *   ->1061 rows in set (0.20 sec) 
     */
    return "select doc.fullName, doc.space, doc.parent, pos.value"
      + " from XWikiDocument as doc, BaseObject as obj, IntegerProperty as pos"
      + " where obj.name=doc.fullName"
      + " and obj.className='Celements2.MenuItem'"
      + " and obj.id = pos.id.id"
      + " and pos.id.name = 'menu_position'"
      + " and doc.translation = 0"
      + " and doc.space <> 'Trash'"
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
    if(parentKey.indexOf('.') < 0) {
      parentKey = menuSpace + "." + parentKey;
    }
    if(parentKey.indexOf(':') < 0) {
      parentKey = wikiName + ":" + parentKey;
    }
    return parentKey;
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#queryCount()
   */
  public int queryCount() {
    return queryCount;
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#flushMenuItemCache(com.xpn.xwiki.XWikiContext)
   */
  synchronized public void flushMenuItemCache(XWikiContext context) {
    /* flushMenuItemCache must be synchronized to prevent following situation:
     * 1) Thread A starts loadMenuForWiki for myWiki and gets current state from DB
     * 2) Thread B changed a document and flushes menuItems for myWiki
     * 3) Thread A stores already old version of myWiki in menuItems cache
     */
    if (context != null) {
      LOGGER.debug("Entered method flushMenuItemCache with context db [{}].",
          context.getDatabase());
      menuItems.remove(context.getDatabase());
    } else {
      LOGGER.warn("skip flushMenuItemCache for context == null].");
    }
  }

  private List<TreeNode> getMenuCacheForParent(
      Map<String, List<TreeNode>> wikiMenuItemsMap, String parentKey) {
    if (wikiMenuItemsMap.containsKey(parentKey)) {
      return wikiMenuItemsMap.get(parentKey);
    } else {
      return new ArrayList<TreeNode>(20);
    }
  }
  
}
