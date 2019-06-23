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

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.pagetype.PageTypeReference;
import com.celements.web.service.IWebUtilsService;

@Component
public class XObjectPageTypeUtils implements XObjectPageTypeUtilsRole {

  private static final String DEFAULT_PAGE_TYPES_SPACE = "PageTypes";

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull String configName) {
    DocumentReference pageTypeDocRef = new DocumentReference(configName, new SpaceReference(
        DEFAULT_PAGE_TYPES_SPACE, webUtilsService.getWikiRef()));
    return pageTypeDocRef;
  }

  @Override
  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull PageTypeReference pageTypeRef) {
    return getDocRefForPageType(pageTypeRef.getConfigName());
  }

}
