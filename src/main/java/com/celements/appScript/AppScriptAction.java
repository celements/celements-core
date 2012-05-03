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
package com.celements.appScript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiAction;

/**
 * Action called when the request URL has the "/app/" string in its path (this
 * is configured in <code>struts-config.xml</code>. It means the request is to
 * execute an application script and display its result in view mode.
 * 
 * @version $Id$
 */
public class AppScriptAction extends XWikiAction {

  private static final String CEL_APPSCRIPT_CONTEXT_PROPERTY = "celAppScript";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      AppScriptAction.class);

  /**
   * The identifier of the view action.
   * 
   * @todo need an enumerated class for actions.
   */
  public static final String VIEW_ACTION = "view";

  /**
   * {@inheritDoc}
   * 
   * @see XWikiAction#action(com.xpn.xwiki.XWikiContext)
   */
  public boolean action(XWikiContext context) throws XWikiException {
    boolean shouldRender = true;
    context.put("action", VIEW_ACTION);
    context.put("appAction", true);
    String path = context.getRequest().getPathInfo();
    if (getAppScriptService().getStartIndex(path) > 0) {
      String celAppScript = getAppScriptService().getScriptNameFromURL();
      LOGGER.debug("action: found script path [" + celAppScript + "].");
      context.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
      VelocityContext vcontext = (VelocityContext) context.get("vcontext");
      vcontext.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
    }
    return shouldRender;
  }

  /**
   * {@inheritDoc}
   * 
   * @see XWikiAction#render(com.xpn.xwiki.XWikiContext)
   */
  public String render(XWikiContext context) throws XWikiException {
    String page = Utils.getPage(context.getRequest(), IAppScriptService.APP_SCRIPT_XPAGE);
    Utils.parseTemplate(page, !page.equals("direct"), context);
    return null;
  }

  private IAppScriptService getAppScriptService() {
    return Utils.getComponent(IAppScriptService.class);
  }

}
