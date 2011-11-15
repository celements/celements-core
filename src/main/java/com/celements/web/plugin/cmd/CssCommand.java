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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.celements.web.css.CSS;
import com.celements.web.css.CSSEngine;
import com.celements.web.pagetype.PageTypeCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CssCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CssCommand.class);

  public List<CSS> getAllCSS(XWikiContext context) throws XWikiException{
    List<CSS> cssResultList = new ArrayList<CSS>();
    List<CSS> cssList = collectAllCSS(context);
    
    mLogger.debug("List of CSS files built. There are " + cssList.size()
        + " CSS files to include.");
    
    Set<CSS> includedCSS = new HashSet<CSS>();
    for (Iterator<CSS> iterator = cssList.iterator(); iterator.hasNext();) {
      CSS css = (CSS) iterator.next();
      if(!includedCSS.contains(css) && (css != null)){
        mLogger.debug("CSS to add to result List: " + css.toString());
        cssResultList.add(css);
        includedCSS.add(css);
      }
    }
    
    return cssResultList;
  }

  private List<CSS> collectAllCSS(XWikiContext context) throws XWikiException {
    List<CSS> cssList = new ArrayList<CSS>();
    cssList.addAll(includeCSSAfterSkin("", context));
    cssList.addAll(includeCSSAfterPreferences("", context));
    cssList.addAll(includeCSSAfterPageType("", context));
    cssList.addAll(includeCSSAfterPageLayout("", context));
    cssList.addAll(includeCSSPage("", context));
    return cssList;
  }
  
  public String displayAllCSS(XWikiContext context) throws XWikiException{
    String CSS = "";
    
    List<CSS> cssList = getAllCSS(context);
    for (Iterator<CSS> iterator = cssList.iterator(); iterator.hasNext();) {
      CSS css = (CSS) iterator.next();
      CSS += css.displayInclude(context);
    }
    
    return CSS;
  }
  
  public List<CSS> getRTEContentCSS(XWikiContext context) throws XWikiException{
    List<CSS> cssResultList = new ArrayList<CSS>();
    List<CSS> cssList = collectAllCSS(context);
    
    mLogger.debug("List of CSS files built. There are " + cssList.size()
        + " CSS files to include.");
    
    Set<CSS> includedCSS = new HashSet<CSS>();
    for (Iterator<CSS> iterator = cssList.iterator(); iterator.hasNext();) {
      CSS css = (CSS) iterator.next();
      if((css != null) && !includedCSS.contains(css) && css.isContentCSS()){
        mLogger.debug("RTE content CSS to add to result List: " + css.toString());
        cssResultList.add(css);
        includedCSS.add(css);
      }
    }
    
    return cssResultList;
  }

  public List<CSS> includeCSSPage(String css, XWikiContext context) {
    List<BaseObject> skins = null;
    if (!new PageLayoutCommand().layoutExists(context.getDoc().getSpace(), context)) {
      XWikiDocument doc = context.getDoc();
      skins = doc.getObjects("Skins.UserCSS");
      mLogger.debug("CSS Page: " + doc.getFullName() + " has attached "
          + ((skins != null)?skins.size():"0") + " Skins.UserCSS objects.");
    }
    return includeCSS(css, "cel_css_list_page", skins, context);
  }

  public List<CSS> includeCSSAfterPreferences(String css,
      XWikiContext context) throws XWikiException{
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList = Collections.emptyList();
    
    if (vcontext != null){
      
      String space = (String)vcontext.get("stylesheet_space");
      if((space == null) || space.trim().equals("")){
        space = context.getDoc().getSpace();
      }

      mLogger.debug("WebPreferences space is: '" + space + "'");
      String baseCSS = context.getWiki().getWebPreference("stylesheet", space,
          "", context) + " ";
      baseCSS = context.getWiki().getWebPreference("stylesheets", space, "",
          context) + " ";
      
      mLogger.debug("CSS Prefs: has '" + baseCSS + "' as CSS to add.");

      List<BaseObject> baseList = new ArrayList<BaseObject>();
      String xwikiPrefFullName = "XWiki.XWikiPreferences";
      if (context.getWiki().exists(xwikiPrefFullName, context)) {
        baseList.addAll(addUserSkinCss(context.getWiki().getDocument(
            xwikiPrefFullName, context)));
      }
      String webPrefFullName = context.getDoc().getSpace() + ".WebPreferences";
      if (context.getWiki().exists(webPrefFullName, context)) {
        baseList.addAll(addUserSkinCss(context.getWiki().getDocument(
            webPrefFullName, context)));
      }
      cssList = includeCSS(baseCSS + css, "cel_css_list_pref", baseList, context);
    }
    
    return cssList;
  }
  
  public List<CSS> includeCSSAfterSkin(String css, XWikiContext context){
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList = Collections.emptyList();
    
    if (vcontext != null){
      List<BaseObject> baseList = new ArrayList<BaseObject>();
      baseList.addAll(addUserSkinCss((Document)vcontext.get("skin_doc")));
      baseList.addAll(addUserSkinCss((Document)vcontext.get("after_skin_cssdoc")));
      cssList = includeCSS(css, "cel_css_list_skin", baseList, context);
    }
    return cssList;
  }
  
  public List<CSS> includeCSSAfterPageType(String css, XWikiContext context){
    XWikiDocument pageTypeDoc = null;
    try {
      pageTypeDoc = PageTypeCommand.getInstance().getPageTypeObj(context.getDoc(),
          context).getTemplateDocument(context);
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList = Collections.emptyList();
    
    if ((pageTypeDoc != null) && (vcontext != null))  {
      List<BaseObject> baseList = new ArrayList<BaseObject>();
      baseList.addAll(addUserSkinCss(pageTypeDoc));
      baseList.addAll(addUserSkinCss((Document)vcontext.get("after_pagetype_cssdoc")));
      cssList = includeCSS(css, "cel_css_list_pagetype", baseList, context);
    }
    return cssList;
  }

  public List<CSS> includeCSSAfterPageLayout(String css, XWikiContext context){
    XWikiDocument pageLayoutDoc = null;
    pageLayoutDoc = new PageLayoutCommand().getLayoutPropDoc(context);
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList = Collections.emptyList();
    
    if ((pageLayoutDoc != null) && (vcontext != null))  {
      List<BaseObject> baseList = new ArrayList<BaseObject>();
      baseList.addAll(addUserSkinCss(pageLayoutDoc));
      baseList.addAll(addUserSkinCss((Document)vcontext.get("after_pagelayout_cssdoc")));
      cssList = includeCSS(css, "cel_css_list_pagelayout", baseList, context);
    }
    return cssList;
  }

  private List<BaseObject> addUserSkinCss(Document docAPI) {
    if(docAPI != null) {
      return addUserSkinCss(docAPI.getDocument());
    }
    return Collections.emptyList();
  }

  private List<BaseObject> addUserSkinCss(XWikiDocument docAPI) {
    if(docAPI != null) {
      List<BaseObject> objs = docAPI.getObjects("Skins.UserCSS");
      mLogger.debug("CSS Skin: " + docAPI.getFullName() + " has attached "
          + ((objs != null)?objs.size():"0") + " Skins.UserCSS objects.");
      if(objs != null){
        return objs;
      }
    }
    return Collections.emptyList();
  }
  
  /**
   * 
   * @param css
   * @param field
   * @param baseCSSList
   * @param context
   * @return the returned list is XWikiContext dependent and therefore may not be cached
   *         or similar. The list is as a consequence too not thread safe.
   *         TODO: Fix mix of API and backend. Extract business objects (controller)
   *         from CSS classes and use them here.
   */
  private List<CSS> includeCSS(String css, String field,
      List<BaseObject> baseCSSList, XWikiContext context){
    return CSSEngine.getCSSEngine(context).includeCSS(css, field, baseCSSList, context);
  }

}
