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
package com.celements.pagetype;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.pagetype.classes.PageTypeClass;
import com.celements.pagetype.classes.PageTypePropertiesClass;
import com.celements.web.service.IWebUtilsService;

/**
 * @deprecated use {@link PageTypeClass} and {@link PageTypePropertiesClass}
 */
@Deprecated
@Component
public class PageTypeClassConfig implements IPageTypeClassConfig {

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public DocumentReference getPageTypePropertiesClassRef(WikiReference wikiRef) {
    return new DocumentReference(PAGE_TYPE_PROPERTIES_CLASS_DOC, new SpaceReference(
        PAGE_TYPE_PROPERTIES_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getPageTypeClassRef() {
    return getPageTypeClassRef(webUtils.getWikiRef());
  }

  @Override
  public DocumentReference getPageTypeClassRef(WikiReference wikiRef) {
    return new DocumentReference(PAGE_TYPE_CLASS_DOC, new SpaceReference(PAGE_TYPE_CLASS_SPACE,
        wikiRef));
  }

}
