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
package com.celements.web.plugin.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.web.pagetype.PageType;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class GetPageTypesCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      GetPageTypesCommand.class);

  public List<String> getPageTypesForCategories(Set<String> catList,
      boolean onlyVisible, XWikiContext context) {
    Set<String> pageTypeSet = new HashSet<String>();
    String currentDatabase = context.getDatabase();
    try {
      context.setDatabase("celements2web");
      pageTypeSet.addAll(getPateTypesForOneDatabase(catList, onlyVisible, context));
    } finally {
      context.setDatabase(currentDatabase);
    }
    pageTypeSet.addAll(getPateTypesForOneDatabase(catList, onlyVisible, context));
    if (doesContainEmptyCategory(catList)) {
      pageTypeSet = inMemoryFilterList(catList, pageTypeSet, context);
    }
    return new ArrayList<String>(pageTypeSet);
  }

  private List<String> getPateTypesForOneDatabase(Set<String> catList,
      boolean onlyVisible, XWikiContext context) {
    List<String> result = Collections.emptyList();
    try {
      result = context.getWiki().search(getPThql(catList, onlyVisible), context);
    } catch (XWikiException exp) {
      mLogger.error("getPageTypesForCategories: Failed to get pagetypes.", exp);
    }
    return result;
  }

  private Set<String> inMemoryFilterList(Set<String> catList,
      Set<String> pageTypeSet, XWikiContext context) {
    catList = new HashSet<String>(catList);
    Set<String> filteredPTset = new HashSet<String>();
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
      hql += " and catName.id.id=obj.id and catName.id.name='category'"
        + " and catName.value in (";
      for (String catString : catList) {
        hql += "'" + catString + "', ";
      }
      hql = hql.replaceAll(", $", "");
      hql += ")";
    }
    if (onlyVisible) {
      hql += " and visible.id.id=obj.id and visible.id.name='visible'"
        + " and visible.id.value=1 ";
    }
    return hql;
  }

  private boolean doesContainEmptyCategory(Set<String> catList) {
    return catList.contains("");
  }

}
