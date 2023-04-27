/* See the NOTICE file distributed with this work for additional
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
package com.celements.navigation.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.PartNameGetter;
import com.xpn.xwiki.web.Utils;

final class MenuItemObjectPartNameGetter implements PartNameGetter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MenuItemObjectPartNameGetter.class);

  private final IModelAccessFacade modelAccess;

  public MenuItemObjectPartNameGetter() {
    modelAccess = Utils.getComponent(IModelAccessFacade.class);
  }

  @Override
  public String getPartName(DocumentReference docRef) {
    return XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(docRef))
        .filter(INavigationClassConfig.MENU_ITEM_CLASS_REF)
        .stream().findFirst()
        .map(cobj -> cobj.getStringValue(INavigationClassConfig.PART_NAME_FIELD))
        .orElse("");
  }
}
