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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.sajson.Builder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * TODO Unit Tests!!!
 * @author fabian
 *
 */
public class ContextMenuCSSClassesCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      ContextMenuCSSClassesCommand.class);

  public String getAllContextMenuCSSClassesAsJSON(XWikiContext context) {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openArray();
    for (String cssClass : getCM_CSSclasses(context)) {
      jsonBuilder.addString(cssClass);
    }
    jsonBuilder.closeArray();
    return jsonBuilder.getJSON();
  }

  public List<String> getCM_CSSclasses(XWikiContext context) {
    Set<String> cmCSSclassesSet = new HashSet<String>();
    if ("view".equals(context.getAction())) {
      cmCSSclassesSet.addAll(getCMcssClassesOneDB(context));
      if (!"celements2web".equals(context.getDatabase())) {
        String currentDB = context.getDatabase();
        try {
          context.setDatabase("celements2web");
          cmCSSclassesSet.addAll(getCMcssClassesOneDB(context));
        } finally {
          context.setDatabase(currentDB);
        }
      }
    }
    return new ArrayList<String>(cmCSSclassesSet);
  }

  Set<String> getCMcssClassesOneDB(XWikiContext context) {
    Set<String> cmCSSclasses = new HashSet<String>(); 
    try {
      List<Object> resultList = context.getWiki().search(getCMhql(), context);
      for (Object classNameObject : resultList) {
        cmCSSclasses.add(classNameObject.toString());
      }
    } catch (XWikiException exp) {
      mLogger.error("Failed to get list of cm css class names.", exp);
    }
    return cmCSSclasses;
  }

  String getCMhql() {
    return "select doc.name from XWikiDocument as doc, BaseObject as obj"
      + " where obj.name = doc.fullName and doc.space = 'CelementsContextMenu'"
      + " and obj.className = 'Celements2.ContextMenuItemClass'";
  }


}
