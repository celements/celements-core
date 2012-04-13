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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.web.service.CelementsWebScriptService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
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
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    String celAppScript = context.getRequest().getPathInfo();
    LOGGER.debug("action: found script path [" + celAppScript + "].");
    context.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
    vcontext.put(CEL_APPSCRIPT_CONTEXT_PROPERTY, celAppScript);
    return shouldRender;
  }

  /**
   * {@inheritDoc}
   * 
   * @see XWikiAction#render(com.xpn.xwiki.XWikiContext)
   */
  public String render(XWikiContext context) throws XWikiException {
    String celAppScript = (String) context.get(CEL_APPSCRIPT_CONTEXT_PROPERTY);
    try {
      checkScriptAvailable(context, celAppScript);
      return CelementsWebScriptService.APP_SCRIPT_XPAGE;
    } catch (IOException e) {
        context.getResponse().setStatus(404);
        return "docdoesnotexist";
    }
  }

  private byte[] checkScriptAvailable(XWikiContext context, String path
      ) throws IOException {
    return context.getWiki().getResourceContentAsBytes(path);
  }

}
