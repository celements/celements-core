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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class RenameCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenameCommand.class);

  /**
   * @param spaceName
   * @param newSpaceName
   * @param context
   * @return renamed pages
   */
  public List<String> renameSpace(String spaceName, String newSpaceName, XWikiContext context) {
    ArrayList<String> renamedPages = new ArrayList<>();
    try {
      for (String docName : context.getWiki().getSpaceDocsName(spaceName, context)) {
        String fullname = spaceName + "." + docName;
        String newDocName = newSpaceName + "." + docName;
        if (renameDoc(fullname, newDocName, true, context)) {
          renamedPages.add(docName);
        } else {
          LOGGER.error("renameSpace: Failed to rename Document [" + fullname + "] to ["
              + newDocName + "].");
        }
      }
    } catch (XWikiException exp) {
      LOGGER.error("renameSpace: Failed to rename Space [" + spaceName + "] to [" + newSpaceName
          + "].", exp);
    }
    return renamedPages;
  }

  public boolean renameDoc(String fullname, String newDocName, XWikiContext context) {
    return renameDoc(fullname, newDocName, false, context);
  }

  boolean renameDoc(String fullname, String newDocName, boolean flushMenuCacheExternal,
      XWikiContext context) {
    if (context.getWiki().exists(fullname, context) && !context.getWiki().exists(newDocName,
        context)) {
      try {
        XWikiDocument thePage = context.getWiki().getDocument(fullname, context);
        thePage.rename(newDocName, context);
        return true;
      } catch (XWikiException exp) {
        LOGGER.error("renameDoc: Failed to rename Document [" + fullname + "] to [" + newDocName
            + "].", exp);
      }
    }
    LOGGER.warn("renameDoc: Failed to rename Document [" + fullname + " ; "
        + context.getWiki().exists(fullname, context) + "] to [" + newDocName + " ; "
        + context.getWiki().exists(newDocName, context) + "].");
    return false;
  }

}
