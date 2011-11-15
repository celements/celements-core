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
package com.celements.web.plugin.api;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;

public class PageLayoutApi extends Api {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      PageLayoutApi.class);


  private PageLayoutCommand pageLayoutCmd;
  private String layoutSpaceName;

  public PageLayoutApi(String layoutSpaceName, XWikiContext context) {
    super(context);
    this.pageLayoutCmd = new PageLayoutCommand();
    this.layoutSpaceName = layoutSpaceName;
  }

  public PageLayoutCommand getPageLayoutCommand() {
    if (hasProgrammingRights()) {
      return pageLayoutCmd;
    }
    return null;
  }

  public boolean isActive() {
    return pageLayoutCmd.isActive(layoutSpaceName, context);
  }

  public String getPrettyName() {
    return pageLayoutCmd.getPrettyName(layoutSpaceName, context);
  }

  /**
   * 
   * @param withDocHistory
   * @return successful
   */
  public boolean exportLayoutXAR(boolean withDocHistory) {
    try {
      pageLayoutCmd.exportLayoutXAR(layoutSpaceName, withDocHistory, context);
      return true;
    } catch (XWikiException exp) {
      mLogger.error("Failed to export page layout [" + layoutSpaceName + "].", exp);
    } catch (IOException exp) {
      mLogger.error("Failed to export page layout [" + layoutSpaceName + "].", exp);
    }
    return false;
  }

}
