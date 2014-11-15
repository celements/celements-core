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
package com.celements.pagetype.xobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.xpn.xwiki.XWikiContext;

@Component
public class XObjectPageTypeCache implements IXObjectPageTypeCacheRole {

  GetPageTypesCommand getPageTypeCmd = new GetPageTypesCommand();

  Map<String, List<PageTypeReference>> pageTypeRefCache;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  private XObjectPageTypeConfig getXObjectPTConfigForFN(String pageTypeFN) {
    return new XObjectPageTypeConfig(pageTypeFN);
  }

  public void invalidateCacheForDatabase(String database) {
    if ("celements2web".equals(database)) {
      pageTypeRefCache = null;
    } else if (pageTypeRefCache != null) {
      pageTypeRefCache.remove(database);
    }
  }

  Map<String, List<PageTypeReference>> getPageTypeRefCache() {
    if (pageTypeRefCache == null) {
      pageTypeRefCache = new HashMap<String, List<PageTypeReference>>();
    }
    return pageTypeRefCache;
  }

  @Override
  public List<PageTypeReference> getPageTypesRefsForDatabase(String database) {
    if (!getPageTypeRefCache().containsKey(database)) {
      List<PageTypeReference> pageTypeList = new ArrayList<PageTypeReference>();
      Set<String> pageTypeSet = getPageTypeCmd.getAllXObjectPageTypes(getContext());
      for (String pageTypeFN : pageTypeSet) {
        XObjectPageTypeConfig xObjPT = getXObjectPTConfigForFN(pageTypeFN);
        pageTypeList.add(new PageTypeReference(xObjPT.getName(),
            "com.celements.XObjectPageTypeProvider", xObjPT.getCategories()));
      }
      getPageTypeRefCache().put(database, pageTypeList);
    }
    return getPageTypeRefCache().get(database);
  }

}
