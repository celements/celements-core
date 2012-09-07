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
package com.celements.web.plugin;

import com.celements.pagetype.PageTypeCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class RTEConfig {
  public static final String CONFIG_CLASS_NAME = "Classes.RTEConfigTypeClass";
  public static final String CONFIG_PROP_NAME = "rteconfig";
  public static final String PROP_CLASS_NAME = "Classes.RTEConfigTypePropertiesClass";
  private PageTypeCommand injectedPageTypeInstance;
  
  private RTEConfig() { }
  
  public static RTEConfig getInstance(XWikiContext context) {
    Object obj = context.get("celementsRTEConfig");
    if((obj != null) || !(obj instanceof RTEConfig)) {
      context.put("celementsRTEConfig", new RTEConfig());
    }
    return ((RTEConfig)context.get("celementsRTEConfig"));
  }
  
  public String getRTEConfigField(String name, XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc = context.getDoc();
    String resultConfig = "";
    
    // Doc
    resultConfig = getPreferenceFromConfigObject(name, doc, context);
    if("".equals(resultConfig.trim())) {
      resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME, doc);
    }
    
    // PageType
    if("".equals(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPageType(name, context);
    }
    
    // WebPreferences
    if("".equals(resultConfig.trim())) {
      String space = context.getDoc().getSpace();
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, space + ".WebPreferences",
          context);
    }
    
    // XWikiPreferences
    if("".equals(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, "XWiki.XWikiPreferences",
          context);
    }
    return resultConfig;
  }

  private String getRTEConfigFieldFromPageType(String name, XWikiContext context
      ) throws XWikiException {
    String resultConfig = "";
    String pageTypeDocFN = getPageType().getPageTypeDocFN(context.getDoc(), context);
    if ((pageTypeDocFN != null) && context.getWiki().exists(pageTypeDocFN, context)) {
      XWikiDocument pageTypeDoc = context.getWiki().getDocument(pageTypeDocFN,
          context);
      resultConfig = getPreferenceFromConfigObject(name, pageTypeDoc, context);
      if("".equals(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME,
            pageTypeDoc);
      }
    }
    return resultConfig;
  }

  private String getRTEConfigFieldFromPreferenceDoc(String name, String docName,
     XWikiContext context) throws XWikiException {
    XWikiDocument prefDoc = context.getWiki().getDocument(docName, context);
    String resultConfig = "";
    resultConfig = getPreferenceFromConfigObject(name, prefDoc, context);
    if("".equals(resultConfig.trim())) {
      resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME, prefDoc);
      if("".equals(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject("rte_" + name,
            "XWiki.XWikiPreferences", prefDoc);
      }
    }
    return resultConfig;
  }

  String getPreferenceFromConfigObject(String name, XWikiDocument doc,
      XWikiContext context) throws XWikiException {
    String configDocName = getPreferenceFromPreferenceObject(CONFIG_PROP_NAME,
        CONFIG_CLASS_NAME, doc);
    if(!"".equals(configDocName.trim())){
      XWikiDocument configDoc = context.getWiki().getDocument(configDocName, context);
      return getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME, configDoc);
    }
    return "";
  }

  String getPreferenceFromPreferenceObject(String name, String className,
      XWikiDocument doc) {
    BaseObject prefObj = doc.getObject(className);
    if(prefObj != null) {
      return prefObj.getStringValue(name);
    }
    return "";
  }
  
  /**
   * FOR TESTS ONLY!!!
   * @param test_Instance
   */
  public void injectPageTypeInstance(PageTypeCommand test_Instance) {
    injectedPageTypeInstance = test_Instance;
  }

  PageTypeCommand getPageType() throws XWikiException {
    if (injectedPageTypeInstance == null) {
      injectedPageTypeInstance = PageTypeCommand.getInstance();
    }
    return injectedPageTypeInstance;
  }
}
