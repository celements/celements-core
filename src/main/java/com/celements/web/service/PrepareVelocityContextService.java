package com.celements.web.service;

import groovy.lang.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
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
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiRequest;

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

  public void prepareVelocityContext(VelocityContext vcontext) {
    fixLanguagePreference(vcontext);
    fixTdocForInvalidLanguage(vcontext);
    initCelementsVelocity(vcontext);
    initPanelsVelocity(vcontext);
  }

  /**
   * @param context
   * 
   * @deprecated instead use prepareVelocityContext(VelocityContext)
   */
  @Deprecated
  public void prepareVelocityContext(XWikiContext context) {
    prepareVelocityContext((VelocityContext) context.get("vcontext"));
  }

  void fixLanguagePreference(VelocityContext vcontext) {
    String langPref = getLanguagePreference(getContext());
    if (vcontext != null) {
      vcontext.put("language", langPref);
    }
  }

  void initPanelsVelocity(VelocityContext vcontext) {
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

  void initCelementsVelocity(VelocityContext vcontext) {
    if ((vcontext != null) && (getContext().getWiki() != null)) {
      if (!vcontext.containsKey(getVelocityName())) {
        vcontext.put(getVelocityName(), getContext().getWiki().getPluginApi(getVelocityName(),
            getContext()));
      }
      if (!vcontext.containsKey("default_language")) {
        vcontext.put("default_language", webUtilsService.getDefaultLanguage());
      }
      if (!vcontext.containsKey("langs")) {
        vcontext.put("langs", webUtilsService.getAllowedLanguages());
      }
      if (!vcontext.containsKey("hasedit")) {
        try {
          if (getContext().getDoc() != null) {
            vcontext.put("hasedit", getContext().getWiki(
                ).getRightService().hasAccessLevel("edit", getContext().getUser(),
                    getContext().getDoc().getFullName(), getContext()));
          } else {
            vcontext.put("hasedit", new Boolean(false));
          }
        } catch (XWikiException exp) {
          LOGGER.error("Failed to check edit Access Rights on "
              + getContext().getDoc().getDocumentReference(), exp);
          vcontext.put("hasedit", new Boolean(false));
        }
      }
      if (!vcontext.containsKey("skin_doc")) {
        try {
          String skinDocName = getContext().getWiki().getSkin(getContext());
          Document skinDoc = getContext().getWiki().getDocument(
              webUtilsService.resolveDocumentReference(skinDocName), getContext()
              ).newDocument(getContext());
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
            && getContext().getUser().startsWith("xwiki:")));
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
        vcontext.put("celements2_skin", getCelementsSkinDoc(getContext()));
      }
      if (!vcontext.containsKey("celements2_baseurl")
          && (getCelementsSkinDoc(getContext()) != null)) {
        String celements2_baseurl = getCelementsSkinDoc(getContext()).getURL("view");
        if (celements2_baseurl.indexOf("/",8) > 0) {
          vcontext.put("celements2_baseurl", celements2_baseurl.substring(0,
              celements2_baseurl.indexOf("/",8)));
        }
      }
      if (!vcontext.containsKey("page_type")) {
        vcontext.put("page_type", new PageTypeCommand().getPageType(getContext().getDoc(),
            getContext()));
      }
      if (!vcontext.containsKey("user")) {
        vcontext.put("user", getContext().getUser());
      }
      if (!vcontext.containsKey("isContentEditor")) {
        vcontext.put("isContentEditor", getContext().getWiki().getUser(
            getContext().getUser(), getContext()).isUserInGroup(
                "XWiki.ContentEditorsGroup"));
      }
      if (!vcontext.containsKey("q")) {
        vcontext.put("q", "'");
      }
      if (!vcontext.containsKey("Q")) {
        vcontext.put("Q", "\"");
      }
      if (!vcontext.containsKey("tinyMCE_width")) {
        vcontext.put("tinyMCE_width", getRTEwidth(getContext()));
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

  /**
   * First try to find the current language in use from the XWiki context. If
   * none is used and if the wiki is not multilingual use the default language
   * defined in the XWiki preferences. If the wiki is multilingual try to get
   * the language passed in the request. If none was passed try to get it from a
   * cookie. If no language cookie exists then use the user default language and
   * barring that use the browser's "Accept-Language" header sent in HTTP
   * request. If none is defined use the default language.
   * 
   * @return the language to use
   * 
   * @deprecated introduced in 2.18.0 because of a but in xwiki 2.7.2 it is fixed
   *             in unstable-xwiki4
   */
  @Deprecated
  String getLanguagePreference(XWikiContext context) {
    LOGGER.debug("getLanguagePreference: start " + context.getLanguage());
    String language = context.getLanguage();

    XWiki xwiki = context.getWiki();
    String defaultLanguage = xwiki.getDefaultLanguage(context);

    LOGGER.debug("getLanguagePreference: isMultiLingual ["
        + context.getWiki().isMultiLingual(context) + "] defaultLanguage ["
        + defaultLanguage + "].");
    // If the wiki is non multilingual then the language is the default
    // language.
    if (!context.getWiki().isMultiLingual(context)) {
      language = defaultLanguage;
      context.setLanguage(language);
      return language;
    }

    // As the wiki is multilingual try to find the language to use from the
    // request by looking
    // for a language parameter. If the language value is "default" use the
    // default language
    // from the XWiki preferences settings. Otherwise set a cookie to remember
    // the language
    // in use.
    try {
      language = Util.normalizeLanguage(context.getRequest().getParameter("language"));
      if ((language != null) && (!language.equals(""))) {
        if (isInvalidLanguageOrDefault(language)) {
          // forgetting language cookie
          Cookie cookie = new Cookie("language", "");
          cookie.setMaxAge(0);
          cookie.setPath("/");
          context.getResponse().addCookie(cookie);
          language = defaultLanguage;
        } else {
          // setting language cookie
          Cookie cookie = new Cookie("language", language);
          cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
          cookie.setPath("/");
          context.getResponse().addCookie(cookie);
        }
        context.setLanguage(language);
        LOGGER.debug("getLanguagePreference: found parameter language " + language);
        return language;
      }
    } catch (Exception e) {
      LOGGER.error(e);
    }

    // As no language parameter was passed in the request, try to get the
    // language to use
    // from a cookie.
    try {
      // First we get the language from the cookie
      language = Util.normalizeLanguage(xwiki.getUserPreferenceFromCookie("language",
          context));
      if ((language != null) && (!language.equals(""))) {
        context.setLanguage(language);
        LOGGER.debug("getLanguagePreference: found cookie language " + language);
        return language;
      }
    } catch (Exception e) {
      LOGGER.error(e);
    }

    // Next from the default user preference
    try {
      String userFN = context.getUser();
      XWikiDocument userdoc = null;
      userdoc = xwiki.getDocument(webUtilsService.resolveDocumentReference(userFN),
          context);
      if (userdoc != null) {
        language = Util.normalizeLanguage(userdoc.getStringValue("XWiki.XWikiUsers",
            "default_language"));
        if (!language.equals("")) {
          context.setLanguage(language);
          LOGGER.debug("getLanguagePreference: found userpref language " + language);
          return language;
        }
      }
    } catch (XWikiException e) {
      LOGGER.error(e);
    }

    // If the default language is preferred, and since the user didn't
    // explicitly ask for a
    // language already, then use the default wiki language.
    if (xwiki.Param("xwiki.language.preferDefault", "0").equals("1")
        || xwiki.getSpacePreference("preferDefaultLanguage", "0", context).equals("1")) {
      language = defaultLanguage;
      context.setLanguage(language);
      LOGGER.debug("getLanguagePreference: found preferDefault language " + language);
      return language;
    }

    // Then from the navigator language setting
    if (context.getRequest() != null) {
      String acceptHeader = context.getRequest().getHeader("Accept-Language");
      // If the client didn't specify some languages, skip this phase
      if ((acceptHeader != null) && (!acceptHeader.equals(""))) {
        List<String> acceptedLanguages = getAcceptedLanguages(context.getRequest());
        LOGGER.debug("getLanguagePreference: getAcceptedLanguages " + acceptedLanguages);
        LOGGER.debug("getLanguagePreference: forceSupported "
            + xwiki.Param("xwiki.language.forceSupported", "0"));
        // We can force one of the configured languages to be accepted
        if (xwiki.Param("xwiki.language.forceSupported", "0").equals("1")) {
          List<String> available = getValidLanguages();
          LOGGER.debug("getLanguagePreference: forceSupported lang " + available
              + " languages [" + xwiki.getXWikiPreference("languages", context)
              + "] splitArray [" + Arrays.deepToString(available.toArray(new String[] {}))
              + "].");
          // Filter only configured languages
          acceptedLanguages.retainAll(available);
        }
        LOGGER.debug("getLanguagePreference: acceptedLanguages after "
            + acceptedLanguages);
        if (acceptedLanguages.size() > 0) {
          // Use the "most-preferred" language, as requested by the client.
          context.setLanguage(acceptedLanguages.get(0));
          LOGGER.debug("getLanguagePreference: found accepted language " + language);
          return acceptedLanguages.get(0);
        }
        // If none of the languages requested by the client is acceptable, skip
        // to next
        // phase (use default language).
      }
    }

    // Finally, use the default language from the global preferences.
    context.setLanguage(defaultLanguage);
    LOGGER.debug("getLanguagePreference: use default language " + language);
    return defaultLanguage;
  }

  private List<String> getValidLanguages() {
    List<String> available = Arrays.asList(getContext().getWiki().getXWikiPreference(
        "languages", getContext()).split("[, |]"));
    return available;
  }

  boolean isInvalidLanguageOrDefault(String language) {
    return language.equals("default")
          || (isSuppressInvalid() && !getValidLanguages().contains(language));
  }

  private boolean isSuppressInvalid() {
    return getContext().getWiki().getXWikiPreference("celSuppressInvalidLang",
        "celements.language.suppressInvalid", "0", getContext()).equals("1");
  }

  /**
   * Construct a list of language codes (ISO 639-1) from the Accept-Languages
   * header. This method filters out some bugs in different browsers or
   * containers, like returning '*' as a language (Jetty) or using '_' as a
   * language--country delimiter (some versions of Opera).
   * 
   * @param request
   *          The client request.
   * @return A list of language codes, in the client preference order; might be
   *         empty if the header is not well formed.
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  private List<String> getAcceptedLanguages(XWikiRequest request)
  {
      List<String> result = new ArrayList<String>();
      Enumeration<Locale> e = request.getLocales();
      while (e.hasMoreElements()) {
          String language = e.nextElement().getLanguage().toLowerCase();
          LOGGER.debug("getAcceptedLanguages: found language " + language);
          // All language codes should have 2 letters.
          if (StringUtils.isAlpha(language)) {
              result.add(language);
          }
      }
      return result;
  }

  void fixTdocForInvalidLanguage(VelocityContext vcontext) {
    XWikiDocument doc = getContext().getDoc();
    if ((doc != null) && (vcontext != null)) {
      try {
        XWikiDocument tdoc = doc.getTranslatedDocument(getContext());
        try {
            String rev = (String) getContext().get("rev");
            if (StringUtils.isNotEmpty(rev)) {
                tdoc = getContext().getWiki().getDocument(tdoc, rev, getContext());
            }
        } catch (Exception ex) {
          LOGGER.debug("Invalid version, just use the most recent one.", ex);
        }
        getContext().put("tdoc", tdoc);
        vcontext.put("tdoc", tdoc.newDocument(getContext()));
      } catch (XWikiException exp) {
        LOGGER.error("Faild to get translated document for ["
            + getContext().getDoc().getDocumentReference() + "].", exp);
      }
    }
  }

}
