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
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class MenuService implements IMenuService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(MenuService.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  @Requirement
  QueryManager queryManager;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<BaseObject> getMenuHeaders() {
    LOGGER.trace("getMenuHeaders_internal start");
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    addMenuHeaders(menuHeadersMap);
    getContext().setDatabase("celements2web");
    addMenuHeaders(menuHeadersMap);
    getContext().setDatabase(getContext().getOriginalDatabase());
    ArrayList<BaseObject> resultList = new ArrayList<BaseObject>();
    resultList.addAll(menuHeadersMap.values());
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("getMenuHeaders_internal returning: "
        + Arrays.deepToString(resultList.toArray()));
    }
    LOGGER.debug("getMenuHeaders_internal end");
    return resultList;
  }

  boolean hasview(DocumentReference menuBarDocRef) throws XWikiException {
    String database = getContext().getDatabase();
    getContext().setDatabase(getContext().getOriginalDatabase());
    if (webUtilsService.getRefDefaultSerializer().serialize(menuBarDocRef
        ).endsWith("Celements2.AdminMenu")) {
      LOGGER.debug("hasview: AdminMenu [" + getContext().getUser() + "] isAdvancedAdmin ["
          + webUtilsService.isAdvancedAdmin() + "].");
      return webUtilsService.isAdvancedAdmin();
    }
    getContext().setDatabase("celements2web");
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        menuBarDocRef.getLastSpaceReference().getName(), menuBarDocRef.getName());
    String menuBar2webFullName = webUtilsService.getRefDefaultSerializer(
        ).serialize(menuBar2webDocRef);
    boolean centralView = !getContext().getWiki().exists(menuBar2webDocRef, getContext())
      || getContext().getWiki().getRightService().hasAccessLevel("view",
          getContext().getUser(), menuBar2webFullName, getContext());
    LOGGER.debug("hasview: centralView [" + menuBar2webFullName + "] for ["
        + getContext().getUser() + "] -> [" + centralView + "] on database ["
        + getContext().getDatabase() + "].");
    getContext().setDatabase(getContext().getOriginalDatabase());
    DocumentReference menuBarLocalDocRef = new DocumentReference(getContext(
        ).getOriginalDatabase(), menuBarDocRef.getLastSpaceReference().getName(),
        menuBarDocRef.getName());
    String menuBarFullName = webUtilsService.getRefDefaultSerializer(
        ).serialize(menuBarLocalDocRef);
    boolean localView = !getContext().getWiki().exists(menuBarLocalDocRef, getContext())
      || getContext().getWiki().getRightService().hasAccessLevel("view",
          getContext().getUser(), menuBarFullName, getContext());
    LOGGER.debug("hasview: localView [" + menuBarFullName + "] for ["
        + getContext().getUser() + "] -> [" + localView + "] on database ["
        + getContext().getDatabase() + "].");
    getContext().setDatabase(database);
    return centralView && localView;
  }

  void addMenuHeaders(SortedMap<Integer, BaseObject> menuHeadersMap) {
    try {
      List<String> result = queryManager.createQuery(getHeadersXWQL(), Query.XWQL
          ).execute();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("addMenuHeaders received for " + getContext().getDatabase()
          + ": " + Arrays.deepToString(result.toArray()));
      }
      for(String fullName : new HashSet<String>(result)) {
        DocumentReference menuBarDocRef = webUtilsService.resolveDocumentReference(
            fullName);
        if (hasview(menuBarDocRef)) {
          List<BaseObject> headerObjList = getContext().getWiki().getDocument(
              menuBarDocRef, getContext()).getXObjects(getMenuBarHeaderClassRef(
                  menuBarDocRef.getWikiReference().getName()));
          LOGGER.trace("addMenuHeaders: hasview for [" + 
              webUtilsService.getRefDefaultSerializer().serialize(menuBarDocRef) + 
              "] adding items [" + ((headerObjList != null) ?headerObjList.size() : "null"
              ) + ".");
          if (headerObjList != null) {
            for (BaseObject obj : headerObjList) {
              menuHeadersMap.put(obj.getIntValue("pos"), obj);
            }
          }
        } else {
          LOGGER.trace("addMenuHeaders: NO hasview for [" + 
              webUtilsService.getRefDefaultSerializer().serialize(menuBarDocRef) + "].");
        }
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    } catch (QueryException e) {
      LOGGER.error(e);
    }
  }

  public DocumentReference getMenuBarHeaderClassRef() {
    return getMenuBarHeaderClassRef(getContext().getDatabase());
  }

  public DocumentReference getMenuBarHeaderClassRef(String database) {
    return new DocumentReference(database, "Celements", "MenuBarHeaderItemClass");
  }


  public DocumentReference getMenuBarSubItemClassRef() {
    return getMenuBarSubItemClassRef(getContext().getDatabase());
  }

  public DocumentReference getMenuBarSubItemClassRef(String database) {
    return new DocumentReference(database, "Celements", "MenuBarSubItemClass");
  }

  String getHeadersXWQL() {
    return "from doc.object(Celements.MenuBarHeaderItemClass) as mHeader";
  }

  public List<BaseObject> getSubMenuItems(Integer headerId) {
    TreeMap<Integer, BaseObject> menuItemsMap = new TreeMap<Integer, BaseObject>();
    addMenuItems(menuItemsMap, headerId);
    getContext().setDatabase("celements2web");
    addMenuItems(menuItemsMap, headerId);
    getContext().setDatabase(getContext().getOriginalDatabase());
    ArrayList<BaseObject> resultList = new ArrayList<BaseObject>();
    resultList.addAll(menuItemsMap.values());
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("getSubMenuItems_internal returning: "
        + Arrays.deepToString(resultList.toArray()));
    }
    LOGGER.debug("getSubMenuItems_internal end");
    return resultList;
  }

  private void addMenuItems(TreeMap<Integer, BaseObject> menuItemsMap, Integer headerId) {
    try {
      List<Object[]> result = queryManager.createQuery(getSubItemsXWQL(), Query.XWQL
          ).bindValue("headerId", headerId).execute();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("addMenuItems received for " + getContext().getDatabase()
          + ": " + Arrays.deepToString(result.toArray()));
      }
      for(Object[] resultObj : result) {
        String fullName = resultObj[0].toString();
        int objectNr = Integer.parseInt(resultObj[1].toString());
        DocumentReference menuBarDocRef = webUtilsService.resolveDocumentReference(
            fullName);
        BaseObject obj = getContext().getWiki().getDocument(menuBarDocRef, getContext()
            ).getXObject(getMenuBarSubItemClassRef(menuBarDocRef.getWikiReference(
                ).getName()), objectNr);
        menuItemsMap.put(obj.getIntValue("itempos"), obj);
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    } catch (QueryException e) {
      LOGGER.error(e);
    }
  }

  private String getSubItemsXWQL() {
    return "select doc.fullName, subItem.number"
      + " from Document as doc, doc.object(Celements.MenuBarSubItemClass) as subItem"
      + " where subItem.header_id = :headerId";
  }

}
