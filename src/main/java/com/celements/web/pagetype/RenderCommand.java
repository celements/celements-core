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
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class RenderCommand {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(RenderCommand.class);

  private PageTypeCommand pageTypeCmd;

  private String defaultPageType = null;

  private XWikiRenderingEngine renderingEngine;

  private static XWikiRenderingEngine defaultRenderingEngine;

  public RenderCommand() {}

  private XWikiContext getContext() {
    return (XWikiContext)getExecutionContext().getProperty("xwikicontext");
  }

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  public void setDefaultPageType(String defaultPageType) {
    this.defaultPageType = defaultPageType;
  }

  public String renderCelementsCell(String elementFullName) throws XWikiException {
    XWikiDocument cellDoc = getTemplateDoc(elementFullName);
    return renderCelementsDocument(cellDoc, "view");
  }

  public String renderCelementsDocument(XWikiDocument cellDoc, String renderMode
      ) throws XWikiException {
    LOGGER.trace("renderCelementsDocument: cellDoc [" + cellDoc.getFullName()
        + "] renderMode [" + renderMode + "].");
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    vcontext.put("celldoc", cellDoc.newDocument(getContext()));
    PageType cellType = pageTypeCmd().getPageTypeWithDefaultObj(cellDoc, defaultPageType,
        getContext());
    String renderTemplatePath = getRenderTemplatePath(cellType, cellDoc.getFullName(),
        renderMode);
    String templateContent;
    XWikiDocument templateDoc = getContext().getDoc();
    if (renderTemplatePath.startsWith(":")) {
      String templatePath = getTemplatePathOnDisk(renderTemplatePath);
      try {
        templateContent = getContext().getWiki().getResourceContent(templatePath);
      } catch (IOException exp) {
        LOGGER.debug("Exception while parsing template [" + templatePath + "].", exp);
        return "";
      }
    } else {
      templateDoc = getTemplateDoc(renderTemplatePath);
      templateContent = getTranslatedContent(templateDoc);
    }
    return getRenderingEngine().renderText(templateContent,
        templateDoc, getContext().getDoc(), getContext());
  }

  public String renderDocument(DocumentReference docRef) throws XWikiException {
    XWikiDocument xwikidoc = getContext().getWiki().getDocument(docRef, getContext());
    return renderDocument(xwikidoc);
  }

  public String renderDocument(XWikiDocument document) throws XWikiException {
    return getRenderingEngine().renderText(getTranslatedContent(document), document,
        getContext());
  }

  XWikiRenderingEngine getRenderingEngine() throws XWikiException {
    if (this.renderingEngine == null) {
      this.renderingEngine = getDefaultRenderingEngine(getContext());
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
    return RenderCommand.initRenderingEngine(rendererNames, getContext());
  }

  public void setRenderingEngine(XWikiRenderingEngine renderingEngine) {
      this.renderingEngine = renderingEngine;
  }

  PageTypeCommand pageTypeCmd() {
    if (pageTypeCmd == null) {
      pageTypeCmd = new PageTypeCommand();
    }
    return pageTypeCmd;
  }

  void inject_PageTypeCmd(PageTypeCommand mockPTcmd) {
    pageTypeCmd = mockPTcmd;
  }

  String getTranslatedContent(XWikiDocument templateDoc) throws XWikiException {
    String translatedContent = templateDoc.getTranslatedContent(getContext());
    if (!getRenderingEngine().getRendererNames().contains("xwiki")) {
      return translatedContent.replaceAll("\\{pre\\}|\\{/pre\\}", "");
    } else {
      return translatedContent;
    }
  }

  XWikiDocument getTemplateDoc(String templateFullName) throws XWikiException {
    return getContext().getWiki().getDocument(templateFullName, getContext());
  }

  String getTemplatePathOnDisk(String renderTemplatePath) {
    return renderTemplatePath.replaceAll("^:(Templates\\.)?", "/templates/celTemplates/"
        ) + ".vm";
  }

  String getRenderTemplatePath(PageType cellType, String cellDocFN,
      String renderMode) throws XWikiException {
    if (cellType != null) {
      String renderTemplateFullName = cellType.getRenderTemplate(renderMode,
          getContext());
      if ((renderTemplateFullName != null) && !"".equals(renderTemplateFullName)) {
        LOGGER.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
            + cellType.getFullName() + "] and renderTemplate ["
            + renderTemplateFullName + "].");
        return renderTemplateFullName;
      }
      LOGGER.debug("getRenderTemplatePath for [" + cellDocFN + "] with cellType ["
          + cellType.getFullName() + "] using content of cellDoc [" + cellDocFN + "].");
    }
    return cellDocFN;
  }

}
