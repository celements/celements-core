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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class RenameCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(RenameCommand.class);
  private IWebUtils injectedWebUtils;

  /**
   * 
   * @param spaceName
   * @param newSpaceName
   * @param context
   * @return renamed pages
   */
  public List<String> renameSpace(String spaceName, String newSpaceName,
      XWikiContext context) {
    ArrayList<String> renamedPages = new ArrayList<String>();
    try {
      for (String docName : context.getWiki().getSpaceDocsName(spaceName, context)) {
        String fullname = spaceName + "." + docName;
        String newDocName = newSpaceName + "." + docName;
        if (renameDoc(fullname, newDocName, true, context)) {
          renamedPages.add(docName);
        } else {
          mLogger.error("renameSpace: Failed to rename Document [" + fullname + "] to ["
            + newDocName + "].");
        }
      }
      if (!renamedPages.isEmpty()) {
        getWebUtils().flushMenuItemCache(context);
      }
    } catch (XWikiException exp) {
      mLogger.error("renameSpace: Failed to rename Space [" + spaceName + "] to ["
          + newSpaceName + "].", exp);
    }
    return renamedPages;
  }

  IWebUtils getWebUtils() {
    if (injectedWebUtils != null) {
      return injectedWebUtils;
    }
    return WebUtils.getInstance();
  }

  /**
   * FOR TEST ONLY!!!!
   * @param mockWebUtils
   */
  void inject_webUtils(IWebUtils mockWebUtils) {
    injectedWebUtils = mockWebUtils;
  }

  public boolean renameDoc(String fullname, String newDocName, XWikiContext context) {
    return renameDoc(fullname, newDocName, false, context);
  }

  boolean renameDoc(String fullname, String newDocName,
      boolean flushMenuCacheExternal, XWikiContext context) {
    if (context.getWiki().exists(fullname, context) && !context.getWiki().exists(
        newDocName, context)) {
      try {
        XWikiDocument thePage = context.getWiki().getDocument(fullname,
            context);
        thePage.rename(newDocName, context);
        if (!flushMenuCacheExternal) {
          getWebUtils().flushMenuItemCache(context);
        }
        return true;
      } catch (XWikiException exp) {
        mLogger.error("renameDoc: Failed to rename Document [" + fullname + "] to ["
            + newDocName + "].", exp);
      }
    }
    mLogger.warn("renameDoc: Failed to rename Document [" + fullname + " ; "
        + context.getWiki().exists(fullname, context) + "] to ["
        + newDocName + " ; " + context.getWiki().exists(newDocName, context) + "].");
    return false;
  }

}
