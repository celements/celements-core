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

import java.util.List;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PageTypeApi implements IPageType {
  private XWikiContext context;
  private XWiki xwiki;
  private XWikiDocument doc;
  
  public PageTypeApi(String fullName, XWikiContext context
      ) throws XWikiException{
    this.context = context;
    xwiki = context.getWiki();
    doc = xwiki.getDocument(fullName, context);
  }
  
  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#getTemplateDocument()
   */
  public Document getTemplateDocument() throws XWikiException {
    return getPageTypeObjForCurrentDoc().getTemplateDocument(
        context).newDocument(context);
  }

  private PageType getPageTypeObjForCurrentDoc() {
    return PageTypeCommand.getInstance().getPageTypeObj(doc, context);
  }
  
  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#getPageType()
   */
  public String getPageType() {
    return PageTypeCommand.getInstance().getPageType(doc, context);
  }
  
  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#getPageTypeObject()
   */
  public com.xpn.xwiki.api.Object getPageTypeObject() {
    BaseObject pageTypeObject = PageTypeCommand.getInstance().getPageTypeObject(
        doc, context);
    if (pageTypeObject != null) {
      return pageTypeObject.newObjectApi(pageTypeObject, context);
    } else {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#showFrame()
   */
  public boolean showFrame() throws XWikiException {
    return getPageTypeObjForCurrentDoc().showFrame(context);
  }
  
  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#getPageTypeProperties()
   */
  public com.xpn.xwiki.api.Object getPageTypeProperties() throws XWikiException{
    BaseObject pageTypePropObj = getPageTypeObjForCurrentDoc().getPageTypeProperties(
        context);
    if (pageTypePropObj != null) {
      return pageTypePropObj.newObjectApi(pageTypePropObj, context);
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see com.celements.web.plugin.IPageType#getRenderTemplate(java.lang.String)
   */
  public String getRenderTemplate(String renderMode) throws XWikiException{
    return getPageTypeObjForCurrentDoc().getRenderTemplate(renderMode, context);
  }

  public boolean hasPageTitle() {
    return getPageTypeObjForCurrentDoc().hasPageTitle(context);
  }

  public List<String> getCategories() {
    return getPageTypeObjForCurrentDoc().getCategories(context);
  }

  public String getCategoryString() {
    return getPageTypeObjForCurrentDoc().getCategoryString(context);
  }

  public String getPrettyName() {
    return PageTypeCommand.getInstance().getPrettyName(doc, context);
  }

  public boolean isVisible() {
    return PageTypeCommand.getInstance().isVisible(doc, context);
  }

  public String getPageTypeClassFullName() {
    return PageTypeCommand.PAGE_TYPE_CLASSNAME;
  }

}
