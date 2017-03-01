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
package com.celements.pagetype.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.celements.pagetype.PageType;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class GetPageTypesCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(GetPageTypesCommand.class);

  private String _CEL_GETXOBJ_PAGETYPES_COUNTER = "celGetXObjectPageTypesCounter";
  private String _CEL_GETXOBJ_PAGETYPES_TOTALTIME = "celGetXObjectPageTypesTotelTime";

  private ExecutionContext getExecContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  /**
   * @deprecated since 1.100 instead use page type script service
   *             com.celements.pagetype.service.PageTypeScriptService.
   *             getPageTypesByCategories()
   */
  @Deprecated
  public List<String> getPageTypesForCategories(Set<String> catList, boolean onlyVisible,
      XWikiContext context) {
    LOGGER.warn("deprecated usage of getPageTypesForCategories.");
    Set<String> pageTypeSet = getXObjectPageTypes(catList, onlyVisible, context);
    if (doesContainEmptyCategory(catList)) {
      pageTypeSet = inMemoryFilterList(catList, pageTypeSet, context);
    }
    return new ArrayList<>(pageTypeSet);
  }

  public Set<String> getAllXObjectPageTypes(XWikiContext context) {
    return getXObjectPageTypes(Collections.<String>emptySet(), false, context);
  }

  private Set<String> getXObjectPageTypes(Set<String> catList, boolean onlyVisible,
      XWikiContext context) {
    Set<String> pageTypeSet = new HashSet<>();
    String currentDatabase = context.getDatabase();
    Integer count = 0;
    if (getExecContext().getProperty(_CEL_GETXOBJ_PAGETYPES_COUNTER) != null) {
      count = (Integer) getExecContext().getProperty(_CEL_GETXOBJ_PAGETYPES_COUNTER);
    }
    count = count + 1;
    long startMillis = System.currentTimeMillis();
    try {
      context.setDatabase("celements2web");
      pageTypeSet.addAll(getPageTypesForOneDatabase(catList, onlyVisible, context));
    } finally {
      context.setDatabase(currentDatabase);
    }
    pageTypeSet.addAll(getPageTypesForOneDatabase(catList, onlyVisible, context));
    getExecContext().setProperty(_CEL_GETXOBJ_PAGETYPES_COUNTER, count);
    if (LOGGER.isInfoEnabled()) {
      long endMillis = System.currentTimeMillis();
      long timeUsed = endMillis - startMillis;
      Long totalTimeUsed = 0L;
      if (getExecContext().getProperty(_CEL_GETXOBJ_PAGETYPES_TOTALTIME) != null) {
        totalTimeUsed = (Long) getExecContext().getProperty(_CEL_GETXOBJ_PAGETYPES_TOTALTIME);
      }
      totalTimeUsed += timeUsed;
      getExecContext().setProperty(_CEL_GETXOBJ_PAGETYPES_TOTALTIME, totalTimeUsed);
      LOGGER.info("getXObjectPageTypes [" + count + "]: finished in [" + timeUsed
          + "], total time [" + totalTimeUsed + "].");
    }
    return pageTypeSet;
  }

  private List<String> getPageTypesForOneDatabase(Set<String> catList, boolean onlyVisible,
      XWikiContext context) {
    List<String> result = Collections.emptyList();
    try {
      result = context.getWiki().search(getPThql(catList, onlyVisible), context);
    } catch (XWikiException exp) {
      LOGGER.error("getPageTypesForCategories: Failed to get pagetypes.", exp);
    }
    return result;
  }

  private Set<String> inMemoryFilterList(Set<String> catList, Set<String> pageTypeSet,
      XWikiContext context) {
    catList = new HashSet<>(catList);
    Set<String> filteredPTset = new HashSet<>();
    for (String pageTypeFN : pageTypeSet) {
      String category = new PageType(pageTypeFN).getCategoryString(context);
      if (catList.contains(category)) {
        filteredPTset.add(pageTypeFN);
      }
    }
    return filteredPTset;
  }

  String getPThql(Set<String> catList, boolean onlyVisible) {
    String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj";
    if (!catList.isEmpty() && !doesContainEmptyCategory(catList)) {
      hql += ", StringProperty as catName";
    }
    if (onlyVisible) {
      hql += ", IntegerProperty as visible";
    }
    hql += " where doc.space='PageTypes' and doc.translation=0 and obj.name=doc.fullName "
        + " and obj.className='Celements2.PageTypeProperties'";
    if (!catList.isEmpty() && !doesContainEmptyCategory(catList)) {
      hql += " and catName.id.id=obj.id and catName.id.name='category'" + " and catName.value in (";
      for (String catString : catList) {
        hql += "'" + catString + "', ";
      }
      hql = hql.replaceAll(", $", "");
      hql += ")";
    }
    if (onlyVisible) {
      hql += " and visible.id.id=obj.id and visible.id.name='visible'" + " and visible.id.value=1 ";
    }
    return hql;
  }

  private boolean doesContainEmptyCategory(Set<String> catList) {
    return catList.contains("");
  }

}
