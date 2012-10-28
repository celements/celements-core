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
import java.util.List;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageType;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWikiContext;

@Component("com.celements.XObjectPageTypeProvider")
public class XObjectPageTypeProvider implements IPageTypeProviderRole {

  GetPageTypesCommand getPageTypeCmd = new GetPageTypesCommand();
  PageTypeCommand pageTypeCmd = new PageTypeCommand();

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  public IPageTypeConfig getPageTypeByReference(PageTypeReference pageTypeRef) {
    String pageTypeFN = pageTypeCmd.completePageTypeDocName(pageTypeRef.getConfigName());
    return new XObjectPageTypeConfig(pageTypeFN);
  }

  public List<PageTypeReference> getPageTypes() {
    ArrayList<PageTypeReference> pageTypeList = new ArrayList<PageTypeReference>();
    Set<String> pageTypeSet = getPageTypeCmd.getAllXObjectPageTypes(getContext());
    for (String pageTypeFN : pageTypeSet) {
      PageType pageType = new PageType(pageTypeFN);
      List<String> categories = pageType.getCategories(getContext());
      pageTypeList.add(new PageTypeReference(pageType.getConfigName(getContext()),
          "com.celements.XObjectPageTypeProvider", categories));
    }
    return pageTypeList;
  }

}
