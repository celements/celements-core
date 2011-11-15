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
package com.celements.web.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class CSSEngine implements ICSSEngine {

  private static final String _CSS_ENGINE_OBJECT_KEY = "com.celements.web.css.CSSEngine";
  static Log mLogger = LogFactory.getFactory().getInstance(
      CSSEngine.class);

  private CSSEngine() {
  }

  public static ICSSEngine getCSSEngine(XWikiContext context) {
    Object storeObj = context.get(_CSS_ENGINE_OBJECT_KEY);
    if ((storeObj == null) || !(storeObj instanceof ICSSEngine)) {
      context.put(_CSS_ENGINE_OBJECT_KEY, new CSSEngine());
    }
    return (ICSSEngine) context.get(_CSS_ENGINE_OBJECT_KEY);
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
  @SuppressWarnings("unchecked")
  public List<CSS> includeCSS(String css, String field,
      List<BaseObject> baseCSSList, XWikiContext context){
    mLogger.debug("adding '" + css + "' to " + field + ". List contains already "
        + ((baseCSSList != null)?baseCSSList.size():"0") + " items.");
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList = Collections.emptyList();
    
    if(vcontext != null){
      if(vcontext.containsKey(field)){
        cssList = (List<CSS>)vcontext.get(field);
      } else{
        cssList = new ArrayList<CSS>();
        if(baseCSSList != null){
          for (BaseObject cssObj : baseCSSList) {
            if(cssObj != null) {
              cssList.add(new CSSBaseObject(cssObj, context));
            }
          }
        }
      }
      
      String[] newCSSList = css.split(" ");
      for (int i = 0; i < newCSSList.length; i++) {
        if((newCSSList[i] != null) && (!newCSSList[i].trim().equals(""))){
          cssList.add(new CSSString(newCSSList[i], context));
        }
      }
      
      vcontext.put(field, cssList);
    }
    
    return cssList;
  }

}
