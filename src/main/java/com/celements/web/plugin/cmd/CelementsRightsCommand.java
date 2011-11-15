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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CelementsRightsCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CelementsRightsCommand.class);
  
  public boolean isCelementsRights(String fullName, XWikiContext context) {
    try {
      XWikiDocument doc = context.getWiki().getDocument(fullName, context);
      Vector<BaseObject> rights = doc.getObjects("XWiki.XWikiRights");
      if (rights != null){
        for (BaseObject right : rights) {
          boolean validGroups = isValidGroups(right);
          boolean validUsers = isValidUsers(right);
          boolean validLevels = isValidLevels(right);
          mLogger.debug("isCelementsRights: for doc [" + fullName + "], objNr ["
              + right.getNumber() + "] results: " + validGroups + ", " + validUsers
              + ", " + validLevels);
          if (!(validGroups && validUsers && validLevels)) return false;
        }
      }
      return true;
    } catch (Exception e) {
      mLogger.error("Exception while trying to check celements rights", e);
    }
    return false;
  }
  
  boolean isValidGroups(BaseObject right) {
    return ((getPropertyList(right, "groups").size() == 0) 
        || ((getPropertyList(right, "groups").size() == 1) 
            && (getPropertyList(right, "users").size() == 0)));
  }

  boolean isValidUsers(BaseObject right) {
    if (getPropertyList(right, "users").size() == 0) {
      return true;
    }
    if ((getPropertyList(right, "users").size() == 1)
        && "XWiki.XWikiGuest".equals(getPropertyList(right, "users").get(0))
        && (getPropertyList(right, "groups").size() == 0)) {
      return true;
    }
    return false;
  }
  
  boolean isValidLevels(BaseObject right) {
    List<String> levels = getPropertyList(right, "levels");
    if ((levels.size() == 0) || ((levels.size() == 1) && levels.contains("view"))) {
      return true;
    }
    else if ((levels.size() == 4) && levels.contains("view") && levels.contains("edit") 
        && levels.contains("delete") && levels.contains("undelete")) {
      return true;
    }
    return false;
  }
  
  private List<String> getPropertyList(BaseObject right, String key) {
    mLogger.trace("getPropertyList: key [" + key + "] value ["
        + right.getLargeStringValue(key) + "] " );
    if ((right.getLargeStringValue(key) == null)
        || "".equals(right.getLargeStringValue(key))) return Collections.emptyList();
    return Arrays.asList(right.getLargeStringValue(key).split(","));
  }
}