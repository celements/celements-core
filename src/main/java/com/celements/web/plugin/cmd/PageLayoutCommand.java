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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.cells.CellRenderer;
import com.celements.cells.DivWriter;
import com.celements.cells.IRenderStrategy;
import com.celements.cells.RenderingEngine;
import com.celements.inheritor.InheritorFactory;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.packaging.DocumentInfo;
import com.xpn.xwiki.plugin.packaging.PackageAPI;

public class PageLayoutCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      PageLayoutCommand.class);

  public static final String CEL_LAYOUT_EDITOR_PL_NAME = "CelLayoutEditor";

  /**
   * The name of the internal packaging plugin.
   */
  private static final String PACKAGEPLUGIN_NAME = "package";

  private InheritorFactory _injectedInheritorFactory;

  public Map<String, String> getAllPageLayouts(XWikiContext context) {
    return getPageLayoutMap(false, context);
  }

  private Map<String, String> getPageLayoutMap(boolean onlyActive, XWikiContext context) {
    Map<String, String> plMap = new HashMap<String, String>();
    try {
      for (Object resultRowObj : context.getWiki().search(getPageLayoutHQL(onlyActive),
          context)) {
        Object[] resultRow = (Object[]) resultRowObj;
        plMap.put(resultRow[0].toString(), resultRow[1].toString());
      }
    } catch (XWikiException exp) {
      mLogger.error("Failed to get all page layouts", exp);
    }
    return plMap ;
  }

  String getPageLayoutHQL(boolean onlyActive) {
    String hql = "select doc.space, pl.prettyname"
      + " from XWikiDocument as doc, BaseObject obj,"
      + " Celements.PageLayoutPropertiesClass as pl"
      + " where doc.fullName = obj.name"
      + " and obj.className='Celements.PageLayoutPropertiesClass'"
      + " and pl.id.id=obj.id";
    if (onlyActive) {
      hql += " and pl.isActive = 1";
    }
      hql += " order by pl.prettyname asc";
    return hql;
  }

  public Map<String, String> getActivePageLyouts(XWikiContext context) {
    return getPageLayoutMap(true, context);
  }

  public static final String PAGE_LAYOUT_PROPERTIES_CLASS =
    "Celements.PageLayoutPropertiesClass";

  public String createNew(String layoutSpaceName, XWikiContext context) {
    if (!"".equals(layoutSpaceName)) {
      if (!layoutExists(layoutSpaceName, context)) {
        try {
          XWikiDocument propXdoc = context.getWiki().getDocument(standardPropDocFN(
              layoutSpaceName), context);
          BaseObject layoutPropObj = propXdoc.newObject(PAGE_LAYOUT_PROPERTIES_CLASS,
              context);
          layoutPropObj.setStringValue("prettyname", layoutSpaceName + " Layout");
          context.getWiki().saveDocument(propXdoc, "Creating page layout", false,
              context);
          return "cel_layout_create_successful";
        } catch (XWikiException exp) {
          mLogger.error("createNew: failed to create new page layout.", exp);
        }
      }
    }
    return "cel_layout_empty_name_msg";
  }

  public boolean layoutExists(String layoutSpaceName, XWikiContext context){
    try {
      return (!getPropDocs(layoutSpaceName, context).isEmpty());
    } catch (XWikiException exp) {
      mLogger.error("layoutExists: failed to get executed search.", exp);
    }
    return false;
  }

  private List<String> getPropDocs(String layoutSpaceName, XWikiContext context)
      throws XWikiException {
    List<String> params = Arrays.asList(layoutSpaceName);
    List<String> existingPropDocs = context.getWiki().getStore().search(
        getPageLayoutPropertiesHQL(), 0, 0, params, context);
    return existingPropDocs;
  }

  public BaseObject getLayoutPropertyObj(String layoutSpaceName, XWikiContext context) {
    XWikiDocument layoutPropDoc = getLayoutPropDoc(layoutSpaceName, context);
    if (layoutPropDoc != null) {
      return layoutPropDoc.getObject(PAGE_LAYOUT_PROPERTIES_CLASS);
    } else {
      return null;
    }
  }

  public XWikiDocument getLayoutPropDoc(XWikiContext context) {
    return getLayoutPropDoc(getPageLayoutForCurrentDoc(context), context);
  }
   
  public XWikiDocument getLayoutPropDoc(String layoutSpaceName, XWikiContext context) {
    XWikiDocument layoutPropDoc = null;
    try {
      List<String> existingPropDocs = getPropDocs(layoutSpaceName, context);
      if (!existingPropDocs.isEmpty()) {
        layoutPropDoc = context.getWiki().getDocument(standardPropDocFN(layoutSpaceName),
            context);
      }
    } catch (XWikiException exp) {
      mLogger.error("getLayoutPropDoc: failed to get layout property obj.", exp);
    }
    return layoutPropDoc;
  }

  private String standardPropDocFN(String layoutSpaceName) {
    return layoutSpaceName + ".WebHome";
  }

  private String getPageLayoutPropertiesHQL() {
    String hql = "select doc.fullName"
      + " from XWikiDocument as doc, BaseObject obj,"
      + " Celements.PageLayoutPropertiesClass as pl"
      + " where doc.fullName = obj.name"
      + " and doc.space = ?"
      + " and obj.className='" + PAGE_LAYOUT_PROPERTIES_CLASS + "'"
      + " and pl.id.id=obj.id";
    return hql;
  }

  public String renderPageLayout(XWikiContext context) {
    return renderPageLayout(getPageLayoutForCurrentDoc(context), context);
  }

  public String renderPageLayout(String spaceName, XWikiContext context) {
    IRenderStrategy cellRenderer = new CellRenderer(context).setOutputWriter(
        new DivWriter());
   RenderingEngine renderEngine = new RenderingEngine().setRenderStrategy(cellRenderer);
    renderEngine.renderPageLayout(spaceName, context);
   return cellRenderer.getAsString();
  }
 
  public String getPageLayoutForCurrentDoc(XWikiContext context) {
    return getPageLayoutForDoc(context.getDoc().getFullName(), context);
  }
  
  public String getPageLayoutForDoc(String fullName, XWikiContext context) {
    if (layoutExists(fullName.split("\\.")[0], context)) {
      return CEL_LAYOUT_EDITOR_PL_NAME;
    } else {
      return getInheritorFactory().getPageLayoutInheritor(fullName, context
        ).getStringValue("page_layout", null);
    }
  }

  private InheritorFactory getInheritorFactory() {
   if (_injectedInheritorFactory != null) {
     return _injectedInheritorFactory;
    }
    return new InheritorFactory();
  }
 
 /**
  * For TESTS ONLY!!!
  * @param injectedInheritorFactory
  */
 void inject_TEST_InheritorFactory(InheritorFactory injectedInheritorFactory) {
   _injectedInheritorFactory = injectedInheritorFactory;
 }

  public boolean isActive(String layoutSpaceName, XWikiContext context) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceName, context);
    if (layoutPropertyObj != null) {
      return layoutPropertyObj.getIntValue("isActive", 0) > 0;
    }
    return false;
  }

  public String getPrettyName(String layoutSpaceName, XWikiContext context) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceName, context);
    if ((layoutPropertyObj != null)
        && (layoutPropertyObj.getStringValue("prettyname") != null)) {
      return layoutPropertyObj.getStringValue("prettyname");
    }
    return "";
  }

  public String getVersion(String layoutSpaceName, XWikiContext context) {
    BaseObject layoutPropertyObj = getLayoutPropertyObj(layoutSpaceName, context);
    if ((layoutPropertyObj != null)
        && (layoutPropertyObj.getStringValue("version") != null)) {
      return layoutPropertyObj.getStringValue("version");
    }
    return "";
  }

  public boolean layoutEditorAvailable(XWikiContext context) {
    return layoutExists(CEL_LAYOUT_EDITOR_PL_NAME, context);
  }

  /**
   * Export an page layout space into XAR using Packaging plugin.
   * 
   * @param layoutSpaceName the name of the application to export.
   * @param withDocHistory indicate if history of documents is exported.
   * @param context the XWiki context.
   * @throws XWikiException error when :
   *             <ul>
   *             <li>or getting page-layouts documents to export.</li>
   *             <li>or when apply export.</li>
   *             </ul>
   * @throws IOException error when apply export.
   */
  public void exportLayoutXAR(String layoutSpaceName, boolean withDocHistory,
      XWikiContext context) throws XWikiException, IOException {
      PackageAPI export = ((PackageAPI) context.getWiki().getPluginApi(PACKAGEPLUGIN_NAME,
          context));
      export.setName(getPrettyName(layoutSpaceName, context) + "-" + getVersion(
          layoutSpaceName, context));
      for (String documentName : context.getWiki().getSpaceDocsName(layoutSpaceName,
          context)) {
          export.add(extendToFullName(layoutSpaceName, documentName),
              DocumentInfo.ACTION_OVERWRITE);
      }
      export.setWithVersions(withDocHistory);
      export.export();
  }

  String extendToFullName(String layoutSpaceName, String documentName) {
    if (!documentName.contains(".")) {
      return layoutSpaceName + "." + documentName;
    } else {
      return documentName;
    }
  }

}
