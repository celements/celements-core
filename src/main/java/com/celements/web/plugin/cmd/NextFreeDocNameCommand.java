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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

/**
 * @Deprecated: since 2.47.0 instead use {@link INextFreeDocRole}
 */
@Deprecated
public class NextFreeDocNameCommand {

  /**
   * @Deprecated: since 2.47.0 instead use
   *              {@link INextFreeDocRole #getNextTitledPageDocRef(SpaceReference, String)}
   */
  @Deprecated
  public String getNextTitledPageFullName(String space, String title, XWikiContext context) {
    DocumentReference docRef = getNextFreeDocService().getNextTitledPageDocRef(new SpaceReference(
        space, new WikiReference(context.getDatabase())), title);
    return getWebUtilsService().getRefLocalSerializer().serialize(docRef);
  }

  /**
   * @Deprecated: since 2.47.0 instead use
   *              {@link INextFreeDocRole #getNextTitledPageDocRef(SpaceReference, String)}
   */
  @Deprecated
  public DocumentReference getNextTitledPageDocRef(String space, String title,
      XWikiContext context) {
    return getNextFreeDocService().getNextTitledPageDocRef(new SpaceReference(space,
        new WikiReference(context.getDatabase())), title);
  }

  /**
   * @Deprecated: since 2.47.0 instead use
   *              {@link INextFreeDocRole #getNextUntitledPageDocRef(SpaceReference)}
   */
  @Deprecated
  public String getNextUntitledPageFullName(String space, XWikiContext context) {
    DocumentReference docRef = getNextFreeDocService().getNextUntitledPageDocRef(new SpaceReference(
        space, new WikiReference(context.getDatabase())));
    return getWebUtilsService().getRefLocalSerializer().serialize(docRef);
  }

  /**
   * @Deprecated: since 2.47.0 instead use
   *              {@link INextFreeDocRole #getNextUntitledPageDocRef(SpaceReference)}
   */
  @Deprecated
  public String getNextUntitledPageName(String space, XWikiContext context) {
    DocumentReference docRef = getNextFreeDocService().getNextUntitledPageDocRef(new SpaceReference(
        space, new WikiReference(context.getDatabase())));
    return docRef.getName();
  }

  private INextFreeDocRole getNextFreeDocService() {
    return Utils.getComponent(INextFreeDocRole.class);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
