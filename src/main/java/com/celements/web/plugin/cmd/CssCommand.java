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
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.PageTypeCommand;
import com.celements.web.css.CSS;
import com.celements.web.css.CSSEngine;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CssCommand {

  public static final String SKINS_USER_CSS_CLASS_SPACE = "Skins";
  public static final String SKINS_USER_CSS_CLASS_DOC = "UserCSS";
  public static final String SKINS_USER_CSS_CLASS = SKINS_USER_CSS_CLASS_SPACE + "."
    + SKINS_USER_CSS_CLASS_DOC;

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CssCommand.class);

  public DocumentReference getSkinsUserCssClassRef(String wikiName) {
    return new DocumentReference(wikiName, SKINS_USER_CSS_CLASS_SPACE,
        SKINS_USER_CSS_CLASS_DOC);
  }

  public List<CSS> getAllCSS(XWikiContext context) throws XWikiException{
    List<CSS> cssResultList = new ArrayList<CSS>();
    List<CSS> cssList = collectAllCSS(context);
    
    LOGGER.debug("List of CSS files built. There are " + cssList.size()
        + " CSS files to include.");
    
    Set<CSS> includedCSS = new HashSet<CSS>();
    for (Iterator<CSS> iterator = cssList.iterator(); iterator.hasNext();) {
      CSS css = (CSS) iterator.next();
      if(!includedCSS.contains(css) && (css != null)){
        LOGGER.debug("CSS to add to result List: " + css.toString());
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
    for (CSS css : cssList) {
      LOGGER.debug("displayAllCSS: displayInclude for [" + css.getCSS() + "]." );
      CSS += css.displayInclude(context);
    }
    
    return CSS;
  }
  
  public List<CSS> getRTEContentCSS(XWikiContext context) throws XWikiException{
    List<CSS> cssResultList = new ArrayList<CSS>();
    List<CSS> cssList = collectAllCSS(context);
    
    LOGGER.debug("List of CSS files built. There are " + cssList.size()
        + " CSS files to include.");
    
    Set<CSS> includedCSS = new HashSet<CSS>();
    for (Iterator<CSS> iterator = cssList.iterator(); iterator.hasNext();) {
      CSS css = (CSS) iterator.next();
      if((css != null) && !includedCSS.contains(css) && css.isContentCSS()){
        LOGGER.debug("RTE content CSS to add to result List: " + css.toString());
        cssResultList.add(css);
        includedCSS.add(css);
      }
    }
    
    return cssResultList;
  }

  public List<CSS> includeCSSPage(String css, XWikiContext context) {
    List<BaseObject> skins = null;
    if (!new PageLayoutCommand().layoutExists(context.getDoc().getDocumentReference(
        ).getLastSpaceReference())) {
      XWikiDocument doc = context.getDoc();
      skins = doc.getXObjects(getSkinsUserCssClassRef(context.getDatabase()));
      LOGGER.debug("CSS Page: " + doc.getDocumentReference() + " has attached "
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

      LOGGER.debug("WebPreferences space is: '" + space + "'");
      String baseCSS = context.getWiki().getSpacePreference("stylesheet", space,
          "", context) + " ";
      baseCSS = context.getWiki().getSpacePreference("stylesheets", space, "",
          context) + " ";
      
      LOGGER.debug("CSS Prefs: has '" + baseCSS + "' as CSS to add.");

      List<BaseObject> baseList = new ArrayList<BaseObject>();
      DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
          "XWiki", "XWikiPreferences");
      if (context.getWiki().exists(xwikiPrefDocRef, context)) {
        baseList.addAll(addUserSkinCss(context.getWiki().getDocument(
            xwikiPrefDocRef, context)));
      }
      DocumentReference webPrefDocRef = new DocumentReference("WebPreferences",
          context.getDoc().getDocumentReference().getLastSpaceReference());
      if (context.getWiki().exists(webPrefDocRef, context)) {
        baseList.addAll(addUserSkinCss(context.getWiki().getDocument(
            webPrefDocRef, context)));
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
      LOGGER.error(e);
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
    pageLayoutDoc = new PageLayoutCommand().getLayoutPropDoc();
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
      List<BaseObject> objs = docAPI.getXObjects(getSkinsUserCssClassRef(
          docAPI.getDocumentReference().getWikiReference().getName()));
      LOGGER.debug("CSS Skin: " + docAPI.getDocumentReference() + " has attached "
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
