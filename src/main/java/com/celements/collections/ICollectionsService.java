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
package com.celements.collections;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Deprecated
@ComponentRole
public interface ICollectionsService {

  /**
   * @since 5.8 instead use {@link XWikiObjectFetcher}
   */
  @Deprecated
  List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc);

  /**
   * @since 5.8 instead use {@link XWikiObjectFetcher}
   */
  @Deprecated
  List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2);
}
