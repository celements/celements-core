package com.celements.web.service;

import groovy.lang.Singleton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.pagetype.PageTypeApi;
import com.celements.web.pagetype.PageTypeCommand;
import com.celements.web.plugin.cmd.CheckClassesCommand;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

@Component
@Singleton
public class PrepareVelocityContextService implements IPrepareVelocityContext {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      PrepareVelocityContextService.class);

  @Requirement
  Execution execution;

  @Requirement
  IWebUtilsService webUtilsService;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public void prepareVelocityContext() {
    initCelementsVelocity(getContext());
    initPanelsVelocity(getContext());
  }

  /**
   * @param context
   * 
   * @deprecated instead use prepareVelocityContext()
   */
  @Deprecated
  public void prepareVelocityContext(XWikiContext context) {
    initCelementsVelocity(context);
    initPanelsVelocity(context);
  }

  void initPanelsVelocity(XWikiContext context) {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    if (vcontext != null) {
      if (!vcontext.containsKey("rightPanels")) {
        LOGGER.debug("setting rightPanels in vcontext: " + getRightPanels());
        vcontext.put("rightPanels", getRightPanels());
      }
      if (!vcontext.containsKey("leftPanels")) {
        LOGGER.debug("setting leftPanels in vcontext: " + getLeftPanels());
        vcontext.put("leftPanels", getLeftPanels());
      }
      if (!vcontext.containsKey("showRightPanels")) {
        vcontext.put("showRightPanels", Integer.toString(showRightPanels()));
      }
      if (!vcontext.containsKey("showLeftPanels")) {
        vcontext.put("showLeftPanels", Integer.toString(showLeftPanels()));
      }
      LOGGER.debug("leftPanels [" + vcontext.get("leftPanels")
          + "] and rightPanels [" + vcontext.get("rightPanels")
          + "] after initPanelsVelocity.");
    }
  }

  void initCelementsVelocity(XWikiContext context) {
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    if ((vcontext != null) && (context.getWiki() != null)) {
      if (!vcontext.containsKey(getVelocityName())) {
        vcontext.put(getVelocityName(), context.getWiki().getPluginApi(getVelocityName(),
            context));
      }
      if (!vcontext.containsKey("default_language")) {
        vcontext.put("default_language", webUtilsService.getDefaultLanguage());
      }
      if (!vcontext.containsKey("langs")) {
        vcontext.put("langs", WebUtils.getInstance().getAllowedLanguages(context));
      }
      if (!vcontext.containsKey("hasedit")) {
        try {
          if (context.getDoc() != null) {
            vcontext.put("hasedit", context.getWiki(
                ).getRightService().hasAccessLevel("edit", context.getUser(),
                    context.getDoc().getFullName(), context));
          } else {
            vcontext.put("hasedit", new Boolean(false));
          }
        } catch (XWikiException exp) {
          LOGGER.error("Failed to check edit Access Rights on "
              + context.getDoc().getFullName(), exp);
          vcontext.put("hasedit", new Boolean(false));
        }
      }
      if (!vcontext.containsKey("skin_doc")) {
        try {
          String skinDocName = context.getWiki().getSkin(context);
          Document skinDoc = context.getWiki().getDocument(skinDocName, context
              ).newDocument(context);
          vcontext.put("skin_doc", skinDoc);
        } catch (XWikiException e) {
          LOGGER.error("Failed to get skin_doc");
        }
      }
      if (!vcontext.containsKey("isAdmin")) {
        vcontext.put("isAdmin", webUtilsService.isAdminUser());
      }
      if (!vcontext.containsKey("isSuperAdmin")) {
        vcontext.put("isSuperAdmin", (webUtilsService.isAdminUser()
            && context.getUser().startsWith("xwiki:")));
      }
      if (!vcontext.containsKey("admin_language")) {
        vcontext.put("admin_language", webUtilsService.getAdminLanguage());
        LOGGER.debug("added admin_language to vcontext: "
            + webUtilsService.getAdminLanguage());
      }
      if (!vcontext.containsKey("adminMsg")) {
        vcontext.put("adminMsg", webUtilsService.getAdminMessageTool());
      }
      if (!vcontext.containsKey("celements2_skin")) {
        vcontext.put("celements2_skin", getCelementsSkinDoc(context));
      }
      if (!vcontext.containsKey("celements2_baseurl")
          && (getCelementsSkinDoc(context) != null)) {
        String celements2_baseurl = getCelementsSkinDoc(context).getURL("view");
        if (celements2_baseurl.indexOf("/",8) > 0) {
          vcontext.put("celements2_baseurl", celements2_baseurl.substring(0,
              celements2_baseurl.indexOf("/",8)));
        }
      }
      if (!vcontext.containsKey("page_type")) {
        vcontext.put("page_type", new PageTypeCommand().getPageType(context.getDoc(),
            context));
      }
      if (!vcontext.containsKey("user")) {
        vcontext.put("user", context.getUser());
      }
      if (!vcontext.containsKey("isContentEditor")) {
        vcontext.put("isContentEditor", context.getWiki().getUser(context.getUser(),
            getContext()).isUserInGroup("XWiki.ContentEditorsGroup"));
      }
      if (!vcontext.containsKey("tinyMCE_width")) {
        vcontext.put("tinyMCE_width", getRTEwidth(context));
      }
    }
  }

  String getRTEwidth(XWikiContext context) {
    int tinyMCEwidth = -1;
    String tinyMCEwidthStr = "";
    if (getCelementsSkinDoc(context) != null) {
      BaseObject pageTypeObj = new PageTypeCommand().getPageTypeObj(context.getDoc(),
          context).getPageTypeProperties(context);
      if (pageTypeObj != null) {
        tinyMCEwidth = pageTypeObj.getIntValue("rte_width", -1);
        tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
      }
      if (tinyMCEwidth < 0) {
        tinyMCEwidthStr = context.getWiki().getSpacePreference("editbox_width", context);
        if ((tinyMCEwidthStr != null) && !"".equals(tinyMCEwidthStr)) {
          tinyMCEwidth = Integer.parseInt(tinyMCEwidthStr);
        }
      }
    }
    if (tinyMCEwidth < 0) {
      tinyMCEwidth = 453;
      tinyMCEwidthStr = Integer.toString(tinyMCEwidth);
    }
    return tinyMCEwidthStr;
  }

  private Document getCelementsSkinDoc(XWikiContext context) {
    Document skinDoc = null;
    try {
      skinDoc = context.getWiki(
          ).getDocument(new DocumentReference("celements2web", "XWiki", "Celements2Skin"),
              context).newDocument(context);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to load celements2_skin"
          + " (celements2web:XWiki.Celements2Skin) ", exp);
    }
    return skinDoc;
  }


  public int showRightPanels() {
    if (showRightPanelsBoolean(getContext()) && !getRightPanels().isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showRightPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showRightPanels");
  }

  public int showLeftPanels() {
    if (showLeftPanelsBoolean(getContext()) && !getLeftPanels().isEmpty()) {
      return 1;
    } else {
      return 0;
    }
  }

  private boolean showLeftPanelsBoolean(XWikiContext context) {
    return showPanelByConfigName(context, "showLeftPanels");
  }

  public List<String> getRightPanels() {
    if (showRightPanelsBoolean(getContext())) {
      return Arrays.asList(getPanelString(getContext(), "rightPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  public List<String> getLeftPanels() {
    if (showLeftPanelsBoolean(getContext())) {
      return Arrays.asList(getPanelString(getContext(), "leftPanels").split(","));
    } else {
      return Collections.emptyList();
    }
  }

  private boolean showPanelByConfigName(XWikiContext context,
      String configName) {
    if (isPageShowPanelOverwrite(configName, context.getDoc())) {
      return (1 == getPagePanelObj(configName, context.getDoc()
          ).getIntValue("show_panels"));
    } else if ((getPageTypeDoc(context) != null)
        && isPageShowPanelOverwrite(configName, getPageTypeDoc(context))) {
      boolean showPanels = (1 == getPagePanelObj(configName, getPageTypeDoc(context)
          ).getIntValue("show_panels"));
      LOGGER.debug("using pagetype for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (isSpaceOverwrite(context)) {
      boolean showPanels = "1".equals(context.getWiki().getSpacePreference(configName,
          getSpaceOverwrite(context), "0", context));
      LOGGER.debug("using spaceover webPrefs for panels " + configName
          + "," + getSpaceOverwrite(context) +" -> "+ showPanels);
      return showPanels;
    } else if (isGlobalPref(context)) {
      boolean showPanels = ("1".equals(context.getWiki().getXWikiPreference(configName,
          context)));
      LOGGER.debug("using globalPref for panels " + configName + " -> "+ showPanels);
      return showPanels;
    } else if (context.getWiki() != null) {
      boolean showPanels = ("1".equals(context.getWiki().getSpacePreference(configName,
          context)));
      LOGGER.debug("using webPrefs for panels " + configName + " -> "+ showPanels);
      return showPanels;
    }
    return false;
  }

  private XWikiDocument getPageTypeDoc(XWikiContext context) {
    if(context.getDoc() != null) {
      try {
        Document templateDocument = new PageTypeApi(
        context.getDoc().getFullName(), context).getTemplateDocument();
        XWikiDocument pageTypeDoc = context.getWiki().getDocument(
            templateDocument.getFullName(), context);
        LOGGER.debug("getPageTypeDoc: pageTypeDoc=" + pageTypeDoc + " , "
            + templateDocument);
        return pageTypeDoc;
      } catch (XWikiException e) {
        LOGGER.error(e);
      }
    }
    return null;
  }

  private BaseObject getPagePanelObj(String configName, XWikiDocument theDoc) {
    if (theDoc != null) {
      return theDoc.getObject(CheckClassesCommand.CLASS_PANEL_CONFIG_CLASS, "config_name",
        getPanelType(configName), false);
    } else {
      return null;
    }
  }

  private boolean isPageShowPanelOverwrite(String configName,
      XWikiDocument theDoc) {
    try {
      return ((getPagePanelObj(configName, theDoc) != null)
         && (((BaseProperty)getPagePanelObj(configName, theDoc
             ).get("show_panels")).getValue() != null));
    } catch (XWikiException e) {
      LOGGER.error(e);
      return false;
    }
  }

  private String getPanelType(String configName) {
    if ("showLeftPanels".equals(configName)) {
      return "leftPanels";
    } else if ("showRightPanels".equals(configName)) {
      return "rightPanels";
    } else {
      return configName;
    }
  }

  private boolean isSpaceOverwrite(XWikiContext context) {
    return ((getSpaceOverwrite(context) != null)
        && !"".equals(getSpaceOverwrite(context)));
  }

  private String getPanelString(XWikiContext context, String configName) {
    String panelsString = "";
    if(isGlobalPref(context)) {
      panelsString = context.getWiki().getXWikiPreference(configName, context);
    } else if(isPagePanelsOverwrite(configName, context.getDoc())) {
      panelsString = getPagePanelObj(configName, context.getDoc()
          ).getStringValue("panels");
    } else if((getPageTypeDoc(context) != null)
        && isPagePanelsOverwrite(configName, getPageTypeDoc(context))) {
      panelsString = getPagePanelObj(configName, getPageTypeDoc(context)
          ).getStringValue("panels");
    } else if(isSpaceOverwrite(context)) {
      panelsString = context.getWiki().getSpacePreference(configName,
           getSpaceOverwrite(context), "", context);
    } else {
      panelsString = context.getWiki().getUserPreference(configName, context);
      LOGGER.debug("else with panels in userPreferences: " + panelsString);
      if("".equals(panelsString)) {
         panelsString = context.getWiki().getSpacePreference(configName, context);
         LOGGER.debug("else with panels in webPreferences: " + panelsString);
      }
    }
    LOGGER.debug("panels for config " + configName + " ; " + panelsString);
    return panelsString;
  }

  private boolean isPagePanelsOverwrite(String configName,
      XWikiDocument theDoc) {
    return ((getPagePanelObj(configName, theDoc) != null)
        && (!"".equals(getPagePanelObj(configName, theDoc
            ).getStringValue("panels"))));
  }

  private String getSpaceOverwrite(XWikiContext context) {
    if(context.getRequest() != null) {
      return context.getRequest().get("space");
    }
    return "";
  }

  private boolean isGlobalPref(XWikiContext context) {
    if ((context.getDoc() != null) && (context.getRequest() != null)) {
      return new DocumentReference(context.getDatabase(), "XWiki", "XWikiPreferences"
          ).equals(context.getDoc().getDocumentReference())
          || "globaladmin".equals(context.getRequest().get("editor"));
    }
    return false;
  }

  public String getVelocityName() {
    return "celementsweb";
  }

}
