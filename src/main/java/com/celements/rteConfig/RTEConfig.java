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
package com.celements.rteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.pagetype.cmd.PageTypeCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class RTEConfig {

  private static Log LOGGER = LogFactory.getFactory().getInstance(RTEConfig.class);

  private static final String RTE_CONFIG_TYPE_PROP_CLASS_NAME =
        "RTEConfigTypePropertiesClass";
  public static final String RTE_CONFIG_TYPE_PROP_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_CLASS_NAME = "RTEConfigTypeClass";
  public static final String CONFIG_CLASS_NAME = RTE_CONFIG_TYPE_CLASS_SPACE + "."
        + RTE_CONFIG_TYPE_CLASS_NAME;
  public static final String CONFIG_PROP_NAME = "rteconfig";
  public static final String PROP_CLASS_NAME = RTE_CONFIG_TYPE_PROP_CLASS_SPACE + "."
        + RTE_CONFIG_TYPE_PROP_CLASS_NAME;

  private final static Map<String, String> rteConfigFieldDefaults =
      new HashMap<String, String>();
      static {
        rteConfigFieldDefaults.put("blockformats", "rte_heading1=h1,rte_text=p");
      };

  private PageTypeCommand injectedPageTypeInstance;

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  public DocumentReference getRTEConfigTypePropClassRef(String wikiName) {
    return new DocumentReference(wikiName, RTE_CONFIG_TYPE_PROP_CLASS_SPACE,
        RTE_CONFIG_TYPE_PROP_CLASS_NAME);
  }

  public String getRTEConfigField(String name) throws XWikiException {
    XWikiDocument doc = getContext().getDoc();
    String resultConfig = "";
    
    // Doc
    resultConfig = getPreferenceFromConfigObject(name, doc);
    if("".equals(resultConfig.trim())) {
      resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME, doc);
    }
    
    // PageType
    if("".equals(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPageType(name);
    }
    
    // WebPreferences
    if("".equals(resultConfig.trim())) {
      String space = getContext().getDoc().getSpace();
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, space + ".WebPreferences");
    }
    
    // XWikiPreferences
    if("".equals(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, "XWiki.XWikiPreferences");
    }

    // xwiki.cfg
    if("".equals(resultConfig.trim())) {
      resultConfig = getContext().getWiki().Param("celements.rteconfig." + name,
          rteConfigFieldDefaults.get(name));
    }
    return resultConfig;
  }

  private String getRTEConfigFieldFromPageType(String name) throws XWikiException {
    String resultConfig = "";
    String pageTypeDocFN = getPageType().getPageTypeDocFN(getContext().getDoc(),
        getContext());
    if ((pageTypeDocFN != null) && getContext().getWiki().exists(pageTypeDocFN,
        getContext())) {
      XWikiDocument pageTypeDoc = getContext().getWiki().getDocument(pageTypeDocFN,
          getContext());
      resultConfig = getPreferenceFromConfigObject(name, pageTypeDoc);
      if("".equals(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME,
            pageTypeDoc);
      }
    }
    return resultConfig;
  }

  private String getRTEConfigFieldFromPreferenceDoc(String name, String docName
      ) throws XWikiException {
    XWikiDocument prefDoc = getContext().getWiki().getDocument(docName, getContext());
    String resultConfig = "";
    resultConfig = getPreferenceFromConfigObject(name, prefDoc);
    if("".equals(resultConfig.trim())) {
      resultConfig = getPreferenceFromPreferenceObject(name, PROP_CLASS_NAME, prefDoc);
      if("".equals(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject("rte_" + name,
            "XWiki.XWikiPreferences", prefDoc);
      }
    }
    return resultConfig;
  }

  String getPreferenceFromConfigObject(String name, XWikiDocument doc
      ) throws XWikiException {
    String configDocName = getPreferenceFromPreferenceObject(CONFIG_PROP_NAME,
        CONFIG_CLASS_NAME, doc);
    if(!"".equals(configDocName.trim())){
      XWikiDocument configDoc = getContext().getWiki().getDocument(configDocName,
          getContext());
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

  public List<DocumentReference> getRTEConfigsList() {
    List<DocumentReference> rteConfigsList = new ArrayList<DocumentReference>();
    try {
      List<String> resultList = getQueryManager().createQuery(getRteConfigsXWQL(),
          Query.XWQL).execute();
      for (String result : resultList) {
        rteConfigsList.add(getWebUtilsService().resolveDocumentReference(result));
      }
    } catch (QueryException exp) {
      LOGGER.error("Failed to get RTE-Configs list.", exp);
    }
    return rteConfigsList;
  }

  private String getRteConfigsXWQL() {
    return "from doc.object(" + PROP_CLASS_NAME + ") as rteConfig"
        + " where doc.translation = 0";
  }

  private QueryManager getQueryManager() {
    return Utils.getComponent(QueryManager.class);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
