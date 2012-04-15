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
package com.celements.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.web.service.CelementsWebScriptService;
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

  private static final String APP_SCRIPT_ACTION_NAME_CONF_PROPERTY = "celements.appScript.actionName";

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
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    String path = context.getRequest().getPathInfo();
    if (getStartIndex(path, context) > 0) {
      String celAppScript = path.substring(getStartIndex(path, context));
      LOGGER.debug("action: found script path [" + celAppScript + "].");
      context.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
      vcontext.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
    }
    return shouldRender;
  }

  private int getStartIndex(String path, XWikiContext context) {
    String actionName = getAppActionName(context);
    return path.indexOf("/" + actionName + "/") + actionName.length() + 2;
  }

  private String getAppActionName(XWikiContext context) {
    return context.getWiki().Param(APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        CelementsWebScriptService.APP_SCRIPT_XPAGE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see XWikiAction#render(com.xpn.xwiki.XWikiContext)
   */
  public String render(XWikiContext context) throws XWikiException {
    String page = Utils.getPage(context.getRequest(),
        CelementsWebScriptService.APP_SCRIPT_XPAGE);
    Utils.parseTemplate(page, !page.equals("direct"), context);
    return null;
  }

}
