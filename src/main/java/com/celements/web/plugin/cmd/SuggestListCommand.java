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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.web.utils.SuggestBaseClass;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class SuggestListCommand {
  private static Log mLogger = LogFactory.getFactory().getInstance(
      SuggestListCommand.class);

  public List<Object> getSuggestList(DocumentReference classRef, String fieldname, 
      List<String> excludes, String input, String firstCol, String secCol, int limit, 
      XWikiContext context) {
    SuggestBaseClass sbc = new SuggestBaseClass(classRef, fieldname, context);
    String classname = getFullNameFromDocRef(classRef);
    if(excludes == null) {
      excludes = Collections.emptyList();
    }
    Boolean isMultiSelect = (sbc.isMultiSelect() != null) && sbc.isMultiSelect();
    input = getSeparatedInput(input, sbc.getSeparators(), isMultiSelect);
    String hibquery = sbc.getSql();
    QueryManager queryManager = Utils.getComponent(QueryManager.class);
    Query query = null;
    String xwql = "";
    List<Object> results = Collections.emptyList();
    if(isExistingClassAndField(classRef, fieldname, context)) {
      if((hibquery != null) && !"".equals(hibquery.trim())) {
        xwql = getDBStringListXWQL(hibquery, firstCol, secCol);
      } else {
        if((sbc.isRelationalStorage() != null) && sbc.isRelationalStorage() 
            && isMultiSelect) {
          xwql = "select distinct obj." + fieldname + " from Document as doc, doc.object("
          + classname + ") as obj where ?1 member of obj." + fieldname + " ";
        } else if(isMultiSelect) {
          xwql = "select distinct obj."+ fieldname +" from Document as doc, doc.object("
          + classname + "?) as obj where lower(obj." + fieldname + ") like ?1 ";
        } else {
          xwql = "select distinct obj." + fieldname + " from Document doc, doc.object(" 
          + classname + ") as obj where lower(obj." + fieldname + ") like ?1 ";
        }
      }
      for(int i = 0; i < excludes.size(); i++) {
        xwql += "and doc.fullName <> ?" + (i+2) + " ";
      }
      mLogger.trace("searching for xwql " + xwql);
      try {
        query = queryManager.createQuery(xwql, Query.XWQL);
        query.bindValue(1, input);
        for(int i = 0; i < excludes.size(); i++) {
          query.bindValue(i+2, excludes.get(i));
        }
        query.setLimit(limit);
        mLogger.trace(query.getStatement());
        results = query.execute();
      } catch(QueryException qe) {
        mLogger.error("Exception while querying suggest for classname '" + classname +
            "', fieldname '" + fieldname + "', input '" + input + "'.", qe);
      }
    }
    return results;
  }

  boolean isExistingClassAndField(DocumentReference classRef, String fieldname,
      XWikiContext context) {
    XWikiDocument classDoc = null;
    try {
      classDoc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error("Exception while getting doc for DocumentReference " + classRef, e);
    }
    if(classDoc != null) {
      BaseClass bc = classDoc.getXClass();
      PropertyInterface property = bc.get(fieldname);
      if(property != null) {
        return true;
      }
    }
    return false;
  }

  String getFullNameFromDocRef(DocumentReference classRef) {
    String classname = classRef.getLastSpaceReference().getName() + "." 
        + classRef.getName();
    return classname;
  }

  String getSeparatedInput(String input, String sep, Boolean isMultiSelect) {
    if(isMultiSelect && (sep != null) && !"".equals(sep)) {
      String[] inputWords = input.split(Pattern.quote(sep));
      System.out.println(inputWords.length);
      input = inputWords[inputWords.length-1].trim();
    }
    return input;
  }

  String getDBStringListXWQL(String query, String firstCol, String secCol) {
    String likeCol = firstCol;
    if((secCol != null) && !"".equals(secCol.trim()) && !"-".equals(secCol)) {
      likeCol = secCol;
    }
    String xwql;
    int idxWhere = query.toLowerCase().indexOf("where");
    if(idxWhere < 0) {
      xwql = query.concat(" where lower("+ likeCol.replaceAll("[^a-zA-Z0-9_.]", "") 
          + ") like ? ");
    } else {
      idxWhere = idxWhere + 5;
      xwql = query.substring(0, idxWhere) 
          + " lower("+ firstCol.replaceAll("[^a-zA-Z0-9_.]", "") + ") like ? and " 
          + query.substring(idxWhere);
    }
    return xwql;
  }
}
