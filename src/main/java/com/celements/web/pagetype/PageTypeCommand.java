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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PageTypeCommand {

  public static final String PAGE_TYPE_CLASSNAME = "Celements2.PageType";
  private static PageTypeCommand pageTypeInstance;
  
  private static Log mLogger = LogFactory.getFactory().getInstance(PageTypeCommand.class);
  
  PageTypeCommand() {
  }
  
  public static IPageType getApiInstance(XWikiContext context) throws XWikiException {
    Object obj = context.get("pageTypeApi");
    if ((obj == null) || !(obj instanceof IPageType)) {
      context.put("pageTypeApi", new PageTypeApi(context.getDoc().getFullName(), context));
    }
    return ((IPageType)context.get("pageTypeApi"));
  }
  
  public static PageTypeCommand getInstance() {
    if (pageTypeInstance == null) {
      pageTypeInstance = new PageTypeCommand();
    }
    return pageTypeInstance;
  }

  public BaseObject getPageTypeObject(XWikiDocument doc, XWikiContext context){
    if((doc != null) && doc.isNew()) {
      doc = getTemplateDoc(doc, context);
    }
    if ((doc != null) && (doc.getObjects(PAGE_TYPE_CLASSNAME) != null)
        && (doc.getObjects(PAGE_TYPE_CLASSNAME).size() > 0)) {
      return doc.getObject(PAGE_TYPE_CLASSNAME);
    }
    return null;
  }

  public String getPageType(XWikiDocument doc, XWikiContext context){
    //TODO get default PageType from WebPreferences
    return getPageTypeWithDefault(doc, "RichText", context);
  }

  public String getPageTypeWithDefault(XWikiDocument doc, String defaultPageType,
      XWikiContext context) {
    String pageType = "";
    if (doc != null) {
      BaseObject pageTypeObj = getPageTypeObject(doc, context);
      if(pageTypeObj != null) {
        pageType = pageTypeObj.getStringValue("page_type");
      }
    }
    if ((pageType == null) || "".equals(pageType.trim())) {
      return defaultPageType;
    }
    return pageType.trim();
  }

  XWikiDocument getTemplateDoc(XWikiDocument doc, XWikiContext context) {
    String templName = context.getRequest().get("template");
    if((templName != null) && !"".equals(templName.trim())
        && context.getWiki().exists(templName, context)) {
      try {
        doc = context.getWiki().getDocument(templName, context);
      } catch (XWikiException e) {
        mLogger.error("Exception while getting template doc '" + templName + "'", e);
      }
    }
    return doc;
  }

  public String getPageTypeDocFN(XWikiDocument doc, XWikiContext context) {
    return completePageTypeDocName(getPageType(doc, context));
  }

  private String completePageTypeDocName(String pageTypeName) {
    if (pageTypeName.indexOf('.') > 0) {
      return pageTypeName;
    } else {
      return "PageTypes." + pageTypeName;
    }
  }

  public PageType getPageTypeObj(XWikiDocument currentDoc, XWikiContext context) {
    return new PageType(getPageTypeDocFN(currentDoc, context));
  }

  public PageType getPageTypeWithDefaultObj(XWikiDocument currentDoc,
      String defaultPageType, XWikiContext context) {
    String pageTypeDocName = getPageTypeWithDefault(currentDoc, defaultPageType, context);
    if ((pageTypeDocName != null) && !"".equals(pageTypeDocName)) {
      return new PageType(completePageTypeDocName(pageTypeDocName));
    }
    return null;
  }

  public PageType getPageTypeObj(String pageTypeFN, XWikiContext context) {
    return new PageType(pageTypeFN);
  }

  public String getPrettyName(XWikiDocument doc, XWikiContext context) {
    return getPageTypeObj(doc, context).getPageTypeProperties(context).getStringValue(
        "type_name");
  }

  public boolean isVisible(XWikiDocument doc, XWikiContext context) {
    return getPageTypeObj(doc, context).getPageTypeProperties(context).getIntValue(
        "visible", 0) > 0;
  }

}
