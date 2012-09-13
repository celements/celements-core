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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.IPartNameGetStrategy;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class GetNotMappedMenuItemsForParentCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      GetNotMappedMenuItemsForParentCommand.class);

  private HashMap<String, Map<String, List<TreeNode>>> menuItems;
  
  private int queryCount = 0;

  public GetNotMappedMenuItemsForParentCommand() {
    menuItems = new HashMap<String, Map<String, List<TreeNode>>>();
  }

  HashMap<String, Map<String, List<TreeNode>>> getMenuItemsCache() {
    return menuItems;
  }

  public List<TreeNode> getTreeNodesForParentKey(String searchParentKey,
      XWikiContext context) {
    String cacheKey = getCacheKey(searchParentKey, context);
    LOGGER.trace("getNotMappedMenuItemsFromDatabase: for cacheKey [" + cacheKey + "].");
    if (!menuItems.containsKey(cacheKey)) {
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
        List<Object[]> results = getFromDBForParentKey(searchParentKey, context);
        long end = System.currentTimeMillis();
        LOGGER.info("getNotMappedMenuItemsFromDatabase: time for searchDocumentsNames: "
            + (end-start));
        start = System.currentTimeMillis();
        for (Object[] docData : results) {
          docCount++;
          LOGGER.debug("got item from db: " + docData[0].toString());
          oldParentKey = parentKey;
          parentKey = getParentKey(docData[2].toString(), docData[1].toString(), context);
          if(!oldParentKey.equals(parentKey) || (menu == null)) {
            if (menu != null) {
              LOGGER.debug("put menu in cache for parent [" + oldParentKey + "]");
              wikiMenuItemsMap.put(oldParentKey, menu);
            }
            menu = getMenuCacheForParent(wikiMenuItemsMap, parentKey);
          }
          LOGGER.debug("put item [" + docData[0].toString() + "] in cache [" + parentKey
              + "]: ");
          if ((context.getDatabase() == null) || (docData[1].toString() == null) ||
              (docData[0].toString().split("\\.")[1] == null)
              || "".equals(context.getDatabase()) || "".equals(docData[1].toString()) ||
                  "".equals(docData[0].toString().split("\\.")[1])) {
            LOGGER.warn("getNotMappedMenuItemsFromDatabase: skip ["
                + docData[0].toString() + "] because of null value!! "
                + context.getDatabase() + ", " + docData[1].toString() + ", "
                + docData[0].toString().split("\\.")[1]);
          } else {
            TreeNode treeNode = new TreeNode(new DocumentReference(context.getDatabase(),
                docData[1].toString(), docData[0].toString().split("\\.")[1]),
                docData[2].toString(), (Integer) docData[3]);
            treeNode.setPartNameGetStrategy(new IPartNameGetStrategy() {
              
              public String getPartName(String fullName, XWikiContext context) {
                try {
                  XWikiDocument itemdoc = context.getWiki().getDocument(fullName, context);
                  BaseObject cobj = itemdoc.getObject("Celements2.MenuItem");
                  if(cobj != null) {
                    return cobj.getStringValue("part_name");
                  }
                } catch (XWikiException exp) {
                  LOGGER.error(exp);
                }
                return "";
              }
            });
            menu.add(treeNode);
          }
        }
        LOGGER.info(docCount + " documents found.");
        if (menu != null) {
          wikiMenuItemsMap.put(parentKey, menu);
        }
        end = System.currentTimeMillis();
        LOGGER.info("getNotMappedMenuItemsFromDatabase: time for building cache: "
            + (end-start));
        LOGGER.info("getNotMappedMenuItemsFromDatabase: cache size: "
            + wikiMenuItemsMap.size());
      } catch (XWikiException exp) {
        LOGGER.error("getSubMenuItemsForParent ", exp);
      }
      menuItems.put(cacheKey, wikiMenuItemsMap);
    }
    if ((menuItems.get(cacheKey) != null)
        && (menuItems.get(cacheKey).get(searchParentKey) != null)){
      return menuItems.get(cacheKey).get(searchParentKey);
    }
    return Collections.emptyList();
  }

  List<Object[]> getFromDBForParentKey(String parentKey, XWikiContext context)
      throws XWikiException {
    String databaseBefore = context.getDatabase();
    List<Object[]> results = executeSearch(parentKey, context);
    context.setDatabase(databaseBefore);
    return results;
  }

  List<Object[]> executeSearch(String parentKey, XWikiContext context)
      throws XWikiException {
    context.setDatabase(getWikiName(parentKey, context));
    String hql = getHQL();
    LOGGER.debug("Executing on db [" + context.getDatabase() + "] hql: " + hql);
    List<Object[]> results = context.getWiki().getStore().search(hql, 0, 0,
        context);
    return results;
  }

  String getHQL() {
    /*
     * select doc.XWD_FULLNAME from xwikidoc doc, xwikiobjects obj, xwikiintegers pos
     *  where obj.XWO_NAME=doc.XWD_FULLNAME and obj.XWO_CLASSNAME='Celements2.MenuItem'
     *   and obj.XWO_ID = pos.XWI_ID and pos.XWI_NAME = 'menu_position'
     *   and doc.XWD_TRANSLATION = 0 and doc.XWD_WEB <> 'Trash'
     *   order by doc.XWD_WEB, doc.XWD_PARENT, pos.XWI_VALUE;
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
      + " order by doc.space, doc.parent, pos.value";
  }

  String getCacheKey(String parentKey, XWikiContext context) {
    return getWikiName(parentKey, context);
  }

  private String getWikiName(String parentKey, XWikiContext context) {
    String wikiName = context.getDatabase();
    if (parentKey.contains(":")) {
      wikiName = parentKey.split(":", 2)[0];
    }
    return wikiName;
  }

  String getParentKey(String parent, String menuSpace, XWikiContext context) {
    String parentKey = "";
    if (parent != null) {
      parentKey = parent;
    }
    if(parentKey.indexOf('.') < 0) {
      parentKey = menuSpace + "." + parentKey;
    }
    if(parentKey.indexOf(':') < 0) {
      parentKey = context.getDatabase() + ":" + parentKey;
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
  public void flushMenuItemCache(XWikiContext context) {
    LOGGER.debug("Entered method flushMenuItemCache with context");
    menuItems.remove(context.getDatabase());
  }

  private List<TreeNode> getMenuCacheForParent(
      Map<String, List<TreeNode>> wikiMenuItemsMap, String parentKey) {
    if (wikiMenuItemsMap.containsKey(parentKey)) {
      return wikiMenuItemsMap.get(parentKey);
    } else {
      return new ArrayList<TreeNode>();
    }
  }
  
}
