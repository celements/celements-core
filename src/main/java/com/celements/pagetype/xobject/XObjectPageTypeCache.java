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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;

@Component
public class XObjectPageTypeCache implements IXObjectPageTypeCacheRole {

  @Requirement
  private IWebUtilsService webUtilsService;

  GetPageTypesCommand getPageTypeCmd = new GetPageTypesCommand();

  final ConcurrentMap<WikiReference, List<PageTypeReference>> pageTypeRefCache = new ConcurrentHashMap<>();

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
  }

  private XObjectPageTypeConfig getXObjectPTConfigForFN(String pageTypeFN) {
    DocumentReference pageTypeDocRef = webUtilsService.resolveDocumentReference(pageTypeFN);
    return new XObjectPageTypeConfig(pageTypeDocRef);
  }

  @Override
  public synchronized void invalidateCacheForWiki(WikiReference wikiRef) {
    if (webUtilsService.getCentralWikiRef().equals(wikiRef)) {
      pageTypeRefCache.clear();
    } else if (pageTypeRefCache != null) {
      pageTypeRefCache.remove(wikiRef);
    }
  }

  ConcurrentMap<WikiReference, List<PageTypeReference>> getPageTypeRefCache() {
    return pageTypeRefCache;
  }

  @Override
  public List<PageTypeReference> getPageTypesRefsForWiki(WikiReference wikiRef) {
    List<PageTypeReference> pageTypeList = pageTypeRefCache.get(wikiRef);
    if (pageTypeList == null) {
      pageTypeList = putPageTypeRefsForWikiToCache(wikiRef);
    }
    return pageTypeList;
  }

  private synchronized List<PageTypeReference> putPageTypeRefsForWikiToCache(
      WikiReference wikiRef) {
    List<PageTypeReference> pageTypeList = pageTypeRefCache.get(wikiRef);
    if (pageTypeList == null) {
      List<PageTypeReference> newPageTypeList = new ArrayList<PageTypeReference>();
      Set<String> pageTypeSet = getPageTypeCmd.getAllXObjectPageTypes(getContext());
      for (String pageTypeFN : pageTypeSet) {
        XObjectPageTypeConfig xObjPT = getXObjectPTConfigForFN(pageTypeFN);
        newPageTypeList.add(new PageTypeReference(xObjPT.getName(),
            "com.celements.XObjectPageTypeProvider", xObjPT.getCategories()));
      }
      pageTypeList = Collections.unmodifiableList(newPageTypeList);
      pageTypeRefCache.putIfAbsent(wikiRef, pageTypeList);
    }
    return pageTypeList;
  }

}
