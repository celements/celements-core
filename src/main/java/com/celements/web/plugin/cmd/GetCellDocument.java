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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class GetCellDocument {

  private PageLayoutCommand injected_pageLayoutCommand = null;
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      GetCellDocument.class);

  public XWikiDocument getCellDoc(String className, String field, String value, 
      XWikiContext context) {
    PageLayoutCommand layoutCommand= getPageLayoutCommand();
    return getCellDoc(layoutCommand.getLayoutPropDoc(context).getSpace(), className, 
        field, value, context);
  }
  
  public XWikiDocument getCellDoc(String layoutSpace, String className, String field, 
      String value, XWikiContext context) {
    String hql = getHql();
    List<String> argsList = new ArrayList<String>();
    argsList.add(layoutSpace);
    argsList.add(className);
    argsList.add(field);
    argsList.add(value);
    List<XWikiDocument> docs = null;
    try {
      docs = context.getWiki().getStore().searchDocuments(hql, argsList, context);
    } catch (XWikiException e) {
      mLogger.error("Exception while searching cell document in layout '" + layoutSpace +
          "' with object of class '" + className + "' and field '" + field + "' == '" + 
          value + "'.", e);
    }
    if((docs != null) && (docs.size() >= 1)) {
      return docs.get(0);
    }
    return null;
  }

  private String getHql() {
    String hql = ", BaseObject as obj, StringProperty as str " +
        "where doc.space = ? " +
        "and obj.className = ? " +
        "and obj.name = doc.fullName " +
        "and obj.id = str.id.id " +
        "and str.id.name = ? " +
        "and str.value = ?";
    return hql;
  }

  PageLayoutCommand getPageLayoutCommand() {
    if(injected_pageLayoutCommand != null) {
      return injected_pageLayoutCommand;
    }
    return new PageLayoutCommand();
  }
  
  void injectPageLayoutCommand(PageLayoutCommand injectCommand) {
    injected_pageLayoutCommand = injectCommand;
  }
}
