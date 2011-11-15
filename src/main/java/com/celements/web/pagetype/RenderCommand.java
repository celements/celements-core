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
package com.celements.web.pagetype;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;

public class RenderCommand {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(RenderCommand.class);

  private XWikiContext context;

  private PageTypeCommand injected_pageTypeCmd;

  private String defaultPageType = null;

  private XWikiRenderingEngine renderingEngine;

  private static XWikiRenderingEngine defaultRenderingEngine;

  public void setDefaultPageType(String defaultPageType) {
    this.defaultPageType = defaultPageType;
  }

  public RenderCommand(XWikiContext context) {
    this.context = context;
  }

  public String renderCelementsCell(String elementFullName) throws XWikiException {
    XWikiDocument cellDoc = getTemplateDoc(elementFullName);
    return renderCelementsDocument(cellDoc, "view");
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String renderMode
      ) throws XWikiException {
    mLogger.trace("renderCelementsDocument: cellDoc [" + cellDoc.getFullName()
        + "] renderMode [" + renderMode + "].");
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    vcontext.put("celldoc", cellDoc.newDocument(context));
    PageType cellType = pageTypeCmd().getPageTypeWithDefaultObj(cellDoc, defaultPageType,
        context);
    String renderTemplatePath = getRenderTemplatePath(cellType, cellDoc.getFullName(),
        renderMode);
    String templateContent;
    XWikiDocument templateDoc = context.getDoc();
    if (renderTemplatePath.startsWith(":")) {
      String templatePath = getTemplatePathOnDisk(renderTemplatePath);
      try {
        templateContent = context.getWiki().getResourceContent(templatePath);
      } catch (IOException exp) {
        mLogger.debug("Exception while parsing template [" + templatePath + "].", exp);
        return "";
      }
    } else {
      templateDoc = getTemplateDoc(renderTemplatePath);
      templateContent = getTranslatedContent(templateDoc);
    }
    return getRenderingEngine().renderText(templateContent,
        templateDoc, context.getDoc(), context);
  }

  public String renderDocument(XWikiDocument document) throws XWikiException {
    return getRenderingEngine().renderText(getTranslatedContent(document), document,
        context);
  }

  XWikiRenderingEngine getRenderingEngine() throws XWikiException {
    if (this.renderingEngine == null) {
      this.renderingEngine = getDefaultRenderingEngine(context);
    }
    return this.renderingEngine;
  }

  static XWikiRenderingEngine getDefaultRenderingEngine(XWikiContext context
      ) throws XWikiException {
    if (defaultRenderingEngine == null) {
      defaultRenderingEngine = initRenderingEngine(Arrays.asList("velocity", "groovy"),
          context);
    }
    return defaultRenderingEngine;
  }

  static XWikiRenderingEngine initRenderingEngine(List<String> rendererNames,
      XWikiContext context) throws XWikiException {
    return new DefaultCelementsRenderingEngine(rendererNames, context);
  }

  public XWikiRenderingEngine initRenderingEngine(List<String> rendererNames
      ) throws XWikiException {
    return RenderCommand.initRenderingEngine(rendererNames, context);
  }

  public void setRenderingEngine(XWikiRenderingEngine renderingEngine) {
      this.renderingEngine = renderingEngine;
  }

  PageTypeCommand pageTypeCmd() {
    if (injected_pageTypeCmd != null) {
      return injected_pageTypeCmd;
    }
    return PageTypeCommand.getInstance();
  }

  void inject_PageTypeCmd(PageTypeCommand mockPTcmd) {
    injected_pageTypeCmd = mockPTcmd;
  }

  String getTranslatedContent(XWikiDocument templateDoc) throws XWikiException {
    String translatedContent = templateDoc.getTranslatedContent(context);
    if (!getRenderingEngine().getRendererNames().contains("xwiki")) {
      return translatedContent.replaceAll("\\{pre\\}|\\{/pre\\}", "");
    } else {
      return translatedContent;
    }
  }

  XWikiDocument getTemplateDoc(String templateFullName) throws XWikiException {
    return context.getWiki().getDocument(templateFullName, context);
  }

  String getTemplatePathOnDisk(String renderTemplatePath) {
    return renderTemplatePath.replaceAll("^:(Templates\\.)?", "/templates/celTemplates/"
        ) + ".vm";
  }

  String getRenderTemplatePath(PageType cellType, String cellDocFN,
      String renderMode) throws XWikiException {
    if (cellType != null) {
      String renderTemplateFullName = cellType.getRenderTemplate(renderMode, context);
      if ((renderTemplateFullName != null) && !"".equals(renderTemplateFullName)) {
        mLogger.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
            + cellType.getFullName() + "] and renderTemplate ["
            + renderTemplateFullName + "].");
        return renderTemplateFullName;
      }
      mLogger.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
          + cellType.getFullName() + "] using content of cellDoc [" + cellDocFN + "].");
    }
    return cellDocFN;
  }

}
