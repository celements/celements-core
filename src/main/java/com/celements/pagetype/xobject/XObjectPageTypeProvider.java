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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.web.service.IWebUtilsService;

@Component(XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER)
public class XObjectPageTypeProvider implements IPageTypeProviderRole {

  public static final String X_OBJECT_PAGE_TYPE_PROVIDER = "com.celements.XObjectPageTypeProvider";

  private static Logger LOGGER = LoggerFactory.getLogger(XObjectPageTypeProvider.class);

  private String _CEL_XOBJ_GETALLPAGETYPES_COUNTER = "celXObjectGetAllPageTypesCounter";
  private String _CEL_XOBJ_GETALLPAGETYPES_TOTALTIME = "celXObjectGetAllPageTypesTotelTime";

  public static final String DEFAULT_PAGE_TYPES_SPACE = "PageTypes";

  @Requirement
  IXObjectPageTypeCacheRole xobjectPageTypeCache;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  Execution execution;

  private ExecutionContext getExecContext() {
    return execution.getContext();
  }

  public IPageTypeConfig getPageTypeByReference(PageTypeReference pageTypeRef) {
    DocumentReference pageTypeDocRef = new DocumentReference(pageTypeRef.getConfigName(),
        new SpaceReference(DEFAULT_PAGE_TYPES_SPACE, webUtilsService.getWikiRef()));
    return new XObjectPageTypeConfig(pageTypeDocRef);
  }

  public List<PageTypeReference> getPageTypes() {
    Integer count = 0;
    if (getExecContext().getProperty(_CEL_XOBJ_GETALLPAGETYPES_COUNTER) != null) {
      count = (Integer) getExecContext().getProperty(_CEL_XOBJ_GETALLPAGETYPES_COUNTER);
    }
    count = count + 1;
    long startMillis = System.currentTimeMillis();
    List<PageTypeReference> pageTypeList = xobjectPageTypeCache.getPageTypesRefsForWiki(
        webUtilsService.getWikiRef());
    getExecContext().setProperty(_CEL_XOBJ_GETALLPAGETYPES_COUNTER, count);
    if (LOGGER.isInfoEnabled()) {
      long endMillis = System.currentTimeMillis();
      long timeUsed = endMillis - startMillis;
      Long totalTimeUsed = 0L;
      if (getExecContext().getProperty(_CEL_XOBJ_GETALLPAGETYPES_TOTALTIME) != null) {
        totalTimeUsed = (Long) getExecContext().getProperty(_CEL_XOBJ_GETALLPAGETYPES_TOTALTIME);
      }
      totalTimeUsed += timeUsed;
      getExecContext().setProperty(_CEL_XOBJ_GETALLPAGETYPES_TOTALTIME, totalTimeUsed);
      LOGGER.info("XOBJECT-getPageTypes [" + count + "]: finished in [" + timeUsed
          + "], total time [" + totalTimeUsed + "].");
    }
    return pageTypeList;
  }

}
