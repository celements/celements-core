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
package com.celements.web.classcollections;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

@Component
public class OldCoreClassConfig implements IOldCoreClassConfig {

  @Override
  public DocumentReference getFromStorageClassRef(WikiReference wikiRef) {
    return new DocumentReference(FORM_STORAGE_CLASS_DOC, new SpaceReference(
        FORM_STORAGE_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getXWikiUsersClassRef(WikiReference wikiRef) {
    return new DocumentReference(XWIKI_USERS_CLASS_DOC, new SpaceReference(XWIKI_USERS_CLASS_SPACE,
        wikiRef));
  }

  @Override
  public ClassReference getPhotoAlbumClassRef() {
    return new ClassReference(PHOTO_ALBUM_CLASS_SPACE, PHOTO_ALBUM_CLASS_DOC);
  }

}
