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
package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.contextmenu.ContextMenuBuilderApi;
import com.celements.web.plugin.cmd.ContextMenuCSSClassesCommand;
import com.xpn.xwiki.XWikiContext;

@Component("contextMenu")
public class ContextMenuScriptService implements ScriptService {

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getAllContextMenuCSSClassesAsJSON() {
    return new ContextMenuCSSClassesCommand().getAllContextMenuCSSClassesAsJSON(
        getContext());
  }

  public ContextMenuBuilderApi getContextMenuBuilder() {
    return new ContextMenuBuilderApi(getContext());
  }

  public List<String> getAllCMcssClasses() {
    return new ContextMenuCSSClassesCommand().getCM_CSSclasses(getContext());
  }

}
